package com.suterastudio.drypers.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.suterastudio.drypers.DrypersException;

public class Babbler {
	public String id;
	public String address;
	public String age;	
	public String avatar_id;
	public String avatar_url;	
	public String country;
	public String dob;
	public String email;
	public String facebook;	 
	public String name;
	public String phone;
	public String points;	
	public String position;
	public String postcode;
	public String state;
	public String created_at;
	public String updated_at;
	
	public Babbler(JSONObject babbler) throws DrypersException {
		if(babbler == null) {
			throw new DrypersException("Invalid credentials");
		}
		
		try {
			id = babbler.getString("id");
			address = babbler.getString("address");
			age = babbler.getString("age");
			avatar_id = babbler.getString("avatar_id");
			avatar_url = babbler.getString("avatar_url");
			country = babbler.getString("country");
			dob = babbler.getString("dob");
			email = babbler.getString("email");
			facebook = babbler.getString("facebook");
			name = babbler.getString("name");
			phone = babbler.getString("phone");
			points = babbler.getString("points");
			position = babbler.getString("position");
			postcode = babbler.getString("postcode");
			state = babbler.getString("state");
			created_at = babbler.getString("created_at");
			updated_at = babbler.getString("updated_at");
		} catch (JSONException e) {
			e.printStackTrace();
			throw new DrypersException(e.getMessage(), e);
		}
	}
}
