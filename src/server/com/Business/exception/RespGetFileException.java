package server.com.Business.exception;

/*
 * author: Venkata Prashant
 * Description: This class is for representing an exception message for generating a response to get file tracker object from the received message
 * Version: 0.1
 */
public class RespGetFileException extends Exception {

	public RespGetFileException()
	{
		super("Exception in response file");
	}
}
