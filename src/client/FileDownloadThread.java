package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import server.com.File.Models.FileTracker;
import server.com.File.Models.PeerInfo;
import server.com.File.Models.SharedFileDetails;

public class FileDownloadThread implements Callable<Boolean> {

	private String filename;
	private PrintWriter out;
	private BufferedReader in;
	private Semaphore semaphore = new Semaphore(1);
	private int max_segment_size;
	private String currentPath;
	private BlockingQueue<Long> myQueue; 
	private FileSenderManager fsManager;
	private UpdateTrackerThread utThread;




	public FileDownloadThread(String filename, PrintWriter out, BufferedReader in, int max_segment_size, String currentPath)
	{
		this.filename = filename;
		this.out = out;
		this.in = in;
		this.max_segment_size = max_segment_size;
		this.currentPath = currentPath;

		try {
			ServerSocket sample = new ServerSocket(0);

			int port = sample.getLocalPort();
			myQueue = new LinkedBlockingQueue<Long>();
			/*fsManager = new FileSenderManager(currentPath + filename, port, myQueue);
			fsManager.run();
			utThread = new UpdateTrackerThread(filename, myQueue, sample.getInetAddress().toString().substring(1), String.valueOf(port), out, in);
			utThread.run();*/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	public FileTracker getRecentTracker(String filename)
	{

		String msg = "<GET " + filename + ".track>";
		out.println(msg);
		ArrayList<String> message = new ArrayList<String>();
		String resp = "";
		while( resp != null && !resp.contains("GET END") )
		{
			try {
				resp = in.readLine();
				message.add(resp);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		if( message.size() == 0 )
		{
			System.out.println("Server did not response to " + msg);
			System.exit(1);
		}

		//obtain the MD5 from the server response
		String md5_recvd = message.get(message.size()-1).split(" ")[3];
		//remove the pesky '>' character
		md5_recvd = md5_recvd.substring(0, md5_recvd.length()-1);

		//compare the received md5 with the computed for validity
		md5_recvd = getMd5(md5_recvd);
		//To compute the MD5 hash, the header and tail of the message are ignored
		//then put all the data into one string and compute the hash
		String content =  message.subList(1,message.size()-1).toString().replaceAll("\\[|\\]", "").replaceAll(", ", "\n");
		//content = content.substring(1,content.length()-1);
        content += "\n";
		content = getMd5(content);
		if( content.equals(md5_recvd))
		{
			FileTracker tracker = new FileTracker(message);
			return tracker;
		}
		else {
			System.out.println("MD5 values for response to " + msg + "do not match.");
			System.out.println("Expected: " + md5_recvd);
			System.out.println("Actual: " + content);
		}

		message.clear();
		return null;

	}

	public String getMd5(String msg) {
		String digest = null;
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] hash = md.digest(msg.getBytes("UTF-8"));

			StringBuilder sb = new StringBuilder(2 * hash.length);
			for (byte b : hash) {
				sb.append(String.format("%02x", b & 0xff));
			}

			digest = sb.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return digest;
	}

	public void writeSampleData(int noOfSegments)
	{
		try {
			FileOutputStream fileOut = new FileOutputStream(currentPath + filename);
			BufferedOutputStream buffOut = new BufferedOutputStream(fileOut);

			byte[] sampleSegmentData = new byte[max_segment_size];
			for(int i = 0; i < max_segment_size ; i++)
			{
				sampleSegmentData[i] = 0;
			}

			for(int i = 0; i< noOfSegments ; i++)
			{
				buffOut.write(sampleSegmentData);
				buffOut.flush();
			}
            fileOut.flush();
            fileOut.close();
			buffOut.close();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void addSegmentToShare(long start, long end)
	{
        Object[] myQueueArray = myQueue.toArray();
        ArrayList<Long> removeObjects = new ArrayList<Long>();
        long share_start = start;
        long share_end = end;
        for(int lcv = 0;lcv < myQueueArray.length; lcv++)
        {
            Long sampleStart = (Long) myQueueArray[lcv];
            lcv++;
            Long sampleEnd = (Long) myQueueArray[lcv];
            if(sampleEnd == start - 1 || sampleStart == end + 1 || (sampleStart>= start && sampleEnd<=end))
            {

                removeObjects.add(sampleStart);
                removeObjects.add(sampleEnd);
                share_start = (sampleStart < share_start) ? sampleStart : share_start;
                share_start = ( start < share_start )  ? start : share_start;
                share_end   = (sampleEnd > share_end ) ? sampleEnd : share_end;
                share_end   = (end > share_end) ? end : share_end;
            }
        }
        try {

            if(removeObjects.size() > 0)
				myQueue.removeAll(removeObjects);
            myQueue.put(share_start);
            myQueue.put(share_end);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

/*		fsManager.wait();
			utThread.wait();
			if(removeObjects.size() > 0)
				myQueue.removeAll(removeObjects);

//			fsManager.notify();
*			utThread.notify();

		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		*/

	}

	public ArrayList<PeerInfo> getSortedPeersOnTimestamp(ArrayList<PeerInfo> peers)
	{
		Collections.sort(peers,	new Comparator<PeerInfo> ()
				{
			@Override
			public int compare(PeerInfo o1, PeerInfo o2) {
				return o1.getTime().compareTo(o2.getTime());
			}

			@Override
			public Comparator<PeerInfo> reversed() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Comparator<PeerInfo> thenComparing(
					Comparator<? super PeerInfo> other) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <U> Comparator<PeerInfo> thenComparing(
					Function<? super PeerInfo, ? extends U> keyExtractor,
					Comparator<? super U> keyComparator) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <U extends Comparable<? super U>> Comparator<PeerInfo> thenComparing(
					Function<? super PeerInfo, ? extends U> keyExtractor) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Comparator<PeerInfo> thenComparingInt(
					ToIntFunction<? super PeerInfo> keyExtractor) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Comparator<PeerInfo> thenComparingLong(
					ToLongFunction<? super PeerInfo> keyExtractor) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Comparator<PeerInfo> thenComparingDouble(
					ToDoubleFunction<? super PeerInfo> keyExtractor) {
				// TODO Auto-generated method stub
				return null;
			}
				}
				);

		return peers;
	}

	public long findSegmentStartForClient(Object[] myQueueArray, long start, long end)
	{
        if( myQueueArray.length == 0)
        {
            return start;
        }
        for(int i = 0; i < myQueueArray.length ; i++)
        {
            long downloadedStart = (Long) myQueueArray[i];
            i++;
            long downloadedEnd = (Long) myQueueArray[i];

            if( downloadedStart <= start  && end <= downloadedEnd )
            {
                return -1;
            }
            else if( downloadedStart == start && downloadedEnd <= end )
            {
                return downloadedEnd + 1;
            }

        }
        return start;
    }


	public Boolean call()
	{
		int noOfSegmentsDownloaded = 0;
		long filesize = -1;
		Object[] myQueueArray = myQueue.toArray();
		FileTracker tracker = new FileTracker();
        Socket socket = null;
        PrintWriter peer_out = null;
        BufferedInputStream peer_in = null;

		while(myQueueArray.length != 2 || !(((Long) myQueueArray[0]) == 0) && (((Long)myQueueArray[1]) == filesize))
		{

			if(noOfSegmentsDownloaded == 0)
			{
				Thread t = new Thread();
				try {
					t.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
				
				tracker = this.getRecentTracker(filename);
                filesize = tracker.getDetails().getFilesize();
			}

			String filepath = currentPath + "/" + filename;

			File file = new File(filepath);

			try {
				RandomAccessFile raFile = new RandomAccessFile(file, "rwd");
				ArrayList<PeerInfo> peers = this.getSortedPeersOnTimestamp(tracker.getPeers());

				for(PeerInfo peer : peers)
				{
					myQueueArray =  myQueue.toArray();
					long start = this.findSegmentStartForClient(myQueueArray, peer.getStart(), peer.getEnd());
					long end = start + max_segment_size - 1;
                    end = (end > peer.getEnd() ) ? peer.getEnd():  end;
                    if( start != -1) {
                        byte[] buffer = new byte[(int) (end - start + 1)];
                        try {
                            socket = new Socket(peer.getIp(), peer.getPort());
                            peer_out = new PrintWriter(socket.getOutputStream(), true);
                            peer_in = new BufferedInputStream(socket.getInputStream());
                        } catch (IOException e) {
                        //e.printStackTrace();
                        }
                        peer_out.println("<GET " + this.filename + " " + start + " " + end+ ">");
                        int read  = peer_in.read(buffer, 0, (int) ((int) end - start + 1));

                        raFile.seek(start);
                        raFile.write(buffer, 0, read);
                        socket.close();
                        peer_out.close();
                        peer_in.close();
                        this.addSegmentToShare(start, end);

                    }
				}
                raFile.close();

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        SharedFileDetails info = new SharedFileDetails();
        File f = new File(currentPath);
        info.filesize = f.length();
        info.filename = filename;
        info.description = "desc";


        try
        {
            FileInputStream in = new FileInputStream(f);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            DigestInputStream din = new DigestInputStream(in, md5);
            while (din.read() != -1);
            din.close();
            byte[] digest = md5.digest();
            StringBuilder sb = new StringBuilder(2 * digest.length);
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            info.md5 = sb.toString();
        }
        catch (IOException ex)
        {
            System.err.println(ex);
        }
        catch (NoSuchAlgorithmException ex)
        {
            System.err.println(ex);
        }
		return info.md5 == tracker.getDetails().getMd5();
	}

}
