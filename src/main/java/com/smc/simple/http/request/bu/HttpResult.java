package com.smc.simple.http.request.bu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpResult{
	public HttpURLConnection httpConn;
	private int statusCode;
	private InputStream inputStream;
	private String requestUrl, method;
	private Map<String, List<String>> parameters, requestHeaders, responseHeaders;
	
	public HttpResult(HttpURLConnection conn) {
		httpConn = conn;
		parameters = new HashMap<>();
		requestHeaders = new HashMap<>();
		responseHeaders = new HashMap<>();
	}
	public String getMethod() {
		return httpConn.getRequestMethod();
		//return method;
	}
	public int getStatusCode() throws IOException{
		return httpConn.getResponseCode();
		//eturn statusCode;
	}
	public InputStream getInputStream() {
		return inputStream;
	}
	public String getBody() {
		StringBuilder stringBuilder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
			br.lines().forEach(stringBuilder::append);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return stringBuilder.toString();
	}
	public String getRequestUrl() {
		return httpConn.getURL().toString();
	}
	public Map<String, List<String>> getParameters() {
		return parameters;
	}
	public Map<String, List<String>> getRequestHeaders() {
		return httpConn.getRequestProperties();
	}
	public Map<String, List<String>> getResponseHeaders() {
		return httpConn.getHeaderFields();
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}
	public void addParameters(String key, String value) {
		List<String> temp = this.parameters.getOrDefault(key, new ArrayList<String>());
		temp.add(value);
		this.parameters.put(key, temp);
	}
	public void setRequestHeaders(Map<String, List<String>> requestHeaders) {
		this.requestHeaders = requestHeaders;
	}
	public void setResponseHeaders(Map<String, List<String>> responseHeaders) {
		this.responseHeaders = responseHeaders;
	}
}