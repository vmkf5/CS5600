package client;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * Runnable class to handle sending a file segment to a connected client.
 *
 * Created by Levi Malott on 10/19/14.
 */
public class FileSender implements Runnable
{
    private Socket socket;
    private static final Integer MAX_SEGMENT_SIZE = 1024;
    private FileInputStream fis;
    private BufferedReader peer_in;
    private BufferedOutputStream peer_out;
    private BufferedInputStream file_in;
    private String share_file;
    private BlockingQueue<Long> myQueue;

    public FileSender(Socket socket, String share_file, BlockingQueue<Long> myQueue)
    {
        this.socket = socket;
        File file = new File("");
        this.share_file = file.getAbsolutePath() + share_file;
        while(myQueue.size()%2 == 0)
        {
        	try {
				this.wait((long)20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        this.myQueue = myQueue;
        
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
        String line = null;
        String filename = null;
        long start_msg = 0;
        long end_msg = 0;
        byte[] buf = null;
        int read = 0;
        int len = 0;
        String path = null;

        try
        {
            peer_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            peer_out = new BufferedOutputStream(socket.getOutputStream());

            line = peer_in.readLine();
            String[] tokens = line.split(" ");
            //Separate the received command into corresponding fields according to protocol
            filename = tokens[1];
            Object[] myQueueArray = myQueue.toArray();
            boolean isPresentWithinSharedPortion = false;
            for(int lcv = 0; lcv < myQueueArray.length ; lcv++)
            {
            	long sampleStart = (Long) myQueueArray[lcv];
            	lcv++;
            	long sampleEnd = (Long) myQueueArray[lcv];
            	if(start_msg >= sampleStart && end_msg <= sampleEnd)
            	{
            		isPresentWithinSharedPortion = true;
            		break;
            	}
            }
            start_msg    = Long.parseLong(tokens[2]);
            end_msg      = Long.parseLong(tokens[3].substring(0,tokens[3].length()-1));
            len      = (int)(end_msg - start_msg);
            buf = new byte[len];
            if(isPresentWithinSharedPortion)
            {
            //Read the selected chunk from the file stream and push to the peer
            //path = combine(share_dir, filename);
            File file = new File(share_file);
            fis = new FileInputStream(share_file);
            file_in = new BufferedInputStream(fis);
            read = file_in.read(buf, (int) start_msg, len);
            peer_out.write(buf, 0, len);
            
            peer_in.close();
            peer_out.close();
            fis.close();
            file_in.close();
            }
            else
            {
            	String errorRetrun = "ferr";
            	peer_out.write(errorRetrun.getBytes());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        

    }
}
