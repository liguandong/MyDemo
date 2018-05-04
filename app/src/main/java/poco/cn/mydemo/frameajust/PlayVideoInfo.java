package poco.cn.mydemo.frameajust;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.RectF;
import android.opengl.Matrix;

import java.io.Serializable;

/**
 * Created by: fwc
 * Date: 2017/9/27
 */
public class PlayVideoInfo
{

	private static final int MAX_SCALE = 2;

	public String path;
	public int width;
	public int height;

	public float[] mvpMatrix = new float[16];
	public float[] texMatrix = new float[16];

	public float[] saveMatrix = new float[16];

	private float curScale = 1f;
	private int rotateAngle = 0;

	private float initScaleX;
	private float initScaleY;

	private float originScaleX;
	private float originScaleY;

	private float minScale;
	private float originMinScale;

	private RectF mRectF = new RectF();

	private boolean doAnimation = false;

	private FrameAdjustInfo mAdjustInfo;
	private float videoRatio = 1.0f;


	public void initMvpMatrix(float scaleX, float scaleY, float showRatio) {

		scaleX = checkScale(scaleX);
		scaleY = checkScale(scaleY);

		originScaleX = initScaleX = scaleX;
		originScaleY = initScaleY = scaleY;

		if (showRatio > 1) {
			minScale = Math.min(1 / initScaleX, 1 / initScaleY / showRatio);
		} else {
			minScale = Math.min(showRatio / initScaleX, 1 / initScaleY);
		}
		originMinScale = minScale = checkScale(minScale);

		Matrix.setIdentityM(mvpMatrix, 0);
		Matrix.scaleM(mvpMatrix, 0, scaleX, scaleY, 1);

		Matrix.setIdentityM(texMatrix, 0);

		mAdjustInfo = new FrameAdjustInfo();
	}

	private float checkScale(float scale) {
		return (int)(scale * 100) / 100f;
	}

	public void enterFrameAdjust() {
		mAdjustInfo.save(this);
	}

	public void resetFrameAdjust() {
		mAdjustInfo.reset(this);
		initScaleX = originScaleX;
		initScaleY = originScaleY;
		minScale = originMinScale;
	}

	public FrameAdjustInfo onSave() {
		mAdjustInfo.save(this);
		return mAdjustInfo;
	}

	public void onRestore(FrameAdjustInfo info) {
		info.reset(this);
	}

	public void translate(float dx, float dy, float left, float top) {

		if (doAnimation) {
			return;
		}

		dy = -dy;

		float bottom = -top;
		float right = -left;

		mapRect();

		if (mRectF.right - mRectF.left >= 2 * right) {
			if (mRectF.left + dx > left) {
				dx = left - mRectF.left;
			}

			if (mRectF.right + dx < right) {
				dx = right - mRectF.right;
			}
		} else {
			dx = 0;
		}


		if (mRectF.top - mRectF.bottom >= 2 * top) {
			if (mRectF.top + dy < top) {
				dy = top - mRectF.top;
			}

			if (mRectF.bottom + dy > bottom) {
				dy = bottom - mRectF.bottom;
			}
		} else {
			dy = 0;
		}


		if (dx != 0 || dy != 0) {
			MatrixUtils.translateM(mvpMatrix, 0, dx, dy, 0);
		}
	}

	public void scale(int width, int height, float px, float py, float scale, float left, float top) {

		if (doAnimation) {
			return;
		}

		float right = -left;
		float bottom = -top;

		if (curScale * scale > MAX_SCALE) {
			scale = MAX_SCALE / curScale;
		} else if (curScale * scale < minScale) {
			scale = minScale / curScale;
		}

		scaleMvp(width / 2f, height / 2f, px, py, scale);

		curScale *= scale;

//		if (scale < 1) {
//			checkBounds(left, top, right, bottom);
//		}

		checkFitCenter(left, top, right, bottom);
	}

