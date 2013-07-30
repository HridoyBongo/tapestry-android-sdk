package com.tapad.tapestry.http;

import static com.tapad.tapestry.http.HttpStackFactory.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

class HttpClientStack implements HttpStack {
	private DefaultHttpClient client;

	public HttpClientStack(String userAgent) {
		client = createClient(userAgent);
	}

	@Override
	public String performGet(String uri, String partnerId) throws Exception {
		HttpGet http = new HttpGet(uri);
		http.addHeader(HEADER_PARTNER_ID, partnerId);

		HttpResponse response = client.execute(http);
		String responseBody = null;
		HttpEntity entity = response.getEntity();
		validateOrThrowResponse(response);
		responseBody = readEntityBody(entity);
		return responseBody;
	}

	private void validateOrThrowResponse(HttpResponse response) throws HttpStackException {
		if (response == null || response.getStatusLine() == null) {
			throw new HttpStackException("Server returned no response.");
		}
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode < 200 || statusCode >= 400) {
		 	throw new HttpStackException("Invalid HTTP response code: " + statusCode + ", " + response.getStatusLine().getReasonPhrase());
		}
	}

	/**
	 * Creates a thread-safe http client with settings based on the
	 * {@link android.net.http.AndroidHttpClient}. We do not depend on the
	 * AndroidHttpClient in order to be compatible with older Android SDKs.
	 * 
	 * @param userAgent
	 *            The user agent to use
	 * @return an http client
	 */
	protected DefaultHttpClient createClient(String userAgent) {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, CONTENT_CHARSET);
		HttpProtocolParams.setUserAgent(params, userAgent);
		HttpConnectionParams.setStaleCheckingEnabled(params, false);
		HttpConnectionParams.setConnectionTimeout(params, TIMEOUT_CONNECT);
		HttpConnectionParams.setSoTimeout(params, TIMEOUT_CONNECT);
		HttpConnectionParams.setSocketBufferSize(params, 8192);
		HttpClientParams.setRedirecting(params, false);
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		ClientConnectionManager manager = new ThreadSafeClientConnManager(params, schemeRegistry);
		DefaultHttpClient client = new DefaultHttpClient(manager, params);
		return client;
	}
}