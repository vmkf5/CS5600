package server.com.File.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Semaphore;

import server.com.Business.models.FileDetails;
import server.com.Business.models.RespList;
import server.com.File.Models.FileTracker;
import server.com.File.Models.PeerInfo;

public class Removal15Mins extends Thread 
{
	public String classpath = new File("").getAbsolutePath();
	public String section = "/src/com/Data/server/";
	public long waitTime = 15 * 60 * 60;
	private Semaphore sem;
	
	
	public Removal15Mins(Semaphore sem) {
		this.sem = sem;
	}

	public void run()
	{
		while(true)
		{
			File directory = new File(classpath + section);
			File[] files = directory.listFiles();

			FileTrackerModify ftModify = new FileTrackerModify();

			for(File file : files)
			{
				try {
					sem.acquire();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String fileName = file.getName();
				FileTracker tracker = ftModify.read(fileName);

				ArrayList<PeerInfo> peers = tracker.getPeers();
				for(PeerInfo peer : peers)
				{
					long current_timestamp = new Date().getTime();
					if(peer.getTime() - current_timestamp >= waitTime)
					{
						peers.remove(peer);
					}
				}

				tracker.setPeers(peers);

				ftModify.fileDelete(fileName);
				ftModify.write(tracker);

				sem.release();
				
			}

			try {
				this.sleep(waitTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
