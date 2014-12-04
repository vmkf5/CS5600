package server.com.Business.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import server.com.Business.exception.CreateTrackerException;
import server.com.Business.exception.GetFileTrackerMessageException;
import server.com.Business.exception.UpdateTrackerException;
import server.com.Business.models.RespCreateTracker;
import server.com.Business.models.RespGetFile;
import server.com.Business.models.RespList;
import server.com.Business.models.RespUpdateTracker;
import server.com.Business.models.createFileTrackerMessage;
import server.com.Business.models.getFileTracker;
import server.com.Business.models.updateTracker;
import server.com.File.manager.FileManager;

public class ClientWorker extends Thread {

	private Socket client;
	private Semaphore sem;


	public ClientWorker()
	{

	}

	public ClientWorker(Socket client, Semaphore sem) {
		this.client = client;
		this.sem = sem;
	}


	public void run()
	{
		String line = "";
		BufferedReader in = null;
		PrintWriter out = null;
		try
		{
			while(!client.isClosed())
			{
				try{
					in = new BufferedReader(new InputStreamReader(client.getInputStream()));
					line = in.readLine();

					System.out.println("read message" + line);
					out = new PrintWriter(client.getOutputStream(), true);
					if(line.contains("createtracker"))
					{
						System.out.println("Inside create tracker");
						try {
							RespCreateTracker resp = new RespCreateTracker();
							if(new FileManager(sem).executeCreateTracker(new createFileTrackerMessage(line)))
							{
								resp.setResponse("succ");
							}
							else
							{
								resp.setResponse("ferr");
							}
							System.out.println("create tracker result: " + resp);
							//System.out.println("client details" + client.);
							out.println(resp.toString());
							System.out.println("after sending message");

						} catch (CreateTrackerException e) {
							// TODO Auto-generated catch block
							System.out.println("Inside catch block");
							RespCreateTracker resp = new RespCreateTracker();
							resp.setResponse("fail");
							System.out.println(resp);
							out.println(resp.toString());
						}
					}
					else if(line.contains("updatetracker"))
					{
						System.out.println("Inside update tracker");
						try {
							RespUpdateTracker resp = new RespUpdateTracker();
							updateTracker message = new updateTracker(line);
							resp = new FileManager(sem).executeUpdateTracker(message);
							System.out.println(resp);
							out.println(resp.toString());
						} catch (UpdateTrackerException e) {
							RespUpdateTracker resp = new RespUpdateTracker();
							resp.setFileName("fail");
							resp.setResponse("fail");
							System.out.println(resp);
							out.println(resp.toString());
						}
					}
					else if(line.contains("REQ"))
					{
						System.out.println("Inside List");
						RespList resp = new RespList();
						resp = new FileManager(sem).executeList();
						System.out.println(resp);
						out.println(resp);

					}
					else if(line.contains("GET"))
					{
						System.out.println("Inside GET");
						RespGetFile resp = new RespGetFile();
						try {
							resp = new FileManager(sem).executeGet(new getFileTracker(line));
							System.out.println(resp.toString());
							out.println(resp.toString());
						} catch (GetFileTrackerMessageException e) {
							out.print("ResponseGetMessage ferr");
						}

					}
					else
					{
						System.out.println("did not get through");
					}


				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("in or out failed");
					System.exit(-1);
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("exiting...");
		}
	}


	//old method
	/*public void run(){
		String line = "";
		BufferedReader in = null;
		PrintWriter out = null;
		while(!client.isClosed())
		{
			try{
				in = new BufferedReader(new 
						InputStreamReader(client.getInputStream()));
				line = in.readLine();

				System.out.println("read message" + line);
				out = new PrintWriter(client.getOutputStream(), true);
				if(line.contains("createtracker"))
				{
					System.out.println("Inside create tracker");
					try {
						RespCreateTracker resp = new RespCreateTracker();
						if(new HibernateManager().executeCreateTracker(new createFileTrackerMessage(line)))
						{
							resp.setResponse("succ");
						}
						else
						{
							resp.setResponse("ferr");
						}
						System.out.println("create tracker result: " + resp);
						//System.out.println("client details" + client.);
						out.println(resp.toString());
						System.out.println("after sending message");

					} catch (CreateTrackerException e) {
						// TODO Auto-generated catch block
						System.out.println("Inside catch block");
						RespCreateTracker resp = new RespCreateTracker();
						resp.setResponse("fail");
						System.out.println(resp);
						out.println(resp.toString());
					}
				}
				else if(line.contains("updatetracker"))
				{
					System.out.println("Inside update tracker");
					try {
						RespUpdateTracker resp = new RespUpdateTracker();
						updateTracker message = new updateTracker(line);
						resp = new HibernateManager().executeUpdateTracker(message);
						System.out.println(resp);
						out.print(resp.toString());
					} catch (UpdateTrackerException e) {
						RespUpdateTracker resp = new RespUpdateTracker();
						resp.setFileName("fail");
						resp.setResponse("fail");
						System.out.println(resp);
						out.println(resp.toString());
					}
				}
				else if(line.contains("REQ"))
				{
					System.out.println("Inside List");
					RespList resp = new RespList();
					resp = new HibernateManager().executeList();
					System.out.println(resp);
					out.println(resp);

				}
				else if(line.contains("GET"))
				{
					System.out.println("Inside GET");
					RespGetFile resp = new RespGetFile();
					try {
						resp = new HibernateManager().executeGet(new getFileTracker(line));
						System.out.println(resp.toString());
						out.println(resp.toString());
					} catch (GetFileTrackerMessageException e) {
						out.print("ResponseGetMessage ferr");
					}

				}
				else
				{
					System.out.println("did not get through");
				}


			} catch (IOException e) {
				System.out.println("in or out failed");
				System.exit(-1);
			}
		}

	}*/

}
