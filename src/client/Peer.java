
import com.Business.models.updateTracker;
import com.Business.models.RespList;

public class Peer
{
    public void readConfig(String filename);
    public String sendCreateTracker(createFileTrackerMessage tracker);
    public String getFileTracker(String filename);
    public String sendUpdateTracker(updateTracker tracker);
    public void getSegment(String ip, String filename, String md5, Integer segment);
    public RespList getTrackerList();

}