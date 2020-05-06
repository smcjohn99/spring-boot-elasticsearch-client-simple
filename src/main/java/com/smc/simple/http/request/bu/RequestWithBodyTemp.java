package com.smc.simple.http.request.bu;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class RequestWithBodyTemp extends AbstractRequestTemp{
	String body;
	public RequestWithBodyTemp(String method, String url) throws IOException{
		super(method, url);
		httpConn.setDoOutput(true);
	}
	public RequestWithBodyTemp setBody(String body) {
		this.body = body;
		return this;
	}
	@Override
	public HttpResult request() throws IOException{
		if(body != null) {
			OutputStream outputStream = httpConn.getOutputStream();
			outputStream.write(this.body.getBytes());
			outputStream.flush();
			outputStream.close();
		}
        httpResult.setStatusCode(httpConn.getResponseCode());
        if (httpResult.getStatusCode() == HttpURLConnection.HTTP_OK)
        	httpResult.setInputStream(httpConn.getInputStream());
        else 
        	httpResult.setInputStream(httpConn.getErrorStream());
        return httpResult;
	}
}