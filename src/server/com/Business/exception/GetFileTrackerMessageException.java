package server.com.Business.exception;

/*
 * author: Venkata Prashant
 * Description: This class is for representing an exception message for generating a getFileTracker object from the received message
 * Version: 0.1
 */
public class GetFileTrackerMessageException extends Exception {
	
	public GetFileTrackerMessageException()
	{
		super("Get file tracker message is corrupted");
	}

}
