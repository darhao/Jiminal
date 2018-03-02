package cc.darhao.jiminal.entity;

import cc.darhao.jiminal.annotation.Parse;
import cc.darhao.jiminal.annotation.Protocol;
import cc.darhao.jiminal.constant.JustForTestClientDevice;
import cc.darhao.jiminal.constant.JustForTestControlResult;
import cc.darhao.jiminal.constant.JustForTestErrorCode;
import cc.darhao.jiminal.core.BasePackage;

@Protocol(0x43)
public class JustForTestControlReplyPackage extends BasePackage {

	@Parse({ 0, 1 })
	private JustForTestClientDevice justForTestClientDevice;
	@Parse({ 1, 1 })
	private JustForTestControlResult justForTestControlResult;
	@Parse({ 2, 1 })
	private JustForTestErrorCode justForTestErrorCode;

	public JustForTestClientDevice getClientDevice() {
		return justForTestClientDevice;
	}

	public void setClientDevice(JustForTestClientDevice justForTestClientDevice) {
		this.justForTestClientDevice = justForTestClientDevice;
	}

	public JustForTestErrorCode getErrorCode() {
		return justForTestErrorCode;
	}

	public void setErrorCode(JustForTestErrorCode justForTestErrorCode) {
		this.justForTestErrorCode = justForTestErrorCode;
	}

	public JustForTestControlResult getControlResult() {
		return justForTestControlResult;
	}

	public void setControlResult(JustForTestControlResult justForTestControlResult) {
		this.justForTestControlResult = justForTestControlResult;
	}

}
