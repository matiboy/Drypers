package com.suterastudio.drypers.data;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.suterastudio.drypers.DrypersException;

public class Babble implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6987302685447965250L;
	public String id;
	public String babbler_id;
	public String babbler_name;	
	public String gender;
	public String image_id;
	public String image_url;
	public String likes;
	public String position;
	public String title;
	public String track_id;
	public String track_url;
	public String created_at;
	public String updated_at;
	
	public Babble(JSONObject babble) throws DrypersException {
		try {
			id = babble.getString("id");
			babbler_id = babble.getString("babbler_id");
			babbler_name = babble.getString("babbler_name");
			gender = babble.getString("gender");
			image_id = babble.getString("image_id");
			image_url = babble.getString("image_url");
			likes = babble.getString("likes");
			position = babble.getString("position");
			title = babble.getString("title");
			track_id = babble.getString("track_id");
			track_url = babble.getString("track_url");
			created_at = babble.getString("created_at");
			updated_at = babble.getString("updated_at");
		} catch (JSONException e) {
			e.printStackTrace();
			throw new DrypersException(e.getMessage(), e);
		}
	}
}
