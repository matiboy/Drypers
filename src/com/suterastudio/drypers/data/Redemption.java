package com.suterastudio.drypers.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.suterastudio.drypers.DrypersException;

public class Redemption {
	public String id;
	public String approved;
	public String award_id;
	public String award_title;
	public String babbler_id;
	public String babbler_name;
	public String position;
	public String created_at;
	public String updated_at;
	
	public Redemption(JSONObject redemption) throws DrypersException {
		try {
			approved = redemption.getString("approved");
			id = redemption.getString("id");
			award_id = redemption.getString("award_id");
			award_title = redemption.getString("award_title");
			babbler_id = redemption.getString("babbler_id");
			babbler_name = redemption.getString("babbler_name");
			position = redemption.getString("position");
			created_at = redemption.getString("created_at");
			updated_at = redemption.getString("updated_at");
		} catch (JSONException e) {
			e.printStackTrace();
			throw new DrypersException(e.getMessage(), e);
		}		
	}
}
