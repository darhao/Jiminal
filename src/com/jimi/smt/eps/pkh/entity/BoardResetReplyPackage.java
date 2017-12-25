package com.jimi.smt.eps.pkh.entity;

import com.jimi.smt.eps.pkh.constant.ClientDevice;
import com.jimi.smt.eps.pkh.constant.ReturnCode;

public class BoardResetReplyPackage extends Package {
	
	private ClientDevice clientDevice;

	private boolean isSucceed;

	private ReturnCode returnCode;

	public ClientDevice getClientDevice() {
		return clientDevice;
	}

	public void setClientDevice(ClientDevice clientDevice) {
		this.clientDevice = clientDevice;
	}

	public boolean isSucceed() {
		return isSucceed;
	}

	public void setSucceed(boolean isSucceed) {
		this.isSucceed = isSucceed;
	}

	public ReturnCode getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(ReturnCode returnCode) {
		this.returnCode = returnCode;
	}
	
}
