package cc.darhao.jiminal.test;

import org.junit.Assert;
import org.junit.Test;

import cc.darhao.jiminal.callback.JiminalCallback;
import cc.darhao.jiminal.callback.JiminalServerCallback;
import cc.darhao.jiminal.config.PackageConfig;
import cc.darhao.jiminal.entity.PPackage;
import cc.darhao.jiminal.entity.PReplyPackage;
import cc.darhao.jiminal.entity.QPackage;
import cc.darhao.jiminal.pack.BasePackage;
import cc.darhao.jiminal.socket.Jiminal;
import cc.darhao.jiminal.socket.JiminalServer;

/**
 * 干扰测试
 * <br>
 * <b>2018年8月31日</b>
 * @author 几米物联自动化部-洪达浩
 */
public class InterferenceTest {

	private Jiminal clientEndPoint;
	
	private Jiminal serverEndPoint;
	
	private JiminalServer server;
	
	private int i = 0;
	
	
	@Test
	public void test() throws InterruptedException {
		System.out.println("===============开始干扰测试===============");
		createServer();
		createClient();
		new Thread(()-> {
			server.listenConnect();
		}).start();
		clientEndPoint.connect();
		PPackage p = new PPackage();
		p.setMsg("");
		clientEndPoint.send(p);
		Thread.sleep(5000);
		clientEndPoint.send(p);
		Thread.sleep(10000);
		clientEndPoint.close();
		serverEndPoint.close();
		Assert.assertEquals(2, i);
		System.out.println("===============干扰测试结束===============");
	}
	
	
	private void createServer() {
		PackageConfig packageConfig = new PackageConfig();
		packageConfig.add(QPackage.class, true);
		packageConfig.add(PPackage.class, false);
		server = new JiminalServer(5018, packageConfig, new JiminalServerCallback() {
			
			@Override
			public void onPackageArrived(BasePackage p, BasePackage r, Jiminal session) {
				PReplyPackage pReplyPackage = (PReplyPackage) r;
				pReplyPackage.setMsg("");
				System.out.println("服务器收到P包");
				if(i == 0) {
					System.out.println("延迟3秒回复");
					try {
						Thread.sleep(3 * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					i = 1;
				}else if(i == 1){
					System.out.println("延迟7秒回复（相当于不回复）");
					try {
						Thread.sleep(7 * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			@Override
			public void onCatchException(Exception e, Jiminal session) {
				e.printStackTrace();
			}
			
			@Override
			public void onCatchClient(Jiminal session) {
				serverEndPoint = session;
				System.out.println("服务端：监听到客户端连接");
			}

			@Override
			public void onReplyArrived(BasePackage r, Jiminal session) {
			}
		});
	}
	
	
	private void createClient() {
		PackageConfig packageConfig = new PackageConfig();
		packageConfig.add(PPackage.class, true);
		packageConfig.add(QPackage.class, false);
		clientEndPoint = new Jiminal("127.0.0.1", 5018, packageConfig, new JiminalCallback() {
			
			@Override
			public void onPackageArrived(BasePackage p, BasePackage r, Jiminal session) {
			}
			
			@Override
			public void onCatchException(Exception e, Jiminal session) {
				if(e.getMessage().equals("对方未在规定时间内收到正确的回复包，已断开连接")) {
					i = 2;
					System.out.println(e.getMessage());
				}
			}
			
			@Override
			public void onReplyArrived(BasePackage r, Jiminal session) {
				System.out.println("客户端收到P包回复");
			}

			@Override
			public void onConnected() {
				System.out.println("客户端：已连接服务器");
			}
		});
	} 
}
