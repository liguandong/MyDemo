package poco.cn.mydemo.frameajust;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import poco.cn.mydemo.base.GLTextureView;

/**
 * Created by lgd on 2018/5/2.
 */
public class FrameRender implements GLTextureView.Renderer
{
    


    public FrameRender()
    {
        super();
    }

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
}
