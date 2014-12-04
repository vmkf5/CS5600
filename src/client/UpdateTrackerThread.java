package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;

import server.com.Business.models.updateTracker;

public class UpdateTrackerThread extends Thread 
{
	
	private String fileName;
	private BlockingQueue<Long> myQueue;
	private String ipAddress;
	private String port;
	private PrintWriter out;
	private BufferedReader in;
	private String peerName;
	
	public UpdateTrackerThread(String fileName, BlockingQueue<Long> myQueue, String ipAddress, String port, PrintWriter out, BufferedReader in, String peerName)
	{
		this.fileName = fileName;
		this.myQueue = myQueue;
		this.ipAddress = ipAddress;
		this.port = port;
		this.out = out;
		this.in = in;
		this.peerName = peerName;
	}

	public void run()
	{
		while(true)
		{
			while(myQueue.size() % 2 != 0)
			{
				try {
					this.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			updateTracker req = new updateTracker();
			req.setFilename(fileName);
			req.setIpAddress(ipAddress.substring(1));
			req.setPortNumber(new Integer(port));
			
			Object[] myQueueArray = myQueue.toArray();
			for(int lcv = 0; lcv < myQueueArray.length ; lcv++)
			{
				req.setStartBytes((Long) myQueueArray[lcv]);
				lcv++;
				req.setEndBytes((Long) myQueueArray[lcv]);

				out.println(req.toString());
                //System.out.println(req.toString());
				String resp = "";
				try
				{
					resp = in.readLine();
                    //System.out.println(resp);
				}
				catch(IOException e)
				{
					//e.printStackTrace();
				}
				
				if(resp == null)
				{
					System.out.println("Unkown response form server");
				}
				else if(resp.contains("succ"))
				{
					System.out.println("I am " + peerName + " and I'm sharing " + fileName + " from " +req.getStartBytes() + " to " + req.getEndBytes() + " bytes.");
				}
				else
				{
					System.out.println("Server updateTracker message returned error");
					lcv = lcv -2;
				}
				
			}
			
			try {
				this.sleep(890000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			
		}
	}
}
