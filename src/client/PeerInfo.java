package client;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by john on 10/14/14.
 */
public class PeerInfo
{
    public InetAddress ip;
    public Integer port;
    public Long start;
    public Long end;
    public Long time;

    public PeerInfo(String msg)
    {
        //Split the msg based on delimiter (:) while removing whitespace
        //Format: <ip address of the peer>:<port number>:<start byte>:<end byte>:<time stamp>
        String[] tokens = msg.split("\\s*:\\s*");


        try {
            ip = InetAddress.getByName(tokens[0]);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        port  = Integer.parseInt(tokens[1]);
        start = Long.parseLong(tokens[2]);
        end   = Long.parseLong(tokens[3]);
        time  = Long.parseLong(tokens[4]);

    }
}
