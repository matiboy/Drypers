package com.suterastudio.android.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import com.suterastudio.drypers.data.DrypersResources;
import com.suterastudio.drypers.data.LanguagePreference;

public class ContextHelper {

	private static LanguagePreference chosenLanguage=LanguagePreference.BAHASA_MALAYSIA;

	public static String getRealPathFromURI(ContentResolver contentResolver,
			Uri contentURI) {
		Cursor cursor = contentResolver.query(contentURI, null, null, null,
				null);
		cursor.moveToFirst();
		int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
		return cursor.getString(idx);
	}

	// Utility function to check if user is online
	public static boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	// method to search through all children and set their status (currently
	// textbox n edittext)
	public static void clearForm(ViewGroup group, boolean status) {
		for (int i = 0, count = group.getChildCount(); i < count; ++i) {
			View view = group.getChildAt(i);
			if (view instanceof EditText) {
				((EditText) view).setEnabled(status);
			}
			if (view instanceof CheckBox) {
				((CheckBox) view).setEnabled(status);
			}

			if (view instanceof ViewGroup
					&& (((ViewGroup) view).getChildCount() > 0))
				clearForm((ViewGroup) view, status);
		}
	}
	
	// method to search through all children and set their status (currently
	// textbox to check if it is filled)
	public static int checkForm(ViewGroup group, int check) {
		for (int i = 0, count = group.getChildCount(); i < count; ++i) {
			View view = group.getChildAt(i);
			if (view instanceof EditText) {
				if ( ((EditText) view).getText().length() > 0 ){
					check++;
				}
			}

			if (view instanceof ViewGroup
					&& (((ViewGroup) view).getChildCount() > 0))
				check = checkForm((ViewGroup) view, check);
		}
		return check;
	}

	public static void createAsset(Context context, File file, String location,
			String fileName) {
		if (!file.exists()) {
			try {
				file.createNewFile();
				Log.i(ContextHelper.class.toString(), "Created new file: "
						+ fileName);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			AssetManager assetManager;
			InputStream mInput;
			try {
				assetManager = context.getResources().getAssets();
				mInput = assetManager.open(location);
				System.out.println("Asset location: " + location.toString());
				OutputStream out = new FileOutputStream(new File(
						DrypersResources.getLibraryDirectory() + fileName));

				int read = 0;
				byte[] bytes = new byte[1024];

				while ((read = mInput.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}
				Log.i(ContextHelper.class.toString(), "Wrote " + fileName
						+ " to file.");
				mInput.close();
				out.flush();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	

	public static void copyFile(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	public static LanguagePreference thawChosenLanguage() {
		return chosenLanguage;
	}

	public static void freezeChosenLanguage(LanguagePreference preference) {
		chosenLanguage=preference;
		
	}
	private static String selectedTemplate;
	public static void setSelectedSong(String string) {
		selectedTemplate=string;
		
	}
	public static String getSelectedSong()
	{
		return selectedTemplate;
	}
}
