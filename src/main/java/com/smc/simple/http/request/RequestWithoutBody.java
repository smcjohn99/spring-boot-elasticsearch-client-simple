package com.smc.simple.http.request;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class RequestWithoutBody extends AbstractRequest{
	public RequestWithoutBody(String method, String url) {
		super(method, url);
	}

	@Override
	public HttpResult request() throws IOException {
		if(this.queryStrings.size() > 0) {
			StringBuilder sBuilder = new StringBuilder("?");
			for(Map.Entry<String, Object> entry:this.queryStrings.entrySet()) {
				sBuilder.append(entry.getKey()+"="+entry.getValue()+"&");
			}
			url += sBuilder.toString();
			if(url.charAt(url.length()-1)=='&')
				url = url.substring(0, url.length()-1);
		}
		requestObject.setUrl(url);
		URL url = new URL(this.url);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod(this.method);
		for(Map.Entry<String, Object> entry:this.headers.entrySet())
			conn.setRequestProperty(entry.getKey(), entry.getValue().toString());
		if(this.isJson) {
			conn.setRequestProperty("Accept", "application/json");
		}
		
		requestObject.setStatusCode(conn.getResponseCode());

		if(conn.getResponseCode() == 200)
			requestObject.inputStream = conn.getInputStream();
		else
			requestObject.inputStream = conn.getErrorStream();
		return this.requestObject;
	}
}