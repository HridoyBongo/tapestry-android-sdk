package com.tapad.sample;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;

import com.tapad.tapestry.R;

public class MainActivity extends FragmentActivity {
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_tabs);
        
        FragmentTabHost host = (FragmentTabHost)findViewById(android.R.id.tabhost);
        host.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        host.addTab(host.newTabSpec("demo")
                .setIndicator("Demo"),
                CarExampleFragment.class, null);
        host.addTab(host.newTabSpec("test")
                .setIndicator("Debug"),
                DebugFragment.class, null);
    }
}
