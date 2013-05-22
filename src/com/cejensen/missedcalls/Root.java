package com.cejensen.missedcalls;


public class Root {
	public static final String	logMessenger		= "LOGMESSENGER";
	private static final Root		singleton				= new Root();

	public static Root getSingleton() {
		return singleton;
	}

}
