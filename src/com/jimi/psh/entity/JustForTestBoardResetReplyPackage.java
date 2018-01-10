package com.jimi.psh.entity;

import com.jimi.psh.annotation.Parse;
import com.jimi.psh.annotation.Protocol;
import com.jimi.psh.constant.JustForTestClientDevice;
import com.jimi.psh.constant.JustForTestControlResult;
import com.jimi.psh.constant.JustForTestReturnCode;

@Protocol(0x52)
public class JustForTestBoardResetReplyPackage extends BasePackage {

	@Parse({ 0, 1 })
	private JustForTestClientDevice justForTestClientDevice;
	@Parse({ 1, 1 })
	private JustForTestControlResult justForTestControlResult;
	@Parse({ 2, 1 })
	private JustForTestReturnCode justForTestReturnCode;

	public JustForTestClientDevice getClientDevice() {
		return justForTestClientDevice;
	}

	public void setClientDevice(JustForTestClientDevice justForTestClientDevice) {
		this.justForTestClientDevice = justForTestClientDevice;
	}

	public JustForTestReturnCode getReturnCode() {
		return justForTestReturnCode;
	}

	public void setReturnCode(JustForTestReturnCode justForTestReturnCode) {
		this.justForTestReturnCode = justForTestReturnCode;
	}

	public JustForTestControlResult getControlResult() {
		return justForTestControlResult;
	}

	public void setControlResult(JustForTestControlResult justForTestControlResult) {
		this.justForTestControlResult = justForTestControlResult;
	}

}
