package com.lutshe;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.Log;

public class NotificationProvider {

	private static NotificationProvider instance;
	
	private NotificationTemplate[] notifications;
	private Database database;
	
	private NotificationProvider(Resources resources, Context context) {
		database = new Database(context);
		loadNotifications(resources, database);
	}
	
	private synchronized void loadNotifications(Resources resources, Database database) {
		
		int count = database.getTemplatesCount();

		TypedArray imgs = resources.obtainTypedArray(R.array.icons);
		
		if (count == 0) {
			String[] messages = resources.getStringArray(R.array.messages);
			
			for (int i = 0; i < messages.length; i++) {
				String[] strings = messages[i].split("\\|");
				int icon = Integer.valueOf(strings[3]);
				
				database.addItem(i, strings[0], strings[1], strings[2], icon);
			}
		}
		
		count = database.getTemplatesCount();
		if (count == 0) Log.wtf("wtf????", "really, wtf?");
		
		notifications = new NotificationTemplate[count];
		
		for (int i = 0; i < notifications.length; i++) {
			
			NotificationTemplate nt = database.getItem(i);
			int icon = imgs.getResourceId(nt.getIcon(), -1);
			nt.setIcon(icon);
			notifications[i] = nt;
			
		}
		
	}
	
	public synchronized static NotificationProvider getInstance(Resources resources, Context context) {
		if (instance == null) {
			instance = new NotificationProvider(resources, context);
		}
		
		return instance;
	}
	
	public synchronized NotificationTemplate[] getNotifications() {
		return notifications;
	}

	public synchronized int getPreviousNotificationsCount() {
		return notifications.length;
	}

	public synchronized boolean hasNotificationWithNumber(int number) {
		return number < notifications.length && number >= 0;
	}
	
	public synchronized NotificationTemplate getNotification(int number) {
		if (number >= 0 && number < notifications.length) {
			return notifications[number];
		}
		return null;
	}

	public synchronized int getAvailableNotificationsCount() {
		return notifications.length;
	}

	public synchronized void reload(Resources resources) {
		loadNotifications(resources, database);
	}
	
}
