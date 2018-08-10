package com.adnonstop.decode;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;

import com.adnonstop.media.AVFrameInfo;
import com.adnonstop.media.AVNative;
import com.adnonstop.media.AVPixelFormat;
import com.adnonstop.media.AVVideoDecoder;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by: fwc
 * Date: 2018/8/9
 */
class SoftDecoder extends AbsDecoder {

	private AVVideoDecoder mDecoder;
	private DecodeSurface mSurface;

	private AVFrameInfo mFrameInfo;

	public SoftDecoder(String inputPath, long startTime, long endTime) {
		super(inputPath, startTime, endTime);

		mDecoder = AVVideoDecoder.build(false);
		mDecoder.create(inputPath, AVNative.DATA_FMT_ARRAY_BYTE, AVPixelFormat.AV_PIX_FMT_RGBA);
		mDecoder.setDataReusable(true);
		mDecoder.setSize(1080);

		mFrameInfo = new AVFrameInfo();
	}

	@Override
	protected void init() {
		MediaExtractor extractor = null;
		MediaFormat format;
		try {
			extractor = new MediaExtractor();
			extractor.setDataSource(mInputPath);
			int trackIndex = selectTrack(extractor);
			if (trackIndex < 0) {
				throw new RuntimeException("No video track found in " + mInputPath);
			}
			extractor.selectTrack(trackIndex);
			format = extractor.getTrackFormat(trackIndex);

			mWidth = format.getInteger(MediaFormat.KEY_WIDTH);
			mHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
			if (format.containsKey("rotation-degrees")) {
				mRotation = format.getInteger("rotation-degrees");
			} else {
				MediaMetadataRetriever retriever = new MediaMetadataRetriever();
				try {
					retriever.setDataSource(mInputPath);
					String s = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
					mRotation = mAddRotation = Integer.valueOf(s);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					retriever.release();
				}
			}
			mDuration = format.getLong(MediaFormat.KEY_DURATION);

			if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
				mFrameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
			} else {
				mFrameRate = 30;
			}

			if (mRotation % 180 != 0) {
				int temp = mWidth;
				mWidth = mHeight;
				mHeight = temp;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (extractor != null) {
				extractor.release();
				extractor = null;
			}
		}
	}

	@Override
	protected void startImpl() {
		Object data;
		byte[] bytes;
		ByteBuffer byteBuffer = null;
		while (!mCancal && (data = mDecoder.nextFrame(mFrameInfo)) != null) {
			bytes = (byte[]) data;
			if (byteBuffer == null) {
				byteBuffer = ByteBuffer.allocateDirect(bytes.length);
			}
			byteBuffer.clear();
			byteBuffer.put(bytes);
			byteBuffer.flip();
			mSurface.setByteBuffer(byteBuffer, mFrameInfo.width, mFrameInfo.height, mRotation);
			if (mOnDrawListener != null) {
				mOnDrawListener.onDraw(mFrameInfo.pts * 1000);
			}
		}

		if (byteBuffer != null) {
			byteBuffer.clear();
			byteBuffer = null;
		}

		if (!mCancal && mOnDrawListener != null) {
			mOnDrawListener.onDrawFinish();
		}
	}

	@Override
	public void prepare(DecodeSurface surface) {
		mSurface = surface;
		surface.initSurface(false);
	}

	@Override
	public void release() {
		mSurface = null;
		mFrameInfo = null;
		if (mDecoder != null) {
			mDecoder.release();
			mDecoder = null;
		}
	}
}