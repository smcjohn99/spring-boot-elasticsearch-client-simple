package com.smc.elastic.search.search;

import org.json.JSONObject;

public class ESBool{
	JSONObject jsonObject;
	public ESBool() {
		jsonObject = new JSONObject();
	}
	public ESBool must(Condition... condition) {
		for(Condition cond:condition)
			jsonObject.append("must", cond.toDoc());
		return this;
	}
	public ESBool filter(Condition... condition) {
		for(Condition cond:condition)
			jsonObject.append("filter", cond.toDoc());
		return this;
	}
	public ESBool should(Condition... condition) {
		for(Condition cond:condition)
			jsonObject.append("should", cond.toDoc());
		return this;
	}
	
	public JSONObject toDoc() {
		return new JSONObject().put("bool", this.jsonObject);
	}
	
}

