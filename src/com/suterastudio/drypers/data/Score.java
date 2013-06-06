package com.suterastudio.drypers.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.suterastudio.drypers.DrypersException;

public class Score {
	public String id;
	public String action;
	public String amount;
	public String babbler_id;
	public String babbler_name;
	public String position;
	public String created_at;
	public String updated_at;
	
	public Score(JSONObject score) throws DrypersException {
		try {
			id = score.getString("id");
			action = score.getString("action");
			amount = score.getString("amount");
			babbler_id = score.getString("babbler_id");
			babbler_name = score.getString("babbler_name");
			position = score.getString("position");
			created_at = score.getString("created_at");
			updated_at = score.getString("updated_at");
		} catch (JSONException e) {
			e.printStackTrace();
			throw new DrypersException(e.getMessage(), e);
		}		
	}
}
