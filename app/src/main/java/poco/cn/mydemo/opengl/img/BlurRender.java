package poco.cn.mydemo.opengl.img;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import poco.cn.mydemo.R;
import poco.cn.mydemo.imgprocess.BlurBitmapUtil;
import poco.cn.mydemo.opengl.utils.MatrixUtils;
import poco.cn.mydemo.opengl.utils.ShaderUtils;

/**
 * Created by lgd on 2018/3/29.
 */
public class BlurRender implements GLSurfaceView.Renderer
{

    private Context mContext;
    private int mProgram;
    private int glHPosition;
    private int glHTexture;
    private int glHCoordinate;
    private int glHMatrix;
    private Bitmap mBitmap;
    private Bitmap mBlurBitmap;

    private FloatBuffer bPos;
    private FloatBuffer bCoord;


    private String vertex;
    private String fragment;
    private float[] mViewMatrix=new float[16];
    private float[] mProjectMatrix=new float[16];
    private float[] mMVPMatrix=new float[16];

    private final float[] sPos={
            -1.0f,1.0f,
            -1.0f,-1.0f,
            1.0f,1.0f,
            1.0f,-1.0f
    };

    private final float[] sCoord={
            0.0f,0.0f,
            0.0f,1.0f,
            1.0f,0.0f,
            1.0f,1.0f,
    };

    public BlurRender(Context context)
    {
        super();
        mBitmap = (BitmapFactory.decodeResource(context.getResources(), R.drawable.homepage_img1));
        mBlurBitmap = BlurBitmapUtil.blurBitmap(context,mBitmap,15);
        bPos = createFloatBuffer(sPos);
        bCoord = createFloatBuffer(sCoord);
    }

    public static FloatBuffer createFloatBuffer(float[] coords) {
        // Allocate a direct ByteBuffer, using 4 bytes per float, and copy coords into it.
        FloatBuffer fb = ByteBuffer.allocateDirect(coords.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(coords);
        fb.position(0);
        return fb;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);


        mProgram= ShaderUtils.createProgram(mContext.getResources(),vertex,fragment);
        glHPosition= GLES20.glGetAttribLocation(mProgram,"vPosition");
        glHCoordinate= GLES20.glGetAttribLocation(mProgram,"vCoordinate");
        glHTexture= GLES20.glGetUniformLocation(mProgram,"vTexture");
        glHMatrix= GLES20.glGetUniformLocation(mProgram,"vMatrix");

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        GLES20.glViewport(0,0,width,height);
        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        MatrixUtils.getCenterInsideMatrix(mMVPMatrix,w,h,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
        GLES20.glUseProgram(mProgram);
        GLES20.glUniformMatrix4fv(glHMatrix,1,false,mMVPMatrix,0);
        GLES20.glEnableVertexAttribArray(glHPosition);
        GLES20.glEnableVertexAttribArray(glHCoordinate);
        GLES20.glUniform1i(glHTexture,0);
        ShaderUtils.createTexture(mBitmap);
        GLES20.glVertexAttribPointer(glHPosition,2,GLES20.GL_FLOAT,false,0,bPos);
        GLES20.glVertexAttribPointer(glHCoordinate,2,GLES20.GL_FLOAT,false,0,bCoord);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);
        GLES20.glDeleteProgram(mProgram);
    }
}
