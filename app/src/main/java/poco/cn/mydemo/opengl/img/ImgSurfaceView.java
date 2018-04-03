package poco.cn.mydemo.opengl.img;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import poco.cn.mydemo.opengl.img.filter.AFilter;


/**
 * Created by lgd on 2018/3/26.
 */

public class ImgSurfaceView extends GLSurfaceView
{
    private ImgRender render;

    public ImgSurfaceView(Context context)
    {
       this(context,null);
    }



    public ImgSurfaceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
//        render = new ImgRender();
//        setRenderer(render);
//        setRenderMode(RENDERMODE_WHEN_DIRTY);
//        setEGLContextClientVersion(2);
//        setFilter(new ContrastColorFilter(getContext(), ContrastColorFilter.Filter.NONE));
//        requestRender();
        setEGLContextClientVersion(2);
        render=new ImgRender(this);
        setRenderer(render);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public ImgRender getRender(){
        return render;
    }

    public void setFilter(AFilter filter){
        render.setFilter(filter);
    }

}
