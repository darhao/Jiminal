package com.jimi.psh.entity;

import java.util.Date;

import com.jimi.psh.annotation.Parse;
import com.jimi.psh.annotation.Protocol;
import com.jimi.psh.constant.JustForTestLine;

@Protocol(0x4C)
public class JustForTestLoginReplyPackage extends BasePackage {
	
	@Parse({0,1})
	private JustForTestLine justForTestLine;
	@Parse({1,4})
	private Date timestamp;

	public JustForTestLine getLine() {
		return justForTestLine;
	}

	public void setLine(JustForTestLine justForTestLine) {
		this.justForTestLine = justForTestLine;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
}
