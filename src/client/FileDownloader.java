/*package client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import server.com.File.Models.FileTracker;
import server.com.File.Models.PeerInfo;
import server.com.File.Models.SharedFileDetails;


*//**
 * Handles downloading file segments from a connected peer in its own thread.
 * Once complete, the FileDownloader alerts the Peer through callback functions.
 *
 * Created by Levi Malott on 10/19/14.
 *//*
public class FileDownloader implements Runnable
{
	private Socket socket;
	private Integer segment_size;
	private Peer callback;
	private FileTracker tracker;
	private InputStream peer_in;
	private PrintWriter peer_out;
	private Long start;
	private Long end;
	byte[] buffer;

	public FileDownloader(FileTracker tracker, Integer segment_size)
	{
		this.tracker = tracker;
		this.segment_size = segment_size;       
	}

	private static String combine(String path1, String path2)
	{
		File f1 = new File(path1);
		File f2 = new File(f1, path2);
		return f2.getPath();
	}

	public void run()
	{
		boolean isFileNotComplete = true;
		long noOfSegments = tracker.getDetails().getFilesize() / segment_size;
		Map<Long,Long> downloadedSegments = new TreeMap<Long,Long>();
		
		while(isFileNotComplete)
		{
			while(tracker.getPeers() == null || tracker.getPeers().size() == 0)
			{
				System.out.println("Waiting for peers");
						try {
							this.wait(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

			}


			while();
			{
				
			}
		}
	}

	 @Override
    public void run()
    {
     //Assumes peers are listed in order of most recent server contact time
        ArrayList<PeerInfo> all_peers = tracker.getPeers();
        SharedFileDetails details = tracker.getDetails();
        SharedFileDetails updated_details = new SharedFileDetails(details);
        String filename = details.getFilename();
        RandomAccessFile file = null;

        //Create a new file to save the downloaded contents
        try {
            file = new RandomAccessFile(combine(callback.getSharedDir(), filename), "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Long filesize      = details.getFilesize();
        Long current_start = this.start;
        Long current_end   = (this.segment_size-1 > filesize) ? filesize : Long.valueOf(this.segment_size-1) ;        //Iterate over peers to request content from, if it fails it goes to the next client
        Integer len           = (int) current_end - current_start;
        buffer = new byte[this.segment_size];
        int read = 0;
        boolean got_segment = false;
        updated_details.setStart(current_start);

        for(; current_start < end; current_start+=this.segment_size)
        {
            got_segment = false;
            for( PeerInfo peer : all_peers )
            {
                try {
                    socket = new Socket(peer.getIp(), peer.getPort());
                    peer_out = new PrintWriter(socket.getOutputStream(), true);
                    peer_in = socket.getInputStream();
                } catch (IOException e) {
                    //e.printStackTrace();
                }
                if (socket != null)
                {
                    peer_out.println("<GET " + filename + " " + current_start + " " + current_end+ ">");
                    try
                    {
                        file.seek(current_start);
                        read = peer_in.read(buffer, 0, len);
                        file.write(buffer, 0, read);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    updated_details.setEnd(current_end);
                    current_end += segment_size;
                    if (current_end >= filesize) {
                        current_end = filesize;
                    }
                    got_segment=true;

                    try {
                        socket.close();
                        peer_out.close();
                        peer_in.close();
                        file.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            if(got_segment) {
                callback.updateSharedFileDetails(updated_details);
            }
            else
            {
                break;
            }
        }
        if(current_end==filesize) {
            callback.downloadComplete(tracker);
        }
    }
}
*/