package com.adnonstop.decode;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;

/**
 * Created by: fwc
 * Date: 2018/8/9
 */
public abstract class AbsDecoder implements IDecoder {

	protected final String mInputPath;
	protected final long mStartTime;
	protected final long mEndTime;

	protected int mWidth;
	protected int mHeight;
	protected int mRotation;
	protected long mDuration;
	protected int mFrameRate;

	protected int mAddRotation = 0;

	protected boolean isStart = false;

	protected volatile boolean mCancal;

	protected OnDrawListener mOnDrawListener;

	AbsDecoder(String inputPath, long startTime, long endTime) {
		mInputPath = inputPath;
		mStartTime = startTime * 1000;
		mEndTime = endTime != -1 ? endTime * 1000 : -1;

		init();
	}

	public void setOnDrawListener(OnDrawListener listener) {
		mOnDrawListener = listener;
	}

	protected abstract void init();

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

//	public int getRotation() {
//		return mRotation;
//	}

	public int getAddRotation() {
		return -mAddRotation;
	}

	public int getFrameRate() {
		return mFrameRate;
	}

	public long getDuration() {
		return mDuration;
	}

	public void cancel() {
		mCancal = true;
	}

	public static AbsDecoder create(String inputPath, long startTime, long endTime) {
		int width = 1920;
		int height = 1080;
		MediaMetadataRetriever retriever = null;
		try {
			retriever = new MediaMetadataRetriever();
			retriever.setDataSource(inputPath);
			String widthString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
			String heightString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
			width = Integer.valueOf(widthString);
			height = Integer.valueOf(heightString);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (retriever != null) {
				retriever.release();
				retriever = null;
			}
		}

		final int maxSize = Math.max(width, height);
		if (maxSize > 3500) {
			return new SoftDecoder(inputPath, startTime, endTime);
		} else {
			return new HardDecoder(inputPath, startTime, endTime);
		}
	}

	@Override
	public void start() {
		if (isStart) {
			return;
		}

		isStart = true;
		startImpl();
	}

	protected abstract void startImpl();

	protected static int selectTrack(MediaExtractor extractor) {
		int numTracks = extractor.getTrackCount();
		for (int i = 0; i < numTracks; i++) {
			MediaFormat format = extractor.getTrackFormat(i);
			String mime = format.getString(MediaFormat.KEY_MIME);
			if (mime.startsWith("video/")) {
				return i;
			}
		}

		return -1;
	}

	public interface OnDrawListener {
		void onDraw(long presentationTime);

		void onDrawFinish();
	}
}
