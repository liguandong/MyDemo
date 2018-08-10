package com.adnonstop.community;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.adnonstop.decode.DecodeSurface;

/**
 * Created by: fwc
 * Date: 2018/3/21
 */
class VideoRenderer implements GLSurface.Renderer {

	private Context mContext;

	private DecodeSurface mSurface;

	private FrameFilter mFrameFilter;

	private float[] mModelMatrix = new float[16];
	private float[] mSTMatrix = new float[16];

	public VideoRenderer(Context context) {
		mContext = context;

		Matrix.setIdentityM(mModelMatrix, 0);
	}

	@Override
	public void onSurfaceCreated() {
		GLES20.glClearColor(0, 0, 0, 1);

		mSurface = new DecodeSurface();
	}

	@Override
	public void onSurfaceChanged(int width, int height) {
		GLES20.glViewport(0, 0, width, height);
	}

	@Override
	public DecodeSurface getSurface() {
		return mSurface;
	}

	public void setFrameMatrix(float offsetX, float offsetY, float scaleX, float scaleY, float rotation) {
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, offsetX, offsetY, 0);
		Matrix.scaleM(mModelMatrix, 0, scaleX, scaleY, 1);
		Matrix.rotateM(mModelMatrix, 0, rotation, 0, 0, 1);
	}

	@Override
	public void onDrawFrame() {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

		mSurface.getTransformMatrix(mSTMatrix);

		if (mFrameFilter == null) {
			mFrameFilter = new FrameFilter(mContext, mSurface.isHardDecoder());
		}
		mFrameFilter.drawFrame(mSurface.getTextureId(), mModelMatrix, mSTMatrix);
	}

	@Override
	public void onSurfaceDestroyed() {
		if (mFrameFilter != null) {
			mFrameFilter.release();
			mFrameFilter = null;
		}

		if (mSurface != null) {
			mSurface.release();
			mSurface = null;
		}
	}
}
