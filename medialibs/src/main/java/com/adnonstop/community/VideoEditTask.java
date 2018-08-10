package com.adnonstop.community;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.FloatRange;
import android.text.TextUtils;
import android.view.Surface;

import com.adnonstop.decode.AbsDecoder;
import com.adnonstop.encode.EncodeThread;
import com.adnonstop.encode.EncodeUtils;
import com.adnonstop.encode.VideoEncoderCore;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * Created by: fwc
 * Date: 2018/4/16
 */
public class VideoEditTask implements Runnable {

	private static final int TARGET_WIDTH = 720;
	private static final int TARGET_HEIGHT = 1280;

	private static final int MAX_FRAME_RATE = 30;

	private final Builder mBuilder;

	private CallbackHandler mHandler;

	private AbsDecoder mDecoder;
	private EncodeThread mEncodeThread;
	private GLSurface mGLSurface;

	private long mDuration;

	/**
	 * 避免重复进度回调
	 */
	private int mLastProgress;

	private volatile boolean isCancel = false;

	private boolean isError = false;

	/**
	 * 每帧间隔，若为0即不限制帧率
	 */
	private long mFrameInterval;

	/**
	 * 下一帧绘制时间戳
	 */
	private long mNextDrawTimestamp;

	private int mDrawFrameNum;

	private int mLeftFrameNum;
	private long mNextLeftFrameDraw = TIME_UNIT;

	private final static int TIME_UNIT = 1000000;

	/**
	 * 开始渲染的时间，单位微秒
	 */
	private long mStartRenderTime = -1;
	private AbsDecoder.OnDrawListener mOnDrawListener = new AbsDecoder.OnDrawListener() {
		@Override
		public void onDraw(long presentationTime) {

			mGLSurface.awaitNewImage();

			if (mStartRenderTime == -1) {
				if (mBuilder.mClipStartTime * 1000 > presentationTime) {
					return;
				} else {
					mStartRenderTime = presentationTime;
					mDuration = mBuilder.mClipEndTime * 1000 - mStartRenderTime;
				}
			}

			long timestamp = presentationTime - mStartRenderTime;

			if (mFrameInterval == 0) {
				onDrawFrame(timestamp);
			} else {
				long nextDrawTimestamp = mNextDrawTimestamp;

				while (nextDrawTimestamp <= timestamp) {
					if (mLeftFrameNum > 0 && nextDrawTimestamp > mNextLeftFrameDraw) {
						onDrawFrame(nextDrawTimestamp - mFrameInterval / 2);
						onDrawFrame(nextDrawTimestamp);
						mNextLeftFrameDraw += TIME_UNIT;
						mLeftFrameNum--;
					} else {
						onDrawFrame(nextDrawTimestamp);
					}
					nextDrawTimestamp += mFrameInterval;
				}

				mNextDrawTimestamp = nextDrawTimestamp;
			}
		}

		@Override
		public void onDrawFinish() {
			if (mDrawFrameNum < mBuilder.mFrameNum) {
				onDrawFrame(mNextDrawTimestamp - mFrameInterval / 2);
			}
		}
	};

	private void onDrawFrame(long timestamp) {
		mDrawFrameNum++;
		mGLSurface.drawFrame();

		mEncodeThread.encode();
		mGLSurface.setPresentationTime(timestamp * 1000);
		mGLSurface.swapBuffers();

		if (mDuration != 0) {
			float progress = timestamp * 100f / mDuration;
			onProgress(Math.min(100, (int)(progress + 0.5f)));
		}
	}

	private VideoEditTask(Builder builder) {

		mBuilder = builder;
		mHandler = new CallbackHandler(Looper.getMainLooper());
		mHandler.setOnCompressListener(mBuilder.mOnProcessListener);
	}

	/**
	 * 取消压缩任务
	 */
	public void cancel() {
		isCancel = true;
		if (mDecoder != null) {
			mDecoder.cancel();
		}
	}

