package org.humanoid.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public abstract class NetworkListener extends BroadcastReceiver {

	private transient final ConnectivityManager manager;

	public abstract void onNetworkStateChange(boolean isConnected);

	public NetworkListener(final Context context) {
		super();

		this.manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	public void register(final Context context) {
		context.registerReceiver(this, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));
	}

	public void unregister(final Context context) {
		context.unregisterReceiver(this);
	}

	@Override
	public void onReceive(final Context context, final Intent intent) {
		final NetworkInfo info = manager.getActiveNetworkInfo();
		this.onNetworkStateChange(info != null && info.isAvailable());
	}
}
