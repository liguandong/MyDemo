package poco.cn.mydemo.denoising;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import poco.cn.mydemo.R;
import poco.cn.mydemo.base.Drawable2d;
import poco.cn.mydemo.base.GlUtil;
import poco.cn.mydemo.opengl.utils.MatrixUtils;
import poco.cn.mydemo.opengl.utils.ShaderUtils;


/**
 *  * 1 编写着色器
 * 2 准备数据
 * 3 创建程序 和绑定着色器
 * 4 获取着色器变量句柄
 * 5 对变量赋值   投影，相机，转换
 * Created by lgd on 2018/4/2.
 */
public class DenoisingRender implements GLSurfaceView.Renderer
{
    private float alpha = 1.0f;
    private float radius = 1f;
    private float centerX = 0f;
    private float centerY = 0f;
    private float XY = 1f;
    private float offsetX;
    private float offsetY;
    private Context context;
    private String vertex;
    private String fragment;
    private Drawable2d mDrawable2d = new Drawable2d();
    private int mProgram = 0;
    private int aPositionLoc;
    private int aTextureCoordLoc;
    private int uMVPMatrixLoc;
    private int uTexMatrixLoc;
    private int uAlphaLoc;
    private int uRadiusLoc;
    private int uXYLoc;
    private Bitmap mNormalBitmip;

    private float[] mMVPMatrix=new float[16];
    private int uNormalTextureLoc;
    private int mNormalTextureId = GlUtil.NO_TEXTURE;
    private int uOffsetX;
    private int uOffsetY;

    public DenoisingRender(Context context)
    {
        this.context = context;
        this.vertex = GlUtil.loadFromAssetsFile("blur/vertex_denoising1.glsl",context.getResources());
        this.fragment = GlUtil.loadFromAssetsFile("blur/fragment_denoising1.glsl",context.getResources());


        mNormalBitmip = BitmapFactory.decodeResource(context.getResources(), R.drawable.homepage_img1);
//        mNormalBitmip = Bitmap.createScaledBitmap(mNormalBitmip,mNormalBitmip.getWidth()/2,mNormalBitmip.getHeight()/2,false);
        XY = mNormalBitmip.getWidth() * 1.0f / mNormalBitmip.getHeight();

//        offsetX = 1.0f/mNormalBitmip.getWidth();
//        offsetY = 1.0f/mNormalBitmip.getHeight();
    }

    protected void onUseProgram(){
        GLES20.glUseProgram(mProgram);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        GLES20.glClearColor(1.0f,1.0f,1.0f,1.0f);
//        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
//        mProgram = ShaderUtils.createProgram(context.getResources(), vertex, fragment);
        mProgram = GlUtil.createProgram(vertex,fragment);
        aPositionLoc = GLES20.glGetAttribLocation(mProgram, "aPosition");
        aTextureCoordLoc = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        uMVPMatrixLoc = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        uTexMatrixLoc = GLES20.glGetUniformLocation(mProgram, "uTexMatrix");
        uNormalTextureLoc = GLES20.glGetUniformLocation(mProgram, "uNormalTexture");
        uAlphaLoc = GLES20.glGetUniformLocation(mProgram, "uAlpha");
        uRadiusLoc = GLES20.glGetUniformLocation(mProgram, "uRadius");
        uXYLoc = GLES20.glGetUniformLocation(mProgram, "uXY");
        uOffsetX = GLES20.glGetUniformLocation(mProgram, "uOffsetX");
        uOffsetY = GLES20.glGetUniformLocation(mProgram, "uOffsetY");
        mNormalTextureId = ShaderUtils.createTexture(mNormalBitmip);


    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        GLES20.glViewport(0,0,width,height);
        offsetX = 1.0f/width;
        offsetY = 1.0f/height;
//        offsetX = 2.0f/ width ;
//        offsetY = 2.0f/ height  ;
        MatrixUtils.getCenterInsideMatrix(mMVPMatrix,mNormalBitmip.getWidth(),mNormalBitmip.getHeight(),width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
        GLES20.glClearColor(1.0f,1.0f,1.0f,1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT| GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mNormalTextureId);

        GLES20.glUseProgram(mProgram);
        GLES20.glUniform1f(uAlphaLoc, alpha);
        GLES20.glUniform1f(uRadiusLoc, radius);
        GLES20.glUniform1f(uXYLoc, XY);
        GLES20.glUniform1f(uOffsetX, offsetX);
        GLES20.glUniform1f(uOffsetY, offsetY);

        GLES20.glUniform1i(uNormalTextureLoc, 0);
        GLES20.glUniformMatrix4fv(uMVPMatrixLoc, 1, false, mMVPMatrix, 0);
        GLES20.glEnableVertexAttribArray(aPositionLoc);
        GLES20.glVertexAttribPointer(aPositionLoc, mDrawable2d.getCoordsPerVertex(),
                GLES20.GL_FLOAT, false, mDrawable2d.getVertexStride(), mDrawable2d.getVertexArray());

        GLES20.glEnableVertexAttribArray(aTextureCoordLoc);
        GLES20.glVertexAttribPointer(aTextureCoordLoc, 2,
                GLES20.GL_FLOAT, false, mDrawable2d.getTexCoordStride(), mDrawable2d.getTexCoordArray());

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mDrawable2d.getVertexCount());

        GLES20.glDisableVertexAttribArray(aPositionLoc);
        GLES20.glDisableVertexAttribArray(aTextureCoordLoc);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
    }


    public void setAlpha(float alpha)
    {
        this.alpha = alpha;
    }
}
