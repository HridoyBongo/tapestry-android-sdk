package com.tapad.tapestry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;


public abstract class HttpStack {
	
	protected static final String HEADER_PARTNER_ID = "X-Tapestry-Id";
	protected static final int TIMEOUT_CONNECT = 10 * 1000;
	protected static final String CONTENT_CHARSET = "UTF-8";
	
	private String partnerId;

	public HttpStack(String partnerId, String userAgent) {
		this.partnerId = partnerId;
	}
	
	public String getPartnerId() {
		return partnerId;
	}
	
	public abstract String performGet(String uri) throws Exception;
	
	protected String readEntityBody(HttpEntity entity) throws IOException, UnsupportedEncodingException {
		String responseBody;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		entity.writeTo(bout);
		entity.consumeContent();
		responseBody = bout.toString(CONTENT_CHARSET);
		return responseBody;
	}
}
