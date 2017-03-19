package preapp;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * 接受mq消息队列中的消息，发往cosp
 * */
public class MsgRecvThread implements Runnable{

	static Logger logger = null;
	String cospIP = "107.6.61.44";
	int port = 12179;
	int time_interval = 5000; //默认5秒
	
	public MsgRecvThread(Logger logger,String cospIP,int port, int time_interval) {
		
		this.logger = logger;
		this.cospIP = cospIP;
		this.port = port;
		this.time_interval = time_interval;
	}
	
	@Override
	public void run() {
		
		System.out.println("启动MQ监听...(接收MQ消息发往COSP)");
		info2("启动MQ监听...(接收MQ消息发往COSP)");
		for(;;) {
			
	        //间隔一段时间，继续监听
	        try {
				Thread.sleep(time_interval);
			} catch (InterruptedException e) {
				info2(MyUtil.getExcpMsg(e));
				e.printStackTrace();
			}
			
//	        MQManager mqm = new MQManager();
//	        List list = (List)mqm.recvMsg2FromMQ();
	        
			List list = (List)MQManager.recvMsg2FromMQ();
			
	        if(list == null || list.isEmpty()) {
//	        	info2("没有消息");
	        	continue;
	        }
	        
	        for(int i=0;i<list.size();i++) {
//	        	byte[] bs = (byte[])list.get(i);
	        	String recvinfo = (String)list.get(i);
	        	if(recvinfo == null || recvinfo.trim().length() == 0) {
	        		info2("消息为空");
		        	continue;
	        	}
	        	
		        String msg = "";
//				try {
//					msg = new String(bs,"utf-8");
//				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
//					info2(MyUtil.getExcpMsg(e));
//				}
		        
		        msg = recvinfo;
		        
				info2("==================================================================");
		        info2("|监听到中债登发来的消息|");
		        info2(msg);
		        info2("==================================================================");
		        
		        //发往cosp  TODO
		        info2("|准备消息发往cosp|");
		        
		        try {
					SocketSender ss = new SocketSender(cospIP,port,logger);
					ss.sendMsg(msg);
				} catch (UnknownHostException e) {
					info2(MyUtil.getExcpMsg(e));
					e.printStackTrace();
				}
	        	
	        }

		}
		
	}
	
	private static void info2(String msg) {
		try {
			logger.info(new String(msg.getBytes("utf-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

}
