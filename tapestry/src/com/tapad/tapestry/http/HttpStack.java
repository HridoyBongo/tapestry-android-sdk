package com.tapad.tapestry.http;

/**
 * Can perform HTTP requests.
 */
public interface HttpStack {
	public abstract String performGet(String uri, String headerName, String headerValue) throws Exception;
}