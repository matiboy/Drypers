package com.suterastudio.ui.multitouch;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import com.suterastudio.ui.multitouch.MultiTouchController.PositionAndScale;

public class MaskedTouchImage {
	private static final int UI_MODE_ROTATE = 1, UI_MODE_ANISOTROPIC_SCALE = 2;

	private int mUIMode = UI_MODE_ROTATE;

	private BitmapDrawable mImageDrawable;
	private Bitmap mMask;

	private Paint mPaint = new Paint();

	private boolean firstLoad;

	private int imageWidth, imageHeight, maskWidth, maskHeight,displayWidth, displayHeight;

	private float centerX, centerY, scaleX, scaleY, angle;

	private float minX, maxX, minY, maxY;

	private static final float SCREEN_MARGIN = 0;

	public MaskedTouchImage(Bitmap image, Bitmap mask, Context context){//Resources res) {
		this.firstLoad = true;
		mImageDrawable = new BitmapDrawable(image);
		mMask = mask;
		
//		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//		Point size = new Point();
//		display.getSize(size);
//		displayWidth = size.x / 2;
//		displayHeight = size.y / 2;

		getMetrics(context.getResources());
		process(context.getResources());//res);
	}

	private void getMetrics(Resources res) {
		DisplayMetrics metrics = res.getDisplayMetrics();
		// The DisplayMetrics don't seem to always be updated on screen rotate,
		// so we hard code a portrait
		// screen orientation for the non-rotated screen here...
		// this.displayWidth = metrics.widthPixels;
		// this.displayHeight = metrics.heightPixels;
		this.displayWidth = res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? Math
				.max(metrics.widthPixels, metrics.heightPixels) : Math.min(
				metrics.widthPixels, metrics.heightPixels);
		this.displayHeight = res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? Math
				.min(metrics.widthPixels, metrics.heightPixels) : Math.max(
				metrics.widthPixels, metrics.heightPixels);
	}

	/** Called by activity's onResume() method to load the images */
	public void process(Resources res) {
		getMetrics(res);

		this.imageWidth = mImageDrawable.getIntrinsicWidth();
		this.imageHeight = mImageDrawable.getIntrinsicHeight();

		this.maskWidth = mMask.getWidth();
		this.maskHeight = mMask.getHeight();

		float cx, cy, sx, sy;
		if (firstLoad) {
			// cx = SCREEN_MARGIN + (float) (Math.random() * (displayWidth - 2 *
			// SCREEN_MARGIN));
			// cy = SCREEN_MARGIN + (float) (Math.random() * (displayHeight - 2
			// * SCREEN_MARGIN));
			DisplayMetrics metrics = res.getDisplayMetrics();
			cx = (float) (displayWidth/3.5); //195f; // middle of the babyhead mask...
			cy = (float) (displayHeight/5.5); //215f; // middle of the babyhead mask... 

			// float sc = (float) (Math.max(displayWidth, displayHeight) /
			// (float) Math.max(width, height) * Math.random() * 0.3 + 0.2);
			// sx = sy = sc;
			float sc = (float) 3;//(maskHeight / imageHeight * 0.3 + 0.2);
			sx = sy = sc;

			firstLoad = false;
		} else {
			// Reuse position and scale information if it is available
			// FIXME this doesn't actually work because the whole activity is
			// torn down and re-created on rotate
			cx = this.centerX;
			cy = this.centerY;
			sx = this.scaleX;
			sy = this.scaleY;
			// Make sure the image is not off the screen after a screen rotation
			if (this.maxX < SCREEN_MARGIN)
				cx = SCREEN_MARGIN;
			else if (this.minX > displayWidth - SCREEN_MARGIN)
				cx = displayWidth - SCREEN_MARGIN;
			if (this.maxY > SCREEN_MARGIN)
				cy = SCREEN_MARGIN;
			else if (this.minY > displayHeight - SCREEN_MARGIN)
				cy = displayHeight - SCREEN_MARGIN;
		}
			setPos(cx, cy, sx, sy, 0.0f);
	}