	@Override
	public void run() {
		mLastProgress = -1;

		onStart();

		final String outputPath = mBuilder.mContext.getCacheDir().getAbsolutePath() + File.separator + UUID.randomUUID();

		try {
			mDecoder = AbsDecoder.create(mBuilder.mInputPath, mBuilder.mClipStartTime, mBuilder.mClipEndTime);
			mDecoder.setOnDrawListener(mOnDrawListener);

			int videoWidth = mDecoder.getWidth();
			int videoHeight = mDecoder.getHeight();
			mDuration = mDecoder.getDuration();
			checkClipTime(mDuration / 1000);

			int width;
			int height;
			if (mBuilder.mSizeRatio > 0 && mBuilder.mSizeRatio <= 1) {
				width = (int)(videoWidth * mBuilder.mSizeRatio + 0.5f);
				height = (int)(videoHeight * mBuilder.mSizeRatio + 0.5f);
			} else if (mBuilder.mWidth > 0 && mBuilder.mHeight > 0) {
				width = mBuilder.mWidth;
				height = mBuilder.mHeight;
			} else {
				int[] size = EncodeUtils.getVideoSupportSize(videoWidth, videoHeight, TARGET_WIDTH, TARGET_HEIGHT);
				width = size[0];
				height = size[1];
			}

			float scaleX = 1;
			float scaleY = 1;

			if (mBuilder.mFrameRatio != 0) {
				if (width / (float)height < mBuilder.mFrameRatio) {
					int originHeight = height;
					height = (int)(width / mBuilder.mFrameRatio + 0.5f);
					scaleY = originHeight / (float)height;
				} else {
					int originWidth = width;
					width = (int)(height * mBuilder.mFrameRatio + 0.5f);
					scaleX = originWidth / (float)width;
				}
			}

			width = EncodeUtils.checkEncodeSize(width);
			height = EncodeUtils.checkEncodeSize(height);

			final float offsetX = mBuilder.mFrameOffsetX * 2 / width;
			final float offsetY = mBuilder.mFrameOffsetY * 2 / height;

			int frameRate;
			if (mBuilder.mFrameRate != 0) {
				frameRate = mBuilder.mFrameRate;
				mFrameInterval = 1000000 / frameRate;
			} else if (mBuilder.mFrameNum != 0) {
				final long duration = mDuration / 1000000;
				frameRate = (int)(mBuilder.mFrameNum / (float)duration);
				mFrameInterval = 1000000 / frameRate;
				mLeftFrameNum = mBuilder.mFrameNum - (int)(frameRate * duration);
			} else {
				mFrameInterval = 0;
				frameRate = mDecoder.getFrameRate();
			}

			VideoEncoderCore.EncodeConfig config = new VideoEncoderCore.EncodeConfig(width, height, frameRate, outputPath);
			if (mBuilder.mBitRate > 0) {
				config.bitRate = mBuilder.mBitRate;
			}
			mEncodeThread = new EncodeThread(config);
			Surface encodeSurface = mEncodeThread.getEncodeSurface();
			new Thread(mEncodeThread).start();

			mGLSurface = new GLSurface(width, height, encodeSurface);
			mGLSurface.setRenderer(new VideoRenderer(mBuilder.mContext));
			mGLSurface.setFrameMatrix(offsetX, offsetY, scaleX, scaleY, mDecoder.getAddRotation());

			mDecoder.prepare(mGLSurface.getSurface());
			mDecoder.start();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			isError = true;
		} finally {

			if (mEncodeThread != null) {
				isError = isError || !mEncodeThread.waitForFinish();
			}

			if (mDecoder != null) {
				mDecoder.release();
				mDecoder = null;
			}

			if (mGLSurface != null) {
				mGLSurface.release();
				mGLSurface = null;
			}
		}

		if (!isCancel && !isError) {
			boolean success = false;
			if (mBuilder.isSaveAudio) {
				success = Utils.muxerMp4(outputPath, mBuilder.mInputPath, mBuilder.mOutputPath, mStartRenderTime, mBuilder.mClipEndTime * 1000);
			}

			if (!success) {
				Utils.renameOrCopy(outputPath, mBuilder.mOutputPath);
			}

//			Utils.copyAudio(mBuilder.mContext, outputPath, mBuilder.mInputPath, mBuilder.mOutputPath, mStartRenderTime / 1000, mBuilder.mClipEndTime);
		}

		Utils.delete(outputPath);

		if (isError) {
			onError();
		} else {
			onFinish(mBuilder.mOutputPath);
		}
	}

