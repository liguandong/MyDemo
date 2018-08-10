package com.adnonstop.media;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class AVPlayer {
    private static final int OK = 0;
    private static final int EOF = -1;
    private static final int WAIT = -2;
    private static final int UNKNOWN = -3;

    private static final int MESSAGE_COMPLETE = 1;
    private static final int MESSAGE_PROGRESS = 2;
    private static final int MESSAGE_VIDEOCHANGED = 3;
    private int mDataType = AVNative.DATA_FMT_BITMAP;
    private Object mVideoSync = new Object();
    private Object mAudioSync = new Object();
    private boolean mHardwareDecode;
    private AudioTrack mAudioTrack;
    private boolean mPaused;
    private boolean mStoped = true;
    private boolean mSeeking = false;
    private boolean mPausingBeforeSeeking = false;
    private boolean mAudioComplete = false;
    private boolean mVideoComplete = false;
    private Thread mAudioDecodeThread;
    private Thread mVideoDecodeThread;
    private Thread mAudioPlayThread;
    private Thread mVideoPlayThread;
    private ArrayBlockingQueue<AVFrameInfo> mAudioQueue = new ArrayBlockingQueue<AVFrameInfo>(1);
    private ArrayBlockingQueue<AVFrameInfo> mVideoQueue = new ArrayBlockingQueue<AVFrameInfo>(1);
    private OnPlayStatusListener mOnPlayStatusListener;
    private OnPositionListener mOnPositionListener;
    private long mCurrentPosition;
    private long mLastPosition;
    private long mTime;
    private long mEscaped = 0;
    private boolean mRepeat = false;
    private long mTimeStart = 0;
    private long mTimeEnd = 0;
    private boolean mAccurateTime;
    private float mVolume = 1;
    private int mProgressNotifyInterval = 1000;
    private IDrawFrameCallback mDrawFrameCallback;
    private AVVideoComp mVideoComp;

    public interface IDrawFrameCallback
    {
        /**
         * 帧绘制回调
         * @param player 播放器实例
         * @param frame 保存帧相关绘制数据
         */
        void onAVPlayerDrawFrame(AVPlayer player, AVFrameInfo frame);
    }

    public interface OnPlayStatusListener
    {
        /**
         * 视频播放完毕回调
         * @param player 播放器实例
         */
        void onCompletion(AVPlayer player);

        /**
         * 视频切换回调
         * @param player 播放器实例
         * @param index 视频的数组索引值
         * @param file  文件
         * @param info  视频信息
         */
        void onVideoChanged(AVPlayer player, int index, String file, AVInfo info);
    }

    public interface OnPositionListener
    {
        /**
         * 播放进度回调
         * @param player 播放器实例
         */
        void onPosition(AVPlayer player);
    }

    /**
     * 构造播放器
     * @param hardware     是否使用硬件解码，小于等于720p的视频，软硬差不多。大于720p的视频用硬解会快一点，2k以上视频用硬解会明显快很多，4k以上视频用硬解部分机器可能无法解码。
     *                     软解因为没有与系统和硬件相关，因此稳定性最佳。
     *                     硬解暂时发现处理大分辨率视频，或用多线程同时处理多个较大视频时部分机型会无法解码。
     *                     硬解因为解码部分未使用cpu，因此cpu占用率比软解低。
     */
    public AVPlayer(boolean hardware)
    {
        mHardwareDecode = hardware;
    }

    /**
     * 设置播放状态监听器
     * @param l 监听器
     */
    public void setOnPlayStatusListener(OnPlayStatusListener l)
    {
        mOnPlayStatusListener = l;
    }

    /**
     * 设置进度监听器
     * @param l 监听器
     * @param updateInterval 进度更新时间间隔，单位毫秒
     */
    public void setOnPositionListener(OnPositionListener l, int updateInterval)
    {
        if(updateInterval < 1)
        {
            updateInterval = 1;
        }
        mProgressNotifyInterval = updateInterval;
        mOnPositionListener = l;
    }

    /**
     * 设置帧绘制回调
     * @param cb 回调接口
     */
    public void setDrawFrameCallback(IDrawFrameCallback cb)
    {
        mDrawFrameCallback = cb;
    }

    /**
     * 创建播放器，函数执行需要一定时间，建议在线程调用
     * @param files        输入文件，可以是音频也可以是视频
     * @param dataType     指定输出数据类型，值为AVNative.DATA_FMT_*， 支持byte[],int[],Bitmap
     * @param pixFormat    指定解码输出格式，值为AVPixelFormat.*，如需输出Bitmap，应指定为AV_PIX_FMT_RGBA或AV_PIX_FMT_RGB565
     * @return             是否成功
     */
    public boolean create(String[] files, int dataType, int pixFormat)
    {
        if(mVideoComp != null){
            return false;
        }
        mDataType = dataType;
        mVideoComp = new AVVideoComp();
        return mVideoComp.create(files, pixFormat, mHardwareDecode);
    }

    /**
     * 设置循环播放
     * @param repeat       是否循环播放
     */
    public void setRepeat(boolean repeat)
    {
        mRepeat = repeat;
    }

    /**
     * 设置播放的起始和结束点
     * @param start        设置播放起始点
     * @param end          设置播放结束点
     * @param accurate     是否需要设置为精准时间点，如为true表示时间是精准的，但如果start不为0，则开始播放时会有一定延迟，具体延迟时间与视频的关键帧间隔有关。
     *                     如为false则实际起始点可能小于start，具体时间差与离start之前最近的视频关键帧的位置有关。
     */
    public void setScope(int start, int end, boolean accurate)
    {
        if(mTimeStart > mTimeEnd){
            return;
        }
        mAccurateTime = accurate;
        mTimeStart = start;
        mTimeEnd = end;
        if(mVideoComp != null)
        {
            mVideoComp.setEndPosition(mTimeEnd);
        }
    }

    /**
     * 设置音量
     * @param volume     0~1
     */
    public void setVolume(float volume)
    {
        mVolume = volume;
        if(mAudioTrack != null) {
            mAudioTrack.setStereoVolume(volume, volume);
        }
    }

    /**
     * 获取总时长
     * @return  所有视频的总时长，单位毫秒
     */
    public long getDuration()
    {
        if(mVideoComp != null) {
            return mVideoComp.getDuration();
        }
        return 0;
    }

    /**
     * 获取当前播放位置
     * @return  当前播放位置，单位毫秒
     */
    public long getCurrentPosition()
    {
        return mCurrentPosition;
    }

    /**
     * 是否在播放状态
     * @return  是否在播放状态
     */
    public boolean isPlaying()
    {
        return mStoped == false && mPaused == false;
    }

    /**
     * 保存当前播放状态，保存之后调用restorePlayStatus来恢复播放状态。
     * 例如当用户拖动进度条时会调用不断的调用seek函数，调用seek函数会导致播放器从播放状态变为暂停状态，如果没有做处理，用户抬起手指时仍为暂停状态
     * 可已在seek开始前调用savePlayStatus来保存播放状态，在用户抬起手指时调用restorePlayStatus即可恢复播放的状态。
     */
    public void savePlayStatus()
    {
        mPausingBeforeSeeking = mPaused;
    }

    /**
     * 恢复调用savePlayStatus时的播放状态
     */
    public void restorePlayStatus()
    {
        if(mPausingBeforeSeeking == false && mStoped == false)
        {
            play();
        }
    }

    /**
     * seek到指定时间，可重复调用
     * @param time          时间戳
     * @param seekKeyFrame  是否以关键帧为起点，如果为true的话，下一次输出帧所花的时间极短，但输出帧的时间戳可能比time小很多。为false的话，能确保输出的帧时间戳与time极其接近，但下一次输出帧所花的时间会比较长
     * @return              是否成功
     */
    public boolean seek(long time, boolean seekKeyFrame)
    {
        if(time < mTimeStart)
        {
            time = mTimeStart;
        }
        synchronized(mAudioSync) {
            synchronized(mVideoSync) {
                pause();
                if(mVideoComp.seek(time, seekKeyFrame)) {
                    mSeeking = true;
                    mAudioComplete = false;
                    mVideoComplete = false;
                    updatePosition(time);
                    return true;
                }
                return false;
            }
        }
    }

    /**
     * 播放
     */
    public boolean play()
    {
        if(mVideoComp == null) {
            return false;
        }
        if(mAudioDecodeThread == null) {
            if(mTimeStart > 0)
            {
                if(seek((int) mTimeStart, !mAccurateTime) == false)
                {
                    return false;
                }
            }

            mPaused = false;
            mStoped = false;
            mAudioComplete = false;
            mVideoComplete = false;
            updatePosition(0);

            mAudioDecodeThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    audioLoop();
                }
            });
            mAudioDecodeThread.start();

            mVideoDecodeThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    videoLoop();
                }
            });
            mVideoDecodeThread.start();

            mAudioPlayThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    audioPlayLoop();
                }
            });
            mAudioPlayThread.start();

            mVideoPlayThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    videoPlayLoop();
                }
            });
            mVideoPlayThread.start();
        }
        else if(mPaused && mStoped == false)
        {
            mPaused = false;
            mStoped = false;
            mTime = System.currentTimeMillis();
            synchronized(mAudioSync) {
                synchronized(mVideoSync) {
                    if(mAudioTrack != null && mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED) {
                        mAudioTrack.play();
                    }
                }
            }
        }
        return true;
    }

    /**
     * 暂停
     */
    public void pause()
    {
        mTime = System.currentTimeMillis();
        mPaused = true;
        if(mAudioTrack != null)
        {
            mAudioTrack.pause();
        }
    }

    /**
     * 停止
     */
    public void stop()
    {
        mPaused = true;
        mStoped = true;
        if(mAudioDecodeThread != null)
        {
            mAudioDecodeThread.interrupt();
        }
        if(mVideoDecodeThread != null)
        {
            mVideoDecodeThread.interrupt();
        }
        if(mAudioPlayThread != null)
        {
            mAudioPlayThread.interrupt();
        }
        if(mVideoPlayThread != null)
        {
            mVideoPlayThread.interrupt();
        }
    }

    /**
     * 释放解码器
     */
    public void release()
    {
        synchronized(mAudioSync) {
            synchronized(mVideoSync) {
                if(mVideoComp != null) {
                    mVideoComp.release();
                }
                stop();
            }
        }
    }

    private void updatePosition(long position)
    {
        mCurrentPosition = position;
        if(mCurrentPosition - mLastPosition >= mProgressNotifyInterval
                || mCurrentPosition < mLastPosition
                || mCurrentPosition == 0)
        {
            mLastPosition = mCurrentPosition;
            if(mOnPositionListener != null)
            {
                Message msg = new Message();
                msg.what = MESSAGE_PROGRESS;
                mHandler.sendMessage(msg);
            }
        }
    }

    private synchronized void loopLeave()
    {
        synchronized(mAudioSync) {
            synchronized(mVideoSync) {
                if(mAudioComplete && mVideoComplete) {
                    mVideoComp.seek(0, true);
                    mPaused = true;
                    mStoped = true;
                    mAudioDecodeThread = null;
                    mVideoDecodeThread = null;
                    if(mAudioPlayThread != null) {
                        mAudioPlayThread.interrupt();
                        mAudioPlayThread = null;
                    }
                    if(mVideoPlayThread != null) {
                        mVideoPlayThread.interrupt();
                        mVideoPlayThread = null;
                    }
                    if(mAudioTrack != null) {
                        synchronized(mAudioTrack) {
                            if(mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                                mAudioTrack.stop();
                            }
                            mAudioTrack.release();
                        }
                        mAudioTrack = null;
                    }

                    if(mOnPlayStatusListener != null) {
                        Message msg = new Message();
                        msg.what = MESSAGE_COMPLETE;
                        mHandler.sendMessage(msg);
                    }
                }
            }
        }
    }

    private boolean checkComplete()
    {
        if(mVideoComplete && mAudioComplete)
        {
            if(mRepeat == false) {
                return true;
            }
            else
            {
                synchronized(mAudioSync) {
                    synchronized(mVideoSync) {
                        if(!mVideoComp.seek(mTimeStart, !mAccurateTime)) {
                            return true;
                        }
                    }
                }
                mSeeking = true;
                mVideoComplete = false;
                mAudioComplete = false;
                updatePosition(mTimeStart);
                mTime = System.currentTimeMillis();
                mEscaped = mTimeStart;
                return false;
            }
        }
        return false;
    }

    private void audioLoop()
    {
        if(mVideoComp == null) {
            return;
        }
        Log.d("hwq", "enter audioLoop:"+mStoped);
        mAudioComplete = false;
        mVideoComp.setEndPosition(mTimeEnd);
        while(!mStoped) {
            if(mPaused || mAudioComplete) {
                try {
                    Thread.sleep(5);
                } catch(Exception e) {
                    break;
                }
                if(mRepeat == false && checkComplete())
                {
                    break;
                }
                continue;
            }

            AVFrameInfo audio = new AVFrameInfo();
            int code = nextAudioFrame(audio);
            if(audio.data != null) {
                updatePosition(audio.pts);
                try {
                    mAudioQueue.put(audio);
                } catch(Exception e) {
                    break;
                }
            }
            else
            {
                if(code != WAIT) {
                    mAudioComplete = true;
                    if(checkComplete()) {
                        break;
                    }
                }
                else
                {
                    try {
                        Thread.sleep(5);
                    } catch(Exception e) {
                        break;
                    }
                }
            }
        }
        mAudioComplete = true;
        loopLeave();
        Log.d("hwq", "leave audioLoop");
    }

    private void videoLoop()
    {
        if(mVideoComp == null) {
            return;
        }
        Log.d("hwq", "enter videoLoop");
        mVideoComplete = false;
        mTime = System.currentTimeMillis();
        mEscaped = 0;
        mVideoComp.setEndPosition(mTimeEnd);
        while(!mStoped) {
            if((mPaused && mSeeking == false) || mVideoComplete) {
                try {
                    Thread.sleep(5);
                } catch(Exception e) {
                    break;
                }
                if(mRepeat == false && checkComplete())
                {
                    break;
                }
                continue;
            }

            AVFrameInfo video = new AVFrameInfo();
            int code = nextVideoFrame(video);
            if(video.data != null) {
                AVVideo v = mVideoComp.getCurrent();
                if(v != null && v.info.audioDuration == 0) {
                    updatePosition(video.pts);
                }
                boolean seeking = mSeeking;
                try {
                    mVideoQueue.put(video);
                    long c = System.currentTimeMillis();
                    mEscaped += (c - mTime);
                    mTime = c;
                    if(seeking)
                    {
                        mEscaped = video.pts;
                        mSeeking = false;
                    }
                    long sleep = video.pts - mEscaped;
                    if(sleep > 0) {
                        Thread.sleep(sleep);
                    }
                } catch(Exception e) {
                    break;
                }
            }
            else
            {
                if(code != WAIT) {
                    mVideoComplete = true;
                    if(checkComplete()) {
                        break;
                    }
                }
                else
                {
                    try {
                        Thread.sleep(5);
                    } catch(Exception e) {
                        break;
                    }
                }
            }
        }
        mVideoComplete = true;
        loopLeave();
        Log.d("hwq", "leave videoLoop");
    }

    private void audioPlayLoop()
    {
        Log.d("hwq", "enter audioPlayLoop");
        while(!mStoped) {
            if(mPaused) {
                try {
                    Thread.sleep(5);
                } catch(Exception e) {
                    break;
                }
                continue;
            }
            try {
                AVFrameInfo info = mAudioQueue.take();
                playSound((byte[]) info.data, info.sampleRate, info.channels);
            } catch(Exception e) {
                break;
            }
        }
        Log.d("hwq", "leave audioPlayLoop");
    }

    private void videoPlayLoop()
    {
        Log.d("hwq", "enter videoPlayLoop");
        while(!mStoped) {
            try {
                AVFrameInfo info = mVideoQueue.take();
                if(mDrawFrameCallback != null)
                {
                    mDrawFrameCallback.onAVPlayerDrawFrame(this, info);
                }
            } catch(Exception e) {
                break;
            }
        }
        Log.d("hwq", "leave videoPlayLoop");
    }

    private void playSound(byte[] data, int sampleRate, int channels)
    {
        buildAudioTrack(sampleRate, channels);
        if(mAudioTrack != null)
        {
            synchronized(mAudioTrack) {
                mAudioTrack.write(data, 0, data.length);
            }
        }
    }

    private void buildAudioTrack(int sampleRate, int channels) {
        if(mAudioTrack == null) {
            int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channels>1? AudioFormat.CHANNEL_OUT_STEREO: AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channels>1? AudioFormat.CHANNEL_OUT_STEREO: AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);
            mAudioTrack.setStereoVolume(mVolume, mVolume);
            mAudioTrack.play();
        }
    }

    /**
     * 读取下一视频帧
     * @param info          输出，调用前不需要赋值，用于接收解码的帧信息
     * @return              返回帧数据，具体类型由dataType确定，为了提高效率，接口会重用Bitmap，建议不要对返回的Bitmap进行recycle操作
     */
    private int nextVideoFrame(AVFrameInfo info)
    {
        synchronized(mVideoSync) {
            if(mVideoComp == null) {
                return UNKNOWN;
            }
            return mVideoComp.nextVideoFrame(mDataType, null, info);
        }

    }

    /**
     * 读取下一音频帧
     * @param info          输出，调用前不需要赋值，用于接收解码的帧信息
     * @return              返回帧数据
     */
    private int nextAudioFrame(AVFrameInfo info)
    {
        synchronized(mAudioSync)
        {
            if(mVideoComp == null) {
                return UNKNOWN;
            }
            return mVideoComp.nextAudioFrame(null, info);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }

    private Handler mHandler = new Handler(Looper.myLooper())
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(msg.what == MESSAGE_COMPLETE) {
                if(mOnPlayStatusListener != null) {
                    mOnPlayStatusListener.onCompletion(AVPlayer.this);
                }
            }
            else if(msg.what == MESSAGE_PROGRESS)
            {
                if(mOnPositionListener != null) {
                    mOnPositionListener.onPosition(AVPlayer.this);
                }
            }
            else if(msg.what == MESSAGE_VIDEOCHANGED)
            {
                if(mOnPlayStatusListener != null) {
                    mOnPlayStatusListener.onVideoChanged(AVPlayer.this, mVideoComp.getCurrentIndex(), mVideoComp.getCurrent().file, mVideoComp.getCurrent().info);
                }
            }
        }
    };

    private class AVVideo
    {
        public int id = -1;
        public String file;
        public AVInfo info = new AVInfo();
        public boolean audioEnd;
        public boolean videoEnd;
        public int frameReadCount;

        public boolean isEnd()
        {
            return audioEnd && videoEnd;
        }

        public boolean hasVideo()
        {
            return info.videoDuration > 0;
        }

        public void setEnd(boolean end)
        {
            audioEnd = end;
            videoEnd = end;
        }

        public int seek(long time, boolean seekKeyFrame)
        {
            int ret = AVNative.AVPlayerSeek(id, (int)time, seekKeyFrame);
            if(ret >= 0) {
                frameReadCount = 0;
            }
            return ret;
        }

        public void reset()
        {
            audioEnd = false;
            videoEnd = false;
            seek(0, true);
        }

        public Object nextVideoFrame(int dataType, Object buffer, AVFrameInfo info)
        {
            Object obj = AVNative.AVPlayerNextVideoFrame(id, dataType, buffer, info);
            frameReadCount++;
            return obj;
        }

        public Object nextAudioFrame(Object buffer, AVFrameInfo info)
        {
            return AVNative.AVPlayerNextAudioFrame(id, buffer, info);
        }
    }

    private class AVVideoComp
    {
        private AVVideo mCurrent;
        private long mEndPosition;
        private long mPtsOffset;
        private ArrayList<AVVideo> mVideos = new ArrayList<AVVideo>();

        public boolean create(String[] inputs, int pixFormat, boolean hardware)
        {
            if(inputs == null || inputs.length == 0)
            {
                return false;
            }
            for(int i = 0; i < inputs.length; i++) {
                AVVideo info = new AVVideo();
                info.file = inputs[i];
                if(info.file == null)
                    return false;
                info.id = AVNative.AVPlayerCreate(info.file, pixFormat, hardware, info.info);
                if(info.id == -1)
                {
                    return false;
                }
                mVideos.add(info);
            }
            nextVideo(false);
            return true;
        }

        public boolean seek(long time, boolean seekKeyFrame)
        {
            long total = getDuration();
            if(time > total){
                time = total;
            }
            if(mTimeEnd > 0 && time > mTimeEnd)
            {
                time = mTimeEnd;
            }
            if(time < 0){
                time = 0;
            }
            if(time < mTimeStart)
            {
                time = mTimeStart;
            }
            long duration = 0;
            for(int i = 0; i < mVideos.size(); i++) {
                AVVideo v = mVideos.get(i);
                if(time <= duration + v.info.duration)
                {
                    time -= duration;
                    for(int j = 0; j < mVideos.size(); j++) {
                        mVideos.get(j).setEnd(j < i);
                    }
                    nextVideo(false);
                    return v.seek(time, seekKeyFrame) >= 0;
                }
                duration += v.info.duration;
            }
            return false;
        }

        public void release()
        {
            for(AVVideo v : mVideos) {
                AVNative.AVPlayerRelease(v.id);
            }
            mVideos.clear();
        }

        public void setEndPosition(long position)
        {
            mEndPosition = position;
        }

        public AVVideo getCurrent()
        {
            return mCurrent;
        }

        public int getCurrentIndex()
        {
            return mVideos.indexOf(mCurrent);
        }

        public long getDuration()
        {
            long duration = 0;
            for(AVVideo v : mVideos) {
                duration += v.info.duration;
            }
            return duration;
        }

        public int nextVideoFrame(int dataType, Object buffer, AVFrameInfo info)
        {
            AVVideo video = mCurrent;
            if(video == null)
            {
                return EOF;
            }
            info.data = video.nextVideoFrame(dataType, null, info);
            info.pts += mPtsOffset;
            if(info.data == null || (mEndPosition > 0 && info.pts >= mEndPosition))
            {
                info.data = null;
                video.videoEnd = true;
                if(video.isEnd() == false)
                {
                    return WAIT;
                }
                AVVideo next = nextVideo(true);
                if(next != null)
                {
                    return nextVideoFrame(dataType, buffer, info);
                }
                return EOF;
            }
            else
            {
                return OK;
            }
        }

        public int nextAudioFrame(Object buffer, AVFrameInfo info)
        {
            AVVideo video = mCurrent;
            if(video == null)
            {
                return EOF;
            }
            //视频读取第一帧有可能很慢，在这里同步一下
            if(video.frameReadCount == 0 && video.hasVideo())
            {
                int count = 0;
                while(video.frameReadCount == 0)
                {
                    try {
                        Thread.sleep(2);
                    }
                    catch(Exception e)
                    {}
                    count++;
                    if(count > 5000)
                    {
                        break;
                    }
                }
            }
            info.data = video.nextAudioFrame(null, info);
            info.pts += mPtsOffset;
            if(info.data == null || (mEndPosition > 0 && info.pts >= mEndPosition))
            {
                info.data = null;
                video.audioEnd = true;
                if(video.isEnd() == false)
                {
                    return WAIT;
                }
                AVVideo next = nextVideo(true);
                if(next != null)
                {
                    return nextAudioFrame(buffer, info);
                }
                return EOF;
            }
            else
            {
                return OK;
            }
        }

        private synchronized AVVideo nextVideo(boolean resetTheNext)
        {
            mPtsOffset = 0;
            AVVideo video = null;
            for(AVVideo v : mVideos) {
                if(v.isEnd() == false)
                {
                    video = v;
                    break;
                }
                mPtsOffset += v.info.duration;
            }
            if(mCurrent != video)
            {
                if(video != null) {
                    mCurrent = video;
                }
                if(video != null && resetTheNext) {
                    video.reset();
                }
                if(mOnPlayStatusListener != null && video != null)
                {
                    Message msg = new Message();
                    msg.what = MESSAGE_VIDEOCHANGED;
                    mHandler.sendMessage(msg);
                }
            }
            return video;
        }
    }
}
