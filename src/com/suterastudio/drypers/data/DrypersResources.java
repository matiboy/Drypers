package com.suterastudio.drypers.data;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.facebook.Session;
import com.loopj.android.http.PersistentCookieStore;
import com.suterastudio.drypers.DrypersException;
import com.suterastudio.drypers.network.SessionHelper.Gender;
import com.suterastudio.media.autotune.AutotuneParameter;

public class DrypersResources {
	public static final String Noggin = "babynoggin.png";
	public static final String BabbleTone = "babbletone.wav";

	public static final File RootDir = new File(Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ File.separator
			+ "BabyBabbleIO");
	public static final File BabblesDir = new File(getLibraryDirectory()
			+ "Babbles");
	public static final File InstrumentalDir = new File(getLibraryDirectory()
			+ "Instrumental");
	public static final File RingtoneDir = new File(getLibraryDirectory()
			+ "Ringtones");

	public static final File BabyHead = new File(RootDir.getAbsolutePath()
			+ File.separator + Noggin);
	public static final File Ringtone = new File(RingtoneDir.getAbsolutePath()
			+ File.separator + BabbleTone);
	public static final Locale locale = Locale.US;

	// Songs
	public static File KPopA = new File(DrypersResources.getLibraryDirectory()
			+ "kpopa.wav");
	public static File KPopB = new File(DrypersResources.getLibraryDirectory()
			+ "kpopb.wav");
	public static File KidsA = new File(DrypersResources.getLibraryDirectory()
			+ "kidsa.wav");
	public static File KidsB = new File(DrypersResources.getLibraryDirectory()
			+ "kidsb.wav");
	public static File Gangster = new File(
			DrypersResources.getLibraryDirectory() + "gangster.wav");
	public static File HipHop = new File(DrypersResources.getLibraryDirectory()
			+ "hiphop.wav");
	public static File SourceFile = new File(
			DrypersResources.getLibraryDirectory() + "source.wav");
	public static File AutotunePass1 = new File(
			DrypersResources.getLibraryDirectory() + "autotuned1.wav");
	public static File AutotunePass2 = new File(
			DrypersResources.getLibraryDirectory() + "autotuned2.wav");
	public static File AutotunePass3 = new File(
			DrypersResources.getLibraryDirectory() + "autotuned3.wav");
	public static File AutotunePass4 = new File(
			DrypersResources.getLibraryDirectory() + "autotuned4.wav");
	public static File AutotunePass5 = new File(
			DrypersResources.getLibraryDirectory() + "autotuned5.wav");
	public static final List<File> AutotunePasses = new ArrayList<File>() {
		{
			add(AutotunePass1);
			add(AutotunePass2);
			add(AutotunePass3);
			add(AutotunePass4);
			add(AutotunePass5);
		}
	};
	public static File LoopedAudio = new File(
			DrypersResources.getLibraryDirectory() + "looped.wav");
	public static File MuxedAudio = new File(
			DrypersResources.getLibraryDirectory() + "muxed.wav");
	public static File FinalFile = new File(
			DrypersResources.getLibraryDirectory() + "final.wav");
	public static final List<AutotuneParameter> KPopAParams = new ArrayList<AutotuneParameter>() {
		{
			add(new AutotuneParameter(-10.0f, 'B'));
			add(new AutotuneParameter(-10.0f, 'B'));
			add(new AutotuneParameter(-10.0f, 'B'));
			add(new AutotuneParameter(-10.0f, 'B'));
			add(new AutotuneParameter(-10.0f, 'B'));
		}
	};
	public static final List<AutotuneParameter> KPopBParams = new ArrayList<AutotuneParameter>() {
		{
			// add(new AutotuneParameter(-10.0f, 'B'));
			// add(new AutotuneParameter(-19.0f, 'D'));
			// add(new AutotuneParameter(-10.0f, 'B'));
			// add(new AutotuneParameter(-19.0f, 'D'));
			// add(new AutotuneParameter(-10.0f, 'B'));
			add(new AutotuneParameter(2.0f, 'B'));
			add(new AutotuneParameter(4.0f, 'D'));
			add(new AutotuneParameter(2.0f, 'B'));
			add(new AutotuneParameter(4.0f, 'D'));
			add(new AutotuneParameter(2.0f, 'B'));

		}
	};
	public static final List<AutotuneParameter> KidsAParams = new ArrayList<AutotuneParameter>() {
		{
			// add(new AutotuneParameter(-17.0f, 'E'));
			// add(new AutotuneParameter(-14.0f, 'G'));
			// add(new AutotuneParameter(-17.0f, 'E'));
			// add(new AutotuneParameter(-14.0f, 'G'));
			// add(new AutotuneParameter(-17.0f, 'E'));
			add(new AutotuneParameter(2.0f, 'E'));
			add(new AutotuneParameter(4.0f, 'G'));
			add(new AutotuneParameter(2.0f, 'E'));
			add(new AutotuneParameter(4.0f, 'G'));
			add(new AutotuneParameter(2.0f, 'E'));
		}
	};
	public static final List<AutotuneParameter> KidsBParams = new ArrayList<AutotuneParameter>() {
		{
			// add(new AutotuneParameter(-17.0f, 'E'));
			// add(new AutotuneParameter(-14.0f, 'G'));
			// add(new AutotuneParameter(-17.0f, 'E'));
			// add(new AutotuneParameter(-14.0f, 'G'));
			// add(new AutotuneParameter(-17.0f, 'E'));
			add(new AutotuneParameter(2.0f, 'E'));
			add(new AutotuneParameter(4.0f, 'G'));
			add(new AutotuneParameter(2.0f, 'E'));
			add(new AutotuneParameter(4.0f, 'G'));
			add(new AutotuneParameter(2.0f, 'E'));
		}
	};
	public static final List<AutotuneParameter> GangsterParams = new ArrayList<AutotuneParameter>() {
		{
			add(new AutotuneParameter(2.0f, 'A'));
			add(new AutotuneParameter(4.0f, 'B'));
			add(new AutotuneParameter(2.0f, 'A'));
			add(new AutotuneParameter(4.0f, 'B'));
			add(new AutotuneParameter(2.0f, 'A'));
		}
	};
	public static final List<AutotuneParameter> HipHopParams = new ArrayList<AutotuneParameter>() {
		{
			add(new AutotuneParameter(2.0f, 'A'));
			add(new AutotuneParameter(4.0f, 'B'));
			add(new AutotuneParameter(2.0f, 'A'));
			add(new AutotuneParameter(4.0f, 'B'));
			add(new AutotuneParameter(2.0f, 'A'));
		}
	};