	public void scaleEnd(float left, float top, Runnable refresh) {

		float right = -left;
		float bottom = -top;

		float dx = 0, dy = 0;
		mapRect();
		final float showWidth = right * 2;
		final float showHeight = top * 2;

		float width = mRectF.right - mRectF.left;
		float dx1 = mRectF.left - left;
		float dx2 = mRectF.right - right;
		if (width <= showWidth) {
			dx = -(dx1 + dx2) / 2;
		} else {
			if (dx1 > 0 && dx2 > 0) {
				dx = -Math.min(dx1, dx2);
			} else if (dx1 < 0 && dx2 < 0) {
				dx = -Math.max(dx1, dx2);
			}
		}

		float height = mRectF.top - mRectF.bottom;
		float dy1 = mRectF.top - top;
		float dy2 = mRectF.bottom - bottom;
		if (height <= showHeight) {
			dy = -(dy1 + dy2) / 2;
		} else {
			if (dy1 > 0 && dy2 > 0) {
				dy = -Math.min(dy1, dy2);
			} else if (dy1 < 0 && dy2 < 0) {
				dy = -Math.max(dy1, dy2);
			}
		}

		if (dx != 0 || dy != 0) {
			translateAnim(dx, dy, refresh);
		}
	}

	private void translateAnim(final float dx, final float dy, final Runnable refresh) {
		doAnimation = true;
		final float startX = mvpMatrix[12];
		final float startY = mvpMatrix[13];
		ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float value = (float)animation.getAnimatedValue();
				mvpMatrix[12] = startX + dx * value;
				mvpMatrix[13] = startY + dy * value;
				refresh.run();
			}
		});
		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				doAnimation = false;
			}
		});
		animator.setDuration(300);
		animator.start();
	}

	public void doubleScale(int width, int height, float px, float py, float left, float top, Runnable refresh) {
		if (doAnimation) {
			return;
		}

		float fromScale;
		if (curScale < 1) {
			fromScale = curScale;
			curScale = 1;
		} else if (curScale == MAX_SCALE) {
			fromScale = MAX_SCALE;
			curScale = 1;
		} else {
			fromScale = curScale;
			curScale = MAX_SCALE;
		}

		scaleAnim(width / 2f, height / 2f, px, py, fromScale, curScale, left, top, refresh);
	}

	private void scaleAnim(final float centerX, final float centerY, final float px, final float py, float fromScale, float toScale, final float left, final float top, final Runnable refresh) {
		doAnimation = true;
//		final float right = -left;
//		final float bottom = -top;
		ValueAnimator animator = ValueAnimator.ofFloat(fromScale, toScale);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float scale = (float)animation.getAnimatedValue();

				Matrix.setIdentityM(mvpMatrix, 0);
				Matrix.scaleM(mvpMatrix, 0, initScaleX, initScaleY, 1);

				scaleMvp(centerX, centerY, px, py, scale);
//				checkBounds(left, top, right, bottom);
				refresh.run();
			}
		});
		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				doAnimation = false;
			}
		});
		animator.setDuration(300);
		animator.start();
	}

	private void scaleMvp(float centerX, float centerY, float px, float py, float scale) {

		MatrixUtils.scaleM(mvpMatrix, 0, scale, scale, 1);

	}

	/**
	 * 边界检测
	 */
	private void checkBounds(float left, float top, float right, float bottom) {
		float dx = 0;
		float dy = 0;

		mapRect();
		if (mRectF.left > left) {
			dx = left - mRectF.left;
		}

		if (mRectF.right < right) {
			dx = right - mRectF.right;
		}

		if (mRectF.top < top) {
			dy = top - mRectF.top;
		}

		if (mRectF.bottom > bottom) {
			dy = bottom - mRectF.bottom;
		}

		if (dx != 0 || dy != 0) {
			MatrixUtils.translateM(mvpMatrix, 0, dx, dy, 0);
		}
	}

	private void checkFitCenter(float left, float top, float right, float bottom) {
		float dx = 0;
		float dy = 0;

		mapRect();

		if (mRectF.left > left && mRectF.right < right) {
			dx = (mRectF.left + mRectF.right);
		}

		if (mRectF.top < top && mRectF.bottom > bottom) {
			dy = -(mRectF.top + mRectF.bottom);
		}

		if (dx != 0 || dy != 0) {
			MatrixUtils.translateM(mvpMatrix, 0, dx, dy, 0);
		}
	}

	private void mapRect() {
		mRectF.left = -mvpMatrix[0] + mvpMatrix[4] + mvpMatrix[12];
		mRectF.top = -mvpMatrix[1] + mvpMatrix[5] + mvpMatrix[13];
		mRectF.right = mvpMatrix[0] - mvpMatrix[4] + mvpMatrix[12];
		mRectF.bottom = mvpMatrix[1] - mvpMatrix[5] + mvpMatrix[13];
	}

	public void rotate(boolean right, float showRatio, Runnable refresh)
	{

		if (doAnimation)
		{
			return;
		}

		float fromDegree = (rotateAngle + 360) % 360;

		rotateAngle += right ? -90 : 90;
		rotateAngle = (rotateAngle + 360) % 360;
		float videoRatio = getVideoRatio();
		if (rotateAngle % 180 != 0)
		{
			videoRatio = 1 / videoRatio;
		}
		float fromScaleX = mvpMatrix[0];
		float fromScaleY = mvpMatrix[5];

		if (videoRatio >= showRatio)
		{
			if (showRatio > 1)
			{
				initScaleX = videoRatio / showRatio;
				initScaleY = 1 / showRatio;
			} else
			{
				initScaleX = videoRatio;
				initScaleY = 1;
			}
		} else
		{
			if (showRatio > 1)
			{
				initScaleX = 1;
				initScaleY = 1 / videoRatio;
			} else
			{
				initScaleX = showRatio;
				initScaleY = showRatio / videoRatio;
			}
		}

		curScale = 1f;
		if (showRatio > 1)
		{
			minScale = Math.min(1 / initScaleX, 1 / initScaleY / showRatio);
		} else
		{
			minScale = Math.min(showRatio / initScaleX, 1 / initScaleY);
		}
		minScale = checkScale(minScale);

		float toDegree = (rotateAngle + 360) % 360;
		if (fromDegree == 0 && toDegree == 270)
		{
			fromDegree = 360;
		} else if (fromDegree == 270 && toDegree == 0)
		{
			fromDegree = -90;
		}
		rotateAnim(fromScaleX, initScaleX, fromScaleY, initScaleY, fromDegree, toDegree, refresh);
	}

	private void rotateAnim(final float fromScaleX, final float toScaleX, final float fromScaleY, final float toScaleY, final float fromDegree, final float toDegree, final Runnable refresh) {
		doAnimation = true;
		ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float ratio = (float)animation.getAnimatedValue();

				Matrix.setIdentityM(mvpMatrix, 0);
				float scaleX = (toScaleX - fromScaleX) * ratio + fromScaleX;
				float scaleY = (toScaleY - fromScaleY) * ratio + fromScaleY;
				Matrix.setIdentityM(mvpMatrix, 0);
				Matrix.scaleM(mvpMatrix, 0, scaleX, scaleY, 1);

				float degree = (toDegree - fromDegree) * ratio + fromDegree;
				Matrix.setIdentityM(texMatrix, 0);
				Matrix.translateM(texMatrix, 0, 0.5f, 0.5f, 0);
				Matrix.rotateM(texMatrix, 0, degree, 0, 0, 1);
				Matrix.translateM(texMatrix, 0, -0.5f, -0.5f, 0);

				refresh.run();
			}
		});
		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				doAnimation = false;
			}
		});
		animator.setDuration(200);
		animator.start();
	}

	public void calculateSaveMatrix(float left, float top) {

		float scaleX = 1;
		float scaleY = 1;
		if (left != 0) {
			scaleX = 1 / Math.abs(left);
		}
		if (top != 0) {
			scaleY = 1 / Math.abs(top);
		}

		float[] temp = new float[16];
		Matrix.setIdentityM(saveMatrix, 0);
		Matrix.setIdentityM(temp, 0);
		Matrix.scaleM(temp, 0, scaleX, scaleY, 1);
		Matrix.multiplyMM(saveMatrix, 0, temp, 0, mvpMatrix, 0);
	}

	public float getVideoRatio()
	{
		return videoRatio;
	}

	public static class FrameAdjustInfo implements Serializable
    {

		private static final long serialVersionUID = 1L;
		public float[] mvpMatrix = new float[16];
		public float[] texMatrix = new float[16];

		private float curScale = 1f;
		private int rotateAngle = 0;

		void save(PlayVideoInfo info) {
			this.curScale = info.curScale;
			this.rotateAngle = info.rotateAngle;

			System.arraycopy(info.mvpMatrix, 0, this.mvpMatrix, 0, 16);
			System.arraycopy(info.texMatrix, 0, this.texMatrix, 0, 16);
		}

		void reset(PlayVideoInfo info) {
			info.curScale = this.curScale;
			info.rotateAngle = this.rotateAngle;

			System.arraycopy(this.mvpMatrix, 0, info.mvpMatrix, 0, 16);
			System.arraycopy(this.texMatrix, 0, info.texMatrix, 0, 16);
		}
	}
}
