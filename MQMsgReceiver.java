package preapp;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


/**
 * 接受mq队列中的消息
 * */
public class MQMsgReceiver {

    private static Logger logger = Logger.getLogger("MQMsgReceiver");
    
    //发往cosp的地址和端口
	private static String cospIP = "107.6.61.44";
	private static int port = 12179;
	private static int time_interval = 5 * 1000; 
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		MQMsgReceiver.doService();
		
		addHook();
	}
	
	private static void addHook() {
		Runtime runtime = Runtime.getRuntime();
		runtime.addShutdownHook(new Thread() {
			public void run() {
				info2("停止MQ监听...(接收MQ消息发往COSP)");
			}
		});
	}
	
	public static void doService() {
		
		PropertyConfigurator.configure("log4j.properties");
		
		Properties properties = new Properties();
    	
    	try {
        	properties.load(new FileInputStream("mqconfig.properties"));

    		port = Integer.parseInt(properties.getProperty("cosp.recv.port"));
    		cospIP = properties.getProperty("cosp.recv.ip");
    		time_interval = Integer.parseInt(properties.getProperty("mq.service.queue.recv.interval"));
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	    // Create the object with the run() method
	    Runnable runnable = new MsgRecvThread(logger,cospIP,port,time_interval);
	    
	    // Create the thread supplying it with the runnable object
	    Thread thread = new Thread(runnable);
	    
	    // Start the thread
	    thread.start();

	}

	private static void info2(String msg) {
		try {
			logger.info(new String(msg.getBytes("utf-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
}
