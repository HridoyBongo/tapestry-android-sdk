package com.tapad.sample;

import java.net.URLEncoder;
import java.util.List;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import com.tapad.tapestry.Logging;
import com.tapad.tapestry.R;
import com.tapad.tapestry.TapestryService;
import com.tapad.tapestry.TapestryClient;
import com.tapad.tapestry.deviceidentification.TypedIdentifier;


public class MainActivity extends FragmentActivity {
	private MenuItem bridgeEmailItem;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_tabs);

		FragmentTabHost host = (FragmentTabHost) findViewById(android.R.id.tabhost);
		host.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		host.addTab(host.newTabSpec("demo").setIndicator("Demo"), CarExampleFragment.class, null);
		host.addTab(host.newTabSpec("debug").setIndicator("Debug"), DebugFragment.class, null);

        // This code should be included in
        GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();
        tracker.startNewSession("UA-30562281-7", this);
        tracker.setDebug(true);
        TapestryAnalyticsPlugin.track(tracker, new TapestryClient(this, "725"));
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GoogleAnalyticsTracker.getInstance().stopSession();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		bridgeEmailItem = menu.add("Compose Bridge Email");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == bridgeEmailItem) {
			composeBridgeEmail();
			return true;
		} else
			return super.onOptionsItemSelected(item);
	}

	private void composeBridgeEmail() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/html");
		intent.putExtra(Intent.EXTRA_SUBJECT, "Tapad Bridge: " + Build.MODEL);

		intent.putExtra(Intent.EXTRA_TEXT, "Please open this URL in your desktop's browser to bridge " + Build.MODEL + ":\n\n" + buildURL());
		startActivity(Intent.createChooser(intent, "Select Email App"));
	}

	private String buildURL() {
		JSONObject bridge = new JSONObject();
		List<TypedIdentifier> deviceIDs = TapestryService.client().getDeviceIDs();
		try {
			for (TypedIdentifier id : deviceIDs) {
				bridge.put(id.getType(), id.getValue());
			}
			Logging.d("Added device ids:\n" + bridge.toString());

			StringBuilder stringBuilder = new StringBuilder(TapestryService.client().getURL());
			stringBuilder.append("?ta_partner_id=");
			stringBuilder.append(TapestryService.client().getPartnerID());
			stringBuilder.append("&ta_bridge=");
			stringBuilder.append(URLEncoder.encode(bridge.toString(), "UTF-8"));
			stringBuilder.append("&ta_redirect=");
			stringBuilder.append(URLEncoder.encode("http://tapestry-demo-test.dev.tapad.com/content_optimization", "UTF-8"));
			return stringBuilder.toString();
		} catch (Exception e) {
			Logging.e("Some error occurred while making URL for bridging", e);
			return "error!";
		}
	}
}
