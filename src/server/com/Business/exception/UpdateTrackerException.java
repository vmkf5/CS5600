package server.com.Business.exception;

/*
 * author: Venkata Prashant
 * Description: This class is for representing an exception message for generating update tracker object from the received message
 * Version: 0.1
 */
public class UpdateTrackerException extends Exception{

	public UpdateTrackerException()
	{
		super("Update Tracker message is corrupted");
	}
	
}
