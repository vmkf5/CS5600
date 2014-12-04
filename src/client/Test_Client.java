package client;

import java.io.File;

public class Test_Client {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		if(args[0].equals("1"))
		{
			Peer peer1 = new Peer("/src/data/config.properties", "Client_1");
			Peer peer2 = new Peer("/src/data/config.properties", "Client_2");
			Peer peer3 = new Peer("/src/data/config.properties", "Client_3");
			Peer peer4 = new Peer("/src/data/config.properties", "Client_4");
			Peer peer5 = new Peer("/src/data/config.properties", "Client_5");
			
			File file = new File("");
			String absolutePath = file.getAbsolutePath();
			
			file = new File(absolutePath + "\\test_client\\client_1\\qute.jpg");
			Long fileLength = file.length();
			System.out.println("fileLength :" + fileLength);
			System.out.println("modified Length :" + Math.floor(0.05*fileLength));
			peer1.startFileSenderManager("\\test_client\\client_1\\qute.jpg", 0, (long) Math.floor(0.05*fileLength));
			peer2.startFileSenderManager("\\test_client\\client_2\\qute.jpg", (long)Math.floor(0.2*fileLength) + 1, (long)Math.floor(0.25*fileLength));
			peer3.startFileSenderManager("\\test_client\\client_3\\qute.jpg",(long) Math.floor(0.4*fileLength) + 1, (long)Math.floor(0.45*fileLength));
			peer4.startFileSenderManager("\\test_client\\client_4\\qute.jpg",(long) Math.floor(0.6*fileLength) + 1, (long)Math.floor(0.65*fileLength));
			peer5.startFileSenderManager("\\test_client\\client_5\\qute.jpg", (long)Math.floor(0.8*fileLength) + 1, (long)Math.floor(0.85*fileLength));
			
			try {
				Thread t = new Thread();
				t.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			peer1.addSegmentToShare((long) Math.floor(0.05*fileLength) + 1, (long) Math.floor(0.1*fileLength));
			peer2.addSegmentToShare((long) Math.floor(0.25*fileLength) + 1, (long) Math.floor(0.3*fileLength));
			peer3.addSegmentToShare((long) Math.floor(0.45*fileLength) + 1, (long) Math.floor(0.5*fileLength));
			peer4.addSegmentToShare((long) Math.floor(0.65*fileLength) + 1, (long) Math.floor(0.7*fileLength));
			peer5.addSegmentToShare((long) Math.floor(0.85*fileLength) + 1, (long) Math.floor(0.9*fileLength));
			
			try {
				Thread t = new Thread();
				t.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			peer1.addSegmentToShare((long) Math.floor(0.1*fileLength) + 1, (long) Math.floor(0.15*fileLength));
			peer2.addSegmentToShare((long) Math.floor(0.3*fileLength) + 1, (long) Math.floor(0.35*fileLength));
			peer3.addSegmentToShare((long) Math.floor(0.5*fileLength) + 1, (long) Math.floor(0.55*fileLength));
			peer4.addSegmentToShare((long) Math.floor(0.7*fileLength) + 1, (long) Math.floor(0.75*fileLength));
			peer5.addSegmentToShare((long) Math.floor(0.9*fileLength) + 1, (long) Math.floor(0.95*fileLength));
			
			try {
				Thread t = new Thread();
				t.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			peer1.addSegmentToShare((long) Math.floor(0.15*fileLength) + 1, (long) Math.floor(0.2*fileLength));
			peer2.addSegmentToShare((long) Math.floor(0.35*fileLength) + 1, (long) Math.floor(0.4*fileLength));
			peer3.addSegmentToShare((long) Math.floor(0.55*fileLength) + 1, (long) Math.floor(0.6*fileLength));
			peer4.addSegmentToShare((long) Math.floor(0.75*fileLength) + 1, (long) Math.floor(0.8*fileLength));
			peer5.addSegmentToShare((long) Math.floor(0.95*fileLength) + 1, fileLength);
			
			while(true);
		}
		else
		{
			Peer peer6 = new Peer("/src/data/config.properties", "Client_6");
			Peer peer7 = new Peer("/src/data/config.properties", "Client_7");
			Peer peer8 = new Peer("/src/data/config.properties", "Client_8");
			Peer peer9 = new Peer("/src/data/config.properties", "Client_9");
			Peer peer10 = new Peer("/src/data/config.properties", "Client_10");
			
			peer6.getTrackerList();
			peer6.getFileTracker("qute.jpg","\\test_client\\client_6");
			peer7.getTrackerList();
			peer7.getFileTracker("qute.jpg","\\test_client\\client_7");
			peer8.getTrackerList();
			peer8.getFileTracker("qute.jpg","\\test_client\\client_8");
			peer9.getTrackerList();
			peer9.getFileTracker("qute.jpg","\\test_client\\client_9");
			peer10.getTrackerList();
			peer10.getFileTracker("qute.jpg","\\test_client\\client_10");
		}
		//peer6.getFileTracker("qute.jpg");
		
		
		
		
	}

}
