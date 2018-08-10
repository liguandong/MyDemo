package com.adnonstop.media;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;

import com.adnonstop.community.VideoEditTask;

import java.io.File;

/**
 * Created by: hwq
 * Date: 2018/5/9
 */
public class AVUtils {

    private AVUtils() {
    }

    private static class Result
    {
        public int code = 0;
        public Object data = null;
    }

    /**
     * 视频缩放(硬解硬编)，为了避免视频被拉伸，调用前需要根据视频的旋转角度值对宽高进行反转处理
     * @param inputPath   原视频路径
     * @param width       输出视频宽度
     * @param height      输出视频高度
     * @param outputPath  视频保存路径
     * @return            是否成功
     */
    public static boolean avResize(Context context, String inputPath, String outputPath, int width, int height, final AVListener l)
    {
        if (!isFileExist(inputPath)) {
            throw new IllegalArgumentException("the file: " + inputPath + " not found.");
        }

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("the output size is not correct.");
        }

        VideoEditTask.Builder builder = new VideoEditTask.Builder(context)
                .setInputPath(inputPath)
                .setOutputPath(outputPath)
                .setTargetSize(width, height)
                .setOnProcessListener(new VideoEditTask.OnProcessListener() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onFinish(String outputpath) {
                    }

                    @Override
                    public void onError() {
                    }

                    @Override
                    public void onProgress(int progress) {
                        if(l != null)
                        {
                            l.onProgress(progress/100f);
                        }
                    }
                });
        return builder.build().syncRun();
    }

    /**
     * 视频缩放(软解or硬解，软编)
     * @param inputPath     原视频路径
     * @param fps           帧率
     * @param fixFps        是否固定帧率，如果为true表示输出帧率一定会等于fps，否则fps只作为上限，如果原始帧率小于fps，那么以原始帧率为准
     * @param vcodecOpts    视频编码器选项，示例：crf=28,xxx=xxx或crf=28，用逗号分隔选项，=号前为key，后为value。
     *                      crf选项用于设置视频输出的质量，取值范围为0~51，其中0为无损模式，数值越大，画质越差，生成的文件却越小。
     *                      从主观上讲，18~28是一个合理的范围。18被认为是视觉无损的（从技术角度上看当然还是有损的），它的输出视频质量和输入视频相当。
     * @param size          输出的视频大小。该数值表示输出视频最短边的大小，如原视频是720*1280，如果size为640，那么输出的视频尺寸为640*1136。
     *                      size为0表示不压缩尺寸，维持原始尺寸。
     *                      如果原始大小比size要小的话，输出的尺寸保持原始大小。
     *                      请注意该参数必须是2的倍数，否则函数内部会自动调整输出宽高为2的倍数
     * @param hwdecode      是否使用硬件解码，小于等于720p的视频，软硬差不多。大于720p的视频用硬解会快一点，2k以上视频用硬解会明显快很多，4k以上视频用硬解部分机器可能无法解码。
     *                      软解因为没有与系统和硬件相关，因此稳定性最佳。
     *                      硬解暂时发现处理大分辨率视频，或用多线程同时处理多个较大视频时部分机型会无法解码。
     *                      硬解因为解码部分未使用cpu，因此cpu占用率比软解低。
     * @param listener      进度监听器
     * @param outputPath    视频保存路径
     * @return              是否成功
     */
    public static boolean avResize(String inputPath, int fps, boolean fixFps, String vcodecOpts, int size, boolean hwdecode, AVListener listener, String outputPath)
    {
        if (!isFileExist(inputPath)) {
            throw new IllegalArgumentException("the file: " + inputPath + " not found.");
        }

        int result = AVNative.AVResize(inputPath, outputPath, fps, fixFps, vcodecOpts, size, hwdecode, listener);

        return result >= 0;
    }

    /**
     * 设置AssetManager，接口支持传递assets目录文件作为输入文件时需要调用此函数，否则不支持。此函数可只调用一次，不需重复调用。传递assets文件时，文件名以 file:///android_asset/文件名 的形式传递
     * @param mgr  AssetManager
     */
    public static void setAssetManager(AssetManager mgr)
    {
        AVNative.SetAssetManager(mgr);
    }

    /**
     * 获取视频相关信息，注意：由于底层会对AVInfo的属性进行赋值，请勿对AVInfo进行混淆加密，否则会出错
     * @param inputPath   输入文件，可以是音频或视频，支持assets目录文件，见setAssetManager函数说明
     * @param info        输出视频相关信息
     * @param detail      是否需要获取详细信息，如maxGop,minGop,keyFrameCount,frameCount，为true的话耗时更长
     * @return            是否成功
     */
    public static boolean avInfo(String inputPath, AVInfo info, boolean detail) {
        if (!isFileExist(inputPath)) {
            throw new IllegalArgumentException("the file: " + inputPath + " not found.");
        }

        int result = AVNative.AVInfo(inputPath, info, detail);

        return result >= 0;
    }

    /**
     * 视频加速
     * @param inputPath   原视频路径，支持assets目录文件，见setAssetManager函数说明
     * @param speedRatio  加速因子
     * @param outputPath  视频保存路径，后缀名需要与输入保持一致
     * @return            是否成功
     */
    public static boolean avSpeed(String inputPath, float speedRatio, String outputPath) {
        if (!isFileExist(inputPath)) {
            throw new IllegalArgumentException("the file: " + inputPath + " not found.");
        }

        int result = AVNative.AVSpeed(inputPath, outputPath, 1 / speedRatio);

        return result >= 0;
    }

    /**
     * 裁剪音视频
     * @param inputPath  输入文件，可以是音频或视频，支持assets目录文件，见setAssetManager函数说明
     * @param start      开始时间，单位毫秒
     * @param end        结束时间，单位毫秒
     * @param outputPath 保存路径，后缀名需要与输入保持一致
     * @return           是否成功
     */
    public static boolean avClip(String inputPath, long start, long end, String outputPath) {
        if (!isFileExist(inputPath)) {
            throw new IllegalArgumentException("the file: " + inputPath + " not found.");
        }

        if (start >= end) {
            throw new IllegalArgumentException("the time is not correct.");
        }

        int result = AVNative.AVClip(inputPath, outputPath, (int)start, (int)end);

        return result >= 0;
    }

    /**
     * 分割视频
     * @param inputPath   原视频路径，支持assets目录文件，见setAssetManager函数说明
     * @param splitTime   分割时间点，单位毫秒
     * @param outputPath1 视频分割保存路径1，后缀名需要与输入保持一致
     * @param outputPath2 视频分割保存路径2，后缀名需要与输入保持一致
     * @return            是否成功
     */
    public static boolean avSegment(String inputPath, long splitTime, String outputPath1, String outputPath2) {
        if (!isFileExist(inputPath)) {
            throw new IllegalArgumentException("the file: " + inputPath + " not found.");
        }

        int result = AVNative.AVSegment(inputPath, (int)splitTime, outputPath1, outputPath2);

        return result >= 0;
    }

    /**
     * 连接多个音视频，支持音频和音频连接，输出支持mp4和aac格式
     * @param files        输入音视频文件，个数必须大于等于2，暂无输入文件格式要求，但必须是全部为音频或视频文件。支持assets目录文件，见setAssetManager函数说明
     * @param outputPath   视频保存路径，如果为音频后缀名必须是aac
     * @return             是否成功
     */
    public static boolean avConcat(String[] files, String outputPath) {
        for(String file : files) {
            if(!isFileExist(file)) {
                throw new IllegalArgumentException("the input file not found.");
            }
        }

        int result = AVNative.AVConcat(files, outputPath);

        return result >= 0;
    }

    /**
     * 视频帧读取
     * @param inputPath    视频路径。支持assets目录文件，见setAssetManager函数说明
     * @param start        解码开始时间，<= 0表示不指定开始时间，从头开始解码
     * @param end          解码结束时间，-1表示不指定结束时间，直到所有帧解码完毕才返回
     * @param dataType     指定onFrame回调data的类型，值为AVNative.DATA_FMT_*， 支持byte[],int[],Bitmap
     * @param pixFormat    指定解码输出格式，值为AVPixelFormat.*，如需输出Bitmap，应指定为AV_PIX_FMT_RGBA或AV_PIX_FMT_RGB565
     * @param cb           解码回调，解码完一帧后通过此方法回调给调用者，AVFrameReceiver.onFrame方法的返回值可以控制是否继续解码，如为false表示继续，true表示中断解码
     * @return             是否成功
     */
    public static boolean avDecode(String inputPath, long start, long end, int dataType, int pixFormat, AVNative.AVFrameReceiver cb) {
        if (!isFileExist(inputPath)) {
            throw new IllegalArgumentException("the file: " + inputPath + " not found.");
        }

        int result = AVNative.AVDecode(inputPath, (int)start, (int)end, dataType, pixFormat, cb);

        return result >= 0;
    }

    /**
     * 读取一帧图像
     * @param inputPath    视频路径。支持assets目录文件，见setAssetManager函数说明
     * @param time         指定帧所在时间戳
     * @param dataType     指定返回数据类型，值为AVNative.DATA_FMT_*， 支持byte[],int[],Bitmap
     * @param pixFormat    指定解码输出格式，值为AVPixelFormat.*，如需输出Bitmap，应指定为AV_PIX_FMT_RGBA或AV_PIX_FMT_RGB565
     * @param hardware     是否是硬件解码
     * @param seekKeyFrame 是否以关键帧为起点，如果为true的话，下一次调用nextFrame返回的帧一定是关键帧，但帧时间戳可能比time小很多。为false的话，能确保返回的帧时间戳与time极其接近，但所花的时间会比较长
     * @param size         输出图像最短边大小,为0表示使用原始大小
     * @return             解码后的帧数据，需要进行强转为与dataType一致的类型，例如
     * Bitmap bmp = (Bitmap)avDecodeOneFrame(xxx, 1000, AVNative.DATA_FMT_BITMAP, AVPixelFormat.AV_PIX_FMT_RGBA);
     */
    public static Object avDecodeOneFrame(String inputPath, long time, int dataType, int pixFormat, boolean hardware, boolean seekKeyFrame, int size) {
        if (!isFileExist(inputPath)) {
            throw new IllegalArgumentException("the file: " + inputPath + " not found.");
        }

        AVVideoDecoder decoder = AVVideoDecoder.build(hardware);
        decoder.create(inputPath, dataType, pixFormat);
        decoder.seek((int)time, seekKeyFrame);
        if(size > 0) {
            decoder.setSize(size);
        }
        AVFrameInfo info = new AVFrameInfo();
        Object data = decoder.nextFrame(info);
        decoder.release();
        return data;
    }

    /**
     * 读取一帧图像
     * @param inputPath    视频路径。支持assets目录文件，见setAssetManager函数说明
     * @param time         指定帧所在时间戳
     * @param hardware     是否是硬件解码
     * @param seekKeyFrame 是否以关键帧为起点，如果为true的话，下一次调用nextFrame返回的帧一定是关键帧，但帧时间戳可能比time小很多。为false的话，能确保返回的帧时间戳与time极其接近，但所花的时间会比较长
     * @param size         输出图像最短边大小,为0表示使用原始大小
     * @return             解码后的帧图片
     */
    public static Bitmap avDecodeOneFrame(String inputPath, long time, boolean hardware, boolean seekKeyFrame, int size) {
        return (Bitmap) avDecodeOneFrame(inputPath, time, AVNative.DATA_FMT_BITMAP, AVPixelFormat.AV_PIX_FMT_RGBA, hardware, seekKeyFrame, size);
    }

    /**
     * 读取一帧图像
     * @param inputPath    视频路径。支持assets目录文件，见setAssetManager函数说明
     * @param time         指定帧所在时间戳
     * @param size         输出图像最短边大小,为0表示使用原始大小
     * @return             解码后的帧图片
     */
    public static Bitmap avDecodeOneFrame(String inputPath, long time, int size) {
        return (Bitmap) avDecodeOneFrame(inputPath, time, AVNative.DATA_FMT_BITMAP, AVPixelFormat.AV_PIX_FMT_RGBA, false, false, size);
    }

    /**
     * 读取一帧图像
     * @param inputPath    视频路径。支持assets目录文件，见setAssetManager函数说明
     * @param time         指定帧所在时间戳
     * @return             解码后的帧图片
     */
    public static Bitmap avDecodeOneFrame(String inputPath, long time) {
        return (Bitmap) avDecodeOneFrame(inputPath, time, AVNative.DATA_FMT_BITMAP, AVPixelFormat.AV_PIX_FMT_RGBA, false, false, 0);
    }

    /**
     * 视频旋转
     * @param inputPath    视频路径。支持assets目录文件，见setAssetManager函数说明
     * @param rotation     0，90,180,270
     * @param append       是否在视频本身的旋转值基础上加上rotation
     * @param outputPath   视频保存路径，后缀名需要与输入保持一致
     * @return             是否成功
     */
    public static boolean avRotate(String inputPath, int rotation, boolean append, String outputPath) {
        if (!isFileExist(inputPath)) {
            throw new IllegalArgumentException("the file: " + inputPath + " not found.");
        }

        int result = AVNative.AVRotate(inputPath, outputPath, rotation, append);

        return result >= 0;
    }

    /**
     * 视频重新封装，主要用于调整mp4 moov box位置
     * @param inputPath     原视频路径
     * @param outputPath    视频保存路径，后缀名需要与输入保持一致
     * @return              是否成功
     */
    public static boolean avRemuxer(String inputPath, String outputPath)
    {
        if (!isFileExist(inputPath)) {
            throw new IllegalArgumentException("the file: " + inputPath + " not found.");
        }

        int result = AVNative.AVRemuxer(inputPath, outputPath);

        return result >= 0;
    }

    /**
     * 是否mp4的moov在mdat后面
     * @param inputPath   mp4文件
     * @return            是否moov在mdat后面
     */
    public static boolean isMoovOnBack(String inputPath)
    {
        if (!isFileExist(inputPath)) {
            throw new IllegalArgumentException("the file: " + inputPath + " not found.");
        }

        if(!inputPath.toLowerCase().endsWith(".mp4"))
        {
            return false;
        }

        return AVNative.IsMoovOnBack(inputPath);
    }

    /**
     * 提取视频文件中的音频
     * @param inputPath    视频路径。支持assets目录文件，见setAssetManager函数说明
     * @param outputPath   音频保存路径,支持wav,aac格式
     * @return             是否成功
     */
    public static boolean avAudioExtract(String inputPath, String outputPath) {
        if (!isFileExist(inputPath)) {
            throw new IllegalArgumentException("the file: " + inputPath + " not found.");
        }

        int result = AVNative.AVAudioExtract(inputPath, outputPath);

        return result >= 0;
    }

    /**
     * 音量调节和前后声音渐变
     * @param inputPath    输入文件，可以是音频或视频。支持assets目录文件，见setAssetManager函数说明
     * @param volume       音量，1为原始音量，否则为提高或降低音量
     * @param fadeIn       否需要渐入（视频开始时声音从低到高变化）
     * @param timeFadeIn   渐入时长
     * @param fadeOut      否需要渐出（视频结束时声音从高到低变化）
     * @param timeFadeOut  渐出时长
     * @param outputPath   保存路径。如果是视频后缀名需要与输入保持一致。如果是音频可以不同格式，支持wav,aac
     * @return             是否成功
     */
    public static boolean avAudioVolume(String inputPath, float volume, boolean fadeIn, int timeFadeIn, boolean fadeOut, int timeFadeOut, String outputPath) {
        if (!isFileExist(inputPath)) {
            throw new IllegalArgumentException("the file: " + inputPath + " not found.");
        }

        int result = AVNative.AVAudioVolume(inputPath, outputPath, volume, fadeIn, timeFadeIn, fadeOut, timeFadeOut);

        return result >= 0;
    }

    /**
     * 替换视频中的声音
     * @param inputPath    视频路径。支持assets目录文件，见setAssetManager函数说明
     * @param audioPath    用来替换的音频文件,如果为null或""则表示清除视频声音，可以是mp4，mp3,aac,wav等格式
     * @param repeat       当视频时长大于音频时长的时候，是否重复音频
     * @param fadeIn       否需要渐入（视频开始时声音从低到高变化）
     * @param timeFadeIn   渐入时长
     * @param fadeOut      否需要渐出（视频结束时声音从高到低变化）
     * @param timeFadeOut  渐出时长
     * @param outputPath   视频保存路径，后缀名需要与输入保持一致
     * @return             是否成功
     */
    public static boolean avAudioReplace(String inputPath, String audioPath, boolean repeat, boolean fadeIn, int timeFadeIn, boolean fadeOut, int timeFadeOut, String outputPath) {
        if (!isFileExist(inputPath)) {
            throw new IllegalArgumentException("the file: " + inputPath + " not found.");
        }

        int result = AVNative.AVAudioReplace(inputPath, outputPath, audioPath, repeat, fadeIn, timeFadeIn, fadeOut, timeFadeOut);

        return result >= 0;
    }

    /**
     * 两个声音混音，输入可以是音频也可以是视频
     * @param inputPath    输入文件，可以是音频也可以是视频。支持assets目录文件，见setAssetManager函数说明
     * @param audioPath    用来混音的音频文件，可以是mp4，mp3,aac,wav等格式
     * @param volumeInput  inputPath的音量调整，1为原始音量，否则为提高或降低音量
     * @param volumeAudio  audioPath的音量调整，1为原始音量，否则为提高或降低音量
     * @param repeat       当视频时长大于音频时长的时候，是否重复音频
     * @param fadeIn       否需要渐入（视频开始时声音从低到高变化）
     * @param timeFadeIn   渐入时长
     * @param fadeOut      否需要渐出（视频结束时声音从高到低变化）
     * @param timeFadeOut  渐出时长
     * @param outputPath   视频保存路径，后缀名需要与输入保持一致
     * @return             是否成功
     */
    public static boolean avAudioMix(String inputPath, String audioPath, float volumeInput, float volumeAudio, boolean repeat, boolean fadeIn, int timeFadeIn, boolean fadeOut, int timeFadeOut, String outputPath) {
        if (!isFileExist(inputPath)) {
            throw new IllegalArgumentException("the file: " + inputPath + " not found.");
        }

        int result = AVNative.AVAudioMix(inputPath, audioPath, outputPath, volumeInput, volumeAudio, repeat, fadeIn, timeFadeIn, fadeOut, timeFadeOut);

        return result >= 0;
    }

    /**
     * 多个音频文件混音
     * @param audios       输入文件，至少是2个或2个以上，可以是mp4，mp3,aac,wav等格式。支持assets目录文件，见setAssetManager函数说明
     * @param fadeIn       否需要渐入（视频开始时声音从低到高变化）
     * @param timeFadeIn   渐入时长
     * @param fadeOut      否需要渐出（视频结束时声音从高到低变化）
     * @param timeFadeOut  渐出时长
     * @param outputPath   保存路径，支持wav,aac
     * @return             是否成功
     */
    public static boolean avAudioMix(String[] audios, boolean fadeIn, int timeFadeIn, boolean fadeOut, int timeFadeOut, String outputPath) {
        for(String file : audios) {
            if(!isFileExist(file)) {
                throw new IllegalArgumentException("the input file not found.");
            }
        }

        int result = AVNative.AVAudioMultiMix(audios, outputPath, fadeIn, timeFadeIn, fadeOut, timeFadeOut);

        return result >= 0;
    }

    /**
     * 音频格式转换
     * @param inputPath    输入文件，可以是mp4，mp3,aac,wav等格式。支持assets目录文件，见setAssetManager函数说明
     * @param outputPath   保存路径，支持wav,aac格式
     * @param sampleRate   音频采样率,除wav外，其它输出格式仅支持48000以下采样率，具体数值请查阅资料，常用采样率为48000,44100
     * @return             是否成功
     */
    public static boolean avAudioConvert(String inputPath, String outputPath, int sampleRate) {
        if (!isFileExist(inputPath)) {
            throw new IllegalArgumentException("the file: " + inputPath + " not found.");
        }

        int result = AVNative.AVAudioConvert(inputPath, outputPath, sampleRate);

        return result >= 0;
    }

    /**
     * 音频延长
     * @param inputPath     输入音频文件，如果inputPath是视频，outputPath的后缀应该是.aac。支持assets目录文件，见setAssetManager函数说明
     * @param srcClipStart  原音频文件的裁剪起始点，不裁剪传0
     * @param srcClipEnd    原音频文件的裁剪结束点，不裁剪传-1
     * @param newDuration   指定输出时长
     * @param outputPath    保存路径，格式需要与输入保持一致
     * @return              是否成功
     */
    public static boolean avAudioRepeat(String inputPath, long srcClipStart, long srcClipEnd, long newDuration, String outputPath) {
        if (!isFileExist(inputPath)) {
            throw new IllegalArgumentException("the file: " + inputPath + " not found.");
        }

        int result = AVNative.AVAudioRepeat(inputPath, outputPath, (int)srcClipStart, (int)srcClipEnd, (int)newDuration);

        return result >= 0;
    }

    /**
     * 创建空白音频（没有声音）
     * @param outputPath    保存路径，支持wav,aac格式
     * @param sampleRate    采样率，除wav外，其它输出格式仅支持48000以下采样率，具体数值请查阅资料，常用采样率为48000,44100
     * @param duration      时长
     * @return              是否成功
     */
    public static boolean avAudioCreateBlankAudio(String outputPath, int sampleRate, long duration) {
        int result = AVNative.AVAudioCreateBlankAudio(outputPath, sampleRate, (int)duration);
        return result >= 0;
    }

    /**
     * 生成音频波形
     * @param inputPath         输入文件，可以是mp4，mp3,aac,wav等格式
     * @param samplePerSecond   每秒钟的音频波形个数
     * @param info              输出，音频长度等相关信息
     * @param progress          进度回调，回调实时更新波形的buffer，如需实时绘制可以在回调种处理。不需要可以传null
     * @return                  波形数据，数值范围是-127~127，返回的对象与AVWaveProgress.onBufferCreated(byte[] buffer)回调中的buffer是一致的
     */
    public static byte[] avAudioWave(String inputPath, float samplePerSecond, AVInfo info, AVWaveProgress progress)
    {
        if (!isFileExist(inputPath)) {
            throw new IllegalArgumentException("the file: " + inputPath + " not found.");
        }
        return AVNative.AVAudioWave(inputPath, samplePerSecond, info, progress);
    }

    /**
     * 生成音频波形Bitmap
     * @param inputPath         输入文件，可以是mp4，mp3,aac,wav等格式
     * @param samplePerSecond   每秒钟的波形个数
     * @param height            生成图片的高度
     * @param minHeight         最小绘制波形的高度，如果为0，则当波形幅度是0的时候不进行该波形的绘制
     * @param waveScale         波形的缩放系数，范围0~1，不需要缩放传1
     * @param sampleWidth       单个波形的绘制宽度。生成的Bitmap宽度 = (音频时长 * samplePerSecond) * (sampleWidth + sampleGap)
     * @param sampleGap         波形之间的间隔
     * @param sampleColor       波形的绘制颜色
     * @param bgColor           Bitmap的背景色
     * @param info              输出，音频长度等相关信息
     * @return
     */
    public static Bitmap avCreateWaveBitmap(String inputPath, float samplePerSecond, int height, int minHeight, float waveScale, int sampleWidth, int sampleGap, int sampleColor, int bgColor, AVInfo info)
    {
        Bitmap bmp = null;
        if(info == null)
        {
            info = new AVInfo();
        }
        if(samplePerSecond < 0.01f){
            samplePerSecond = 0.01f;
        }
        byte[] wave = avAudioWave(inputPath, samplePerSecond, info, null);
        if(wave != null && wave.length > 0)
        {
            if(sampleWidth < 1){
                sampleWidth = 1;
            }
            if(height < 1){
                height = 1;
            }
            int space = sampleWidth + sampleGap;
            bmp = Bitmap.createBitmap(wave.length*space, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            canvas.drawColor(bgColor);
            Paint paint = new Paint();
            paint.setColor(sampleColor);
            int yCenter = height/2;
            for(int i = 0; i < wave.length; i++)
            {
                int v = wave[i];
                v = height*v/256;
                if(v < 0)
                {
                    v = -v;
                }
                v *= waveScale;
                if(v < minHeight)
                {
                    v = minHeight;
                }
                int x = i*space;
                canvas.drawRect(x, yCenter-v, x+sampleWidth, yCenter, paint);
                canvas.drawRect(x, yCenter, x+sampleWidth, yCenter+v, paint);
            }
        }
        return bmp;
    }

    /**
     * 判断所给文件是否存在
     * @param path 文件路径
     * @return 是否存在
     */
    private static boolean isFileExist(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }

        if(path.startsWith("file:///android_asset/"))
        {
            return true;
        }

        File file = new File(path);
        return file.exists() && file.length() > 0;
    }

    /**
     * 删除文件
     * @param path 文件路径
     */
    private static void deleteFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }

        File file = new File(path);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }
}