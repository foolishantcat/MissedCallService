package com.cejensen.missedcalls;

import java.util.Date;

public class LogEntry {
	private long m_id;
	private Date m_date;
	private String m_logText;

	public LogEntry(Date date, String logText) {
		m_id = 0;
		m_date = date;
		m_logText = logText;
	}

	public LogEntry(long id, Date date, String logText) {
		this(date, logText);
		m_id = id;
		m_date = date;
		m_logText = logText;
	}

	public long getId() {
		return m_id;
	}

	public Date getDate() {
		return m_date;
	}

	public String getLogText() {
		return m_logText;
	}

	public String toString() {
		return Utils.getFormattedDateTime(m_date) + " : " + m_logText;
	}
}
