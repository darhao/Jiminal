# Jiminal2.0 开发指南 #
## 前言 ##


- 本指南基于Jiminal1.x进行编写，新版SDK保留了1.x的协议格式，所以关于BasePackage的说明不再赘述，请参考：[https://github.com/darhao/Jiminal/wiki/About-Jiminal-1.x](https://github.com/darhao/Jiminal/wiki/About-Jiminal-1.x "传送门")  
  


- 使用方法更符合人体工程学，仿制了java.net.socket包下的设计模式。因为实现了全双工通讯，所以在使用前需要配置包的“来龙去脉”。  

- 移除鸡肋的三次重连机制，改为一定时间内收不到回复包就断开连接并跑出异常的干练机制。

##  配置包的“来龙去脉” ##
使用PackageConfig对象来保存一种“某包是否是我方主动发送”的映射，并且把这个映射绑定到一个Jiminal对象中。  
>     PackageConfig packageConfig = new PackageConfig();
>     packageConfig.add(QPackage.class, true);
>     packageConfig.add(PPackage.class, false);  


具体参考文末API文档。


## 创建服务器端 ##
服务器端是用来监听客户端连接并实例化Jiminal对象的，类似Java API里的ServerSocket方法。创建一个服务器端实例，需要提供3个参数：  

- 监听端口  
- packageConfig对象  
- 服务器端回调对象  

第1、2个对象不需要多讲，第3个对象是一个接口，稍后讲解。  
构造出服务器端对象JiminalServer后，即可调用它的listenConnect()方法开始监听客户端连接了！  
> 该方法类似Java API里的ServerSocket.accpet()法，也是一个阻塞方法。  


## 创建客户端 ##
客户端的创建方法也很简单，直接使用构造函数即可，需要提供4个参数：

- 服务器IP
- 服务器端口  
- packageConfig对象  
- 客户端回调对象   

前三个参数不多讲，最后一个参数是一个接口，稍后讲解。  
构造出客户端对象Jiminal后，即可调用它的connect()方法进行连接服务器了！  

## 回调对象介绍 ##
Jiminal2.0使用了回调的设计模式进行开发。一共有两种回调对象：

- 客户端回调对象 JiminalCallback
	- onConnect():连接上服务器时回调
- 服务器回调对象 JiminalServerCallback
	- onCatchClient(Jiminal session):监听到客户端连接时回调，参数为连入的客户端实体  

而它们均继承于JiminalBaseCallback，而这个接口由3个方法组成：

	/**
	 * 包到达时调用
	 * @param p 对方发来的包
	 * @param r 在该方法返回时，我方会回复对方的包
	 * @param session 收到包的会话
	 */
	 void onPackageArrived(BasePackage p, BasePackage r, Jiminal session);
	 

	/**
	 * 回复包到达时调用
	 * @param r 对方发来的回复包
	 * @param session 收到包的会话
	 */
	 void onReplyArrived(BasePackage r, Jiminal session);
	 
	 
	/**
	 * 会话遇到异常时调用
	 * @param e 异常实体
	 * @param session 产生异常的会话
	 */
	 void onCatchException(Exception e, Jiminal session);


## 收发信息 ##
发送信息只需要调用Jiminal.send()方法传入包即可；连接成功时，Jiminal会创建接收信息的子线程，收到信息时，子线程会回调onPackageArrived()方法。

## 其他配置 ##
在任何时候都可以修改Jiminal的一些其他配置，如发送包后最大等待回复包的时间，以及一些开始标志和结束标志等参数。只需要调用Jiminal.setSocketConfig()方法即可。


> 获取Jiminal2.0  
[https://github.com/darhao/Jiminal/releases](https://github.com/darhao/Jiminal/releases "发布页面")

=====================================1.x分割线======================================

* # Jiminal自我介绍
你有socket编程的需求吗？  
你在为协议的设计而烧脑吗？  
你在为报文类型的变动而烦恼吗？  
你还在写着又长又臭的通讯报文解析代码吗？  
如果有，那么恭喜你，你是这篇文章的读者了。  
**Jiminal是一个基于socket的通讯协议，也是一个java开源框架。**  
> Jimianl的名字起源于单词Terminal，意为终端。  

***

* # 你能用Jiminal做的事情  
1. 设计基于Jiminal的通讯报文，以下简称为“包”。  
2. 调用内置的api构造客户端（服务端）进行socket通信。

***

* # 开启Jiminal之旅：蛋糕店老板悲伤的故事  
**让我们从一个故事开始讲起：**  
>&emsp;&emsp;有一天，小杨要去给朋友过生日，于是在蛋糕店订了个蛋糕。第二天，蛋糕店老板出了车祸不幸去世了（我很抱歉），小杨的订单只能取消了，于是蛋糕店老板的儿子通知了小杨。  

OK，故事到此结束，虽然这听上去是一个悲伤的故事，但你无须在此为老板默哀三分钟，这交给笔者就好，让我们继续...  
**分析：**   
在这个故事里，有两个动作：  
1. 小杨下订单  
2. 蛋糕店老板儿子通知小杨，订单因为某些原因被取消了 
 
**翻译一下就变成了：**
1. 小杨发送了订单包OrderPackage
2. 蛋糕店老板儿子发送了一个通知包给小杨MessagePackage  
> **Jiminal规矩：包的类名必须以"Package"结尾**  
  

***

* # 小插曲：什么是包  
与其了解什么是包不如先了解Jiminal协议，
因为包就是根据该协议描述出来的一种实体。在该协议中，包分为两种：**发送包和回复包**。它们在一次**发送/回复**过程中成对存在。更多协议规则在后文中会详细描述。  
> Jiminal协议包格式  
> 1. 开始标识：2个字节，作为包的开头  
> 2. 包长度：1个字节，描述了包的长度，即包括下文的3，4，5，6部分  
> 3. 子协议号：1个字节，描述了包的类型，发送包和回复包的子协议号一致  
> 4. 包正文：不定长度，是包的主体部分  
> 5. 信息序列号：2个字节，自动递增的流水号，发送包和回复包信息序列号一致  
> 6. CRC校验码：2个字节，为上文的2，3，4，5部分的计算结果  
> 7. 结束标识：2个字节，作为包的尾部  

那么问题来了，如何编写订单包（OrderPackage）呢？很简单，既然Jiminal是Java的开源框架，那么一切都将从一个类开始 —— **BasePackage类**。  
先来目睹一下BasePackage的五脏六腑吧：  
```
/**
 * 通讯协议包基类
 */
public class BasePackage {

	/**
	 * 包长度
	 */
	public byte length;
	/**
	 * 子协议号
	 */
	public String protocol = "";
	/**
	 * 信息序列号
	 */
	public Short serialNo;
	/**
	 * CRC校验码
	 */
	public Short crc;
	
	/**
	 * 发送者ip
	 */
	public String senderIp = "";
	
	/**
	 * 接受者ip
	 */
	public String receiverIp = "";
	
}
```  
通过上面的类结构，我们很快能发现它和之前说过的协议格式很类似，**其实它就是协议中除正文以外的内容！**

***

* # 让我们先来写个蛋糕订单
好了，不管你看没看懂，都无伤大雅，因为这并不影响你继续使用Jiminal。接下来，我们要做的事情，就是**继承BasePackage类，编写OrderPackage类**。  
聪明的你也许已经猜到了，我们缺少的正是包正文部分！下面我们将在OrderPackage类描述包的正文部分：  
> 根据实际情况，正文大概会有如下信息：
>1. 蛋糕品种
>2. 蛋糕重量（单位：克，最大：10,000g）
>3. 蛋糕上要写的文字（最多10个字）
>4. 是否需要送货上门服务
>5. 是否需要发票
>6. 制作日期  

下面我们把订单信息协议化，设计出如下包正文结构：（后文中长度单位均为字节）  
>1. type 枚举类型 长度1
>2. weight 无符号整型 长度2
>3. text 字符串 长度64
>4. isDoorToDoor 布尔型 长度1/8
>5. isNeedInvoice 布尔型 长度1/8
>6. cookTime 日期时间 长度4  

那么得到的OrderPackage类会是这个样子：  
```
/**
 * 蛋糕订单类
 */
@Protocol(0x79)
public class OrderPackage extends BasePackage{

	@Parse({0,1})
	private CakeType type;

	@Parse({1,2})
	private int weight;

	@Parse(value={3,64}, utf8=true)
	private String text;

	@Parse({67,0})
	private boolean isDoorToDoor;

	@Parse({67,1})
	private boolean isNeedInvoice;

	@Parse({68,4})
	private DateTime cookTime;
	
	//忽略getters、setters 和 构造方法...
}

/**
 * 蛋糕品种枚举
 */
public enum CakeType{
	Cream, Cheese, Sugar, Mousse
}
```  
我知道你已经有一堆问题了：  
* Protocol注解里面是啥？
* Parse注解里面又是啥？
* 各个属性的数据类型又是怎么决定的？  

别急，下一章跟你解释...  

***

* # 正经话题：正文属性的注解和数据类型  
**Protocol注解：**  
>&emsp;&emsp;它的类型是一个byte, 用于存储这个包的子协议号，对应父类BasePackage的protocol属性。这个值可以随意设置，在蛋糕订单包中我们取"Order"的首字母的ASCII码0x79作为子协议号。  
 
**Parse注解：**  
话不多说，上该注解的源码：  
```
/**
 * 用于注解Package的子类的属性
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface Parse{
	
	/**
	 * 接收一个长度为2的数组第一个元素为该字段对应数据包信息内容的第一个字节位置（从0算起）
	 * 第二个元素为该字段的字节长度
	 * PS：如果字段类型为布尔型，则第一个元素表示该布尔值对应数据包的信息内容字节位置（从0算起）
	 * 第二个元素为该值对应字节的bit位置（从0算起，第一位为最右边）
	 */
	int[] value();
	
	/**
	 * 只对类型为int的字段有效，true表示为有符号，false为无符号，默认为false
	 * 注意：只对32位以下数值有效，32位以上会表现为有符号
	 */
	boolean sign() default false;
	
	
	/**
	 * 只对类型为String的字段有效，true表示为该字符串会用UTF-8码解析成文本，
	 * false表示该字符串将直接表示为一串十六进制的哈希字符串，默认为false
	 */
	boolean utf8() default false;
}
```  
**协议支持的正文数据类型有7种：**  
>1. 有符号整型，长度1 ~ 4 (类型int + Parse注解sign为true)
>2. 无符号整型，长度1 ~ 4 (类型int)
>3. 哈希字符串，长度任意 (类型String)
>4. UTF8编码字符串，长度任意 (类型String + Parse注解utf8为true)
>5. 布尔型，长度1/8 (类型boolean)
>6. 日期时间型，长度4 (类型DateTime)
>7. 枚举类型，长度1 ~ 4 (类型:枚举类名字)  

OK,我想你现在应该能知道OrderPackage为什么要那样编写了吧！同理，我相信你也能照葫芦画瓢地写出CancelPackage！  

***

* # 让我们把订单寄给老板  
在此之前，我们先来了解一个类 —— **Communicator**。我们通过这个类，可以与远程设备的socket进行连接，以及收发包。  
>这个类还有一些属性可以配置：  
>1. 超时时间：setTimeout(int timeout) 默认5000ms
>2. 最大重试次数：setMaxRetryTimes(int maxRetryTimes) 默认3次
>3. 开始标识：setStartFlags(int b1, int b2) 默认0x80,0x80
>4. 结束标识：setEndFlags(int b1, int b2) 默认0x0D,0x80A
>5. 结束标识去语义标识：setEndValidFlags(int b1, int b2) 默认0xFF,0xFF
>>所谓**结束标识去语义标识**，即是如果正文中出现结束标志位本身，则会用该去语义标志作为前缀，插入到那些存在正文中的结束标志位之前，起到**注释**作用，避免正文被截断  

由于Communicator类是一个抽象类，所以必须使用它的两个子类 —— **AsyncCommnuicator 或 SyncCommunicator** 中的一个进行实例化。  
>这两个子类的唯一区别就是：异步和同步的区别。异步的类会在操作完成时回调监听器，不会阻塞当前线程；同步的类会在操作完成时返回，在此之前会阻塞当前线程。  
OK，现在假设蛋糕店IP地址是**20.20.20.20**，端口是**22222**，你之前写的OrderPackage等包放在了**com.cake.pack**路径中，那么我们来下个单吧：
```
SyncCommunicator comm = new SyncCommunicator("20.20.20.20", 22222, "com.cake.pack");
comm.connect();
OrderPackage order = new OrderPackage(CakeType.Cream, 4000, "生日快乐，小叶", true, true, new Date());
OrderReplyPackage reply (OrderReplyPackage) comm.send(order);
if(reply.isSucceed()){
  System.out.println("下单成功");
}else{
  System.out.println("下单失败");
}
```  
心细的你也许早已发现，此处多了一个类 —— **OrderReplyPackage**,这个类是OrderPackage的对应的回复包类，该类的成员变量不再赘述，简单地理解为只有一个结果，即是否下单成功。  
> **Jiminal规矩：发送包与回复包必须同时定义，若发送包类名为XXXPackage，则回复包类名必须为XXXReplyPackage**  

***

* # 不好意思，您要的蛋糕我们做不了了  
>&emsp;&emsp;世事难料，虽然下单成功了，但是不一定能真的把蛋糕做出来，中间还会遇到各种事故。蛋糕店老板的儿子整理完自己的情绪后，开始给客户们一一发送暂时停业道歉信。假设小杨料到某些事情会发生，于是他早已部署一台专门用来接收外部消息的电脑，ip为**20.20.20.21**,端口是**21212**，MessagePackage类的路径为**com.cake.pack**，接收外部消息的程序核心代码为：  
```
SyncCommunicator comm = new SyncCommunicator(21212, "com.cake.pack");
comm.startServer(new OnPackageArrivedListener(){
  
  @Override
  public void onPackageArrived(BasePackage p, BasePackage r) {
    if(p instanceof MessagePackage) {
      MessagePackage msgP = (MessagePackage)p;
      MessageReplyPackage msgR = (MessageReplyPackage)r;
      System.out.println(msgP.getMsg());
      msgR.setMsg("消息已收到！");
    }
  }
  
  //忽略其他方法的实现...

});
```  
由于小杨已经向社区公开了自己的ip和端口，这样，蛋糕店就能主动发消息给小杨了。 

***
* # 后记：关于Jiminal的一些默认行为  
1. 在双方都使用Jiminal的情况下，如果发送方没有接收到回复包，则表示以下几种情况之一：  
> 1. 发送方未发送成功，原因是包序列化时出错
> 2. 接收方网络原因
> 3. 接收方已接收到包，但包解析过程中出现错误
> 4. 发送方网络原因  
2. 发送方在发送完包的最后一个字节到收到回复包的第一个字节之间的时间如果大于**超时时间**（默认5秒），则会**重新与接收方连接**，并重新发包，直到成功或达到**最大重试次数**（默认3次）为止。  
> **更多API请下载源代码后参考Javadoc**  
> **如果要直接使用请：[点击下载Jar包](https://github.com/darhao/Jiminal/releases/tag/1.0-Releases)**  
