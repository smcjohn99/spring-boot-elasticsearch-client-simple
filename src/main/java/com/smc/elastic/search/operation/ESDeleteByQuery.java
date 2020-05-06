package com.smc.elastic.search.operation;


import java.io.IOException;

import org.json.JSONObject;

import com.smc.simple.http.request.SimpleHttpRequest;
import com.smc.elastic.search.search.ESBool;
import com.smc.simple.http.request.HttpResult;


public class ESDeleteByQuery {
	JSONObject body;
	protected ESDeleteByQuery(ESBool query){
		this.body = new JSONObject().put("query", query.toDoc());
	}
	public JSONObject getBody() {
		return body;
	}
	
	public HttpResult request(String url) throws IOException {
		return SimpleHttpRequest.post(url).asJson().body(body.toString()).request();
	}
}
