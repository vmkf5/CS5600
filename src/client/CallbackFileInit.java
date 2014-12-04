package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import server.com.File.Models.SharedFileDetails;

/**
 * Created by Levi Malott on 10/14/14.
 */
public class CallbackFileInit implements Runnable
{
    private String filename;
    private Peer callback;
    private String desc;
    private String share_dir;

    public CallbackFileInit(String share_dir, String filename, String desc, Peer peer)
    {
        this.filename = filename;
        this.callback = peer;
        this.desc     = desc;
        this.share_dir = share_dir;
    }

    private static String combine(String path1, String path2)
    {
        File f1 = new File(path1);
        File f2 = new File(f1, path2);
        return f2.getPath();
    }


    @Override
    public void run()
    {
        SharedFileDetails info = new SharedFileDetails();
        File f = new File(combine(share_dir, filename));
        info.filesize = f.length();
        info.filename = filename;
        info.description = "desc";


        try
        {
            FileInputStream in = new FileInputStream(f);
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