	/**
	 * 同步调用
	 * @return 是否成功
	 */
	public boolean syncRun() {
		run();
		return !isError;
	}

	private void onStart() {
		mHandler.sendEmptyMessage(CallbackHandler.MSG_START);
	}

	private void checkClipTime(long duration) {
		if (mBuilder.mClipStartTime >= duration) {
			throw new RuntimeException("the clip time is not correct.");
		}
		if (mBuilder.mClipEndTime == -1 || mBuilder.mClipEndTime > duration) {
			mBuilder.mClipEndTime = duration;
		}
	}

	private void onError() {
		mHandler.sendEmptyMessage(CallbackHandler.MSG_ERROR);
	}

	private void onFinish(String path) {
		Message message = new Message();
		message.obj = path;
		message.what = CallbackHandler.MSG_FINISH;
		mHandler.sendMessage(message);
	}

	private void onProgress(int progress) {
		if (mLastProgress != progress) {
			mHandler.obtainMessage(CallbackHandler.MSG_PROGRESS, progress, 0).sendToTarget();
			mLastProgress = progress;
		}
	}

	public interface OnProcessListener {

		void onStart();

		void onFinish(String outputpath);

		void onError();

		/**
		 * 进度回调
		 *
		 * @param progress 0 ~ 100
		 */
		void onProgress(int progress);
	}

	public static class Builder {

		private Context mContext;
		private String mInputPath;
		private String mOutputPath;
		private int mBitRate;

		private int mWidth;
		private int mHeight;

		private float mSizeRatio;

		private long mClipStartTime = 0;
		private long mClipEndTime = -1;

		/**
		 * 画幅比例
		 */
		private float mFrameRatio = 0;

		/**
		 * 画幅偏移量
		 */
		private float mFrameOffsetX = 0;
		private float mFrameOffsetY = 0;

		/**
		 * 帧率，默认为按原视频的帧率
		 */
		private int mFrameRate = 0;

		/**
		 * 帧数
		 */
		private int mFrameNum = 0;

		/**
		 * 是否保留音频数据，默认true
		 */
		private boolean isSaveAudio = true;

		private OnProcessListener mOnProcessListener;

		public Builder(Context context) {
			mContext = context;
		}

		/**
		 * 设置视频输入路径，必须设置
		 *
		 * @param inputPath 视频路径
		 */
		public Builder setInputPath(String inputPath) {
			mInputPath = inputPath;
			return this;
		}

		/**
		 * 设置视频压缩输出路径，必须设置
		 *
		 * @param outputPath 视频路径
		 */
		public Builder setOutputPath(String outputPath) {
			mOutputPath = outputPath;
			return this;
		}

		/**
		 * 设置压缩后视频的尺寸
		 *
		 * @param width  视频宽度
		 * @param height 视频高度
		 */
		public Builder setTargetSize(int width, int height) {
			mWidth = width;
			mHeight = height;
			return this;
		}

		/**
		 * 设置视频尺寸压缩因子，若设置了这个会忽略掉 target size
		 *
		 * @param ratio 压缩因子
		 */
		public Builder setSizeRatio(@FloatRange(from = 0, to = 1) float ratio) {
			mSizeRatio = ratio;
			return this;
		}

		/**
		 * 设置视频压缩的码率
		 *
		 * @param bitRate 视频码率
		 */
		public Builder setBitRate(int bitRate) {
			mBitRate = bitRate;
			return this;
		}

