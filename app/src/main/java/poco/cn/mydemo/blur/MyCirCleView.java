package poco.cn.mydemo.blur;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

/**
 * Created by lgd on 2018/4/3.
 */
public class MyCirCleView extends View
{
    private final float MIN_RADIUS = 100;
    private float radius = MIN_RADIUS;
    private float centerX;
    private float centerY;
    private float ratios = 720 * 1.0f/960;
    GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    public MyCirCleView(Context context)
    {
        this(context,null);
    }

    public MyCirCleView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
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
                distanceX = -distanceX;
                distanceY = -distanceY;
                centerX += distanceX;
                centerY -= distanceY;

                if(centerX - radius < 0){
                    centerX = radius;
                }else if(centerX + radius > getWidth()){
                    centerX = getWidth() - radius;
                }

                if(centerY - radius < 0){
                    centerY = radius;
                }else if(centerY + radius > getHeight()){
                    centerY = getHeight() - radius;
                }
                invalidate();
                if(cirCleCallBack != null){
                    cirCleCallBack.onCallBack((centerX- getWidth()/2)/(getWidth()/2),(centerY - getHeight()/2)/(getHeight()/2),0.5f);
                }
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
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {

        return super.onTouchEvent(event);
    }

    public void setRadius(int radius)
    {
        this.radius = radius;
        invalidate();
    }

    private CirCleCallBack cirCleCallBack;

    public void setCirCleCallBack(CirCleCallBack cirCleCallBack)
    {
        this.cirCleCallBack = cirCleCallBack;
    }

    public interface CirCleCallBack{
        void onCallBack(float centerX,float centerY,float radius);
    }
}
