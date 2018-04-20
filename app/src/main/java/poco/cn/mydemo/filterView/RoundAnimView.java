package poco.cn.mydemo.filterView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;


/**
 * Created by lgd on 2018/4/11.
 */
public class RoundAnimView extends View
{
    private Paint mPaint;
    private RectF mRectF;
    private int mDefCornerSize = 20;
    private int mStartH;
    private int mBkColor = Color.RED;

    public RoundAnimView(Context context)
    {
        super(context);
        mPaint = new Paint();
        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mBkColor);
        mRectF = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        mRectF.set(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        mRectF.set(0, mStartH, getWidth(), getHeight());
        canvas.clipRect(mRectF);
        mRectF.set(0, 0, getWidth(), getHeight());
        canvas.drawRoundRect(mRectF, mDefCornerSize, mDefCornerSize, mPaint);
    }

    public void setCornerSize(int cornerSize)
    {
        this.mDefCornerSize = cornerSize;
    }

    public void setStartH(int startH)
    {
        this.mStartH = startH;
        invalidate();
    }
}
