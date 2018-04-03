package poco.cn.mydemo.opengl.img.filter;

import android.content.Context;

/**
 * Created by lgd on 2018/3/30.
 */
public class NoneFilter extends AFilter
{


    public NoneFilter(Context context, String vertex, String fragment)
    {
        super(context, vertex, fragment);
    }

    @Override
    public void onDrawSet()
    {

    }

    @Override
    public void onDrawCreatedSet(int mProgram)
    {

    }
}
