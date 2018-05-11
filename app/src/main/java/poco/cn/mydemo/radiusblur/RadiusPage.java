package poco.cn.mydemo.radiusblur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import poco.cn.mydemo.R;
import poco.cn.mydemo.base.GLTextureView;

/**
 * Created by lgd on 2018/4/4.
 *
 *
 * 1 半径大小不超过宽的一半
 * 2 中心点不离开屏幕的范围
 * 3 缩放时检查1 ，2 的条件 或者扩大半径，不移动中心点
 * 4 换算成opengl的归一化和比例处理
 */
public class RadiusPage extends GLTextureView
{
    RadiusRenderer mRender;
    private int mFrW;
    private int mFrH;
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private Bitmap mBitmap;
    public RadiusPage(Context context, int frW, int frH)
    {
        super(context);
        Init(frW, frH);
        setmBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.homepage_img1));
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    protected void Init(int frW, int frH)
    {
        mFrW = frW;
        mFrH = frH;
        setFocusableInTouchMode(true);
        setEGLContextClientVersion(2);

        mRender = new RadiusRenderer(getContext());
        setRenderer(mRender);
        setRenderMode(RENDERMODE_WHEN_DIRTY);

        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener()
        {
            @Override
            public boolean onDown(MotionEvent e)
            {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e)
            {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e)
            {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
            {
                if (mScaleGestureDetector.isInProgress()) {
                    return false;
                }
                distanceX = -distanceX;
                mRender.setDeltaMove(distanceX * 2/getWidth(),distanceY* 2/getHeight());
                requestRender();
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e)
            {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
            {
                return false;
            }
        });

        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener()
        {
            @Override
            public boolean onScale(ScaleGestureDetector detector)
            {
                float scaleFactor = detector.getScaleFactor();
                float focusX = detector.getFocusX();
                float focusY = detector.getFocusY();
                mRender.setScale(scaleFactor);
                requestRender();
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector)
            {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector)
            {

            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mRender.isShowMask(true);
                requestRender();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mRender.isShowMask(false);
                requestRender();
                break;
        }
        boolean handled = false;
        handled |= mScaleGestureDetector.onTouchEvent(event);
        if (mGestureDetector.onTouchEvent(event))
        {
            handled = true;
        }
        if(handled){
            return true;
        }else
        {
            return super.onTouchEvent(event);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = mFrW;
        int height = mFrH;

        if(mBitmap != null){
            float scale1 = (float)mFrW / (float)mBitmap.getWidth();
            float scale2 = (float)mFrH / (float)mBitmap.getHeight();
            float scale = (scale1 > scale2) ? scale2 : scale1;
            width = (int)(scale * mBitmap.getWidth());
            height = (int)(scale * mBitmap.getHeight());
        }
        int widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthSpec, heightSpec);
    }

    public void setmBitmap(final Bitmap mBitmap)
    {
        this.mBitmap = mBitmap;
        requestLayout();
        queueEvent(new Runnable()
        {
            @Override
            public void run()
            {
                mRender.setmNormalBitmip(mBitmap);
                requestRender();
            }
        });

    }

    public void setProcess1(float process)
    {
        mRender.setDist(process);
        requestRender();
    }

    public void setProcess2(float process)
    {
        mRender.setStrength(process);
        requestRender();
    }
}
