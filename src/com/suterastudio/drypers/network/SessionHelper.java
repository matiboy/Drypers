package com.suterastudio.drypers.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;

import android.os.StrictMode;
import android.util.Log;

import com.facebook.Session;
import com.suterastudio.drypers.DrypersException;
import com.suterastudio.drypers.data.AwardHandler;
import com.suterastudio.drypers.data.Babble;
import com.suterastudio.drypers.data.BabbleHandler;
import com.suterastudio.drypers.data.BabblerHandler;
import com.suterastudio.drypers.data.DrypersResources;
import com.suterastudio.drypers.data.RedemptionHandler;
import com.suterastudio.drypers.data.ScoreHandler;
import com.suterastudio.drypers.network.SessionTask.Action;

public class SessionHelper {
	public static String hostUrl = GetBaseURL();

	private static List<BabblerHandler> babblerListeners = new ArrayList<BabblerHandler>();
	private static List<BabbleHandler> babbleListeners = new ArrayList<BabbleHandler>();
	private static List<AwardHandler> awardListeners = new ArrayList<AwardHandler>();
	private static List<RedemptionHandler> redemptionListeners = new ArrayList<RedemptionHandler>();
	private static List<ScoreHandler> scoreListeners = new ArrayList<ScoreHandler>();

	public enum Gender {
		BOY {
			public String toString() {
				return "Boy";
			}
		},
		GIRL {
			public String toString() {
				return "Girl";
			}
		}
	}

	public enum ScoreType {
		LIKE {
			public String toString() {
				return "Like a song";
			}
		},
		CREATE {
			public String toString() {
				return "Create a song";
			}
		},
		SHARE {
			public String toString() {
				return "Share a song to your friends";
			}
		},
		PHOTO {
			public String toString() {
				return "Upload baby's photo";
			}
		},
		PROFILE {
			public String toString() {
				return "Finish filling up profile details";
			}
		},
		REDEEM {
			public String toString() {
				return "Redemption claim : ";
			}
		}
	}

	public static void Register(String name, String email, String password,
			String facebook, BabblerHandler listener) {
		// Perform the Post
		HttpPost httppost = new HttpPost(hostUrl + "/api/accounts");

		MultipartEntity reqEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);
		try {
			reqEntity.addPart("refinery_babbler[email]", new StringBody(email));
			reqEntity.addPart("refinery_babbler[password]", new StringBody(
					password));
			reqEntity.addPart("refinery_babbler[name]", new StringBody(name));
			if (facebook != null) {
				reqEntity.addPart("refinery_babbler[facebook]", new StringBody(
						facebook));
			}
			httppost.setEntity(reqEntity);
		} catch (UnsupportedEncodingException e) {
			listener.onException(new DrypersException(e.getMessage(), e));
			return;
		}
		httppost.setEntity(reqEntity);

