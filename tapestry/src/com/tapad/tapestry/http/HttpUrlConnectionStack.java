package com.tapad.tapestry.http;

import static com.tapad.tapestry.http.HttpStackFactory.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.entity.BasicHttpEntity;

/** 
 * HttpStack implementation based on the new and recommended HttpUrlConnection
 * heavily inspired by Google Volley @ https://android.googlesource.com/platform/frameworks/volley/
 */
class HttpUrlConnectionStack implements HttpStack {
	@Override
	public String performGet(String uri, String partnerId) throws Exception {
		URL url = new URL(uri);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.addRequestProperty(HEADER_PARTNER_ID, partnerId);

		connection.setConnectTimeout(TIMEOUT_CONNECT);
		connection.setReadTimeout(TIMEOUT_CONNECT);
		connection.setUseCaches(false);
		connection.setDoInput(true);

		int responseCode = connection.getResponseCode();
		validateConnection(connection);
		if (responseCode == -1) {
			// -1 is returned by getResponseCode() if the response code could
			// not be retrieved.
			// Signal to the caller that something was wrong with the
			// connection.
			throw new IOException("Could not retrieve response code from HttpUrlConnection.");
		}

		HttpEntity responseEntity = entityFromConnection(connection);
		String responseBody = readEntityBody(responseEntity);
		return responseBody;
	}

	private void validateConnection(HttpURLConnection connection) throws HttpStackException, IOException {
		if (connection == null || connection.getResponseCode() < 0) {
			throw new HttpStackException("Server returned no response.");
		}
		int statusCode = connection.getResponseCode();
		if (statusCode < 200 || statusCode >= 400) {
		 	throw new HttpStackException("Invalid HTTP response code: " + statusCode);
		}		
	}

	/**
	 * Initializes an {@link HttpEntity} from the given
	 * {@link HttpURLConnection}.
	 * 
	 * @param connection
	 * @return an HttpEntity populated with data from <code>connection</code>.
	 */
	private static HttpEntity entityFromConnection(HttpURLConnection connection) {
		BasicHttpEntity entity = new BasicHttpEntity();
		InputStream inputStream;
		try {
			inputStream = connection.getInputStream();
		} catch (IOException ioe) {
			inputStream = connection.getErrorStream();
		}
		entity.setContent(inputStream);
		entity.setContentLength(connection.getContentLength());
		entity.setContentEncoding(connection.getContentEncoding());
		entity.setContentType(connection.getContentType());
		return entity;
	}
}