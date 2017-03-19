package preapp;

public class PreAppMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		MQMsgReceiver.doService();
		
		Thread t = new Thread(){@Override
		public void run() {
			super.run();
			MQMsgSender.start();
		}};
		
		
	}

}
