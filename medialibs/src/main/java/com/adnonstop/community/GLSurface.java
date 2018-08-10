package com.adnonstop.community;

import android.view.Surface;

import com.adnonstop.decode.DecodeSurface;
import com.adnonstop.gles.EglCore;
import com.adnonstop.gles.WindowSurface;

/**
 * Created by: fwc
 * Date: 2018/3/21
 */
public class GLSurface implements DecodeSurface.OnFrameAvailableListener {

	private static final int TIMEOUT_MS = 2500;

	private final int mWidth;
	private final int mHeight;

	private final Surface mEncodeSurface;

	private EglCore mEglCore;
	private WindowSurface mWindowSurface;

	private Renderer mRenderer;

	private final Object mFrameSyncObject = new Object();
	private boolean mFrameAvailable;

	private DecodeSurface mSurface;

	public GLSurface(int width, int height, Surface encodeSurface) {
		mWidth = width;
		mHeight = height;
		mEncodeSurface = encodeSurface;
	}

	public void setRenderer(Renderer renderer) {
		if (renderer == null) {
			throw new IllegalArgumentException("renderer must not be null");
		}
		mRenderer = renderer;

		initEGL();
	}

	private void initEGL() {
		mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE | EglCore.FLAG_TRY_GLES3);
		mWindowSurface = new WindowSurface(mEglCore, mEncodeSurface, false);
		mWindowSurface.makeCurrent();

		mRenderer.onSurfaceCreated();
		mRenderer.onSurfaceChanged(mWidth, mHeight);

		mSurface = mRenderer.getSurface();
		mSurface.setOnFrameAvailableListener(this);
	}

	public DecodeSurface getSurface() {
		return mSurface;
	}

	public void setFrameMatrix(float offsetX, float offsetY, float scaleX, float scaleY, float rotation) {
		mRenderer.setFrameMatrix(offsetX, offsetY, scaleX, scaleY, rotation);
	}

	public void setPresentationTime(long nsecs) {
		mWindowSurface.setPresentationTime(nsecs);
	}

	public void swapBuffers() {
		mWindowSurface.swapBuffers();
	}

	public void release() {

		if (mSurface != null) {
			mSurface.release();
			mSurface = null;
		}

		if (mRenderer != null) {
			mRenderer.onSurfaceDestroyed();
		}

		if (mWindowSurface != null) {
			mWindowSurface.release();
			mWindowSurface = null;
		}

		if (mEglCore != null) {
			mEglCore.release();
			mEglCore = null;
		}
	}

	@Override
	public void onFrameAvailable(DecodeSurface surface) {
		synchronized (mFrameSyncObject) {
			if (mFrameAvailable) {
				throw new RuntimeException("mFrameAvailable already set, frame could be dropped");
			}
			mFrameAvailable = true;
			mFrameSyncObject.notifyAll();
		}
	}

	public void awaitNewImage() {
		synchronized (mFrameSyncObject) {
			while (!mFrameAvailable) {
				try {
					// Wait for onFrameAvailable() to signal us.  Use a timeout to avoid
					// stalling the test if it doesn't arrive.
					mFrameSyncObject.wait(TIMEOUT_MS);
					if (!mFrameAvailable) {
						// TODO: if "spurious wakeup", continue while loop
						throw new RuntimeException("frame wait timed out");
					}
				} catch (InterruptedException ie) {
					// shouldn't happen
					throw new RuntimeException(ie);
				}
			}
			mFrameAvailable = false;
		}

		mSurface.updateTexImage();
	}

	public void drawFrame() {
		mRenderer.onDrawFrame();
	}

	public interface Renderer {

		void onSurfaceCreated();

		void onSurfaceChanged(int width, int height);

		DecodeSurface getSurface();

		void setFrameMatrix(float offsetX, float offsetY, float scaleX, float scaleY, float rotation);

		void onDrawFrame();

		void onSurfaceDestroyed();
	}
}
