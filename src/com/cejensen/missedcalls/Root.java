package com.cejensen.missedcalls;

import android.content.Context;
import android.content.Intent;
import android.os.Messenger;
import android.util.Log;

public class Root {
	public static final String	logMessenger		= "LOGMESSENGER";
	private static final Root		singleton				= new Root();
	private LogHandler					logHandler			= new LogHandler();
	private Intent							runningService	= null;

	public static Root getSingleton() {
		return singleton;
	}

	public LogHandler getLogHandler() {
		return logHandler;
	}

	public Intent getOrStartRunningService(Context context) {
		if (runningService == null) {
			runningService = new Intent(context, MissedCallService.class);
			Messenger messenger = new Messenger(logHandler);
			runningService.putExtra(logMessenger, messenger);
			Log.d("getOrStartRunningService", "service created: " + runningService.hashCode());
		}
		return runningService;
	}

	public void setServiceStopped() {
		runningService = null;
	}

}
