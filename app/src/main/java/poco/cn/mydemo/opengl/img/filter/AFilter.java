package poco.cn.mydemo.opengl.img.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import poco.cn.mydemo.opengl.utils.ShaderUtils;

/**
 * Created by lgd on 2018/3/26.
 *
 * 1 编写着色器
 * 2 准备数据
 * 3 创建程序 和绑定着色器
 * 4 获取着色器变量句柄
 * 5 对变量赋值   投影，相机，转换
 */

public abstract class AFilter implements GLSurfaceView.Renderer
{
    private Bitmap mBitmap;

    private Context context;
    protected String vertex;
    protected String fragment;

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
    private FloatBuffer bPos;
    private FloatBuffer bCoord;
    private int mProgram;
    private int glHPosition;
    private int glHCoordinate;
    private int glHTexture;
    private int glHMatrix;
    private int hIsHalf;
    private int glHUxy;
    private float uXY;

    private float[] mViewMatrix=new float[16];
    private float[] mProjectMatrix=new float[16];
    private float[] mMVPMatrix=new float[16];

    public AFilter(Context context, String vertex, String fragment)
    {
        this.context = context;
        this.vertex = vertex;
        this.fragment = fragment;

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
//        GLES20.glClearColor(1.0f,1.0f,1.0f,1.0f);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);


    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        GLES20.glViewport(0,0,width,height);
        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        float sWH=w/(float)h;
        float sWidthHeight=width/(float)height;
        uXY = sWidthHeight;

        if(width>height){
            if(sWH>sWidthHeight){
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight*sWH,sWidthHeight*sWH, -1,1, 3, 5);
            }else{
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight/sWH,sWidthHeight/sWH, -1,1, 3, 5);
            }
        }else{
            if(sWH>sWidthHeight){
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1/sWidthHeight*sWH, 1/sWidthHeight*sWH,3, 5);
            }else{
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -sWH/sWidthHeight, sWH/sWidthHeight,3, 5);
            }
        }
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 5.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);

//        MatrixUtils.getCenterInsideMatrix(mMVPMatrix,w,h,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT| GLES20.GL_DEPTH_BUFFER_BIT);

        mProgram  = ShaderUtils.createProgram(context.getResources(),vertex,fragment);

        glHPosition =GLES20.glGetAttribLocation(mProgram,"vPosition");
        glHCoordinate =GLES20.glGetAttribLocation(mProgram,"vCoordinate");
        glHTexture =GLES20.glGetUniformLocation(mProgram,"vTexture");
        glHMatrix =GLES20.glGetUniformLocation(mProgram,"vMatrix");
        hIsHalf =GLES20.glGetUniformLocation(mProgram,"vIsHalf");
        glHUxy =GLES20.glGetUniformLocation(mProgram,"uXY");
        onDrawCreatedSet(mProgram);


        GLES20.glUseProgram(mProgram);
        onDrawSet();
        GLES20.glUniformMatrix4fv(glHMatrix,1,false,mMVPMatrix,0);
        GLES20.glEnableVertexAttribArray(glHPosition);
        GLES20.glEnableVertexAttribArray(glHCoordinate);
        GLES20.glUniform1i(glHTexture,0);
        ShaderUtils.createTexture(mBitmap);
        GLES20.glVertexAttribPointer(glHPosition,2,GLES20.GL_FLOAT,false,0,bPos);
        GLES20.glVertexAttribPointer(glHCoordinate,2,GLES20.GL_FLOAT,false,0,bCoord);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);
//        GLES20.glu(mProgram);
    }

    public void setBitmap(Bitmap mBitmap)
    {
        this.mBitmap = mBitmap;
    }


    public abstract void onDrawSet();
    public abstract void onDrawCreatedSet(int mProgram);

}
