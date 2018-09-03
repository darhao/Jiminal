package cc.darhao.jiminal.test;

import org.junit.Assert;
import org.junit.Test;

import cc.darhao.jiminal.callback.JiminalCallback;
import cc.darhao.jiminal.callback.JiminalServerCallback;
import cc.darhao.jiminal.config.PackageConfig;
import cc.darhao.jiminal.config.SocketConfig;
import cc.darhao.jiminal.entity.PPackage;
import cc.darhao.jiminal.entity.PReplyPackage;
import cc.darhao.jiminal.entity.QPackage;
import cc.darhao.jiminal.entity.QReplyPackage;
import cc.darhao.jiminal.pack.BasePackage;
import cc.darhao.jiminal.socket.Jiminal;
import cc.darhao.jiminal.socket.JiminalServer;

/**
 * 全双工测试：测试服务器主动、被动；客户端主动、被动收发功能
 * <br>
 * <b>2018年8月31日</b>
 * @author 几米物联自动化部-洪达浩
 */
public class FullDuplexTest {

	private Jiminal clientEndPoint;
	
	private Jiminal serverEndPoint;
	
	private JiminalServer server;
	
	private SocketConfig socketConfig;

	private int age;
	
	private String msg;
	
	@Test
	public void test() throws InterruptedException {
		System.out.println("===============开始全双工测试===============");
		socketConfig = new SocketConfig();
		socketConfig.setMaxReplyTime(999999);
		createServer();
		createClient();
		clientEndPoint.setSocketConfig(socketConfig);
		new Thread(()-> {
			server.listenConnect();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			server.close();
		}).start();
		clientEndPoint.connect();
		PPackage p = new PPackage();
		p.setMsg("你好世界！Hello!");
		clientEndPoint.send(p);
		Thread.sleep(2000);
		QPackage q = new QPackage();
		serverEndPoint.send(q);
		Thread.sleep(2000);
		clientEndPoint.close();
		serverEndPoint.close();
		Assert.assertEquals("你好世界！Hello!+1", msg);
		Assert.assertEquals(100, age);
		System.out.println("===============全双工测试结束===============");
	}
	
	
	private void createServer() {
		PackageConfig packageConfig = new PackageConfig();
		packageConfig.add(QPackage.class, true);
		packageConfig.add(PPackage.class, false);
		server = new JiminalServer(5017, packageConfig, new JiminalServerCallback() {
			
			@Override
			public void onPackageArrived(BasePackage p, BasePackage r, Jiminal session) {
				System.out.println("服务器收到P包");
				if (p instanceof PPackage) {
					PPackage pPackage = (PPackage) p;
					String string = pPackage.getMsg();
					PReplyPackage pReplyPackage = (PReplyPackage) r;
					pReplyPackage.setMsg(string + "+1");
				}
			}
			
			@Override
			public void onCatchException(Exception e, Jiminal session) {
				e.printStackTrace();
			}
			
			@Override
			public void onCatchClient(Jiminal session) {
				serverEndPoint = session;
				serverEndPoint.setSocketConfig(socketConfig);
				System.out.println("服务端：监听到客户端连接");
			}

			@Override
			public void onReplyArrived(BasePackage r, Jiminal session) {
				System.out.println("服务器收到Q包回复");
				if (r instanceof QReplyPackage) {
					QReplyPackage qReplyPackage = (QReplyPackage) r;
					age = qReplyPackage.getAge();
				}
			}
		});
	}
	
	
	private void createClient() {
		PackageConfig packageConfig = new PackageConfig();
		packageConfig.add(PPackage.class, true);
		packageConfig.add(QPackage.class, false);
		clientEndPoint = new Jiminal("127.0.0.1", 5017, packageConfig, new JiminalCallback() {
			
			@Override
			public void onPackageArrived(BasePackage p, BasePackage r, Jiminal session) {
				System.out.println("客户端收到Q包");
				if (p instanceof QPackage) {
					QReplyPackage qReplyPackage = (QReplyPackage) r;
					qReplyPackage.setAge(100);
				}
			}
			
			@Override
			public void onCatchException(Exception e, Jiminal session) {
				e.printStackTrace();
			}
			
			@Override
			public void onReplyArrived(BasePackage r, Jiminal session) {
				System.out.println("客户端收到P包回复");
				if (r instanceof PReplyPackage) {
					PReplyPackage pReplyPackage = (PReplyPackage) r;
					msg = pReplyPackage.getMsg();
				}
			}

			@Override
			public void onConnected() {
				System.out.println("客户端：已连接服务器");
			}
		});
	} 
}
