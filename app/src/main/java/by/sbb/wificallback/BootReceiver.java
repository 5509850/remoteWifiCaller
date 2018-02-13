package by.sbb.wificallback;



import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
		
//http://blog.divenvrsk.org/2010/09/android.html
	//https://nodeload.github.com/dive/ServiceExample/zipball/master
	
	private NotificationManager mNotificationManager;
	private static final int NOTIFY_ID = 1;
	
	public void onReceive(Context context, Intent intent) 
	{
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) 
		{		
			
			 if (Prefs.getAutorun(context))
			 {
				 			 
	    	        //start service ServiceExample - get call from remote server
				 	Intent serviceLauncher = new Intent(context, ServiceExample.class);
		            context.startService(serviceLauncher);
		            Log.v(this.getClass().getName(), "Service loaded while device boot.");
		            
		            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		            startNotificationsIcon(context);
			 }
			
			    
		}
	
	
	}
	
	 private void startNotificationsIcon(Context context)    
	    {   //readme 	
	    	//http://habrahabr.ru/post/111238/ ()
	    	 //NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	    	 int icon = android.R.drawable.sym_call_incoming;
	    	 CharSequence tickerText = "Запущен сервис Сомбелбанк";
	    	 long when = System.currentTimeMillis();//system time
	    	 Notification notification = new Notification(icon, tickerText, when);
	    	 notification.flags |= Notification.FLAG_ONGOING_EVENT;//blocking hide after click Clear
	    	 CharSequence contentTitle = "Сервис СББ работает";
	    	 CharSequence contentText = "Cервис автоматических звонков";
	    	 Intent notificationIntent = new Intent(context, WifiCallBackActivity.class);
	    	 PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
	    	 notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent); 
	    	 mNotificationManager.notify(NOTIFY_ID, notification);
	    }
	
}