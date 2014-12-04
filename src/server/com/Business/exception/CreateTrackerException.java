package server.com.Business.exception;

/*
 * author: Venkata Prashant
 * Description: This class is for representing an exception message for create tracker object from received message.
 * Version: 0.1
 */
public class CreateTrackerException extends Exception {

	public CreateTrackerException ()
	{
		super("create Tracker message is corrupted");
	}
}
