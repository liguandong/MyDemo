package poco.cn.mydemo.imgprocess;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import poco.cn.mydemo.R;


/**
 * Created by lgd on 2018/3/27.
 */

public class MyView extends View
{
    private Bitmap mBitmap;
    private Bitmap mBlurBitmap;
    private Bitmap mMaxBitmap;
    Rect mTempSrc;
    Rect mTempDst;
    int mode = 1;
    private int NROMAL = 1;
    private int BLUR = 2;
    private int MAX = 3;
    private Paint paint;

    public MyView(Context context)
    {
        this(context, null);
    }

    public MyView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.homepage_img1);
        mBlurBitmap = BlurBitmapUtil.blurBitmap(getContext(),mBitmap,25);
        mBlurBitmap = getTransAlphaBitmap(mBlurBitmap);
//        mBitmap = Bitmap.createScaledBitmap(mBitmap,mBitmap.getWidth()/2,mBitmap.getHeight()/2,false);
//        mBlurBitmap = BlurBitmapUtil.blurBitmap(getContext(),mBitmap,25);
//        mMaxBitmap = Bitmap.createBitmap(mBitmap.getWidth(),mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
//        int centerX = mBitmap.getWidth()/2;
//        int centerY = mBitmap.getHeight()/2;
//        int radius = (int) Math.sqrt(centerX * centerX + centerY * centerY);
//        for (int i = 0; i < mBitmap.getWidth(); i++)
//        {
//            for (int j = 0; j < mBitmap.getHeight(); j++)
//            {
////                int p1 = mBitmap.getPixel(i,j);
////                int p2 = mBlurBitmap.getPixel(i,j);
////                int p = p1/2 + p2/2;
//                int p = mBlurBitmap.getPixel(i,j);
//                int r = (int) Math.sqrt(i * i + j * j);
//                float alpha = 1 - Math.abs(r)* 1.0f/radius;
//                int a = p & 0xff00000;
//                a = (int) (a * alpha);
//                p = p & 0x00ffffff;
//                p = a | p;
//                mMaxBitmap.setPixel(i,j,p);
//            }
//        }
        mTempSrc = new Rect();
        mTempDst = new Rect();
        paint = new Paint();
        int radius = (int) Math.sqrt(mBitmap.getWidth() * mBitmap.getWidth() / 4 + mBitmap.getHeight() * mBitmap.getHeight() / 4);
        RadialGradient radialGradient = new RadialGradient(mBitmap.getWidth() / 2, mBitmap.getHeight() / 2, radius, 0xffffffff, 0x00000000, Shader.TileMode.CLAMP);
        paint.setShader(radialGradient);
    }


    Matrix mDrawMatrix = new Matrix();

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

