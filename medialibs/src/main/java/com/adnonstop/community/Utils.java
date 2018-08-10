package com.adnonstop.community;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by: fwc
 * Date: 2018/3/30
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class Utils {

	private Utils() {

	}

	static boolean muxerMp4(String videoPath, String audioPath, String outputPath,
							long startTime, long endTime) {
		if (TextUtils.isEmpty(videoPath) || TextUtils.isEmpty(audioPath)) {
			return false;
		}

		File file = new File(videoPath);
		if (!file.exists()) {
			return false;
		}

		file = new File(audioPath);
		if (!file.exists()) {
			return false;
		}

		boolean result = false;

		MediaExtractor videoExtractor = null;
		MediaExtractor audioExtractor = null;
		MediaMuxer mediaMuxer = null;
		try {
			videoExtractor = new MediaExtractor();
			videoExtractor.setDataSource(videoPath);
			int trackIndex = selectTrack(videoExtractor, "video/");
			if (trackIndex < 0) {
				return false;
			}
			int bufferSize;
			videoExtractor.selectTrack(trackIndex);
			MediaFormat videoFormat = videoExtractor.getTrackFormat(trackIndex);
			if (videoFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
				bufferSize = videoFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
			} else {
				bufferSize = 4 * 1024;
			}

			audioExtractor = new MediaExtractor();
			audioExtractor.setDataSource(audioPath);
			trackIndex = selectTrack(audioExtractor, "audio/");
			if (trackIndex < 0) {
				return false;
			}
			audioExtractor.selectTrack(trackIndex);
			MediaFormat audioFormat = audioExtractor.getTrackFormat(trackIndex);

			mediaMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
			final int videoTrack = mediaMuxer.addTrack(videoFormat);
			// 格式不支持会崩
			final int audioTrack = mediaMuxer.addTrack(audioFormat);

			mediaMuxer.start();

			ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);

			writeToMuxer(videoExtractor, byteBuffer, mediaMuxer, videoTrack, 0, -1);

			writeToMuxer(audioExtractor, byteBuffer, mediaMuxer, audioTrack, startTime, endTime);

			mediaMuxer.stop();
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (videoExtractor != null) {
				videoExtractor.release();
				videoExtractor = null;
			}

			if (audioExtractor != null) {
				audioExtractor.release();
				audioExtractor = null;
			}

			if (mediaMuxer != null) {
				mediaMuxer.release();
				mediaMuxer = null;
			}
		}

		return result;
	}

	private static void writeToMuxer(MediaExtractor extractor, ByteBuffer byteBuffer,
									 MediaMuxer muxer, int trackIndex,
									 long startTime, long endTime) {
		boolean isFinish = false;
		int sampleSize;
		long sampleTime;
		MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
		long realStartTime = 0;

		if (startTime > 0) {
			extractor.seekTo(startTime, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
			realStartTime = extractor.getSampleTime();
		}
		while (!isFinish) {
			sampleSize = extractor.readSampleData(byteBuffer, 0);
			sampleTime = extractor.getSampleTime();
			if (sampleSize < 0 || (endTime != -1 && endTime <= sampleTime)) {
				isFinish = true;
				bufferInfo.set(0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
			} else {
				bufferInfo.set(0, sampleSize, sampleTime - realStartTime, extractor.getSampleFlags());
			}
			muxer.writeSampleData(trackIndex, byteBuffer, bufferInfo);

			byteBuffer.clear();
			extractor.advance();
		}
	}

	private static int selectTrack(@NonNull MediaExtractor extractor, String mimeStart) {
		int numTracks = extractor.getTrackCount();
		for (int i = 0; i < numTracks; i++) {
			MediaFormat format = extractor.getTrackFormat(i);
			String mime = format.getString(MediaFormat.KEY_MIME);
			if (mime.startsWith(mimeStart)) {
				return i;
			}
		}

		return -1;
	}

	static void renameOrCopy(String from, String to) {

		boolean renameSuccess;
		try {
			renameSuccess = new File(from).renameTo(new File(to));
		} catch (Exception e) {
			e.printStackTrace();
			renameSuccess = false;
		}

		if (!renameSuccess) {
			fileChannelCopy(new File(from), new File(to));
		}
	}

	public static void fileChannelCopy(File s, File t) {
		FileChannel in = null;
		FileChannel out = null;
		try {
			in = new FileInputStream(s).getChannel();
			out = new FileOutputStream(t).getChannel();
			in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	static boolean delete(String path) {
		if (!TextUtils.isEmpty(path)) {
			File file = new File(path);
			if (!file.exists()) {
				return false;
			}

			if (file.isDirectory()) {
				String[] filePaths = file.list();
				if (filePaths != null) {
					for (String filePath : filePaths) {
						delete(path + "/" + filePath);
					}
				}
			}

			return file.delete();
		}

		return false;
	}

//	public static void copyAudio(Context context, String videoPath, String audioPath, String outputPath,
//								 long startTime, long endTime) {
//
//		String tempAudioPath = context.getCacheDir().getAbsolutePath() + File.separator + UUID.randomUUID() + ".aac";
//		boolean result = AVUtils.avAudioExtract(audioPath, tempAudioPath);
//		if (!result) {
//			delete(tempAudioPath);
//			renameOrCopy(videoPath, outputPath);
//			return;
//		}
//
//		String tempAudioPath2 = context.getCacheDir().getAbsolutePath() + File.separator + UUID.randomUUID() + ".aac";
//		result = AVUtils.avClip(tempAudioPath, startTime, endTime, tempAudioPath2);
//		delete(tempAudioPath);
//		if (!result) {
//			delete(tempAudioPath2);
//			renameOrCopy(videoPath, outputPath);
//			return;
//		}
//
//		result = AVUtils.avAudioReplace(videoPath, tempAudioPath2, false, false, 0,
//							   false, 0, outputPath);
//		delete(tempAudioPath2);
//		if (!result) {
//			delete(outputPath);
//			renameOrCopy(videoPath, outputPath);
//		}
//	}
}
