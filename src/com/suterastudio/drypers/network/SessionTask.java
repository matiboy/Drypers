package com.suterastudio.drypers.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.suterastudio.drypers.DrypersException;
import com.suterastudio.drypers.data.Award;
import com.suterastudio.drypers.data.AwardHandler;
import com.suterastudio.drypers.data.Babble;
import com.suterastudio.drypers.data.BabbleHandler;
import com.suterastudio.drypers.data.Babbler;
import com.suterastudio.drypers.data.BabblerHandler;
import com.suterastudio.drypers.data.DrypersResources;
import com.suterastudio.drypers.data.Redemption;
import com.suterastudio.drypers.data.RedemptionHandler;
import com.suterastudio.drypers.data.Score;
import com.suterastudio.drypers.data.ScoreHandler;

public class SessionTask extends AsyncTask<HttpRequestBase, Integer, Boolean> {
	private static HttpClient SharedHttpClient = DrypersResources.getHttpClient();
	private static HttpContext SharedHttpContext = DrypersResources.getHttpContext();
	
	public enum Action {
		LOGIN, LOGOUT, REGISTER, PROFILE, LOOKUP, UPDATE_PROFILE, AWARDS, BABBLES, MY_BABBLES, BABBLE,
		FRIENDS_BABBLES, LIKE_BABBLE, UPLOAD_BABBLE, MY_REDEMPTIONS, UPLOAD_REDEMPTION, MY_SCORES, UPLOAD_SCORE
	}

	private SessionHandler mSessionListener;
	private Action mAction;
	private Babbler babbler;
	private Babble babble;
	private Score score;
	private Redemption redemption;
	private List<Award> awards;
	private List<Babble> babbles;
	private List<Redemption> redemptions;
	private List<Score> scores;
	private JSONObject jsonObject = null;
	private JSONArray jsonArray = null;

	public SessionTask(Action action, SessionHandler listener) {
		mSessionListener = listener;
		mAction = action;
	}

	@Override
	protected Boolean doInBackground(HttpRequestBase... requests) {
		HttpRequestBase request = requests[0];
		
		HttpClient comparedhttpClient = DrypersResources.getHttpClient();
		HttpContext comparedhttpContext = DrypersResources.getHttpContext();
		
		try {
			switch (mAction) {
			case LOGIN:
			case LOGOUT:
			case PROFILE:
			case REGISTER:
			case UPDATE_PROFILE:
				babbler = new Babbler(loadJsonObject(request));
				DrypersResources.setBabbler(babbler);
				break;

			case LOOKUP:
				jsonObject = pluckJsonObject(loadJsonArray(request), 0);
				babbler = jsonObject != null ? new Babbler(jsonObject) : null;
				break;

			case AWARDS:
				jsonArray = loadJsonArray(request);
				awards = new ArrayList<Award>();
				for (int i = 0; i < jsonArray.length(); i++) {
					awards.add(new Award(pluckJsonObject(jsonArray, i)));
				}
				break;

			case BABBLES:
			case MY_BABBLES:
			case FRIENDS_BABBLES:
				jsonArray = loadJsonArray(request);
				babbles = new ArrayList<Babble>();
				for (int i = 0; i < jsonArray.length(); i++) {
					babbles.add(new Babble(pluckJsonObject(jsonArray, i)));
				}
				break;
			case BABBLE:
			case LIKE_BABBLE:
				babble = new Babble(loadJsonObject(request));
				break;
			
			case UPLOAD_BABBLE:
				babble = new Babble(loadJsonObject(request));
				break;

			case MY_REDEMPTIONS:
				jsonArray = loadJsonArray(request);
				redemptions = new ArrayList<Redemption>();
				for (int i = 0; i < jsonArray.length(); i++) {
					redemptions.add(new Redemption(pluckJsonObject(jsonArray, i)));
				}
				break;
				
			case UPLOAD_REDEMPTION:
				redemption = new Redemption(loadJsonObject(request));
				break;

			case MY_SCORES:
				jsonArray = loadJsonArray(request);
				scores = new ArrayList<Score>();
				for (int i = 0; i < jsonArray.length(); i++) {
					scores.add(new Score(pluckJsonObject(jsonArray, i)));
				}
				break;
				
			case UPLOAD_SCORE:
				score = new Score(loadJsonObject(request));
				break;

			default:
				break;
			}
		} catch (DrypersException e) {
			e.printStackTrace();
			mSessionListener
					.onException(new DrypersException(e.getMessage(), e));
		}

		return true;
	}