		new SessionTask(Action.REGISTER, listener).execute(httppost);
	}

	public static void Login(String email, String password,
			BabblerHandler listener) {
		Log.i(SessionHelper.class.toString(), "Logging in " + email);

		HttpPost httppost = new HttpPost(hostUrl + "/api/accounts/login");

		MultipartEntity reqEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);
		try {
			reqEntity.addPart("refinery_babbler[email]", new StringBody(email));
			reqEntity.addPart("refinery_babbler[password]", new StringBody(
					password));
		} catch (UnsupportedEncodingException e) {
			listener.onException(new DrypersException(e.getMessage(), e));
			return;
		}
		httppost.setEntity(reqEntity);

		new SessionTask(Action.LOGIN, listener).execute(httppost);
	}

	public static void Login(String facebook, String email, String token,
			BabblerHandler listener) {
		Log.i(SessionHelper.class.toString(), "Logging in via Facebook "
				+ email + " " + token);

		HttpPost httppost = new HttpPost(hostUrl + "/api/accounts/login");

		MultipartEntity reqEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);
		try {
			reqEntity.addPart("refinery_babbler[email]", new StringBody(email));
			reqEntity.addPart("refinery_babbler[token]", new StringBody(token));
		} catch (UnsupportedEncodingException e) {
			listener.onException(new DrypersException(e.getMessage(), e));
			return;
		}
		httppost.setEntity(reqEntity);

		new SessionTask(Action.LOGIN, listener).execute(httppost);
	}

	public static void Logout(BabblerHandler listener) {
		Log.i(SessionHelper.class.toString(), "Logging out via Facebook");

		HttpGet httpget = new HttpGet(hostUrl + "/api/accounts/logout");

		new SessionTask(Action.LOGOUT, listener).execute(httpget);
	}
	
	public static void LookupBabbler(String email, BabblerHandler listener)
			throws DrypersException {
		String babblerURL = hostUrl + "/api/accounts/profile/" + email;

		HttpGet httpget = new HttpGet(babblerURL);

		new SessionTask(Action.LOOKUP, listener).execute(httpget);
	}

	public static void GetProfile(BabblerHandler listener) {
		Log.i(SessionHelper.class.toString(), "Getting active babbler");

		HttpGet httpget = new HttpGet(hostUrl + "/api/accounts/profile");

		new SessionTask(Action.PROFILE, listener).execute(httpget);
	}

	public static void GetAwards(AwardHandler listener) {
		String awardsURL = hostUrl + "/awards";

		HttpGet httpget = new HttpGet(awardsURL);

		new SessionTask(Action.AWARDS, listener).execute(httpget);
	}

	public static void GetMyRedemptions(RedemptionHandler listener) {
		// Set the userID
		String userID = DrypersResources.getBabbler().id;

		String redemptionsURL = hostUrl + "/redemptions/babbler/" + userID;

		HttpGet httpget = new HttpGet(redemptionsURL);

		new SessionTask(Action.MY_REDEMPTIONS, listener).execute(httpget);
	}

	public static void GetMyScores(ScoreHandler listener) {
		String userID = DrypersResources.getBabbler().id;

		String scoresURL = hostUrl + "/scores/babbler/" + userID;

		HttpGet httpget = new HttpGet(scoresURL);

		new SessionTask(Action.MY_SCORES, listener).execute(httpget);
	}

	public static void GetFriendsBabbles(BabbleHandler listener) {
		String friendsBabblesUrl = hostUrl + "/babbles/friends";

		HttpPost httppost = new HttpPost(friendsBabblesUrl);

		MultipartEntity reqEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);
		try {
			reqEntity.addPart("token", new StringBody(Session
					.getActiveSession().getAccessToken()));
		} catch (UnsupportedEncodingException e) {
			listener.onException(new DrypersException(e.getMessage(), e));
			return;
		}
		httppost.setEntity(reqEntity);

		new SessionTask(Action.FRIENDS_BABBLES, listener).execute(httppost);
	}

	public static void GetMyBabbles(BabbleHandler listener) {
		// Set the userID
		String userID = DrypersResources.getBabbler().id;

		String babblesURL = hostUrl + "/babbles/babbler/" + userID;

		HttpGet httpget = new HttpGet(babblesURL);

		new SessionTask(Action.MY_BABBLES, listener).execute(httpget);
	}

	public static void GetAllBabbles(BabbleHandler listener) {
		String babblesURL = hostUrl + "/babbles";

		HttpGet httpget = new HttpGet(babblesURL);

		new SessionTask(Action.BABBLES, listener).execute(httpget);
	}

	public static void GetBabble(String babbleID, BabbleHandler listener) {
		String babblesURL = hostUrl + "/babbles/" + babbleID;

		HttpGet httpget = new HttpGet(babblesURL);

		new SessionTask(Action.BABBLE, listener).execute(httpget);
	}

	public static void LikeBabble(Babble babble, BabbleHandler listener) {
		String likeBabbleUrl = hostUrl + "/babbles/" + babble.id + "/like";

		HttpGet httpget = new HttpGet(likeBabbleUrl);

		new SessionTask(Action.LIKE_BABBLE, listener).execute(httpget);
	}

	public static void UpdateProfile(String email, String name, String address,
			String dob, String postcode, String state, String country,
			BabblerHandler listener) {
		String userID = DrypersResources.getBabbler().id;

		String updateProfileUrl = hostUrl + "/babblers/" + userID;

		HttpPut httpput = new HttpPut(updateProfileUrl);

		MultipartEntity reqEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);
		try {
			reqEntity.addPart("babbler[id]", new StringBody(userID));
			reqEntity.addPart("babbler[email]", new StringBody(email));
			reqEntity.addPart("babbler[name]", new StringBody(name));
			reqEntity.addPart("babbler[address]", new StringBody(address));
			reqEntity.addPart("babbler[dob]", new StringBody(dob));
			reqEntity.addPart("babbler[postcode]", new StringBody(postcode));
			reqEntity.addPart("babbler[state]", new StringBody(state));
			reqEntity.addPart("babbler[country]", new StringBody(country));
		} catch (UnsupportedEncodingException e) {
			listener.onException(new DrypersException(e.getMessage(), e));
			return;
		}
		httpput.setEntity(reqEntity);

		new SessionTask(Action.UPDATE_PROFILE, listener).execute(httpput);
	}
	
	public static void updateBabble(String title, Gender gender, BabbleHandler listener) {
		String userID = DrypersResources.getBabbler().id;

		String updateBabbleUrl = hostUrl + "/babbles";
		
		HttpPut httpput = new HttpPut(updateBabbleUrl);

		MultipartEntity reqEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);
		try {
			reqEntity.addPart("babble[babbler_id]", new StringBody(userID));
			reqEntity.addPart("babble[title]", new StringBody(title));
			reqEntity.addPart("babble[gender]",
					new StringBody(gender.toString()));

			/*
			// Process Image into ByteArrayOutputStream
			if (image.exists()) {
				byte[] data = org.apache.commons.io.FileUtils
						.readFileToByteArray(image);
				ByteArrayBody photoBAB = new ByteArrayBody(data,
						DrypersResources.Noggin);
				reqEntity.addPart("babble[image]", photoBAB);
			}

			// Process track
			byte[] trackData = org.apache.commons.io.FileUtils
					.readFileToByteArray(track);
			ByteArrayBody trackBab = new ByteArrayBody(trackData, title
					+ ".wav");
			reqEntity.addPart("babble[track]", trackBab);*/
		} catch (UnsupportedEncodingException e) {
			listener.onException(new DrypersException(e.getMessage(), e));
			return;
		} catch (IOException e) {
			listener.onException(new DrypersException(e.getMessage(), e));
			return;
		}
		
		httpput.setEntity(reqEntity);

		new SessionTask(Action.UPLOAD_BABBLE, listener).execute(httpput);
	}

	public static void UploadBabble(String title, Gender gender, File image,
			File track, BabbleHandler listener) {
		String userID = DrypersResources.getBabbler().id;

		String uploadBabbleUrl = hostUrl + "/babbles";

		HttpPost httppost = new HttpPost(uploadBabbleUrl);

		MultipartEntity reqEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);
		try {
			reqEntity.addPart("babble[babbler_id]", new StringBody(userID));
			reqEntity.addPart("babble[title]", new StringBody(title));
			reqEntity.addPart("babble[gender]",
					new StringBody(gender.toString()));

			// Process Image into ByteArrayOutputStream
			if (image.exists()) {
				byte[] data = org.apache.commons.io.FileUtils
						.readFileToByteArray(image);
				ByteArrayBody photoBAB = new ByteArrayBody(data,
						DrypersResources.Noggin);
				reqEntity.addPart("babble[image]", photoBAB);
			}

			// Process track
			byte[] trackData = org.apache.commons.io.FileUtils
					.readFileToByteArray(track);
			ByteArrayBody trackBab = new ByteArrayBody(trackData, title
					+ ".wav");
			reqEntity.addPart("babble[track]", trackBab);
		} catch (UnsupportedEncodingException e) {
			listener.onException(new DrypersException(e.getMessage(), e));
			return;
		} catch (IOException e) {
			listener.onException(new DrypersException(e.getMessage(), e));
			return;
		}
		httppost.setEntity(reqEntity);

		new SessionTask(Action.UPLOAD_BABBLE, listener).execute(httppost);
	}

	public static void UploadRedemption(String awardId,
			RedemptionHandler listener) {
		String userID = DrypersResources.getBabbler().id;

		String uploadRedemptionUrl = hostUrl + "/redemptions";

		HttpPost httppost = new HttpPost(uploadRedemptionUrl);

		MultipartEntity reqEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);
		try {
			reqEntity.addPart("redemption[approved]", new StringBody("0"));
			reqEntity.addPart("redemption[award_id]", new StringBody(awardId));
			reqEntity.addPart("redemption[babbler_id]", new StringBody(userID));
		} catch (UnsupportedEncodingException e) {
			listener.onException(new DrypersException(e.getMessage(), e));
			return;
		}
		httppost.setEntity(reqEntity);

		new SessionTask(Action.UPLOAD_REDEMPTION, listener).execute(httppost);
	}

	public static void UploadScore(ScoreType action, ScoreHandler listener) {
		String userID = DrypersResources.getBabbler().id;
		String uploadScoreUrl = hostUrl + "/scores";
		String amount = "";

		HttpPost httppost = new HttpPost(uploadScoreUrl);

		MultipartEntity reqEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);

		switch (action) {
		case LIKE:
			amount = "1";
			break;
		case CREATE:
		case SHARE:
			amount = "10";
			break;
		case PHOTO:
		case PROFILE:
			amount = "20";
			break;
		default:
			amount = "0";
		}

		try {
			reqEntity.addPart("score[action]",
					new StringBody(action.toString()));
			reqEntity.addPart("score[amount]", new StringBody(amount));
			reqEntity.addPart("score[babbler_id]", new StringBody(userID));
		} catch (UnsupportedEncodingException e) {
			listener.onException(new DrypersException(e.getMessage(), e));
			return;
		}
		httppost.setEntity(reqEntity);

		new SessionTask(Action.UPLOAD_SCORE, listener).execute(httppost);
	}
	
	public static void UploadScoreRedemption(ScoreType action, String title,
			String value, ScoreHandler listener) {
		String amount = "-" + value;
		String userID = DrypersResources.getBabbler().id;
		String uploadScoreUrl = hostUrl + "/scores";

		HttpPost httppost = new HttpPost(uploadScoreUrl);

		MultipartEntity reqEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			reqEntity.addPart("score[action]",
					new StringBody(action.toString() + title));
			reqEntity.addPart("score[amount]", new StringBody(amount));
			reqEntity.addPart("score[babbler_id]", new StringBody(userID));
		} catch (UnsupportedEncodingException e) {
			listener.onException(new DrypersException(e.getMessage(), e));
			return;
		}
		httppost.setEntity(reqEntity);

		new SessionTask(Action.UPLOAD_SCORE, listener).execute(httppost);
	}
	
	public static void NoStrictMode() {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);
	}

	public static String GetBaseURL() {
		NoStrictMode();
		String rootURL = null;
		try {
			// Create a URL for the desired page
			URL url = new URL(
					"http://drypersmalaysia.com/babbleurl/babbleurl.txt");

			// Read all the text returned by the server
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()));
			rootURL = in.readLine();
			in.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.i("SessionTask", "Root URL is : " + rootURL);
		return rootURL;
	}
}
