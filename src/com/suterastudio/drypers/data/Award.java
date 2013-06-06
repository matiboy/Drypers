package com.suterastudio.drypers.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.suterastudio.drypers.DrypersException;

public class Award {
	public String id;
	public String description;
	public String image_id;
	public String image_url;
	public String title;
	public String points;
	public String position;
	public String created_at;
	public String updated_at;
	
	public Award(JSONObject award) throws DrypersException {
		try {
			id = award.getString("id");
			description = award.getString("description");
			image_id = award.getString("image_id");
			image_url = award.getString("image_url");
			title = award.getString("title");
			points = award.getString("points");
			position = award.getString("position");
			created_at = award.getString("created_at");
			updated_at = award.getString("updated_at");
		} catch (JSONException e) {
			e.printStackTrace();
			throw new DrypersException(e.getMessage(), e);
		}		
	}
}
