package server.com.File.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

import server.com.Business.models.FileDetails;
import server.com.Business.models.FilePeers;
import server.com.Business.models.RespCreateTracker;
import server.com.Business.models.RespGetFile;
import server.com.Business.models.RespList;
import server.com.Business.models.RespUpdateTracker;
import server.com.Business.models.createFileTrackerMessage;
import server.com.Business.models.getFileTracker;
import server.com.Business.models.updateTracker;
import server.com.File.Models.FileTracker;
import server.com.File.Models.PeerInfo;
import server.com.File.Models.SharedFileDetails;

public class FileManager {
	public String classpath = new File("").getAbsolutePath();
	//public String section = "\\src\\server\\com\\Data\\server\\";
	public String section;
	private Semaphore sem;

	public FileManager(Semaphore sem, String section) {
		this.sem = sem;
		this.section = section;
	}

	public boolean executeCreateTracker(createFileTrackerMessage createFileTracker)
	{
		File file = new File(classpath + section + createFileTracker.getFilename() + ".tracker");
		System.out.println("Inside File Manager execute message");
		if(!file.exists())
		{
			System.out.println("inside tracker not exists");
			//condition lets us know that there is no tracker file with that name in the server

			FileTracker tracker = new FileTracker();
			SharedFileDetails shDetails = new SharedFileDetails();
			shDetails.setFilename(createFileTracker.getFilename());
			shDetails.setDescription(createFileTracker.getDescription());
			shDetails.setFilesize(createFileTracker.getFileBytes());
			shDetails.setMd5(createFileTracker.getCheckSum());

			/*PeerInfo peer = new PeerInfo();
			peer.setEnd(createFileTracker.getFileBytes());
			peer.setIp(createFileTracker.getIpAddress());
			peer.setPort(createFileTracker.getPort());
			peer.setStart((long)0);
			peer.setTime(new Date().getTime());
			 */
			ArrayList<PeerInfo> peers = new ArrayList<PeerInfo>();
			//peers.add(peer);

			tracker.setDetails(shDetails);
			tracker.setPeers(peers);

			new FileTrackerModify(section).write(tracker);
			return true;
		}
		/*else
		{
			System.out.println("Inside tracker exists");
			// if there is a tracker file already present in the server
			FileTrackerModify ftModify = new FileTrackerModify();

			FileTracker tracker = ftModify.read(createFileTracker.getFilename());
			for(Iterator<PeerInfo> ite = tracker.getPeers().iterator(); ite.hasNext(); )
			{
				PeerInfo peer = ite.next();
				if(peer.getIp().toString().substring(1).equalsIgnoreCase(createFileTracker.getIpAddress()) && peer.getPort() == createFileTracker.getPort())
				{
					return false;
				}
			}

			PeerInfo peer = new PeerInfo();
			peer.setEnd(createFileTracker.getFileBytes());
			peer.setIp(createFileTracker.getIpAddress());
			peer.setPort(createFileTracker.getPort());
			peer.setStart((long)0);
			peer.setTime(new Date().getTime());

			tracker.getPeers().add(peer);

			ftModify.fileDelete(createFileTracker.getFilename());
			ftModify.write(tracker);
		}*/

		return false;
	}

	public RespGetFile executeGet(getFileTracker getFileTracker) {
		System.out.println("Inside File Get:" + classpath + section + getFileTracker.getFileName() + ".tracker");
		File file = new File(classpath + section + getFileTracker.getFileName() + ".tracker");
		if(file.exists())
		{
			try {
				sem.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			FileTracker tracker = new FileTrackerModify(section).read(file.getName());
			sem.release();
			RespGetFile resp = new RespGetFile();
			if(tracker == null)
			{
				System.out.println("tracker is null");
			}
			else if(tracker.getDetails() == null)
				System.out.println("tracker details is null");
			else if(tracker.getPeers() == null)
				System.out.println("tracker peers is null");
			resp.setFileName(tracker.getDetails().getFilename());
			resp.setFileSize(tracker.getDetails().getFilesize());
			resp.setMd5(tracker.getDetails().getMd5());
			resp.setDescription(tracker.getDetails().getDescription());
			
			ArrayList<FilePeers> peers = new ArrayList<FilePeers>();

			for(Iterator<PeerInfo> ite = tracker.getPeers().iterator(); ite.hasNext();)
			{
				PeerInfo peerInfo = ite.next();
				FilePeers peer = new FilePeers();
				peer.setEndByte(peerInfo.getEnd());
				peer.setIpAddress(peerInfo.getIp().toString().substring(1));
				peer.setPortNumber(peerInfo.getPort());
				peer.setStartByte(peerInfo.getStart());
				peer.setTimeStamp(new Date(peerInfo.getTime()));

				peers.add(peer);
			}

			resp.setPeers(peers);
			return resp;

		}
		return null;
	}

	public RespList executeList() {
		File directory = new File(classpath + section);
		File[] files = directory.listFiles();

		FileTrackerModify ftModify = new FileTrackerModify(section);

		RespList respList = new RespList();
		ArrayList<FileDetails> fileDetails = new ArrayList<FileDetails>();
		try {
			sem.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(File file : files)
		{
			FileDetails fileDetail = new FileDetails();
			String fileName = file.getName();
			FileTracker tracker = ftModify.read(fileName);

			fileDetail.setCheckSum(tracker.getDetails().getMd5());
			fileDetail.setFileName(tracker.getDetails().getFilename());
			fileDetail.setFileSize(String.valueOf(tracker.getDetails().getFilesize()));

			fileDetails.add(fileDetail);
		}
		sem.release();
		respList.setFileDetails(fileDetails);

		// TODO Auto-generated method stub
		return respList;
	}

	public RespUpdateTracker executeUpdateTracker(updateTracker message) {
		System.out.println("Absolute filename" + classpath + section + message.getFilename() + ".tracker");
		File file = new File(classpath + section + message.getFilename() + ".tracker");
		RespUpdateTracker resp = new RespUpdateTracker();
		resp.setFileName(message.getFilename());
		
		
		if(file.exists())
		{
			FileTrackerModify ftModify = new FileTrackerModify(section);

			String fileName = file.getName();

			try {
				sem.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			FileTracker tracker = ftModify.read(fileName);
			ArrayList<PeerInfo> peers = tracker.getPeers();
			if(peers != null && peers.size() > 0)
			{
				System.out.println("peers != null");
				ArrayList<PeerInfo> removePeers = new ArrayList<PeerInfo>();
				for(PeerInfo peer : peers)
				{
					if(peer.getIp().toString().substring(1).equals(message.getIpAddress()) && peer.getPort() == message.getPortNumber())
					{
						if(peer.getStart() >= message.getStartBytes() && peer.getEnd() <= message.getEndBytes())
						{
							removePeers.add(peer);
						}
					}
				}
				peers.removeAll(removePeers);
			}
			else
			{
				System.out.println("Peers == null");
				peers = new ArrayList<PeerInfo>();
			}


			PeerInfo peer = new PeerInfo();
			peer.setEnd((long) message.getEndBytes());
			peer.setIp(message.getIpAddress());
			peer.setPort(message.getPortNumber());
			peer.setStart((long)message.getStartBytes());
			peer.setTime(new Date().getTime());

			
			peers.add(peer);
			tracker.setPeers(peers);
			ftModify.fileDelete(fileName);
			ftModify.write(tracker);

			sem.release();

			resp.setResponse("succ");
			return resp;

		}
		resp.setResponse("ferr");
		return resp;
	}
}
