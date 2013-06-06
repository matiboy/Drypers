package com.suterastudio.android.media;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.suterastudio.drypers.ui.BabyHeadView;

public class ImageHelper {	
	public static void rotator(Context context, View target, Boolean fixed, Integer degrees, long timetotal) {
		RotateAnimation rotation = new RotateAnimation(0, degrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		rotation.setFillAfter(fixed);
		rotation.setDuration(timetotal);
		target.startAnimation(rotation);		
	}
	
	public static Bitmap decodeSampledBitmapFromFile(String filepath, int reqWidth, int reqHeight) {
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filepath, options);

		// Calculate inSampleSize
//		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filepath, options);
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}
/*	
	public static Bitmap roundImage(Bitmap bitmap) {
		Bitmap rounded = (Bitmap) ImageHelper.getRoundedCornerBitmap(bitmap, 80);
		return rounded;
	}
*/	
	public static Bitmap getResizedImage(BabyHeadView bhv) {
		// LinearLayout headshotContainer = (LinearLayout)
		// findViewById(R.id.headshot_container);
		bhv.setDrawingCacheEnabled(true);
		bhv.buildDrawingCache();
		Bitmap bitmap = bhv.getDrawingCache();
		return bitmap;
	}
	
	public static Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
		int targetWidth = 200;
		int targetHeight = 200;
		Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(targetBitmap);
		Path path = new Path();
		path.addCircle(((float) targetWidth - 1) / 2,
				((float) targetHeight - 1) / 2,
				(Math.min(((float) targetWidth), ((float) targetHeight)) / 2),
				Path.Direction.CCW);

		canvas.clipPath(path);
		Bitmap sourceBitmap = scaleBitmapImage;
		canvas.drawBitmap(sourceBitmap, new Rect(0, 0, sourceBitmap.getWidth(),
				sourceBitmap.getHeight()), new Rect(0, 0, targetWidth,
				targetHeight), null);

//		Bitmap targetBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.babymask);
		
		return targetBitmap;
	}	
	
	public static void saveImage(Bitmap bitmap, String filename) {
		try {
			FileOutputStream out = new FileOutputStream(filename);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Bitmap getRemoteImage(final URL aURL) {
		try {
			final URLConnection conn = aURL.openConnection();
			conn.connect();
			final BufferedInputStream bis = new BufferedInputStream(
					conn.getInputStream());
			final Bitmap bm = BitmapFactory.decodeStream(bis);
			bis.close();
			return bm;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}	
}
