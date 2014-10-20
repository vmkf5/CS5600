package client;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Represents the peers hosting a specific file. Contains their IP address, port number, and
 * which bytes they have.
 *
 * Created by Levi Malott on 10/14/14.
 */
public class PeerInfo
{
    public InetAddress ip;
    public Integer port;
    public Long start;
    public Long end;
    public Long time;

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(String ip) {
        try {
            this.ip = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }


    public boolean equals(Object other)
    {
        if (other == null) {
            return false;
        }
        if (this.getClass() != other.getClass()) {
            return false;
        }
        if( this.ip != ((PeerInfo)other).getIp())
        {
            return false;
        }
        if( this.port != ((PeerInfo)other).getPort())
        {
            return false;
        }
        if( this.start != ((PeerInfo)other).getStart())
        {
            return false;
        }
        if( this.end != ((PeerInfo)other).getEnd())
        {
            return false;
        }
        if( this.time != ((PeerInfo)other).getTime())
        {
            return false;
        }
        return true;
    }

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
