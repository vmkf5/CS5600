package com.Hibernate.Models;



import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;


/*
 * author: Venkata
 * Description: Java Bean class representing table tracker details. 
 * 				i.e. class variables represent an attribute  in the table.
 * 				used for connectivity with the database
 * 				methods used in this class are simple get and set methods for class variables
 */
@Entity
@Table(name = "trackerdetails")
public class TrackerDetails {

	@Id @GeneratedValue
	private int trackerId;
	private String trackerFileName;
	private int fileSize;
	private String trackerFileDescription;
	private String checkSum;
	
	@OneToMany(mappedBy = "trackerInfo")
	private List<TrackerClientDetails> clientDetails;
	
	
	public int getTrackerId() {
		return trackerId;
	}
	public void setTrackerId(int trackerId) {
		this.trackerId = trackerId;
	}
	public String getTrackerFileName() {
		return trackerFileName;
	}
	public void setTrackerFileName(String trackerFileName) {
		this.trackerFileName = trackerFileName;
	}
	public int getFileSize() {
		return fileSize;
	}
	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}
	public String getTrackerFileDescription() {
		return trackerFileDescription;
	}
	public void setTrackerFileDescription(String trackerFileDescription) {
		this.trackerFileDescription = trackerFileDescription;
	}
	public String getCheckSum() {
		return checkSum;
	}
	public void setCheckSum(String checkSum) {
		this.checkSum = checkSum;
	}
	public List<TrackerClientDetails> getClientDetails() {
		return clientDetails;
	}
	public void setClientDetails(List<TrackerClientDetails> clientDetails) {
		this.clientDetails = clientDetails;
	}
	
	
}
