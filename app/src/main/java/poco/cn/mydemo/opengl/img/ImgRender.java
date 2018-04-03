package poco.cn.mydemo.opengl.img;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.View;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import poco.cn.mydemo.R;
import poco.cn.mydemo.imgprocess.BlurBitmapUtil;
import poco.cn.mydemo.opengl.img.filter.AFilter;
import poco.cn.mydemo.opengl.img.filter.ContrastColorFilter;
import poco.cn.mydemo.opengl.img.filter.NoneFilter;


/**
 * Created by lgd on 2018/3/26.
 */

public class ImgRender implements GLSurfaceView.Renderer
{
    private Bitmap bitmap;
    private Bitmap bitmap1;
    private AFilter mFilter;
    private AFilter mFilter1;
    private int width;
    private int height;
    private EGLConfig config;
    private boolean refreshFlag;

    public ImgRender(View mView)
    {
        super();
        bitmap = (BitmapFactory.decodeResource(mView.getContext().getResources(), R.drawable.homepage_img1));
        bitmap1 = BlurBitmapUtil.blurBitmap(mView.getContext(),bitmap,15);
        mFilter=new NoneFilter(mView.getContext(), "filter/default_vertex.sh", "filter/default_fragment.sh");
        mFilter.setBitmap(bitmap);
        mFilter1=new ContrastColorFilter(mView.getContext(), ContrastColorFilter.Filter.BLUR);
        mFilter1.setBitmap(bitmap1);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        GLES20.glClearColor(1.0f,1.0f,1.0f,1.0f);
        this.config = config;
        mFilter.onSurfaceCreated(gl, config);
        mFilter1.onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        this.width = width;
        this.height = height;
//        gl.glViewport(0,0,width,height);
        mFilter.onSurfaceChanged(gl, width, height);
        mFilter1.onSurfaceChanged(gl, width, height);
    }


    @Override
    public void onDrawFrame(GL10 gl)
    {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT| GLES20.GL_DEPTH_BUFFER_BIT);
        if(refreshFlag&&width!=0&&height!=0){
            mFilter.onSurfaceCreated(gl, config);
            mFilter.onSurfaceChanged(gl,width,height);
            refreshFlag=false;
        }
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendEquation(GLES20.GL_FUNC_ADD);
        GLES20.glBlendFuncSeparate(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA, GLES20.GL_ONE, GLES20.GL_SRC_COLOR);
        mFilter.onDrawFrame(gl);
        mFilter1.onDrawFrame(gl);
        GLES20.glDisable(GLES20.GL_BLEND);
    }
    public void setmFilter(AFilter mFilter){
        refreshFlag=true;
        this.mFilter = mFilter;
        if(bitmap!=null){
            mFilter.setBitmap(bitmap);
        }
    }

    public void refresh(){
        refreshFlag =true;
    }

    public void setBitmap(Bitmap bitmap)
    {
        this.bitmap = bitmap;
        mFilter.setBitmap(bitmap);
    }

    public void setFilter(AFilter filter)
    {
        this.mFilter = filter;
        if(bitmap != null){
            filter.setBitmap(bitmap);
        }
    }
}