	private static HttpClient HttpClient;
	private static HttpContext HttpContext;
	private static CookieStore CookieStore;
	private static Babbler Babbler;
	// private static List<Babble> MyBabbles;

	static {
		if (!RootDir.exists()) {
			RootDir.mkdirs();
		}

		if (!BabblesDir.exists()) {
			BabblesDir.mkdirs();
		}

		if (!InstrumentalDir.exists()) {
			InstrumentalDir.mkdirs();
		}

		if (!RingtoneDir.exists()) {
			RingtoneDir.mkdirs();
		}
	}

	public static void Thaw(Context context) {
		SharedPreferences prefReader = PreferenceManager
				.getDefaultSharedPreferences(context);
		String babblered = prefReader.getString("Babbler", null);

		try {
			if ((babblered != null) && !(babblered.equals(""))) {
				Babbler = new Babbler(new JSONObject(babblered));
			}
			if (CookieStore == null) {
				CookieStore = new PersistentCookieStore(context);
			}
		} catch (DrypersException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static void Freeze(Context context) {
		SharedPreferences prefReader = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor prefEditor = prefReader.edit();
		if (Babbler != null) {
			prefEditor.putString("Babbler",
					new JSONObject(getMap(Babbler)).toString());
		} else {
			prefEditor.putString("Babbler", "");
		}
		prefEditor.commit();
	}

	public static void Reset(Context context) {
		Session session = Session.getActiveSession();
		if (session != null) {
			session.closeAndClearTokenInformation();
		}
		if (CookieStore != null) {
			CookieStore.clear();
		}
		Babbler = null;
		Freeze(context);
	}

	public static void FreezeGender(String gender, Context context) {

		SharedPreferences prefReader = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor prefEditor = prefReader.edit();
		prefEditor.putString("Gender", gender);
		prefEditor.commit();
	}

	public static Gender ThawGender(Context context) {

		Gender gender = Gender.BOY;

		SharedPreferences prefReader = PreferenceManager
				.getDefaultSharedPreferences(context);
		String gendered = prefReader.getString("Gender", null);

		if (gendered != null
				&& (gendered.equals(Gender.BOY.toString()) || gendered
						.equals(Gender.GIRL.toString()))) {
			gender = Gender.valueOf(gendered.toUpperCase(locale));
		}
		return gender;
	}

	public static HttpClient getHttpClient() {
		return (HttpClient == null ? HttpClient = new DefaultHttpClient()
				: HttpClient);
	}

	public static HttpContext getHttpContext() {
		if (HttpContext == null) {
			HttpContext = new BasicHttpContext();
			HttpContext.setAttribute(ClientContext.COOKIE_STORE, CookieStore);
		}
		return HttpContext;
	}

	public static Babbler getBabbler() {
		return Babbler;
	}

	public static void setBabbler(Babbler babbler) {
		Babbler = babbler;
	}

	/*
	 * public static List<Babble> getMyBabbles() { return MyBabbles; }
	 * 
	 * public static void setMyBabbles(List<Babble> babbles) { MyBabbles =
	 * babbles; }
	 */

	public static Typeface getTypeFace(Context context) {
		Typeface typeFace = Typeface.createFromAsset(context.getAssets(),
				"fonts/SetFireToTheRain.ttf");
		return typeFace;
	}

	/*
	 * public static void setStart(Activity activity) throws DrypersException {
	 * Babbler babbler; List<Babble> babbles; Babble babble; Gender gender =
	 * null;
	 * 
	 * babbler = getBabbler(); if (babbler == null) { return; }
	 * 
	 * 
	 * // SessionHelper.getMyBabbles();
	 * 
	 * // babble = babbles.get(0); if (babble.gender.equals("Boy")) { gender =
	 * // Gender.BOY; } else if (babble.gender.equals("Girl")) { gender = //
	 * Gender.GIRL; }
	 * 
	 * 
	 * // First time use if (!DrypersResources.BabyHead.exists()) { Intent
	 * startOver = new Intent(activity, BabbleActivity.class);
	 * activity.startActivity(startOver); // Gender not chosen } else if
	 * (DrypersResources.BabyHead.exists() && gender == null) { Intent
	 * startGender = new Intent(activity, GenderActivity.class);
	 * activity.startActivity(startGender); // Ready to record } else if
	 * (DrypersResources.BabyHead.exists() && gender != null) { Intent
	 * startRecord = new Intent(activity, RecordingActivity.class)
	 * .putExtra("gender", gender); activity.startActivity(startRecord); } }
	 */

	public static int getPackageVersion(Context context) {
		int versionCode = -1;
		try {
			versionCode = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return versionCode;
	}

	public static String getLibraryDirectory() {
		return RootDir.getAbsolutePath() + File.separator;
	}

	public static String getInstrumentalDirectory() {
		return InstrumentalDir.getAbsolutePath() + File.separator;
	}

	public static boolean getFooBar(Context context) {
		SharedPreferences prefReader = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean pref = prefReader.getBoolean(context.getString(0),
				Boolean.parseBoolean(context.getString(0)));
		return pref;
	}

	public static int getSampleRate(Context context) {
		return 22050;
	}

	public static int getBufferSize(Context context) {
		return 6144;
	}

	public static int getBufferSizeAdjuster(Context context) {
		return 1;
	}

	public static String getInstrumentalTrack(Context context) {
		return "";
	}

	private static Map<String, Object> getMap(Object o) {
		Map<String, Object> result = new HashMap<String, Object>();
		Field[] declaredFields = o.getClass().getDeclaredFields();
		String name = "";
		Object value = null;
		for (Field field : declaredFields) {
			try {
				name = field.getName();
				value = field.get(o);
				if (value.equals("null")) {
					value = null;
				}
				result.put(name, value);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}
