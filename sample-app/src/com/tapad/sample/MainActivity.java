package com.tapad.sample;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.tapad.tapestry.Logging;
import com.tapad.tapestry.R;
import com.tapad.tapestry.TapestryClient;
import com.tapad.tapestry.TapestryService;
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
		intent.putExtra(Intent.EXTRA_SUBJECT, "Tapad Bridge Device");

		JSONObject bridge = new JSONObject();
		String deviceIDsEncoded = "";
		try {
			List<TypedIdentifier> deviceIDs = TapestryService.client().getDeviceIDs();
			for (TypedIdentifier id : deviceIDs) {
				bridge.put(id.getType(), id.getValue());
			}
			Logging.d("Added device ids:\n" + bridge.toString());

			deviceIDsEncoded = URLEncoder.encode(bridge.toString(), "UTF-8");
		} catch (Exception e) {
			Logging.e("Could not create bridge URL");
			Toast.makeText(this, "Could not create bridge URL", Toast.LENGTH_LONG).show();
			return;
		}
		String bridgeURL = "http://tapestry-api-test.dev.tapad.com/tapestry/1?&ta_bridge=" + deviceIDsEncoded;
		intent.putExtra(Intent.EXTRA_TEXT, "Pleaes open this URL to bridge your device:\n\n" + bridgeURL);
		startActivity(Intent.createChooser(intent, "Send Bridge Email"));
	}
}
