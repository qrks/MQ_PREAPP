package preapp;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class SocketSender {

	private static Logger logger = null;

	private InetSocketAddress sockaddr;
	
	int tryTimes = 3;
	int timeOut = 60 * 1000;
	int wait = 3 * 1000;


	public SocketSender(String destip, int port, Logger logger) throws UnknownHostException {
		this.logger = logger;

		InetAddress addr = InetAddress.getByName(destip);
		sockaddr = new InetSocketAddress(addr, port);
		info2("|准备连接|");
		logger.info("[" + destip + ":" + port + "]");

	}

	public void sendMsg(String sendMsg) {
		
		OutputStream os = null;
		Socket sock = null;
			
		for(int i=0;i<tryTimes;i++) {
			info2("|准备第|" + (i+1) + "|次发报文|");
			
			try {
				
				sock = new Socket();
				sock.connect(sockaddr);
				sock.setSoTimeout(timeOut);
				byte[] sendbuf = null;
				sendbuf = sendMsg.getBytes("utf-8");
				
				os = sock.getOutputStream();
				os.write(sendbuf);
				os.flush();
				
			}catch(Exception e2) {
				info2(MyUtil.getExcpMsg(e2));
				if (i == tryTimes -1) {
					info2("|重试过后仍然失败|");
					info2("|发送报文到COSP失败}");
					return;
				}
				//停一段时间后再重发
				try {
					Thread.sleep(wait);
				} catch (InterruptedException e) {
					info2(MyUtil.getExcpMsg(e));
				}
				continue;
			}finally {
				MyUtil.closeSrc(os);
				MyUtil.closeSrc(sock);
			}
			
			break;
		}
		
		info2("|发报内容写完毕|");
	
	}

	private static void info2(String msg) {
		try {
			logger.info(new String(msg.getBytes("utf-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	
}
