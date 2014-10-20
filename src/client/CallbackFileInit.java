package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Levi Malott on 10/14/14.
 */
public class CallbackFileInit implements Runnable
{
    private String filename;
    private Peer callback;
    private String desc;

    CallbackFileInit(String filename, String desc, Peer callback)
    {
        this.filename = filename;
        this.callback = callback;
        this.desc     = desc;
    }
    @Override
    public void run()
    {
        SharedFileDetails info = new SharedFileDetails();
        File f = new File(filename);
        info.filesize = f.length();
        info.filename = filename;
        info.end = info.filesize-1;
        info.start = Long.valueOf(0);
        info.description = "";

        try
        {
            FileInputStream in = new FileInputStream(filename);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            DigestInputStream din = new DigestInputStream(in, md5);
            while (din.read() != -1);
            din.close();
            byte[] digest = md5.digest();
            StringBuilder sb = new StringBuilder(2 * digest.length);
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            info.md5 = sb.toString();
        }
        catch (IOException ex)
        {
            System.err.println(ex);
        }
        catch (NoSuchAlgorithmException ex)
        {
            System.err.println(ex);
        }
        callback.initFile(filename, info);
    }
}
