package com.adnonstop.decode;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.support.annotation.NonNull;
import android.view.Surface;

import com.adnonstop.gles.GlUtil;

import java.nio.ByteBuffer;

/**
 * Created by: fwc
 * Date: 2018/8/9
 */
public class DecodeSurface {

	private int mTextureId = GlUtil.NO_TEXTURE;
	private SurfaceTexture mSurfaceTexture;
	private Surface mSurface;

	private OnFrameAvailableListener mListener;

	private boolean isHardDecoder;

	private int mWidth;
	private int mHeight;
	private int mRotation;
	private ByteBuffer mByteBuffer;

	public void initSurface(boolean isHardDecoder) {
		this.isHardDecoder = isHardDecoder;
		if (isHardDecoder) {
			mTextureId = GlUtil.createTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
			mSurfaceTexture = new SurfaceTexture(mTextureId);
			mSurfaceTexture.setOnFrameAvailableListener(mOnFrameAvailableListener);
			mSurface = new Surface(mSurfaceTexture);
		} else {
			mTextureId = GlUtil.createTexture(GLES20.GL_TEXTURE_2D);
		}
	}

	public boolean isHardDecoder() {
		return isHardDecoder;
	}

	public void updateTexImage() {
		if (isHardDecoder) {
			mSurfaceTexture.updateTexImage();
		} else {
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mWidth, mHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mByteBuffer);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		}
	}

	@NonNull
	public Surface getSurface() {
		return mSurface;
	}

	public void getTransformMatrix(float[] mtx) {
		if (isHardDecoder) {
			mSurfaceTexture.getTransformMatrix(mtx);
		} else {
			Matrix.setIdentityM(mtx, 0);

			// 翻转纹理
			mtx[5] = -1;
			mtx[13] = 1;

			rotateM(mtx, 0, mRotation);
		}
	}

	public int getTextureId() {
		return mTextureId;
	}

	public void release() {

		if (mSurface != null) {
			mSurface.release();
			mSurface = null;
		}
		if (mSurfaceTexture != null) {
			mSurfaceTexture.release();
			mSurfaceTexture = null;
		}

		if (mTextureId != GlUtil.NO_TEXTURE) {
			GLES20.glDeleteTextures(1, new int[] {mTextureId}, 0);
			mTextureId = GlUtil.NO_TEXTURE;
		}

		mListener = null;
		mByteBuffer = null;
	}

	public void setByteBuffer(ByteBuffer byteBuffer, int width, int height, int rotation) {
		mByteBuffer = byteBuffer;
		mWidth = width;
		mHeight = height;
		mRotation = rotation;
		notifyFrameAvailable();
	}

	private void notifyFrameAvailable() {
		if (mListener != null) {
			mListener.onFrameAvailable(this);
		}
	}

	public void setOnFrameAvailableListener(OnFrameAvailableListener listener) {
		mListener = listener;
	}

	private static float[] sTempMatrix = new float[32];

	/**
	 * 针对纹理矩阵做的旋转
	 * @param rm 矩阵
	 * @param rmOffset 矩阵偏移
	 * @param degree 旋转角度
	 */
	private static void rotateM(float[] rm, int rmOffset, int degree) {
		degree = (degree + 360) % 360;

		if (degree == 0) {
			return;
		}

		Matrix.setIdentityM(sTempMatrix, 0);
		switch (degree) {
			case 90:
				sTempMatrix[0] = 0;
				sTempMatrix[1] = 1;
				sTempMatrix[4] = -1;
				sTempMatrix[5] = 0;
				sTempMatrix[12] = 1;
				sTempMatrix[13] = 0;
				break;
			case 180:
				sTempMatrix[0] = -1;
				sTempMatrix[1] = 0;
				sTempMatrix[4] = 0;
				sTempMatrix[5] = -1;
				sTempMatrix[12] = 1;
				sTempMatrix[13] = 1;
				break;
			case 270:
				sTempMatrix[0] = 0;
				sTempMatrix[1] = -1;
				sTempMatrix[4] = 1;
				sTempMatrix[5] = 0;
				sTempMatrix[12] = 0;
				sTempMatrix[13] = 1;
				break;
		}

		Matrix.multiplyMM(sTempMatrix, 16, rm, rmOffset, sTempMatrix, 0);
		System.arraycopy(sTempMatrix, 16, rm, rmOffset, 16);
	}

	private SurfaceTexture.OnFrameAvailableListener mOnFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {
		@Override
		public void onFrameAvailable(SurfaceTexture surfaceTexture) {
			notifyFrameAvailable();
		}
	};

	public interface OnFrameAvailableListener {
		void onFrameAvailable(DecodeSurface surface);
	}
}
