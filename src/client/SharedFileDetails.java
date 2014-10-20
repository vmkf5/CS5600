package client;

/**
 * Stores the details of a shared file or file segment.
 *
 * Created by Levi Malott on 10/14/14.
 */
public class SharedFileDetails
{
    public String filename;
    public Long start;
    public Long end;
    public Long filesize;
    public String md5;
    public String description;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
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

    public Long getFilesize() {
        return filesize;
    }

    public void setFilesize(Long filesize) {
        this.filesize = filesize;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

       if(this.md5 != ((SharedFileDetails)other).getMd5())
       {
           return false;
       }
       if(this.filesize != ((SharedFileDetails)other).getFilesize())
       {
           return false;
       }
       if(this.filename != ((SharedFileDetails)other).getFilename())
       {
           return false;
       }
       if(this.start != ((SharedFileDetails)other).getStart())
       {
           return false;
       }
       if(this.end != ((SharedFileDetails)other).getEnd())
       {
           return false;
       }
       return true;
    }

    public SharedFileDetails(SharedFileDetails other)
    {
        this.setDescription(other.getDescription());
        this.setEnd(other.getEnd());
        this.setStart(other.getStart());
        this.setFilename(other.getFilename());
        this.setFilesize(other.getFilesize());
    }

    public SharedFileDetails()
    {

    }
}
