package com.tapad.tapestry.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;

import android.content.Context;
import android.os.Build;

import com.tapad.tapestry.deviceidentification.UserAgent;

public class HttpStackFactory {	
	static final String HEADER_PARTNER_ID = "X-Tapestry-Id";
	static final int TIMEOUT_CONNECT = 10 * 1000;
	static final String CONTENT_CHARSET = "UTF-8";

	protected static String readEntityBody(HttpEntity entity) throws IOException, UnsupportedEncodingException {
		String responseBody;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		entity.writeTo(bout);
		entity.consumeContent();
		responseBody = bout.toString(CONTENT_CHARSET);
		return responseBody;
	}

	public static HttpStack getDefaultStack(Context context) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
			return new HttpClientStack(UserAgent.getUserAgent(context));
		return new HttpUrlConnectionStack();
	}
}