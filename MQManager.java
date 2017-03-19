package preapp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
//import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.ibm.mq.MQC;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;

public class MQManager {

	private static Logger logger = Logger.getLogger("MQManager");
	private static MQQueueManager qMgr;

	//���еĲ������ã�����Ĭ��ֵ����д���������ļ��� mqconfig.preperties
	private static String MQ_MANAGER = "QMZMQ";
	private static String MQ_HOST_NAME = "107.6.141.134";
	private static String MQ_CHANNEL = "SYSTEM.DEF.SVRCONN";
	private static String MQ_QUEUE_SEND_NAME = "LQ_T_ZMQ";
	private static int MQ_PROT = 1414;
	private static int MQ_CCSID = 1381;
	
	private static String MQ_QUEUE_RECV_NAME = "LQ_T_ZMQ";

	static{
		PropertyConfigurator.configure("log4j.properties");
	}
	
	public static void main(String[] args) {
		getMQconfig();
		MQManager mm = new MQManager();
		mm.getConnMQmanager();
		try {
//			mm.sendMsg("this is first message");
//			mm.sendMsg("this is second message");
			mm.receiveMsg();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mm.closeConnMQmanager();
	}

	public static void sendMsg2MQ(String msg) {
		getMQconfig();
		MQManager mm = new MQManager();
		
		try {
			mm.getConnMQmanager();
			mm.sendMsg(msg);
		} catch (Exception e) {
			info2(MyUtil.getExcpMsg(e));
			e.printStackTrace();
		} finally {
			mm.closeConnMQmanager();
		}
		
	}

	private static void getMQconfig() {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("mqconfig.properties"));

			MQ_MANAGER = properties.getProperty("mq.service.queueManger.name");
			MQ_HOST_NAME = properties.getProperty("mq.servece.hostip");
			MQ_CHANNEL = properties.getProperty("mq.service.channel");
			MQ_QUEUE_SEND_NAME = properties.getProperty("mq.service.queue.send.name");
			MQ_QUEUE_RECV_NAME = properties.getProperty("mq.service.queue.recv.name");
			MQ_PROT = Integer.parseInt(properties.getProperty("mq.service.port"));
			MQ_CCSID = Integer.parseInt(properties.getProperty("mq.service.CCSID"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getConnMQmanager() {
		
		if(qMgr != null && (qMgr.isConnected())) {
//			info2("qMgr is Connected��");
		}else {

			info2("���й�����δ����|׼������");

			MQEnvironment.hostname = MQ_HOST_NAME;// MQ������IP
			MQEnvironment.channel = MQ_CHANNEL; // ���й�������Ӧ�ķ���������ͨ��
			/*
			 * ����ͨ���� ZMQ.CCDC ����ͨ���� CC	DC.ZMQ
			 */
			MQEnvironment.CCSID = MQ_CCSID; // �ַ�����
			MQEnvironment.port = MQ_PROT; // ���й������Ķ˿ں�
			
			try {
				qMgr = new MQQueueManager(MQ_MANAGER);// ���й���������
			} catch (Exception e) {
				info2(MyUtil.getExcpMsg(e));
				e.printStackTrace();
			}
		}

	}

	private void closeConnMQmanager() {
		if (qMgr != null) {
			try {
				qMgr.close();
			} catch (MQException e) {
				info2(MyUtil.getExcpMsg(e));
				e.printStackTrace();
			}
		}
	}

	public void sendMsg(String msgStr) throws MQException, IOException {
		//int openOptions = MQC.MQOO_INPUT_AS_Q_DEF | MQC.MQOO_OUTPUT | MQC.MQOO_INQUIRE;
		int openOptions = MQC.MQOO_OUTPUT | MQC.MQOO_FAIL_IF_QUIESCING;
		MQQueue queue = null;
		try {
			// ����Q1ͨ��������
			queue = qMgr.accessQueue(MQ_QUEUE_SEND_NAME, openOptions, null, null, null);
			MQMessage msg = new MQMessage();// Ҫд����е���Ϣ
			msg.format = MQC.MQFMT_STRING;
			msg.characterSet = 1381;
			msg.writeString(msgStr);
			//msg.write(msgStr.getBytes("utf-8"));
			info2("д�������������" );
//			logger.info(msgStr);
			
			MQPutMessageOptions pmo = new MQPutMessageOptions();
			msg.expiry = -1; // ������Ϣ�ò�����
			msg.messageFlags = MQC.MQMF_SEGMENTATION_ALLOWED;
			queue.put(msg, pmo);// ����Ϣ�������
			info2("�����ݷ������[" + MQ_QUEUE_SEND_NAME + "]");
		} catch (MQException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (queue != null) {
				try {
					queue.close();
				} catch (MQException e) {
					info2(MyUtil.getExcpMsg(e));
					e.printStackTrace();
				}
			}
		}
	}

	public List<Object> receiveMsg() {
		int openOptions = MQC.MQOO_INPUT_AS_Q_DEF | MQC.MQOO_OUTPUT | MQC.MQOO_INQUIRE;
		MQQueue queue = null;
		byte[] bs = null;
		List list = new ArrayList();
		try {
			queue = qMgr.accessQueue(MQ_QUEUE_RECV_NAME, openOptions, null, null, null);

			int depth = queue.getCurrentDepth();
			
			if(depth > 0) {
				String queueName = queue.getName();
				info2("�������ƣ�" + queueName);
				info2("�ö��е�ǰ�����Ϊ:" + queue.getCurrentDepth());
				info2("===========================");
			}
			
			// �����е������Ϣ������
			while (depth-- > 0) {
				MQMessage msg = new MQMessage();// Ҫ���Ķ��е���Ϣ
				MQGetMessageOptions gmo = new MQGetMessageOptions();
				
				//����ѡ������Ƿ��ܽ��2033������
				gmo.options = gmo.options + MQC.MQGMO_COMPLETE_MSG;

				gmo.options = gmo.options + MQC.MQGMO_WAIT;

				gmo.options = gmo.options + MQC.MQGMO_FAIL_IF_QUIESCING;

				gmo.waitInterval = 10000;
				
				queue.get(msg, gmo);
				
				int msglen = msg.getMessageLength();
				info2("��Ϣ�Ĵ�СΪ��" + msglen);
				
//				bs = new byte[msglen];
//				msg.readFully(bs);
				String recv_info = msg.readStringOfByteLength(msglen);
				
//				info2("��Ϣ�����ݣ�\n" + recv_info);
////				info2("��Ϣ�����ݣ�\n" + new String(bs));
//				info2("---------------------------");
				
				list.add(recv_info);
				
			}
		} catch (MQException e) {
			info2(MyUtil.getExcpMsg(e));
			e.printStackTrace();
		} catch (Exception e) {
			info2(MyUtil.getExcpMsg(e));
			e.printStackTrace();
		} finally {
			if (queue != null) {
				try {
					queue.close();
				} catch (MQException e) {
					info2(MyUtil.getExcpMsg(e));
					e.printStackTrace();
				}
			}
		}
		return list;
	}

	public static Object recvMsg2FromMQ() {
		getMQconfig();
		MQManager mm = new MQManager();
		byte[] bs = null;
		List list = null;
		try {
			mm.getConnMQmanager();
			list = (List)mm.receiveMsg();
		} catch (Exception e) {
			info2(MyUtil.getExcpMsg(e));
			e.printStackTrace();
		}finally {
			mm.closeConnMQmanager();
		}
		return list;
		
	}

	private static void info2(String msg) {
		try {
			logger.info(new String(msg.getBytes("utf-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
}
