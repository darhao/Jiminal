package com.jimi.psh.entity;

import com.jimi.psh.annotation.Parse;
import com.jimi.psh.annotation.Protocol;
import com.jimi.psh.constant.JustForTestBoardResetReson;
import com.jimi.psh.constant.JustForTestClientDevice;
import com.jimi.psh.constant.JustForTestLine;

@Protocol(0x52)
public class JustForTestBoardResetPackage extends Package {
	
	@Parse({0,1})
	private JustForTestClientDevice justForTestClientDevice;
	@Parse({1,1})
	private JustForTestLine justForTestLine;
	@Parse({2,1})
	private JustForTestBoardResetReson justForTestBoardResetReson;
	
	public JustForTestClientDevice getClientDevice() {
		return justForTestClientDevice;
	}
	public void setClientDevice(JustForTestClientDevice justForTestClientDevice) {
		this.justForTestClientDevice = justForTestClientDevice;
	}
	public JustForTestLine getLine() {
		return justForTestLine;
	}
	public void setLine(JustForTestLine justForTestLine) {
		this.justForTestLine = justForTestLine;
	}
	public JustForTestBoardResetReson getBoardResetReson() {
		return justForTestBoardResetReson;
	}
	public void setBoardResetReson(JustForTestBoardResetReson justForTestBoardResetReson) {
		this.justForTestBoardResetReson = justForTestBoardResetReson;
	}


}
