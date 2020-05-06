package com.smc.elastic.search.operation;

import org.json.JSONObject;

public abstract class ESOperationWithoutBody implements ESOperation {
	String id;
	public ESOperationWithoutBody(String id) {
		this.id = id;
	}
	@Override
	public String toJSONString(String index) {
		return new JSONObject().put(this.operatorName(), new JSONObject().put("_index", index).put("_id", id)).toString()+"\r\n";
	}
	
}
