package cc.darhao.jiminal.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Test;

import cc.darhao.jiminal.callback.OnConnectedListener;
import cc.darhao.jiminal.callback.OnPackageArrivedListener;
import cc.darhao.jiminal.callback.OnReplyPackageArrivedListener;
import cc.darhao.jiminal.constant.JustForTestClientDevice;
import cc.darhao.jiminal.constant.JustForTestControlResult;
import cc.darhao.jiminal.constant.JustForTestControlledDevice;
import cc.darhao.jiminal.constant.JustForTestLine;
import cc.darhao.jiminal.constant.JustForTestOperation;
import cc.darhao.jiminal.constant.JustForTestReturnCode;
import cc.darhao.jiminal.core.AsyncCommunicator;
import cc.darhao.jiminal.core.SyncCommunicator;
import cc.darhao.jiminal.entity.BasePackage;
import cc.darhao.jiminal.entity.JustForTestControlPackage;
import cc.darhao.jiminal.entity.JustForTestControlReplyPackage;
import cc.darhao.jiminal.util.FieldUtil;

/**
 * 套接字单元测试
 * <br>
 * <b>2017年12月29日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class SocketTest {

	private final static String packagePath = "cc.darhao.jiminal.entity";
	
	private JustForTestControlPackage justForTestControlPackage;

	private JustForTestControlReplyPackage justForTestControlReplyPackage;

	
	@Test
	public void retryTest() throws InterruptedException {
		CountDownLatch cdl = new CountDownLatch(1); 
		AsyncCommunicator asyncCommunicator = new AsyncCommunicator("127.0.0.1", 23334, 23334, packagePath);
		asyncCommunicator.startServer(new OnPackageArrivedListener() {
			
			@Override
			public void onPackageArrived(BasePackage p, BasePackage r) {
				try {
					//模拟丢包（概率65%）
					double a = 0;
					if((a = Math.random()) < 0.65) {
						System.out.println("服务器模拟丢包完成，随机数" + a);
						Thread.sleep(3600 * 1000);
					}
					System.out.println("服务器模拟回复完成，随机数" + a);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(p instanceof JustForTestControlPackage) {
					justForTestControlReplyPackage = (JustForTestControlReplyPackage) r;
					justForTestControlReplyPackage.setClientDevice(((JustForTestControlPackage) p).getClientDevice());
					justForTestControlReplyPackage.setControlResult(JustForTestControlResult.SUCCEED);
					justForTestControlReplyPackage.setReturnCode(JustForTestReturnCode.SUCCEED);
				}else {
					Assert.fail("不是Control包");
				}
			}
			
			@Override
			public void onCatchIOException(IOException exception) {
				Assert.fail();
			}
		});
		//构建包
		justForTestControlPackage = new JustForTestControlPackage();
		justForTestControlPackage.setClientDevice(JustForTestClientDevice.APP);
		justForTestControlPackage.setLine(JustForTestLine.L305);
		justForTestControlPackage.setControlledDevice(JustForTestControlledDevice.ALARM);
		justForTestControlPackage.setOperation(JustForTestOperation.ON);
		asyncCommunicator.connect(new OnConnectedListener() {
			
			@Override
			public void onSucceed() {
				cdl.countDown();
			}
			
			@Override
			public void onFailed(IOException e) {
				Assert.fail();
			}
		});
		cdl.await();
		CountDownLatch cdl2 = new CountDownLatch(1);
		asyncCommunicator.setMaxRetryTimes(999);
		asyncCommunicator.setTimeout(2000);
		asyncCommunicator.send(justForTestControlPackage, new OnReplyPackageArrivedListener() {
			
			@Override
			public void onReplyPackageArrived(BasePackage r) {
				Assert.assertEquals(FieldUtil.md5(justForTestControlReplyPackage), FieldUtil.md5(r));
				cdl2.countDown();
			}
			
			@Override
			public void onCatchIOException(IOException exception) {
				exception.printStackTrace();
				Assert.fail();
			}
		});
		cdl2.await();
		asyncCommunicator.close();
	}
	
	
	@Test
	public void pressureTest() throws InterruptedException {
		//初始化
		AsyncCommunicator asyncCommunicator = new AsyncCommunicator("127.0.0.1", 23333, 23333, packagePath);
		//基础测试
		baseCommunicate(asyncCommunicator);
		//压力测试30秒
		Date now = new Date();
		while(new Date().getTime() - now.getTime() < 30 * 1000) {
			pressure();
			Thread.sleep(3000);
		}
		asyncCommunicator.close();
	}
	
	
	private void pressure() throws InterruptedException {
		//线程数量
		int quantity = 128;
		//发送间隔（毫秒）
		long delayTime = 10;
		CountDownLatch cdl = new CountDownLatch(quantity); 
		OnConnectedListener onConnectedListener = new OnConnectedListener() {
			
			@Override
			public void onSucceed() {
				cdl.countDown();
			}
			
			@Override
			public void onFailed(IOException e) {
				Assert.fail();
			}
		};
		
		List<AsyncCommunicator> asyncCommunicators = new ArrayList<AsyncCommunicator>();
		for (int i = 0; i < quantity; i++) {
			AsyncCommunicator communicator2 = new AsyncCommunicator("127.0.0.1", 23333, packagePath);
			communicator2.connect(onConnectedListener);
			asyncCommunicators.add(communicator2);
		}
		cdl.await();
		CountDownLatch cdl2 = new CountDownLatch(quantity); 
		OnReplyPackageArrivedListener onReplyPackageArrivedListener = new OnReplyPackageArrivedListener() {
			
			@Override
			public void onReplyPackageArrived(BasePackage r) {
				Assert.assertEquals(FieldUtil.md5(justForTestControlReplyPackage), FieldUtil.md5(r));
				cdl2.countDown();
			}
			
			@Override
			public void onCatchIOException(IOException exception) {
				Assert.fail();
			}
		};
		for (AsyncCommunicator communicator3 : asyncCommunicators) {
			communicator3.send(justForTestControlPackage, onReplyPackageArrivedListener);
			Thread.sleep(delayTime);
		}
		cdl2.await();
		for (AsyncCommunicator communicator4 : asyncCommunicators) {
			communicator4.close();
		}
	}
	
	
	private void baseCommunicate(AsyncCommunicator asyncCommunicator) throws InterruptedException {
		CountDownLatch cdl = new CountDownLatch(1);
		
		//构建包
		justForTestControlPackage = new JustForTestControlPackage();
		justForTestControlPackage.setClientDevice(JustForTestClientDevice.APP);
		justForTestControlPackage.setLine(JustForTestLine.L305);
		justForTestControlPackage.setControlledDevice(JustForTestControlledDevice.ALARM);
		justForTestControlPackage.setOperation(JustForTestOperation.ON);
		
		//预期抛出异常
		asyncCommunicator.send(justForTestControlPackage, new OnReplyPackageArrivedListener() {
			
			@Override
			public void onReplyPackageArrived(BasePackage r) {
				Assert.fail();
			}
			
			@Override
			public void onCatchIOException(IOException exception) {
				System.out.println(" '客户端未连接' 异常捕捉成功");
				cdl.countDown();
			}
		});
		cdl.await();
		
		//正常测试发送
		CountDownLatch cdl2 = new CountDownLatch(1);
		asyncCommunicator.startServer(new OnPackageArrivedListener() {
			
			@Override
			public void onPackageArrived(BasePackage p, BasePackage r) {
				if(p instanceof JustForTestControlPackage) {
					justForTestControlReplyPackage = (JustForTestControlReplyPackage) r;
					justForTestControlReplyPackage.setClientDevice(((JustForTestControlPackage) p).getClientDevice());
					justForTestControlReplyPackage.setControlResult(JustForTestControlResult.SUCCEED);
					justForTestControlReplyPackage.setReturnCode(JustForTestReturnCode.SUCCEED);
				}else {
					Assert.fail("不是Control包");
				}
			}
			
			@Override
			public void onCatchIOException(IOException e) {
				e.printStackTrace();
				Assert.fail();
			}
		});
		
		asyncCommunicator.connect(new OnConnectedListener() {
			
			@Override
			public void onSucceed() {
				cdl2.countDown();
			}
			
			@Override
			public void onFailed(IOException e) {
				e.printStackTrace();
				Assert.fail();
			}
		});
		
		cdl2.await();
		CountDownLatch cd3 = new CountDownLatch(1);
		
		asyncCommunicator.send(justForTestControlPackage, new OnReplyPackageArrivedListener() {
			
			@Override
			public void onReplyPackageArrived(BasePackage r) {
				Assert.assertEquals(FieldUtil.md5(justForTestControlReplyPackage), FieldUtil.md5(r));
				cd3.countDown();
			}
			
			@Override
			public void onCatchIOException(IOException e) {
				e.printStackTrace();
				Assert.fail();
			}
		});
		cd3.await();
	}
	
	
	@Test
	public void syncTest() throws IOException {
		SyncCommunicator syncCommunicator = new SyncCommunicator("127.0.0.1", 23335, 23335, packagePath);
		syncCommunicator.setMaxRetryTimes(999);
		syncCommunicator.setTimeout(2000);
		new Thread() {
			public void run() {
				syncCommunicator.startServer(new OnPackageArrivedListener() {
					
					@Override
					public void onPackageArrived(BasePackage p, BasePackage r) {
						//模拟丢包（概率65%）
						double a = 0;
						if((a = Math.random()) < 0.65) {
							System.out.println("服务器模拟丢包完成，随机数" + a);
							try {
								Thread.sleep(3600 * 1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						System.out.println("服务器模拟回复完成，随机数" + a);
						if(p instanceof JustForTestControlPackage) {
							justForTestControlReplyPackage = (JustForTestControlReplyPackage) r;
							justForTestControlReplyPackage.setClientDevice(((JustForTestControlPackage) p).getClientDevice());
							justForTestControlReplyPackage.setControlResult(JustForTestControlResult.SUCCEED);
							justForTestControlReplyPackage.setReturnCode(JustForTestReturnCode.SUCCEED);
						}else {
							Assert.fail("不是Control包");
						}
					}
					
					@Override
					public void onCatchIOException(IOException exception) {
						exception.printStackTrace();
						Assert.fail();
					}
				});
			};
		}.start();
		syncCommunicator.connect();
		System.out.println("同步连接服务器成功");
		//构建包
		justForTestControlPackage = new JustForTestControlPackage();
		justForTestControlPackage.setClientDevice(JustForTestClientDevice.APP);
		justForTestControlPackage.setLine(JustForTestLine.L305);
		justForTestControlPackage.setControlledDevice(JustForTestControlledDevice.ALARM);
		justForTestControlPackage.setOperation(JustForTestOperation.ON);
		BasePackage a = syncCommunicator.send(justForTestControlPackage);
		Assert.assertEquals(FieldUtil.md5(justForTestControlReplyPackage), FieldUtil.md5(a));
		System.out.println("同步发包1成功");
		BasePackage b = syncCommunicator.send(justForTestControlPackage);
		Assert.assertEquals(FieldUtil.md5(justForTestControlReplyPackage), FieldUtil.md5(b));
		System.out.println("同步发包2成功");
		BasePackage c = syncCommunicator.send(justForTestControlPackage);
		Assert.assertEquals(FieldUtil.md5(justForTestControlReplyPackage), FieldUtil.md5(c));
		System.out.println("同步发包3成功");
	}
	
}
