package com.smc.elastic.search.search;

import org.json.JSONArray;
import org.json.JSONObject;

public class ESQuery {
	JSONObject jsonObject;
	public ESQuery() {
		jsonObject = new JSONObject();
	}
	public ESQuery must(Condition... condition) {
		JSONArray jsonArray = new JSONArray();
		for(Condition cond:condition){
			jsonArray.put(cond.toDoc());
		}
		jsonObject.put("must", jsonArray);
		return this;
	}
	public ESQuery filter(Condition... condition) {
		JSONArray jsonArray = new JSONArray();
		for(Condition cond:condition){
			jsonArray.put(cond.toDoc());
		}
		jsonObject.put("filter", jsonArray);
		return this;
	}
	public ESQuery should(Condition... condition) {
		JSONArray jsonArray = new JSONArray();
		for(Condition cond:condition){
			jsonArray.put(cond.toDoc());
		}
		jsonObject.put("should", jsonArray);
		return this;
	}
	public JSONObject getQueryObject() {
		return this.jsonObject;
	}
}

