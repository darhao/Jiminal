package cc.darhao.jiminal.entity;

import cc.darhao.jiminal.annotation.Parse;
import cc.darhao.jiminal.annotation.Protocol;
import cc.darhao.jiminal.constant.JustForTestClientDevice;
import cc.darhao.jiminal.constant.JustForTestControlResult;
import cc.darhao.jiminal.constant.JustForTestReturnCode;

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