		/**
		 * 视频时长裁剪
		 *
		 * @param startTime 开始时间，单位毫秒，默认为0
		 * @param endTime   结束时间，单位毫秒，设置为-1表示不限制，默认为-1
		 */
		public Builder setClipTime(long startTime, long endTime) {
			mClipStartTime = Math.max(0, startTime);
			mClipEndTime = endTime;
			return this;
		}

//		/**
//		 * 设置画面裁剪的基准
//		 * @param isBaseWidth 是否以宽度为基准，默认为false
//		 */
//		public Builder setFrameBaseWidth(boolean isBaseWidth) {
//			this.isBaseWidth = isBaseWidth;
//			return this;
//		}

		/**
		 * 设置画幅比例
		 *
		 * @param frameRatio 画幅比例，宽/高，默认为0，表示不限制
		 */
		public Builder setFrameRatio(float frameRatio) {
			mFrameRatio = Math.max(0, frameRatio);
			return this;
		}

		/**
		 * 设置画幅偏移
		 */
		public Builder setFrameOffset(float x, float y) {
			mFrameOffsetX = -x;
			mFrameOffsetY = y;
			return this;
		}

		/**
		 * 设置视频帧率
		 *
		 * @param frameRate 视频帧率，默认为0
		 */
		public Builder setFrameRate(int frameRate) {
			mFrameNum = 0;
			mFrameRate = Math.min(Math.max(0, frameRate), MAX_FRAME_RATE);
			return this;
		}

		/**
		 * 设置帧数
		 *
		 * @param frameNum 视频固定帧数，注意：设置帧数情况下不能裁剪视频
		 */
		public Builder setFrameNum(int frameNum) {
			mFrameRatio = 0;
			mClipStartTime = 0;
			mClipEndTime = -1;
			mFrameNum = Math.max(0, frameNum);
			return this;
		}

		/**
		 * 是否保留音频数据，默认true
		 */
		public Builder setSaveAudio(boolean saveAudio) {
			isSaveAudio = saveAudio;
			return this;
		}

		/**
		 * 监听视频处理的开始和结束
		 *
		 * @param listener OnProcessListener
		 */
		public Builder setOnProcessListener(OnProcessListener listener) {
			mOnProcessListener = listener;
			return this;
		}

		public VideoEditTask build() {
			check();
			return new VideoEditTask(this);
		}

		private void check() {
			if (mContext == null) {
				throw new RuntimeException("the context is null.");
			}
			if (TextUtils.isEmpty(mInputPath)) {
				throw new RuntimeException("the inputPath is null.");
			}

			if (TextUtils.isEmpty(mOutputPath)) {
				throw new RuntimeException("the outputPath is null.");
			}

			if (mClipEndTime != -1 && mClipEndTime <= mClipStartTime) {
				throw new RuntimeException("the clip time is not correct.");
			}
		}
	}

	private static class CallbackHandler extends Handler {

		private static final int MSG_START = 1;
		private static final int MSG_FINISH = 2;
		private static final int MSG_PROGRESS = 3;
		private static final int MSG_ERROR = 4;

		private WeakReference<OnProcessListener> mReference;

		private CallbackHandler(Looper looper) {
			super(looper);
		}

		public void setOnCompressListener(OnProcessListener listener) {
			if (listener != null) {
				mReference = new WeakReference<>(listener);
			}
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_START:
					onStart();
					break;
				case MSG_FINISH:
					String path = (String)msg.obj;
					onFinish(path);
					break;
				case MSG_PROGRESS:
					onProgress(msg.arg1);
					break;
				case MSG_ERROR:
					onError();
					break;
			}
		}

		private void onStart() {
			OnProcessListener listener = get();
			if (listener != null) {
				listener.onStart();
			}
		}

		private void onFinish(String path) {
			OnProcessListener listener = get();
			if (listener != null) {
				listener.onFinish(path);
			}
		}

		private void onProgress(int progress) {
			OnProcessListener listener = get();
			if (listener != null) {
				listener.onProgress(progress);
			}
		}

		private void onError() {
			OnProcessListener listener = get();
			if (listener != null) {
				listener.onError();
			}
		}

		private OnProcessListener get() {
			if (mReference != null) {
				return mReference.get();
			}

			return null;
		}
	}
}
