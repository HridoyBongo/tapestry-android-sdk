package com.tapad.tapestry;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class MenuTabActivity extends TabActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_tabs);

        TabHost host = getTabHost();
        host.addTab(host.newTabSpec("demo")
                .setIndicator("Demo")
                .setContent(new Intent().setClass(this, DemoActivity.class)));
        host.addTab(host.newTabSpec("test")
                .setIndicator("Test")
                .setContent(new Intent().setClass(this, TestActivity.class)));
    }
}
