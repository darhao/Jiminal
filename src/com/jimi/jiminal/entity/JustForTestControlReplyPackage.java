package com.jimi.jiminal.entity;

import com.jimi.jiminal.annotation.Parse;
import com.jimi.jiminal.annotation.Protocol;
import com.jimi.jiminal.constant.JustForTestClientDevice;
import com.jimi.jiminal.constant.JustForTestControlResult;
import com.jimi.jiminal.constant.JustForTestReturnCode;

@Protocol(0x43)
public class JustForTestControlReplyPackage extends BasePackage {

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
