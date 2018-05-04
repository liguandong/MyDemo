package poco.cn.mydemo.frameajust;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.widget.FrameLayout;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import poco.cn.mydemo.R;
import poco.cn.mydemo.blur.texture.GLTextureView;

/**
 * Created by lgd on 2018/5/2.
 */
public class FixPage extends FrameLayout
{
    private GLTextureView mTextureView;
    PlayVideoInfo info;
    private float mShowRatio;
    private Bitmap bitmap;
    public FixPage(@NonNull Context context)
    {
        super(context);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.homepage_img1);
        mShowRatio = bitmap.getWidth() / bitmap.getHeight();


        mTextureView = new GLTextureView(getContext());
        mTextureView.setRenderer(new GLTextureView.Renderer()
        {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config)
            {

            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height)
            {

            }

            @Override
            public boolean onDrawFrame(GL10 gl)
            {
                return false;
            }

            @Override
            public void onSurfaceDestroyed()
            {

            }
        });
        info = new PlayVideoInfo();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    private void calculateVideoMatrix() {
        float videoRatio;
        videoRatio = info.getVideoRatio();
        if (videoRatio >= mShowRatio) {
            if (mShowRatio > 1) {
                info.initMvpMatrix(videoRatio / mShowRatio, 1 / mShowRatio, mShowRatio);
            } else {
                info.initMvpMatrix(videoRatio, 1, mShowRatio);
            }
        } else {
            if (mShowRatio > 1) {
                info.initMvpMatrix(1, 1 / videoRatio, mShowRatio);
            } else {
                info.initMvpMatrix(mShowRatio, mShowRatio / videoRatio, mShowRatio);
            }
        }
    }



}
