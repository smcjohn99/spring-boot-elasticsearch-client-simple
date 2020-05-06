package com.smc.elastic.search.search;

import org.json.JSONArray;
import org.json.JSONObject;

public class Must extends Condition{
	JSONArray JsonArray;
	protected Must(Condition...conditions) {
		JsonArray = new JSONArray();
		for(Condition cond:conditions){
			JsonArray.put(cond.toDoc());
		}
	}
	@Override
	public String key() {
		return "must";
	}

	public JSONObject toDoc() {
		return new JSONObject().put("bool", new JSONObject().put(this.key(), JsonArray));
	}

}