//        drawBitmap(canvas,mBitmap);
//        drawBitmap(canvas,mBlurBitmap);
//        canvas.save();
//        canvas.translate(0,getHeight()/2);
//        drawBitmap(canvas,mBitmap);
//        drawBitmap(canvas,mMaxBitmap);
//        canvas.restore();
//        drawBitmap(canvas, mBitmap);
        drawBitmap(canvas, mBlurBitmap);
    }

    public void drawBitmap(Canvas canvas, Bitmap b)
    {
        mDrawMatrix.reset();
        final int dwidth = b.getWidth();
        final int dheight = b.getHeight() / 2;

        final int vwidth = getWidth();
        final int vheight = getHeight();
        float scale;
        float dx = 0, dy = 0;

//        if (dwidth * vheight > vwidth * dheight) {
//            scale = (float) vheight / (float) dheight;
//            dx = (vwidth - dwidth * scale) * 0.5f;
//        } else {
//            scale = (float) vwidth / (float) dwidth;
//            dy = (vheight - dheight * scale) * 0.5f;
//        }
//
//        mDrawMatrix.setScale(scale, scale);
//        mDrawMatrix.postTranslate(Math.round(dx), Math.round(dy));
        if (dwidth <= vwidth && dheight <= vheight)
        {
            scale = 1.0f;
        } else
        {
            scale = Math.min((float) vwidth / (float) dwidth,
                    (float) vheight / (float) dheight);
        }

        dx = Math.round((vwidth - dwidth * scale) * 0.5f);
        dy = Math.round((vheight - dheight * scale) * 0.5f);

        mDrawMatrix.setScale(scale, scale);
        mDrawMatrix.postTranslate(dx, 0);
        canvas.drawBitmap(b, mDrawMatrix, paint);

    }

    /**
     * 设置图片的透明度从上到下渐变，使下边缘平滑过渡（注意只跟着Y坐标变）
     *
     * @param sourceImg
     * @return
     */
    public static Bitmap getTransAlphaBitmap(Bitmap sourceImg)
    {

        int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];

        sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0, sourceImg

                .getWidth(), sourceImg.getHeight());// 获得图片的ARGB值

        //number的范围为0-100,0为全透明，100为不透明
        float number = 100;

        //透明度数值
        float alpha = number * 255 / 100;

        //图片渐变的范围（只设置图片一半范围由上到下渐变，上面不渐变，即接近边缘的那一半）
        float range = sourceImg.getHeight() / 2.0f;

        //透明度渐变梯度，每次随着Y坐标改变的量，因为最终在边缘处要变为0
        float pos = (number * 1.0f) / range;

        //循环开始的下标，设置从什么时候开始改变
        int start = sourceImg.getWidth() * (sourceImg.getHeight() - (int) range);

        for (int i = start; i < argb.length; i++)
        {
        //同一行alpha数值不改变，因为是随着Y坐标从上到下改变的
            if (i % sourceImg.getWidth() == 0)
            {
                number = number - pos;
                alpha = number * 255 / 100;
            }
            argb[i] = ((int) alpha << 24) | (argb[i] & 0x00FFFFFF);
        }

        sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(), sourceImg

                .getHeight(), Bitmap.Config.ARGB_8888);
        return sourceImg;
    }

    /**
     * 设置图片的透明度从上到下渐变，使下边缘平滑过渡（注意只跟着Y坐标变）
     *
     * @param sourceImg
     * @return
     */
//    public static Bitmap getTransAlphaBitmap(Bitmap sourceImg)
//    {
//
//        int radius = (int) Math.sqrt(sourceImg.getWidth() * sourceImg.getWidth()/4 + sourceImg.getHeight() * sourceImg.getHeight()/4 );
//
//        int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];
//
//        sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0, sourceImg
//
//                .getWidth(), sourceImg.getHeight());// 获得图片的ARGB值
//
//        //number的范围为0-100,0为全透明，100为不透明
//        float number = 100;
//
//        //透明度数值
//        float alpha = number * 255 / 100;
//
//        //图片渐变的范围（只设置图片一半范围由上到下渐变，上面不渐变，即接近边缘的那一半）
//        float range = radius / 2.0f;
//
//        //透明度渐变梯度，每次随着Y坐标改变的量，因为最终在边缘处要变为0
//        float pos = (number * 1.0f) / range;
//
//        //循环开始的下标，设置从什么时候开始改变
//        int start = sourceImg.getWidth() * (sourceImg.getHeight() - (int) sourceImg.getHeight()/2);
//
//        for (int i = start; i < argb.length; i++)
//        {
//            //同一行alpha数值不改变，因为是随着Y坐标从上到下改变的
////            if (i % sourceImg.getWidth() == 0)
////            {
////                number = number - pos;
////                alpha = number * 255 / 100;
////            }
//            number = number - pos;
//            alpha = number * 255 / 100;
//            argb[i] = ((int) alpha << 24) | (argb[i] & 0x00FFFFFF);
//        }
//
//        sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(), sourceImg
//
//                .getHeight(), Bitmap.Config.ARGB_8888);
//        return sourceImg;
//    }



}
