package com.Hibernate.manager;



import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.Hibernate.Models.TrackerClientDetails;

public class UpdateRemoval extends Thread {
	
	private final long max_tracker_alive_time = 930;
	private final long thread_wait_time_ms = 10000;
	/*
	 * author: Venkata
	 * description: run the thread in an infinite loop and call removeUpdatedTracker method after every 10 sec
	 * 
	 */
	public void run()
	{
		while(true)
		{
			try {
				this.sleep(thread_wait_time_ms);
				this.removeOutdatedTrackers();
				new UpdateRemoval().removeOutdatedTrackers();
				
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
			
		}
	}

	/*
	 * author: venkata
	 * description: method to remove trackers that have a timestamp greater than 15mins.
	 */
	private void removeOutdatedTrackers() {
		SessionFactory sessionfactory = new Configuration().configure().buildSessionFactory();
		Session session = sessionfactory.openSession();
		Transaction transaction = session.getTransaction();
		transaction.begin();
		
		Query outdatedTrackersQuery = session.createSQLQuery("Select * from TrackerClientDetails where clientTimeStamp = DATE_SUB(CURDATE(),INTERVAL :parameter1 SECOND)");
		outdatedTrackersQuery.setParameter("parameter1", max_tracker_alive_time);
		
		List outdatedTrackersList = outdatedTrackersQuery.list();
		
		if(outdatedTrackersList.size() > 0)
		{
			for(int i = 0; i< outdatedTrackersList.size(); i++)
			{
				TrackerClientDetails outdatedClient = (TrackerClientDetails) outdatedTrackersList.get(i);
				session.delete(outdatedClient);
			}
		}
		
		transaction.commit();
		session.close();
		
	}

}
