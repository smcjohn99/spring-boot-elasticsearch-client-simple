package com.smc.elastic.search.search;

import org.json.JSONArray;
import org.json.JSONObject;

public class ESBool{
	JSONObject jsonObject;
	public ESBool() {
		jsonObject = new JSONObject();
	}
	public ESBool must(Condition... condition) {
		JSONArray jsonArray = new JSONArray();
		for(Condition cond:condition){
			jsonArray.put(cond.toDoc());
		}
		jsonObject.put("must", jsonArray);
		return this;
	}
	public ESBool filter(Condition... condition) {
		JSONArray jsonArray = new JSONArray();
		for(Condition cond:condition){
			jsonArray.put(cond.toDoc());
		}
		jsonObject.put("filter", jsonArray);
		return this;
	}
	public ESBool should(Condition... condition) {
		JSONArray jsonArray = new JSONArray();
		for(Condition cond:condition){
			jsonArray.put(cond.toDoc());
		}
		jsonObject.put("should", jsonArray);
		return this;
	}
	
	public JSONObject toDoc() {
		return new JSONObject().put("bool", this.jsonObject);
	}
	
}

