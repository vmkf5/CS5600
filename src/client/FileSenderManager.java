package client;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.concurrent.BlockingQueue;

/**
 * Handles incoming file download requests from peers in the background.
 * Each request spawns a new handler thread.
 * Created by Levi Malott on 10/19/14.
 */
public class FileSenderManager extends Thread
{
    private String share_file;
    private int port;
    private ServerSocket server;
   private BlockingQueue<Long> myQueue;
   
    public FileSenderManager(String share_file, int port, BlockingQueue myQueue)
    {
        this.share_file = share_file;
        this.port = port;
        this.myQueue = myQueue;
    }


    @Override
    public void run()
    {
        try {
            server = new ServerSocket(port);
        } catch(BindException e)
        {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true)
        {
            FileSender sender;
            try
            {
                sender = new FileSender(server.accept(), share_file, myQueue);
                Thread t = new Thread(sender);
                t.start();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e)
            {
                e.printStackTrace();
            }
        }
    }
}
