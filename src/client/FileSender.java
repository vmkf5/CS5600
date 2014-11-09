package client;

import java.io.*;
import java.net.Socket;

/**
 * Runnable class to handle sending a file segment to a connected client.
 *
 * Created by Levi Malott on 10/19/14.
 */
public class FileSender implements Runnable
{
    private Socket socket;
    private static final Integer MAX_SEGMENT_SIZE = 1024;
    private BufferedReader peer_in;
    private BufferedOutputStream peer_out;
    private String share_dir;

    public FileSender(Socket socket, String share_dir)
    {
        this.socket = socket;
        this.share_dir = share_dir;
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
        Long start = null;
        Long end = null;
        byte[] buf = null;
        int read = 0;
        int len = 0;
        String path = null;
        RandomAccessFile file;

        try
        {
            peer_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            peer_out = new BufferedOutputStream(socket.getOutputStream());

            line = peer_in.readLine();
            System.out.println("FileSender Received: " + line);
            String[] tokens = line.split(" ");
            //Separate the received command into corresponding fields according to protocol
            filename = tokens[1];
            System.out.println("FileSender transmitting " + filename + "...");

            start    = Long.parseLong(tokens[2]);
            end      = Long.parseLong(tokens[3].substring(0,tokens[3].length()-1));
            len      = (int)(end - start);
            buf = new byte[len];

            //Read the selected chunk from the file stream and push to the peer
            path = combine(share_dir, filename);
            file = new RandomAccessFile(path, "r");
            file.seek(start);
            read = file.read(buf, 0, len);
            peer_out.write(buf, 0, read);
            peer_out.flush();
            peer_in.close();
            peer_out.close();
            file.close();        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
