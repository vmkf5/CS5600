package client;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Levi Malott on 10/14/14.
 */
public class FileTracker
{
    private SharedFileDetails file = new SharedFileDetails();
    private ArrayList<PeerInfo> peers = new ArrayList<PeerInfo>();
    private final int NAME_IDX = 1;
    private final int SIZE_IDX = 2;
    private final int DESC_IDX = 3;
    private final int MD5_IDX  = 4;

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
                        file.filename = tokens[1];
                        break;
                    case SIZE_IDX:
                        tokens = msg.split(":");
                        file.filesize = Long.parseLong(tokens[1]);
                        break;
                    case DESC_IDX:
                        tokens = msg.split(":");
                        file.description = tokens[1];
                        break;
                    case MD5_IDX:
                        tokens = msg.split(":");
                        file.md5 = tokens[1];
                        break;
                    default:
                        if( i != last )
                        {
                            tokens = msg.split(" ");
                            String filename = tokens[1];
                            String filesize = tokens[2];
                            String checksum = tokens[3].substring(0, tokens[3].length() - 1);
                        }
                }
                i++;
            }
        }
    }
}

