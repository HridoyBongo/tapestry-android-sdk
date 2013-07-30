package com.tapad.tapestry.http;

public interface HttpStack {
	public abstract String performGet(String uri, String partnerId) throws Exception;
}