package com.smc.simple.http.request;

import java.io.IOException;
import java.util.HashMap;

public abstract class AbstractRequest{
	String method, url;
	HashMap<String, Object> queryStrings, headers;
	HttpResult requestObject;
	boolean isJson;
	public AbstractRequest(String method, String url) {
		this.url = url;
		this.queryStrings = new HashMap<>();
		this.method = method;
		this.headers = new HashMap<>();
		this.requestObject = new HttpResult();
		isJson = false;
	}
	public AbstractRequest header(String key, Object value) {
		this.headers.put(key, value);
		return this;
	}
	public AbstractRequest headers(HashMap<String, Object> map) {
		this.headers.putAll(map);
		return this;
	}
	public AbstractRequest queryString(String key, Object value) {
		this.queryStrings.put(key, value);
		return this;
	}
	public AbstractRequest queryStrings(HashMap<String, Object> map) {
		this.queryStrings.putAll(map);
		return this;
	}
	public AbstractRequest body(String body) {
		throw new UnsupportedOperationException();
	}
	public AbstractRequest asJson() {
		isJson = true;
		return this;
	}
	public abstract HttpResult request() throws IOException;
}