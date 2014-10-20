package client;
import server.com.Business.exception.CreateTrackerException;
import server.com.Business.models.*;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

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
    private static String REQ_LIST = "<REQ LIST>";
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
    public Peer(String filename) {
        init();
        readConfig(filename);
        connectToServer();
        //initSharedFiles();
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
            my_ip = this.getLocalHostLANAddress().toString();
            System.out.println("IP: " + my_ip);
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
            System.out.println(server_port.toString());
            //TODO: Add checks for valid configuration entries, close on errors
        } catch (FileNotFoundException e) {
            System.out.println("Could not find the configuration file at " + filename);
            System.exit(1);
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
     * Returns the extension of a file even if there are multiple '.'s present.
     * Retrieved from: http://stackoverflow.com/questions/3571223/how-do-i-get-the-file-extension-of-a-file-in-java
     * @param filename name of the file to get the extension of
     * @return extension of the file
     */
    private static final String getExtension(final String filename)
    {
        if (filename == null) return null;
        final String afterLastSlash = filename.substring(filename.lastIndexOf('/') + 1);
        final int afterLastBackslash = afterLastSlash.lastIndexOf('\\') + 1;
        final int dotIndex = afterLastSlash.indexOf('.', afterLastBackslash);
        return (dotIndex == -1) ? "" : afterLastSlash.substring(dotIndex + 1);
    }

    private static final String getLastExtension(final String filename)
    {
        if(filename == null) return null;
        int i = filename.lastIndexOf('.');
        if(i >= 0)
        {
            return filename.substring(i+1);
        }
        else
        {
            return "";
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
            if( !"track".equals(getLastExtension(filename)))
            {
                CallbackFileInit d = new CallbackFileInit(share_dir, filename, " ", this);
                Thread t = new Thread(d);
                t.start();
            }
        }
    }

    /**
     * Returns an <code>InetAddress</code> object encapsulating what is most likely the machine's LAN IP address.
     * <p/>
     * This method is intended for use as a replacement of JDK method <code>InetAddress.getLocalHost</code>, because
     * that method is ambiguous on Linux systems. Linux systems enumerate the loopback network interface the same
     * way as regular LAN network interfaces, but the JDK <code>InetAddress.getLocalHost</code> method does not
     * specify the algorithm used to select the address returned under such circumstances, and will often return the
     * loopback address, which is not valid for network communication. Details
     * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037">here</a>.
     * <p/>
     * This method will scan all IP addresses on all network interfaces on the host machine to determine the IP address
     * most likely to be the machine's LAN address. If the machine has multiple IP addresses, this method will prefer
     * a site-local IP address (e.g. 192.168.x.x or 10.10.x.x, usually IPv4) if the machine has one (and will return the
     * first site-local address if the machine has more than one), but if the machine does not hold a site-local
     * address, this method will return simply the first non-loopback address found (IPv4 or IPv6).
     * <p/>
     * If this method cannot find a non-loopback address using this selection algorithm, it will fall back to
     * calling and returning the result of JDK method <code>InetAddress.getLocalHost</code>.
     * <p/>
     * Retrieved from: https://issues.apache.org/jira/browse/JCS-40
     * This is a known problem on Linux hossts, where the system always returns the localhost address.
     *
     * @throws UnknownHostException If the LAN address of the machine cannot be found.
     */
    private static InetAddress getLocalHostLANAddress() throws UnknownHostException {
        try {
            InetAddress candidateAddress = null;
            // Iterate all NICs (network interface cards)...
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // Iterate all IP addresses assigned to each card...
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {

                        if (inetAddr.isSiteLocalAddress()) {
                            // Found non-loopback site-local address. Return it immediately...
                            return inetAddr;
                        }
                        else if (candidateAddress == null) {
                            // Found non-loopback address, but not necessarily site-local.
                            // Store it as a candidate to be returned if site-local address is not subsequently found...
                            candidateAddress = inetAddr;
                            // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
                            // only the first. For subsequent iterations, candidate will be non-null.
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                // We did not find a site-local address, but we found some other non-loopback address.
                // Server might have a non-site-local address assigned to its NIC (or it might be running
                // IPv6 which deprecates the "site-local" concept).
                // Return this non-loopback candidate address...
                return candidateAddress;
            }
            // At this point, we did not find a non-loopback address.
            // Fall back to returning whatever InetAddress.getLocalHost() returns...
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress;
        }
        catch (Exception e) {
            UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address: " + e);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }
    /**
     * Updates the shared file records and sends a create tracker to the server
     * @param filename name of the shared file
     * @param info information record of the file
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
    private void sendToServer(String msg)
    {
        out.println(msg);
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

        try {
            out.println(msg.toString());
            System.out.println("Sent message: " + msg.toString());
            resp = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Socket unexpectedly closed.");
            this.close();
            System.exit(1);
        }

        System.out.println(resp);

        if(resp.equals("<createtracker succ>"))
        {
            System.out.println("Successfully created tracker for " + info.filename);
        }
        else if ( resp.equals("<createtracker fail>"))
        {
            System.out.println("Server failed to create tracker for " + info.filename);
        }
        else if( resp.equals("<createtracker ferr>"))
        {
            System.out.println("tracker for " + info.filename + " already exists" );
        }
        else
        {
            System.out.println("Shouldn't hit this case");
        }

        return "";
    }

    public void getFileTracker(Integer i)
    {
        String filename = tracker_list.getFilenameAt(i-1);
        this.getFileTracker(filename);
    }

    /**
     * Requests a specific file tracker from the server. If successful, file download begins immediately
     * @param filename name of the desired file
     * @return
     */
    public String getFileTracker(String filename)
    {
        message.clear();
        String msg = "<GET " + filename + ".track>";
        sendToServer(msg);
        recvFromServer("GET END");
        if( message.size() == 0 )
        {
           System.out.println("Server did not response to " + msg);
           System.exit(1);
        }

        //obtain the MD5 from the server response
        for(String e : message)
        {
            System.out.println(e);
        }
        String md5_recvd = message.get(message.size()-1).split(" ")[3];
        //remove the pesky '>' character
        md5_recvd = md5_recvd.substring(0, md5_recvd.length()-1);

        //To compute the MD5 hash, the header and tail of the message are ignored
        //then put all the data into one string and compute the hash
        String content =  message.subList(1,message.size()-1).toString().replaceAll("\\[|\\]", "").replaceAll(", ", "\n") + "\n";
        System.out.println(content);
        content = getMd5(content);
        if( content.equals(md5_recvd))
        {
            FileTracker tracker = new FileTracker(message);
            current_downloads.add(tracker);
            saveTrackerToCache(tracker);
            this.downloadFile(tracker);
        }
        else {
            System.out.println("MD5 values for response to " + msg + "do not match.");
            System.out.println("Expected: " + md5_recvd);
            System.out.println("Actual: " + content);
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

    public synchronized void downloadComplete(FileTracker tracker)
    {
       removeFileFromQueue(tracker);
       deleteTrackerFromCache(tracker);
       System.out.println("Successfully downloaded " + tracker.getDetails().getFilename());
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
    public void recvFromServer(String expected_end)
    {
        String resp = null;
        try {
            while( !(resp = in.readLine()).contains(expected_end) )
            {
                message.add(resp);
            }
            message.add(resp);
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
        message.clear();
        if( socket == null )
        {
            System.out.println("Socket was unexpectedly closed. Exiting..");
            System.exit(1);
        }
        System.out.println("Sent message: " + REQ_LIST);

        out.println(REQ_LIST);
        recvFromServer("REP LIST END");
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

    /**
     * Saves the tracker file to the shared directory
     * @param tracker tracker to save in txt format
     */
    public void saveTrackerToCache(FileTracker tracker)
    {
        SharedFileDetails details = tracker.getDetails();
        String path = combine(share_dir, details.getFilename() + ".track");
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(path);
            writer.println("Filename: " + details.filename);
            writer.println("Filesize: " + details.filesize);
            writer.println("Description: " + details.getDescription());
            writer.println("MD5: " + details.getMd5());
            for (PeerInfo peer : tracker.getPeers())
            {
                writer.println(peer.toString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(writer != null) {
            writer.flush();
            writer.close();
        }

    }

    public void deleteTrackerFromCache(FileTracker tracker)
    {
        String path = combine(share_dir, tracker.getDetails().getFilename() + ".track");
        File file = new File(path);
        if(file.delete())
        {
            System.out.println("Deleted " + tracker.getDetails().getFilename() + "tracker");
        }
        else
        {
            System.out.println("Could not delete " + tracker.getDetails().getFilename() + "tracker");
        }
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

    private static String combine(String path1, String path2)
    {
        File f1 = new File(path1);
        File f2 = new File(f1, path2);
        return f2.getPath();
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
        //peer.close();
        //peer.getTrackerList();
        peer.getFileTracker("qute.jpg");
    }
}
