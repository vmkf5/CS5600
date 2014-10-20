package client;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Handles incoming file download requests from peers in the background.
 * Each request spawns a new handler thread.
 * Created by Levi Malott on 10/19/14.
 */
public class FileSenderManager implements Runnable
{
    private String share_dir;
    private Integer port;
    private ServerSocket server;

    public FileSenderManager(String share_dir, Integer port)
    {
        this.share_dir = share_dir;
        this.port = port;
    }


    @Override
    public void run()
    {
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true)
        {
            FileSender sender;
            try
            {
                sender = new FileSender(server.accept(), share_dir);
                Thread t = new Thread(sender);
                t.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
