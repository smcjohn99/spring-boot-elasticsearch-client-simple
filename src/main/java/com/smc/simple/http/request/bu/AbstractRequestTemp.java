package com.smc.simple.http.request.bu;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRequestTemp{
	protected HttpURLConnection httpConn;
	HttpResult httpResult;
	
	public AbstractRequestTemp(String method, String requestUrl) throws IOException {
		
        URL url = new URL(requestUrl);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod(method);
        httpConn.setDoInput(true);
        httpResult = new HttpResult(httpConn);
        //httpResult.setRequestUrl(requestUrl);
        //httpResult.setMethod(method);
	}
	public AbstractRequestTemp addHeader(String key, String value) {
		httpConn.setRequestProperty(key, value);
		return this;
	}
	public AbstractRequestTemp setBody(String body) {
		throw new UnsupportedOperationException();	
	}
	public AbstractRequestTemp addHeaders(Map<String, String> map) {
		for(String key:map.keySet()) 
			httpConn.setRequestProperty(key, map.get(key));
		return this;
	}
	public AbstractRequestTemp addParameter(String key, String value) {
		httpResult.addParameters(key, value);
		return this;
	}
	public AbstractRequestTemp addParameters(HashMap<String, String> map) {
		for(String key:map.keySet()) 
			httpResult.addParameters(key, map.get(key));
		return this;
	}
	public AbstractRequestTemp asJson() {
		httpConn.setRequestProperty("Content-Type", "application/json");
		return this;
	}
	public abstract HttpResult request() throws IOException;
}