package com.adnonstop.media;

/**
 * Created by hwq on 2018/4/26.
 *
 * 注意：由于底层会对AVInfo的属性进行赋值，请勿对AVInfo进行混淆加密，否则会出错
 */

public class AVInfo {
    public static final int AV_CODEC_ID_MPEG4 = 13;
    public static final int AV_CODEC_ID_H264 = 28;
    public static final int AV_CODEC_ID_MP2 = 0x15000;
    public static final int AV_CODEC_ID_MP3 = 0x15001;
    public static final int AV_CODEC_ID_AAC = 0x15002;
    public static final int AV_CODEC_ID_AC3 = 0x15003;
    public static final int AV_CODEC_ID_DTS = 0x15004;

    public int width;
    public int height;
    public int duration;//时长，audioDuration与videoDuration最大的那个
    public int audioCodecId = -1;//音频解码器
    public int videoCodecId = -1;//视频解码器
    public int audioDuration;//音频时长
    public int videoDuration;//视频时长
    public int videoRotation;//视频旋转角度
    public int audioSampleRate;//音频采样率
    public int videoBitRate;//视频比特率
    public int audioChannels;//音频声道数
    public int maxGop;//最大关键帧间隔
    public int minGop;//最小关键帧间隔
    public int keyFrameCount;//关键帧个数
    public int frameCount;//帧数
}