	/** Set the position and scale of an image in screen coordinates */
	public boolean setPos(PositionAndScale newImgPosAndScale) {

		return setPos(
				newImgPosAndScale.getXOff(),
				newImgPosAndScale.getYOff(),
				(mUIMode & UI_MODE_ANISOTROPIC_SCALE) != 0 ? newImgPosAndScale
						.getScaleX() + 10 : newImgPosAndScale.getScale(),
				(mUIMode & UI_MODE_ANISOTROPIC_SCALE) != 0 ? newImgPosAndScale
						.getScaleY() + 10 : newImgPosAndScale.getScale(),
				newImgPosAndScale.getAngle());
		// FIXME: anisotropic scaling jumps when axis-snapping
		// FIXME: affine-ize
		// return setPos(newImgPosAndScale.getXOff(),
		// newImgPosAndScale.getYOff(),
		// newImgPosAndScale.getScaleAnisotropicX(),
		// newImgPosAndScale.getScaleAnisotropicY(), 0.0f);
	}

	/** Set the position and scale of an image in screen coordinates */
	private boolean setPos(float centerX, float centerY, float scaleX,
			float scaleY, float angle) {
		float ws = (imageWidth / 2) * scaleX, hs = (imageHeight / 2) * scaleY;
		float newMinX = centerX - ws, newMinY = centerY - hs, newMaxX = centerX
				+ ws, newMaxY = centerY + hs;
		if (newMinX > displayWidth - SCREEN_MARGIN || newMaxX < SCREEN_MARGIN
				|| newMinY > displayHeight - SCREEN_MARGIN
				|| newMaxY < SCREEN_MARGIN)
			return false;
		this.centerX = centerX;
		this.centerY = centerY;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.angle = angle;
		this.minX = newMinX;
		this.minY = newMinY;
		this.maxX = newMaxX;
		this.maxY = newMaxY;
		return true;
	}

	/** Return whether or not the given screen coords are inside this image */
	public boolean containsPoint(float scrnX, float scrnY) {
		// FIXME: need to correctly account for image rotation
		return (scrnX >= minX && scrnX <= maxX && scrnY >= minY && scrnY <= maxY);
	}

	public void draw(Canvas canvas) {
		// canvas.save();

		canvas.saveLayerAlpha(0, 0, canvas.getWidth(), canvas.getHeight(), 255,
				Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);

		float dx = (maxX + minX) / 2;
		float dy = (maxY + minY) / 2;
		mImageDrawable
				.setBounds((int) minX, (int) minY, (int) maxX, (int) maxY);
		Bitmap image = Bitmap.createBitmap(mMask.getWidth(), mMask.getHeight(),
				Config.ARGB_8888);
		Canvas tweaked = new Canvas(image);
		tweaked.translate(dx, dy);
		tweaked.rotate(angle * 180.0f / (float) Math.PI);
		tweaked.translate(-dx, -dy);
		mImageDrawable.draw(tweaked);
		canvas.drawBitmap(image, 0, 0, null);
		mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		canvas.drawBitmap(mMask, 0, 0, mPaint);
		mPaint.setXfermode(null);

		canvas.restore();
	}

	public Drawable getDrawable() {
		return mImageDrawable;
	}

	public int getWidth() {
		return imageWidth;
	}

	public int getHeight() {
		return imageHeight;
	}

	public float getCenterX() {
		return centerX;
	}

	public float getCenterY() {
		return centerY;
	}

	public float getScaleX() {
		return scaleX;
	}

	public float getScaleY() {
		return scaleY;
	}

	public float getAngle() {
		return angle;
	}

	// FIXME: these need to be updated for rotation
	public float getMinX() {
		return minX;
	}

	public float getMaxX() {
		return maxX;
	}

	public float getMinY() {
		return minY;
	}

	public float getMaxY() {
		return maxY;
	}
}
