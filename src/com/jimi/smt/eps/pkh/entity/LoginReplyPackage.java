package com.jimi.smt.eps.pkh.entity;

import java.util.Date;

public class LoginReplyPackage extends Package {

	private String line;
	
	private Date timestamp;

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
}
