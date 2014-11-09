package client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents the file trackers the server uses to hold sharing information.
 * TODO: make sure to ignore lines starting with #
 * Created by Levi Malott on 10/14/14.
 */
public class FileTracker
{
    private SharedFileDetails details = new SharedFileDetails();
    private ArrayList<PeerInfo> peers = new ArrayList<PeerInfo>();
    private final int NAME_IDX = 1;
    private final int SIZE_IDX = 2;
    private final int DESC_IDX = 3;
    private final int MD5_IDX  = 4;
    
    public SharedFileDetails getDetails() {
        return details;
    }

    public void setDetails(SharedFileDetails details) {
        this.details = details;
    }

    public ArrayList<PeerInfo> getPeers() {
        return peers;
    }

    public void setPeers(ArrayList<PeerInfo> peers) {
        this.peers = peers;
    }

    public boolean equals(Object other)
    {
        if(other == null)
        {
            return false;
        }
        if(this.getClass() != other.getClass())
        {
            return false;
        }
        if(this.details != ((FileTracker)other).getDetails())
        {
            return false;
        }
        if(this.peers != ((FileTracker) other).getPeers())
        {
            return false;
        }
        return true;
    }

    public FileTracker(ArrayList<String> messages)
    {
        if(messages != null)
		{
            int i = 0;
            int last = messages.size() - 1;
            String msg;
            String[] tokens;
            for(Iterator<String> it = messages.iterator(); it.hasNext(); )
            {
                msg = it.next();
                switch(i)
                {
                    case 0:
                        break;
                    case NAME_IDX:
                        tokens = msg.split(":");
                        details.filename = tokens[1].trim();
                        break;
                    case SIZE_IDX:
                        tokens = msg.split(":");
                        details.filesize = Long.parseLong(tokens[1].trim());
                        break;
                    case DESC_IDX:
                        tokens = msg.split(":");
                        details.description = tokens[1].trim();
                        break;
                    case MD5_IDX:
                        tokens = msg.split(":");
                        details.md5 = tokens[1].trim();
                        break;
                    default:
                        if( i != last )
                        {
                            peers.add(new PeerInfo(msg));
                        }
                }
                i++;
            }
        }
    }

    public FileTracker(File file)
    {
        List<String> lines = new ArrayList<String>();
        try {
            lines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        int i = 0;
        String[] tokens;
        for(String line : lines)
        {
            switch(i)
                {
                    case NAME_IDX:
                        tokens = line.split(":");
                        details.filename = tokens[1].trim();
                        break;
                    case SIZE_IDX:
                        tokens = line.split(":");
                        details.filesize = Long.parseLong(tokens[1].trim());
                        break;
                    case DESC_IDX:
                        tokens = line.split(":");
                        details.description = tokens[1].trim();
                        break;
                    case MD5_IDX:
                        tokens = line.split(":");
                        details.md5 = tokens[1].trim();
                        break;
                    default:
                        peers.add(new PeerInfo(line));
                }
            i++;
        }
    }
}

