package client;
import server.com.Business.exception.CreateTrackerException;
import server.com.Business.models.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class Peer
{
    private Integer server_port;
    private InetAddress server_ip;
    private Integer my_port;
    private Integer refresh_rate;
    private String  share_dir;
    private Integer segment_size;
    private HashMap<String,SharedFileDetails> shared_files;
    private ArrayList<FileTracker> current_downloads;
    private InetAddress my_ip;
    private static String REQ_LIST = "<REQ LIST>\n";
    private RespList tracker_list;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Response response;
    private ArrayList<String> message = new ArrayList<String>();

    public Peer(String filename)
    {
        init();
        readConfig(filename);
        connectToServer();
        initSharedFiles();
    }

    public void init()
    {
        server_port  = null;
        server_ip    = null;
        my_port      = null;
        refresh_rate = null;
        share_dir    = null;
        segment_size = null;
        shared_files = new HashMap<String, SharedFileDetails>();
        current_downloads = new ArrayList<FileTracker>();
        out          = null;
        in           = null;
        tracker_list = null;
        try {
            my_ip = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void readConfig(String filename)
    {
        Properties prop = new Properties();
        InputStream input = null;
        try
        {
            input = new FileInputStream("config.properties");
            prop.load(input);

            this.server_port = Integer.parseInt(prop.getProperty("server_port", "8081"));
            this.server_ip   = InetAddress.getByName(prop.getProperty("server_ip", "localhost"));
            this.my_port   = Integer.parseInt(prop.getProperty("peer_port", "8080"));
            this.refresh_rate= Integer.parseInt(prop.getProperty("refresh_rate", "60"));
            this.share_dir   = prop.getProperty("share_dir", "shares");
            this.segment_size= Integer.parseInt(prop.getProperty("segment_size"));
            //TODO: Add checks for valid configuration entries, close on errors
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally
        {
            if (null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void initSharedFiles()
    {
        File f = new File(this.share_dir);
        String[] files = f.list();
        for (String filename : files)
        {
            CallbackFileInit d = new CallbackFileInit(filename, " ", this);
            Thread t = new Thread(d);
            t.start();
        }
    }

    public synchronized void initFile(String filename, SharedFileDetails info)
    {
        shared_files.put(filename, info);
        sendCreateTracker(info);
    }

    public String connectToServer()
    {
        try
        {
            socket = new Socket(server_ip, server_port);
            out    = new PrintWriter(socket.getOutputStream(), true);
            in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (UnknownHostException e)
        {
           System.out.println("Unknown host: " + server_ip.toString());
           System.exit(1);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        return "";
    }

    public String sendCreateTracker(SharedFileDetails info)
    {
        createFileTrackerMessage msg = null;
        String resp = null;
        try {
             msg = new createFileTrackerMessage(info.filename, info.filesize, info.description,
                    info.md5, this.my_ip.toString(), this.my_port);
        } catch (CreateTrackerException e) {
            e.printStackTrace();
        }
        out.println(msg.toString());
        try {
            resp = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        response = Response.fromString(resp);
        if(response == null)
        {
            System.out.println("Unknown response from server from: " + msg.toString());
            System.exit(1);
        }

        switch(response)
        {
            case CT_SUCCESS:
                System.out.println("Successfully created tracker for " + info.filename);
                break;
            case CT_FAIL:
                System.out.println("Server failed to create tracker for " + info.filename);
                break;
            case CT_EXISTS:
                System.out.println("tracker for " + info.filename + "already exists" );
                break;
            default: break;
        }
        return "";
    }
    public String getFileTracker(String filename)
    {
        String msg = "<GET " + filename + ".track>\n";
        recvFromServer();
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
        String content =  message.subList(1,message.size()-1).toString().replaceAll("\\[|\\]", "").replaceAll(", ", "\n");
        content = getMd5(content);
        if( content.equals(md5_recvd))
        {
            current_downloads.add(new FileTracker(message));
        }
        else {
            System.out.println("MD5 values for response to " + msg + "do not match.");
        }

        message.clear();
        return "";
    }
    public String sendUpdateTracker(updateTracker tracker)
    {
        return "";
    }
    public String getSegment(String ip, String filename, String md5, Integer segment)
    {

        return "";
    }
    /*
    Call after sending a message to the server. Stores all incoming messages into the String ArrayList message.
     */
    public void recvFromServer()
    {
        String resp = null;
        try {
            while( (resp = in.readLine()) != null )
            {
                message.add(resp);
            }
        } catch (IOException e) {
            System.out.println("Communication with server was interrupted. Exiting..");
            System.exit(1);
        }
    }
    public void getTrackerList()
    {

        if( socket == null )
        {
            System.out.println("Socket was unexpectedly closed. Exiting..");
            System.exit(1);
        }

        out.println(REQ_LIST);

        if( message.size() == 0 )
        {
            System.out.println("Server did not respond to " + REQ_LIST);
            System.exit(1);
        }

        tracker_list = new RespList(message);
        tracker_list.print();
        //clear the message queue since we've processed them
        message.clear();
    }
    /*
    Obtained from http://javarevisited.blogspot.com/2013/03/generate-md5-hash-in-java-string-byte-array-example-tutorial.html
    */
    private String getMd5(String msg) {
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
}
