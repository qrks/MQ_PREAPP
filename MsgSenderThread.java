package preapp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;

public class MsgSenderThread implements Runnable{
	
	private static Charset charset = Charset.forName("utf-8");
	private static Logger logger = null;
	
    private SelectionKey key;
    public MsgSenderThread(SelectionKey key,Logger logger){
    	this.logger = logger;
        this.key = key;
    }
    
    @Override
    public void run() {
        SocketChannel sc = (SocketChannel)key.channel();
        ByteBuffer buf = (ByteBuffer)key.attachment();
        buf.clear();
        int len = 0;
        try{
        	String recv_msg_hex = "";
            while((len=sc.read(buf))>0){//非阻塞，立刻读取缓冲区可用字节
                buf.flip();
                
                byte[] bs = new byte[buf.remaining()];
                buf.get(bs,0,bs.length);
                
//                String tmps = charset.decode(buf).toString();
                String tmps = MyUtil.byte2hex(bs);
                
//                info2("客户端发来消息：" + cb.toString());
                recv_msg_hex += tmps;
                buf.clear();
            }

            byte[] recv_bytes = MyUtil.hex2byte(recv_msg_hex);
            String recv_msg = new String(recv_bytes,"utf-8");
//            String recv_msg = new String(recv_bytes);
            
//            info2("全部信息:" + recv_msg);
            if(recv_msg == null || recv_msg.trim().length() == 0) {
            	info2("过滤空消息" );
            	return;
            }
            
            info2("==================================================================");
            info2("|监听到cosp发来的消息|");
            logger.info(recv_msg);
            info2("==================================================================");
            
            //放入mq发送队列中  TODO
//            MQManager mqm = new MQManager();
//            mqm.sendMsg2MQ(recv_msg);
            
            MQManager.sendMsg2MQ(recv_msg);
            
//            String data = "echo back";
//            buf.put(data.getBytes());
//            buf.flip();
//            sc.write(buf);

            //不发送回包
            sc.socket().shutdownOutput();
            
            if(len==-1){
            	info2("|客户端已断开|");
                sc.close();
            }
            
            if(key.isValid()) {
                //没有可用字节,继续监听OP_READ
                key.interestOps(key.interestOps()|SelectionKey.OP_READ);
                key.selector().wakeup();
            }

        }catch(IOException ioe) {
        	ioe.printStackTrace();
        	try {
              sc.close();
          } catch (IOException e1) {
        	  sc = null;
          }
        }catch(CancelledKeyException cke) {
        	info2(MyUtil.getExcpMsg(cke));
//        	key.cancel();
        }
//        catch (Exception e) {
//        	e.printStackTrace();
//            try {
//                sc.close();
//            } catch (IOException e1) {
//            }
//        }finally {
//        	
//        }
    }
    
	private static void info2(String msg) {
		try {
			logger.info(new String(msg.getBytes("utf-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
    
}