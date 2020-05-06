package com.smc.simple.http.request.bu;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

public class RequestWithoutBodyTemp extends AbstractRequestTemp{
	public RequestWithoutBodyTemp(String method, String url) throws IOException{
		super(method, url);
	}

	@Override
	public HttpResult request() throws IOException{
		if(httpResult.getParameters().size()>0) {
			httpConn.setDoOutput(true);
			OutputStream outputStream = httpConn.getOutputStream();
			StringBuilder result = new StringBuilder();
			boolean first = true;
			for(String key:httpResult.getParameters().keySet()) {
				if (first)
		            first = false;
		        else
		            result.append("&");

		        result.append(URLEncoder.encode(key, "UTF-8"));
		        result.append("=");
		        result.append(URLEncoder.encode(httpResult.getParameters().get(key).get(0), "UTF-8"));
			}
			outputStream.write(result.toString().getBytes("UTF-8"));
			outputStream.flush();
			outputStream.close();
		}
        httpResult.setStatusCode(httpConn.getResponseCode());
        if (httpResult.getStatusCode() == HttpURLConnection.HTTP_OK)
        	httpResult.setInputStream(httpConn.getInputStream());
        else 
        	httpResult.setInputStream(httpConn.getErrorStream());
        //httpConn.disconnect();
        return httpResult;
	}
}