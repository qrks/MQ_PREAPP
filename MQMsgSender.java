package preapp;

/*
 * 从cosp接受消息，然后发送到mq
 * */
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class MQMsgSender {
	
    private static int PORT = 9999;
    private static ExecutorService pool;
    private ServerSocketChannel ssc;
//    private ServerSocket ss = null;
    private Selector selector;
    private int n;
    private static int maxThreads = 5;
     
    private static Logger logger = Logger.getLogger("MQMsgSender");
    
    public static void main(String[] args) throws IOException {
//    	MQMsgSender m = new MQMsgSender();
    	MQMsgSender.start();
//    	m.run();
    	
    	addHook();
    }
    
	private static void addHook() {
		Runtime runtime = Runtime.getRuntime();
		runtime.addShutdownHook(new Thread() {
			public void run() {
				info2("停止本地监听(接受COSP消息发往MQ)");
			}
		});
	}
    
    public static void start() {
    	PropertyConfigurator.configure("log4j.properties");
    	Properties properties = new Properties();
    	
    	try {
        	properties.load(new FileInputStream("mqconfig.properties"));

    		PORT = Integer.parseInt(properties.getProperty("local.listen.port"));
    		maxThreads = Integer.parseInt(properties.getProperty("local.threadpool.maxnum"));
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	MQMsgSender server;
		try {
			server = new MQMsgSender();
			server.doService();
		} catch (IOException e) {
			e.printStackTrace();
			info2(MyUtil.getExcpMsg(e));
		}finally {
			pool.shutdown();
		}
    }
     
    public MQMsgSender() throws IOException{
        pool = Executors.newFixedThreadPool(maxThreads);
         
        ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ServerSocket ss = ssc.socket();
        ss.bind(new InetSocketAddress(PORT));
        selector = Selector.open();
        ssc.register(selector,SelectionKey.OP_ACCEPT);
        System.out.println("启动本地监听(接受COSP消息发往MQ)");
        info2("启动本地监听(接受COSP消息发往MQ)");
        
    }
     
    public void doService(){
        while(true){
            try{
                n = selector.select();
            }catch (IOException e) {
                throw new RuntimeException("Selector.select()异常!");
            }
            if(n==0)
                continue;
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iter = keys.iterator();
            while(iter.hasNext()){
                SelectionKey key = iter.next();
                iter.remove();
                if(key.isAcceptable()){
                    SocketChannel sc = null;
                    try{
                        sc = ((ServerSocketChannel)key.channel()).accept();
                        sc.configureBlocking(false);
                        info2("|客户端 " + sc.socket().getInetAddress().getHostAddress() + " 已连接|");
                        SelectionKey k = sc.register(selector, SelectionKey.OP_READ);
                        ByteBuffer buf = ByteBuffer.allocate(1024);
                        k.attach(buf);
                    }catch (Exception e) {
                        try{
                            sc.close();
                        }catch (Exception ex) {
                        	sc = null;
                        }
                    }
                }
                else if(key.isReadable()){
                    key.interestOps(key.interestOps()&(~SelectionKey.OP_READ));
                    pool.execute(new MsgSenderThread(key,logger));
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