package com.adnonstop.decode;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.os.Build;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by: fwc
 * Date: 2018/8/9
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class HardDecoder extends AbsDecoder {

	private static final int TIMEOUT_USEC = 10000;

	private MediaCodec mDecoder;
	private MediaExtractor mExtractor;

	private MediaFormat mFormat;

	public HardDecoder(String inputPath, long startTime, long endTime) {
		super(inputPath, startTime, endTime);
	}

	@Override
	protected void init() {
		try {
			mExtractor = new MediaExtractor();
			mExtractor.setDataSource(mInputPath);
			int trackIndex = selectTrack(mExtractor);
			if (trackIndex < 0) {
				throw new RuntimeException("No video track found in " + mInputPath);
			}
			mExtractor.selectTrack(trackIndex);
			mFormat = mExtractor.getTrackFormat(trackIndex);

			mWidth = mFormat.getInteger(MediaFormat.KEY_WIDTH);
			mHeight = mFormat.getInteger(MediaFormat.KEY_HEIGHT);
			if (mFormat.containsKey("rotation-degrees")) {
				mRotation = mFormat.getInteger("rotation-degrees");
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
			mDuration = mFormat.getLong(MediaFormat.KEY_DURATION);

			if (mFormat.containsKey(MediaFormat.KEY_FRAME_RATE)) {
				mFrameRate = mFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
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
		}
	}

	@Override
	public void prepare(DecodeSurface surface) {
		try {
			surface.initSurface(true);
			String mime = mFormat.getString(MediaFormat.KEY_MIME);
			mDecoder = MediaCodec.createDecoderByType(mime);
			mDecoder.configure(mFormat, surface.getSurface(), null, 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void startImpl() {
		mDecoder.start();
		doExtract();
	}

	private void doExtract() {

		ByteBuffer[] decoderInputBuffers = mDecoder.getInputBuffers();
		MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
		int inputChunk = 0;
		int decodeCount = 0;

		boolean outputDone = false;
		boolean inputDone = false;

		if (mStartTime != 0) {
			mExtractor.seekTo(mStartTime, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
		}
		while (!outputDone && !mCancal) {

			if (!inputDone) {
				int inputBufIndex = mDecoder.dequeueInputBuffer(TIMEOUT_USEC);
				if (inputBufIndex >= 0) {
					ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
					int chunkSize = mExtractor.readSampleData(inputBuf, 0);
					if (chunkSize < 0) {
						mDecoder.queueInputBuffer(inputBufIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
						inputDone = true;
					} else {
						long presentationTimeUs = mExtractor.getSampleTime();
						mDecoder.queueInputBuffer(inputBufIndex, 0, chunkSize, presentationTimeUs, 0);
						inputChunk++;
						mExtractor.advance();
					}
				}
			}

			if (!outputDone) {
				int decoderStatus = mDecoder.dequeueOutputBuffer(info, TIMEOUT_USEC);
				if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
				} else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {

				} else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					MediaFormat newFormat = mDecoder.getOutputFormat();
				} else if (decoderStatus < 0) {

				} else { // decoderStatus >= 0
					if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
						outputDone = true;
					} else if (mEndTime != -1 && info.presentationTimeUs >= mEndTime) {
						outputDone = true;
					}

					boolean doRender = (info.size != 0);

					mDecoder.releaseOutputBuffer(decoderStatus, doRender);
					if (doRender) {

						if (mOnDrawListener != null) {
							mOnDrawListener.onDraw(info.presentationTimeUs);
						}
						decodeCount++;
					}
				}
			}
		}

		if (!mCancal && mOnDrawListener != null) {
			mOnDrawListener.onDrawFinish();
		}
	}

	@Override
	public void release() {
		if (isStart) {
			try {
				mDecoder.stop();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
			isStart = false;
		}

		if (mDecoder != null) {
			mDecoder.release();
			mDecoder = null;
		}

		if (mExtractor != null) {
			mExtractor.release();
			mExtractor = null;
		}
	}


}