	@Override
	protected void onPostExecute(Boolean success) {
		if (!success) {
			// NOOP
			return;
		}
		
		
		try{
			switch (mAction) {
			case LOOKUP:
				((BabblerHandler) mSessionListener).onLookup(babbler);
				break;
		
			case LOGIN:
				((BabblerHandler) mSessionListener).onLogin(babbler);
				break;
		
			case LOGOUT:
				((BabblerHandler) mSessionListener).onLogout(babbler);
				break;
		
			case REGISTER:
				((BabblerHandler) mSessionListener).onRegister(babbler);
				break;
		
			case PROFILE:
				((BabblerHandler) mSessionListener).onProfile(babbler);
				break;
				
			case UPDATE_PROFILE:
				((BabblerHandler) mSessionListener).onUpdate(babbler);
				break;
		
			case AWARDS:
				((AwardHandler) mSessionListener).onAwards(awards);
				break;
		
			case BABBLES:
				((BabbleHandler) mSessionListener).onBabbles(babbles);
				break;
		
			case FRIENDS_BABBLES:
				((BabbleHandler) mSessionListener).onFriendsBabbles(babbles);
				break;
		
			case MY_BABBLES:
				((BabbleHandler) mSessionListener).onMyBabbles(babbles);
				break;
		
			case BABBLE:
				((BabbleHandler) mSessionListener).onBabble(babble);
				break;
				
			case LIKE_BABBLE:
				((BabbleHandler) mSessionListener).onBabbleLiked(babble);
				break;
				
			case UPLOAD_BABBLE:
				((BabbleHandler) mSessionListener).onBabbleUploaded(babble);
				break;
		
			case MY_REDEMPTIONS:
				((RedemptionHandler) mSessionListener)
						.onMyRedemptions(redemptions);
				break;
			
			case UPLOAD_REDEMPTION:
				((RedemptionHandler) mSessionListener).onRedemptionUploaded(redemption);
				break;
		
			case MY_SCORES:
				((ScoreHandler) mSessionListener)
						.onMyScores(scores);
				break;
		
			case UPLOAD_SCORE:
				((ScoreHandler) mSessionListener).onScoreUploaded(score);
				break;
				
			default:
				break;
			}
			// cancels the async task if sucessful
			this.cancel(true);
			return;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private JSONArray loadJsonArray(HttpRequestBase request) {
		String responseBody = "";
		JSONArray jsonArray = null;

		try {
			HttpResponse response = SharedHttpClient.execute(request, SharedHttpContext);
			responseBody = EntityUtils.toString(response.getEntity());
			Log.d(this.getClass().toString(), "Server Array Response "
					+ responseBody);
			jsonArray = new JSONArray(responseBody);
		} catch (JSONException e) {
			e.printStackTrace();
			mSessionListener
					.onException(new DrypersException(e.getMessage(), e));
		} catch (ParseException e) {
			e.printStackTrace();
			mSessionListener
					.onException(new DrypersException(e.getMessage(), e));
		} catch (IOException e) {
			e.printStackTrace();
			mSessionListener
					.onException(new DrypersException(e.getMessage(), e));
		}

		return jsonArray;
	}

	private JSONObject loadJsonObject(HttpRequestBase request) {
		String responseBody = "";
		JSONObject jsonObject = null;

		try {
			HttpResponse response = SharedHttpClient.execute(request, SharedHttpContext);
			responseBody = EntityUtils.toString(response.getEntity());
			Log.d(this.getClass().toString(), "Server Object Response "
					+ responseBody);
			jsonObject = toJsonObject(responseBody);
		} catch (ParseException e) {
			mSessionListener
					.onException(new DrypersException(e.getMessage(), e));
		} catch (IOException e) {
			mSessionListener
					.onException(new DrypersException(e.getMessage(), e));
		} catch (DrypersException e) {
			mSessionListener.onException(e);
		}

		return jsonObject;
	}

	private JSONObject pluckJsonObject(JSONArray jsonArray, int index)
			throws DrypersException {
		JSONObject jsonObject = null;
		if (jsonArray != null && jsonArray.length() >= index) {
			try {
				jsonObject = jsonArray.getJSONObject(index);
			} catch (JSONException e) {
				new DrypersException(e.getMessage(), e);
			}
		}

		return jsonObject;
	}

	private JSONObject toJsonObject(String response) throws DrypersException {
		JSONObject object = null;
		String error = null;
		try {
			object = new JSONObject(response);
			error = object.getString("error");

			if (error != null) {
				throw new DrypersException(error);
			}
		} catch (JSONException e) {
			// No worries
		}
		return object;
	}

	private JSONArray toJsonArray(String response) throws DrypersException {
		JSONArray array = null;
		String error = null;
		try {
			array = new JSONArray(response);
		} catch (JSONException e) {
			JSONObject object;
			try {
				object = new JSONObject(response);
				error = object.getString("error");
			} catch (JSONException e1) {
				e1.printStackTrace();
				throw new DrypersException(e1.getMessage(), e1);
			}
			if (error != null) {
				throw new DrypersException(error);
			}
		}

		return array;
	}
}
