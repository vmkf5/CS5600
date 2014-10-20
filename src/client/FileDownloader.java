package client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Handles downloading file segments from a connected peer in its own thread.
 * Once complete, the FileDownloader alerts the Peer through callback functions.
 *
 * Created by Levi Malott on 10/19/14.
 */
public class FileDownloader implements Runnable
{
    private Socket socket;
    private Integer segment_size;
    private Peer callback;
    private FileTracker tracker;
    private FileOutputStream fos;
    private InputStream peer_in;
    private PrintWriter peer_out;
    private BufferedOutputStream file_out;

    public FileDownloader(FileTracker tracker, Integer segment_size, Peer callback)
    {
       this.tracker = tracker;
       this.segment_size = segment_size;
       this.callback = callback;
    }

    private static String combine(String path1, String path2)
    {
        File f1 = new File(path1);
        File f2 = new File(f1, path2);
        return f2.getPath();
    }

    @Override
    public void run()
    {
     //Assumes peers are listed in order of most recent server contact time
        ArrayList<PeerInfo> all_peers = tracker.getPeers();
        PeerInfo peer;
        SharedFileDetails details = tracker.getDetails();
        SharedFileDetails updated_details = new SharedFileDetails(details);
        String filename = details.getFilename();

        byte[] buf = new byte[segment_size];

        //Create a new file to save the downloaded contents
        try {
            fos = new FileOutputStream(combine(callback.getSharedDir(), filename));
            file_out = new BufferedOutputStream(fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Long current_start = Long.valueOf(0);
        Long current_end   = Long.valueOf(segment_size-1);        //Iterate over peers to request content from, if it fails it goes to the next client
        Integer len           = segment_size.intValue();
        Long filesize      = details.getFilesize();
        updated_details.setStart(current_start);
        updated_details.setEnd(current_start);
        while(current_start < filesize)
        {
            for(Iterator<PeerInfo> it = all_peers.iterator(); it.hasNext(); )
            {
                peer = it.next();
                try
                {
                    socket = new Socket(peer.getIp(), peer.getPort());
                    peer_out = new PrintWriter(socket.getOutputStream(), true);
                    peer_in = socket.getInputStream();
                } catch (IOException e) {
                    //e.printStackTrace();
                }
                if (socket != null)
                {
                    peer_out.write("<GET " + filename + " " + current_start.toString() + " " + current_end.toString() + ">\n");
                    try {
                        peer_in.read(buf, 0, len);
                        file_out.write(buf, 0, len);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    updated_details.setEnd(current_end);
                    current_start += segment_size;
                    current_end += segment_size;
                    if (current_end >= details.getFilesize()) {
                        current_end = details.getFilesize();
                    }
                    try {
                        socket.close();
                        peer_out.close();
                        peer_in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    callback.updateSharedFileDetails(updated_details);

                }

            }
        }
        try {
            file_out.flush();
            file_out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        callback.removeFileFromQueue(tracker);
    }
}
