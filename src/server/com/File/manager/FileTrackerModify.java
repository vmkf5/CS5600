package server.com.File.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import server.com.File.Models.FileTracker;
import server.com.File.Models.PeerInfo;
import server.com.File.Models.SharedFileDetails;

public class FileTrackerModify {
	
	public String classpath = new File("").getAbsolutePath();
	public String section = "/src/server/com/Data/server/";
	public String fileTracker_FileName = "FileName:";
	public String fileTracker_FileSize  = "Filesize:";
	public String fileTracker_Description = "Description:";
	public String fileTracker_MD5 = "MD5:";
	
	
	public FileTrackerModify(String section)
	{
		this.section = section;
	}
	
	public synchronized FileTracker read(String fileName)
	{
		File file = new File(classpath + section + fileName);
		
		
		if(file.isDirectory() || !file.canRead())
		{
			return null;
		}
		
		List<String> lines = new ArrayList<String>();
		try {
			lines = Files.readAllLines(file.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FileTracker fileTracker = new FileTracker();
		SharedFileDetails details = new SharedFileDetails();
		ArrayList<PeerInfo> peers = new ArrayList<PeerInfo>();
		for(String line : lines)
		{
		
			if(!line.contains("#"))
			{
				if(line.contains(fileTracker_FileName))
					details.setFilename(line.split(":")[1]);
				else if (line.contains(fileTracker_FileSize))
					details.setFilesize(new Long(line.split(":")[1]));
				else if(line.contains(fileTracker_Description))
					details.setDescription(line.split(":")[1]);
				else if(line.contains(fileTracker_MD5))
					details.setMd5(line.split(":")[1]);
				else
				{
					PeerInfo peer = new PeerInfo(line);
					peers.add(peer);
				}
			}
		}
		
		fileTracker.setDetails(details);
		fileTracker.setPeers(peers);
		
		return fileTracker;
	}

	public synchronized String write(FileTracker fileTracker)
	{
		File file = new File(classpath + section + fileTracker.getDetails().getFilename() + ".tracker");
		if(file.exists() && !file.isDirectory())
		{
			return "ferr";
		}
		
		try {
			PrintWriter print = new PrintWriter(classpath + section + fileTracker.getDetails().getFilename() + ".tracker");
			print.println(fileTracker_FileName + fileTracker.getDetails().getFilename());
			print.println(fileTracker_FileSize + fileTracker.getDetails().getFilesize());
			print.println(fileTracker_Description + fileTracker.getDetails().getDescription());
			print.println(fileTracker_MD5 + fileTracker.getDetails().getMd5());
			for(Iterator<PeerInfo> ite = fileTracker.getPeers().iterator(); ite.hasNext();)
			{
				print.println(ite.next().toString());
			}
			
			print.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "fail";
		}
		
		
		return "succ";
	}
	
	public boolean fileDelete(String fileName)
	{
		File file = new File(classpath + section + fileName);
		try {
			Files.delete(file.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
		
	}
	
}
