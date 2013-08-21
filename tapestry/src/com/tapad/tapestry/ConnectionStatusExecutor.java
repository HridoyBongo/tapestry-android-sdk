package com.tapad.tapestry;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

/**
 * Only executes Runnables when a network connection becomes available.
 */
public class ConnectionStatusExecutor {
	private static final ExecutorService executor = Executors.newFixedThreadPool(2);
	private final Queue<Runnable> requestQueue = new ArrayBlockingQueue<Runnable>(10000);
	private final AtomicBoolean isConnected = new AtomicBoolean(true);

	public void execute(Runnable r) {
		if (isConnected.get())
			executor.execute(r);
		else
			requestQueue.offer(r);
	}

	public ConnectionStatusExecutor(Context context) {
		BroadcastReceiver onConnectivityChangedReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				isConnected.set(!intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false));
				while (isConnected.get() && !requestQueue.isEmpty())
					executor.execute(requestQueue.remove());
				Logging.d("Connectivity changed, isConnected=" + isConnected.get());
			}
		};

		try {
			IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
			context.registerReceiver(onConnectivityChangedReceiver, intentFilter);
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			isConnected.set(cm.getActiveNetworkInfo() == null || cm.getActiveNetworkInfo().isConnected());
		} catch (Exception e) {
			Logging.w("Could not get connectivity state", e);
		}
	}
}
