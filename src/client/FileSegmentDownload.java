package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

import server.com.File.Models.PeerInfo;

public class FileSegmentDownload implements Callable<Long> {

	Socket socket;
	PrintWriter peer_out;
	InputStream peer_in;
	String filename;
	long current_start;
	long current_end;
	RandomAccessFile file;
	Semaphore sem;
	
	
	public FileSegmentDownload (PeerInfo peer, String filename, long current_start, long current_end, RandomAccessFile file, Semaphore sem)
	{
		try {
            socket = new Socket(peer.getIp(), peer.getPort());
            peer_out = new PrintWriter(socket.getOutputStream(), true);
            peer_in = socket.getInputStream();
        } catch (IOException e) {
            //e.printStackTrace();
        }
		
		this.current_end = current_end;
		this.current_start = current_start;
		this.file = file;
		this.sem = sem;
        this.filename = filename;
	}
	
	@Override
	public Long call() throws Exception 
	{
		long start_segmentDownloadComplete = -1;
		
        if (socket != null)
        {
        	byte[] buffer = new byte[(int) (current_end - current_start)];

            System.out.println("<GET " + this.filename + " " + current_start + " " + current_end+ ">");
            peer_out.println("<GET " + this.filename + " " + current_start + " " + current_end+ ">");
            try
            {
                file.seek(current_start);
                System.out.println("before read");
                int read  = peer_in.read(buffer, 0, (int) ((int) current_end - current_start));
                System.out.println("after read");
                sem.acquire();
                file.write(buffer, 0, read);
                sem.release();
                start_segmentDownloadComplete = current_start;
                socket.close();
                peer_out.close();
                peer_in.close();
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
          }
		return start_segmentDownloadComplete;
	}

}
