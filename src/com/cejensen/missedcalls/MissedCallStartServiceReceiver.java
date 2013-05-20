package com.cejensen.missedcalls;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MissedCallStartServiceReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("MissedCallStartServiceReceiver", "onReceive");
		Intent service = Root.getSingleton().getOrStartRunningService(context);
		context.startService(service);
	}

}
