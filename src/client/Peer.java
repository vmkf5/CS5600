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
    private String my_ip;
    private static String REQ_LIST = "<REQ LIST>\n";
    private static Integer MAX_SEGMENT_SIZE = 1024;
    private RespList tracker_list;
    private FileSenderManager fsManager;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Response response;
    private ArrayList<String> message = new ArrayList<String>();

    /**
     * Constructor
     * @param filename name of the configuration file
     */
    public Peer(String filename)
    {
        init();
        readConfig(filename);
        connectToServer();
        initSharedFiles();
    }

    /**
     * Initializes the class attribute to know values
     */
    private void init()
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
            my_ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a configuration file at 'filename' and initializes corresponding attributes.
     * @param filename
     */
    public void readConfig(String filename)
    {
        Properties prop = new Properties();
        InputStream input = null;
        try
        {
            input = new FileInputStream(filename);
            prop.load(input);

            this.server_port = Integer.parseInt(prop.getProperty("server_port", "8080"));
            this.server_ip   = InetAddress.getByName(prop.getProperty("server_ip", "localhost"));
            this.my_port   = Integer.parseInt(prop.getProperty("peer_port", "8081"));
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

    /**
     * Reads the shared directory, creates the MD5 hash for every file, and sends create tracker
     * messages to the server for each file shared.
     */
    private void initSharedFiles()
    {
        File f = new File(this.share_dir);
        String[] files = f.list();
        for (String filename : files)
        {
            CallbackFileInit d = new CallbackFileInit(share_dir,filename, " ", this);
            Thread t = new Thread(d);
            t.start();
        }
    }

    /**
     * Updates the shared file records and sends a create tracker to the server
     * @param filename
     * @param info
     */
    public synchronized void initFile(String filename, SharedFileDetails info)
    {
        shared_files.put(filename, info);
        sendCreateTracker(info);
    }

    /**
     * Connects to the server located at the IP address and port given in the configuration file.
     * @return
     */
    public String connectToServer()
    {
        try
        {
            socket = new Socket(server_ip, server_port);
            out    = new PrintWriter(socket.getOutputStream(), true);
            in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connected to server:" + server_ip.toString());
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

    /**
     * Starts the file sending manager as a background task to accept incoming queries and
     * transmit file segments.
     */
    public void startFileSenderManager()
    {
        fsManager = new FileSenderManager(share_dir, my_port);
        Thread t = new Thread(fsManager);
        t.start();
    }

    /**
     * Thread-safe getter for obtaining the shared directory name/path
     * @return
     */
    public synchronized String getSharedDir()
    {
        return this.share_dir;
    }

    /**
     * Sends a create tracker to the server with the details provided in info
     * @param info attributes of the file
     * @return
     */
    public String sendCreateTracker(SharedFileDetails info)
    {
        createFileTrackerMessage msg = null;
        String resp = null;
        try {
             msg = new createFileTrackerMessage(info.filename, info.filesize, info.description,
                    info.md5, this.my_ip, this.my_port);
        } catch (CreateTrackerException e) {
            e.printStackTrace();
        }
        out.println(msg.toString());
        System.out.println("Sent message: " + msg.toString());
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

        System.out.println(resp);

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

    public void getFileTracker(Integer i)
    {
        String filename = tracker_list.getFilenameAt(i);
        this.getFileTracker(filename);
    }

    /**
     * Requests a specific file tracker from the server. If successful, file download begins immediately
     * @param filename name of the desired file
     * @return
     */
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
        //To compute the MD5 hash, the header and tail of the message are ignored
        //then put all the data into one string and compute the hash
        String content =  message.subList(1,message.size()-1).toString().replaceAll("\\[|\\]", "").replaceAll(", ", "\n");
        content = content.substring(1,content.length()-1);
        content = getMd5(content);
        if( content.equals(md5_recvd))
        {
            FileTracker tracker = new FileTracker(message);
            current_downloads.add(tracker);
            this.downloadFile(tracker);
        }
        else {
            System.out.println("MD5 values for response to " + msg + "do not match.");
        }

        message.clear();
        return "";
    }

    /**
     * Attempts to download the file designated by the provided tracker
     * @param tracker tracker received from server
     */
    public void downloadFile(FileTracker tracker)
    {
        Thread t = new Thread(new FileDownloader(tracker, this.segment_size, this));
        t.start();
    }

    /**
     * Removes a file from the current download queue. (Thread safe)
     * @param tracker
     */
    public synchronized void removeFileFromQueue(FileTracker tracker)
    {
        current_downloads.remove(tracker);
    }

   /**
    * Updates the Peer's shared file record to reflect a change in status such as more bytes being downloaded.
    * Thread safe.
    * @param file_info  the updated information of respective file
     */
    public synchronized void updateSharedFileDetails(SharedFileDetails file_info)
    {
        String filename = file_info.getFilename();
        shared_files.put(filename, file_info);
    }

    public String sendUpdateTracker(updateTracker tracker)
    {
        return "";
    }

    /*
    Call after sending a message to the server. Stores all incoming messages into queue.
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

    /**
     * Sends a LIST command to the connected server and prints the results.
     */
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

    public void close()
    {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.close();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args)
    {
        Peer peer = new Peer("/home/levi/IdeaProjects/CS5600/src/data/config.properties");
        peer.startFileSenderManager();


    }
}
