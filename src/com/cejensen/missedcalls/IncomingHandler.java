package com.cejensen.missedcalls;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

class IncomingHandler extends Handler {
	private final MissedCallActivity mMissedCallActivity;
	
	public IncomingHandler(MissedCallActivity missedCallActivity) {
		mMissedCallActivity = missedCallActivity;
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case MissedCallService.MSG_LOG_TO_CLIENT:
			Bundle data = msg.getData();
			LogEntry le = (LogEntry) data.getSerializable(MissedCallService.KEY_LOGENTRY);
			mMissedCallActivity.logText(le);
			break;
		default:
			super.handleMessage(msg);
		}
	}
}
