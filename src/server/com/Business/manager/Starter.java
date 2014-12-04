package server.com.Business.manager;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Semaphore;

import server.com.File.manager.Removal15Mins;



public class Starter {
	
	public static Semaphore sem = new Semaphore(1);
	
	String relativePath;

	public Starter(String relativePath)
	{
		this.relativePath = relativePath;
	}
	
	public void ListenSocket()
	{
		ServerSocket server = null;
		try{
			server = new ServerSocket(4000);
			server.setReceiveBufferSize(10000);
		  } catch (IOException e) {
		    System.out.println("Could not listen on port 4000");
		    System.exit(-1);
		  }
		  while(true){
		    ClientWorker w;
		    try{
		    	//server.accept returns a client connection
		      w = new ClientWorker(server.accept(),sem, relativePath);
		      Thread t = new Thread(w);
		      t.start();
		    } catch (IOException e) {
		      System.out.println("Accept failed: 4000");
		      System.exit(-1);
		    }
		  }
	}
	
	/*public static void main(String[] args)
	{
		
		//UpdateRemoval thread1 = new UpdateRemoval();
		//thread1.start();
	}*/
}
