package com.Hibernate.Models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/*
 * author: Venkata
 * Description: Java Bean class representing table tracker client details. 
 * 				i.e. class variables represent an attribute  in the table.
 * 				used for connectivity with the database
 * 				methods used in this class are simple get and set methods for class variables
 */
@Entity
@Table(name = "trackerclientdetails")
public class TrackerClientDetails {
	
	@Id
	@GeneratedValue
	private int trackerClientId;
	
	@ManyToOne
	@JoinColumn(name = "trackerId")
	private TrackerDetails trackerInfo;
	
	private String clientIp;
	private int clientPortNumber;
	private int fileStartByte;
	private int fileEndByte;
	@Temporal(TemporalType.TIMESTAMP)
	private Date clientTimeStamp;
	
	
	public int getTrackerClientId() {
		return trackerClientId;
	}
	public void setTrackerClientId(int trackerClientId) {
		this.trackerClientId = trackerClientId;
	}
	public TrackerDetails getTrackerInfo() {
		return trackerInfo;
	}
	public void setTrackerInfo(TrackerDetails trackerInfo) {
		this.trackerInfo = trackerInfo;
	}
	public String getClientIp() {
		return clientIp;
	}
	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}
	public int getClientPortNumber() {
		return clientPortNumber;
	}
	public void setClientPortNumber(int clientPortNumber) {
		this.clientPortNumber = clientPortNumber;
	}
	public int getFileStartByte() {
		return fileStartByte;
	}
	public void setFileStartByte(int fileStartByte) {
		this.fileStartByte = fileStartByte;
	}
	public int getFileEndByte() {
		return fileEndByte;
	}
	public void setFileEndByte(int fileEndByte) {
		this.fileEndByte = fileEndByte;
	}
	public Date getClientTimeStamp() {
		return clientTimeStamp;
	}
	public void setClientTimeStamp(Date clientTimeStamp) {
		this.clientTimeStamp = clientTimeStamp;
	}
	
	
	
	

}
