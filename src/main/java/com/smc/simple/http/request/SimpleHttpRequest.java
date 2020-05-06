package com.smc.simple.http.request;

import java.io.IOException;


public class SimpleHttpRequest {
	public static RequestWithoutBody get(String url) {
		return new RequestWithoutBody("GET", url);
	}
	public static RequestWithBody post(String url) {
		return new RequestWithBody("POST", url);
	}
	public static RequestWithBody put(String url) {
		return new RequestWithBody("PUT", url);
	}
	public static MultipartRequest multipartRequest(String url) throws IOException{
		return new MultipartRequest(url, "UTF-8");
	}
	public static RequestWithoutBody delete(String url) {
		return new RequestWithoutBody("DELETE", url);
	}
	public static RequestWithoutBody head(String url) {
		return new RequestWithoutBody("HEAD", url);
	}
	public static RequestWithoutBody options(String url) {
		return new RequestWithoutBody("OPTIONS", url);
	}
	public static RequestWithBody patch(String url) {
		return new RequestWithBody("PATCH", url);
	}
}