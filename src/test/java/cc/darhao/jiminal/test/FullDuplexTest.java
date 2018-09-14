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
import cc.darhao.jiminal.entity.UpdatePackage;
import cc.darhao.jiminal.entity.UpdateReplyPackage;
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
	
	private int sum;
	
	
	private void before() throws InterruptedException {
		System.out.println("===============开始全双工测试===============");
		socketConfig = new SocketConfig();
		socketConfig.setMaxReplyTime(999999);
		createServer();
		createClient();
		clientEndPoint.setSocketConfig(socketConfig);
		new Thread(()-> {
			server.listenConnect();
		}).start();
		clientEndPoint.connect();
	}
	
	
	@Test
	public void test1() throws InterruptedException {
		before();
		PPackage p = new PPackage();
		p.setMsg("你好世界！Hello!");
		clientEndPoint.send(p);
		Thread.sleep(2000);
		Assert.assertEquals("你好世界！Hello!+1", msg);
		QPackage q = new QPackage();
		q.setMsg("FF FF 0D 0A");
		serverEndPoint.send(q);
		Thread.sleep(2000);
		Assert.assertEquals(100, age);
	}
	
	
	@Test
	public void test2() throws InterruptedException {
		before();
		for (int i = 0; i < 32; i++) {
			UpdatePackage updatePackage = new UpdatePackage();
			updatePackage.setFaceName("");
			updatePackage.setFingerName("");
			updatePackage.setHeadName("");
			updatePackage.setMd5("");
			updatePackage.setSuffixTime("");
			clientEndPoint.send(updatePackage);
		}
		Thread.sleep(1000);
		Assert.assertEquals(32, sum);
	}
	
	
	private void createServer() {
		PackageConfig packageConfig = new PackageConfig();
		packageConfig.add(QPackage.class, true);
		packageConfig.add(PPackage.class, false);
		packageConfig.add(UpdatePackage.class, false);
		server = new JiminalServer(5017, packageConfig, new JiminalServerCallback() {
			
			@Override
			public void onPackageArrived(BasePackage p, BasePackage r, Jiminal session) {
				if (p instanceof PPackage) {
					PPackage pPackage = (PPackage) p;
					String string = pPackage.getMsg();
					PReplyPackage pReplyPackage = (PReplyPackage) r;
					pReplyPackage.setMsg(string + "+1");
				}
				if(p instanceof UpdatePackage) {
					UpdateReplyPackage updateReplyPackage  = (UpdateReplyPackage) r;
					updateReplyPackage.setResultCode(20);
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
		packageConfig.add(UpdatePackage.class, true);
		clientEndPoint = new Jiminal("127.0.0.1", 5017, packageConfig, new JiminalCallback() {
			
			@Override
			public void onPackageArrived(BasePackage p, BasePackage r, Jiminal session) {
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
				if (r instanceof PReplyPackage) {
					PReplyPackage pReplyPackage = (PReplyPackage) r;
					msg = pReplyPackage.getMsg();
				}
				if (r instanceof UpdateReplyPackage) {
					sum++;
				}
			}

			@Override
			public void onConnected() {
				System.out.println("客户端：已连接服务器");
			}
		});
	} 
}
