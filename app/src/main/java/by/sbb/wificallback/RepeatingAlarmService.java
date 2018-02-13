package by.sbb.wificallback;


//������ ��������� �������� ���������� � ���������� �������, ���������� �������, ������������ ������� ������ ���������, ����������� ������� ���������� � �����, �������� �� ftp ���������� ���������� �� ������.
//������������ ���� ������ RepeatingAlarmService ����������� �� ServiceExample

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Shader;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Vibrator;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class RepeatingAlarmService extends BroadcastReceiver {
	
		//	String url = "http://1.1.1.1/RDov.aspx?BDirection=600&deviceId=7&action=1&mCallId=0";
		//String deviceId = "7";
		//String baseUrl = "http://1.1.1.1/RDov.aspx";
		Context context;
		Intent intent;
		private Vibrator mVibrator;
		private static final int VIBRATE_MILLIS = 150;
		
		private  String fio = "";
		private String mCallId = ""; 
		private String phone = "";		
		private int typeDataId = 0;
		private String value = "";
         
        private static final int calltype = 1;
     	private static final int smstype = 2;
     	private static final int takerecordstype = 3;
		
		private   DBAdapter db;
		private   LevelHelperDB lh;
		
		private final SimpleDateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		private final SimpleDateFormat myTimeFormat = new SimpleDateFormat("HH");
		private final SimpleDateFormat mySecFormat = new SimpleDateFormat("ss");
		
		private static final int NOTIFY_ID = 3;		
		private NotificationManager mNotificationManager;
		
		private boolean noNeedBlockingRecordsIsOff = false;
		
		//http://docs.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
		
		
		
		
		

    @Override
	    public void onReceive(Context context, Intent intent) 
	    {
	        //Toast.makeText(context, getCurrentTimeOnlySec() + " ���", Toast.LENGTH_SHORT).show();//-----------------------------------------------------------------
    		mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	        Log.v(this.getClass().getName(), "Timed alarm onReceive() started at time: " + new java.sql.Timestamp(System.currentTimeMillis()).toString());
	        this.context = context;
	        this.intent = intent;
	        
	        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);      
	        

	        if (!isCallActive(this.context) && isWorkingTime())
	        {	
	        	//Toast.makeText(this.context, "CheckSiteInfo() �����! ", Toast.LENGTH_SHORT).show();//-------------------------------------------------------------
	        	CheckSiteInfo();
	        }
	        else
	        {
	        	if (!isCallActive(this.context))
	        		Toast.makeText(this.context, "�� �������� �� ������� ����� - � ���������� ", Toast.LENGTH_SHORT).show();
	        } 	
	    }
    
    private boolean isWorkingTime()
    {	
    	int nowhour = Integer.parseInt( getCurrentTimeOnlyHour() );//21:30 or 8:10
    	int start = Integer.parseInt(Prefs.geTimeStart(context));//8
    	int stop = Integer.parseInt(Prefs.getTimeStop(context));//21
    	
    	if (nowhour >= start && nowhour < stop)
    		return true;
    	
    	return false;    	
    }
    
    private  String getCurrentTimeOnlyHour() 
	  {
	  	 Date date = new Date();
		 return  myTimeFormat.format(date);
	  }
    
    private  String getCurrentTimeOnlySec() 
	  {
	  	 Date date = new Date();
		 return  mySecFormat.format(date);
	  }
    
    
    	
    	private boolean isCallActive(Context context){
    	   AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    	   if(manager.getMode()==AudioManager.MODE_IN_CALL){
    	         return true;
    	   }
    	   else{
    	       return false;
    	   }
    	}
    	
    	private void Vibrate()
    	{
    		
    		 if (Prefs.getVibro(context)) {
                 mVibrator.vibrate(
                     new long[]{0l, VIBRATE_MILLIS,
                                    50l, VIBRATE_MILLIS,
                                    50l, VIBRATE_MILLIS},
                         -1);
             }
    	}
    	
    	
    	
    	private void startNotificationsIcon(String mess)    
        {   //readme 	
        	//http://habrahabr.ru/post/111238/ ()
        	 //NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    		 if (mNotificationManager != null)
    		 {
    			 int icon = android.R.drawable.ic_dialog_alert;
            	 CharSequence tickerText = "������ ��� ������� ��������� � ��������!";
            	 long when = System.currentTimeMillis();//system time
            	 Notification notification = new Notification(icon, tickerText, when);            	 
            	 CharSequence contentTitle = "������ � ������ <������ ���>";
            	 CharSequence contentText = mess;
            	 Intent notificationIntent = new Intent(context, Prefs.class);
            	 PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
            	 notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent); 
            	 mNotificationManager.notify(NOTIFY_ID, notification);
    		 }        	 
        }
    	
    	 private void stopNotificationsIcon()
    	    {
    		 	if (mNotificationManager != null)
    		 		mNotificationManager.cancel(NOTIFY_ID);
    	    } 
    	
    
    
    	private void CheckSiteInfo()
	    {
    		int WIFI = 1;
    		int GGG = 2;    		
    		int typeNetwork = SharedData.haveNetworkConnectionType(context);
    		 if (typeNetwork != 0)
    		 {
    			 if (Integer.valueOf(Prefs.getIsNetTypeWifi(context)) != null && Integer.valueOf(Prefs.getIsNetTypeWifi(context)) == WIFI)
    				 {
		    				 if (typeNetwork == WIFI)
		    				 {
		    					 String[] params = ParseTXT(getData());
			    					 if (params != null)
							         {
			    						 stopNotificationsIcon();
							        	 if (params.length > 1)
											{    			
												Toast.makeText(context, "��������� ������� ������  (�� wifi)", Toast.LENGTH_LONG).show();
												DoProcessing(params);
											}
											else
											{
												Toast.makeText(context, "��� ������" , Toast.LENGTH_LONG).show();
											}
							         }
							         else
							         {
							     		Toast.makeText(context, "���������� ����������� � �������� �� WIFI" , Toast.LENGTH_LONG).show();
							     		startNotificationsIcon("WIFI - ���������� ����������� � ��������");
							         }
		    				 }
		    				 else
		        			 {
		        				 Toast.makeText(context,"������ ��� ���� WIFI, �� �������� 3G!", Toast.LENGTH_LONG).show();
		        				 startNotificationsIcon("������ ��� ���� WIFI, �� �������� 3G!");
		        			 }	 
    				 }
    			 if (Integer.valueOf(Prefs.getIsNetTypeWifi(context)) != null && Integer.valueOf(Prefs.getIsNetTypeWifi(context)) == GGG)
    			 {
    				 if (typeNetwork == GGG)
    				 {
	    					 String[] params = ParseTXT(getData());
					         if (params != null)
					         {
					        	 stopNotificationsIcon();
					        	 if (params.length > 1)
									{    			
										Toast.makeText(context, "��������� ������� ������ ", Toast.LENGTH_LONG).show();
										DoProcessing(params);
									}
									else
									{
										Toast.makeText(context, "��� ������ (�� 3G)" , Toast.LENGTH_LONG).show();
									}
					         }
					         else
					         {
					     		Toast.makeText(context, "���������� ����������� � �������� ����� 3G" , Toast.LENGTH_LONG).show();
					     		startNotificationsIcon("3G - ���������� ����������� � ��������");
					         }
    				 }
    				 else
					 {
						 Toast.makeText(context,"������ ��� ���� 3G, �� �������� WIFI!", Toast.LENGTH_LONG).show();
						 startNotificationsIcon("������ ��� ���� 3G, �� �������� WIFI!");
					 }
    				 
    			 }
    		 }
    		 else
    		 {
    				Toast.makeText(context, "���� ����������!" , Toast.LENGTH_LONG).show();
    				startNotificationsIcon("���� ����������!");
    		 }	
	    }
    
  //parse txt from string by '|'
  		private String[] ParseTXT(String text)
  		{	if (text == null)
  				return null;		
  			return text.split("\\|"); 				
  		}
    
  		private void DoProcessing(String[] params)
  		{
  			//"fio|{0}|mCallId|{1}|call|{2}|deviceId|{3}"
  			
  			//Toast.makeText(this, "params.length = " + params.length , Toast.LENGTH_LONG).show();
  			
  			
  			try {
  					for (int i = 0; i < params.length; i++)
  					{
  						if (params[i].equals("fio"))
  						{
  							fio = String.valueOf(params[i + 1]);
  						}
  						
  						if (params[i].equals("mCallId"))
  						{
  							mCallId = String.valueOf(params[i + 1]);  							
  						}
  						
  						if (params[i].equals("call"))
  						{
  							phone = String.valueOf(params[i + 1]);
  						}
  						
//  						if (params[i].equals("deviceId"))
//  						{
//  							deviceId = String.valueOf(params[i + 1]);
//  						}
  						
  						if (params[i].equals("typeDataId"))
  						{
  							typeDataId = Integer.parseInt(params[i + 1]);
  						}
  						
  						if (params[i].equals("value"))
  						{
  							value = String.valueOf(params[i + 1]);
  						}
  					}
  				
  					
  					switch (typeDataId)
  					{  						
  						case calltype:  							
  						{  	
  							//TODO
  							Vibrate();  							
  							writetoDB();//������ ������ ������ � ��������� ���� ��� ����������� ������������� CallId ��������� ������ � ������ �������
  							postData(mCallId);  							
  									  							
  							if (Prefs.getCallType(context))
  			            		dialPhone(phone);
  			            	else
  			            		showDialPhone(phone);
  							break;
  						}
  						
  						case smstype:
  						{
  							writeSMStoDB();
  							postData(mCallId);
  							if (sendSMS())
  								postOverData(mCallId);
  							break;
  						}
  						
  						//��������� ����� ���������� ���������� �� IP
  						case takerecordstype:
  						{
  							postData(mCallId);
  							if (!Prefs.getNeedRecCall(context)) //���� ������ ������� ��������� - �� �� ����� �����������/�������������� ������
  								noNeedBlockingRecordsIsOff = true;
  							if (Prefs.getSaveBackUp(context)) //��������� ������
  							{
  								if (makeBackUpFiles(false, SharedData.folderForBackUpCallsRecords))
  								{
  									if (!FtpUploadFolder(SharedData.folderForSaveCallsRecords, true))
  										return;
  								}  	  								
  								else
  									return;
  							}
  							else{//�� ��������� ������
  								if (makeBackUpFiles(true, SharedData.folderTempCallsRecords))
  									{
  									if (!FtpUploadFolder(SharedData.folderTempCallsRecords, true))
  										return;
  								}  	  							
  								else
  									return;
  							}							
  							
  							
  							postOverData(mCallId);
  							break;
  						}
  					}	
  					
  				}
  				 catch (Exception e)
  				 {
  					 Toast.makeText(context, "DoItNow() - " + e.getMessage().toString() , Toast.LENGTH_LONG).show(); 
  				 }			
  		}
  		
  		 private long GetCallId()
  	    {    	
  	    	db = new DBAdapter(context);
  	    	lh = new LevelHelperDB();
  	    	return lh.GetCurrentCallid(db);
  	    }
  		
  		 private boolean FtpUploadFolder(String FolderName, boolean deleteAfterCopy)
  	    {
  			boolean result = false;
  	    	FtpLibrary ftp = new FtpLibrary();
  			try {
  				ftp.connect(context);			
  				ftp.upload(FolderName, deleteAfterCopy);
  				ftp.disconnect();
  				result = true;
  			} catch (SocketException e) {
  				Toast.makeText(context, "SocketException = " + e.getMessage(), Toast.LENGTH_LONG).show();
  				e.printStackTrace();
  			} catch (IOException e) {
  				Toast.makeText(context, "IOException = " + e.getMessage(), Toast.LENGTH_LONG).show();
  				e.printStackTrace();
  			}
  			catch (Exception e) {
  				Toast.makeText(context, "Exception = " + e.getMessage(), Toast.LENGTH_LONG).show();
  				e.printStackTrace();
  			} 
  			return result;
  	    }
  		
  	/*	private void stopRec()
  		{
  			if (!Prefs.getNeedRecCall(context))
 				 return;
  			
  			if (recorder != null)
  	    	{
  				if(!recording) return;
  	    		try{
  	    			stopNotificationsRecordIcon();
  	    			recorder.stop();
  	    	       	recorder.reset();   // You can reuse the object by going back to setAudioSource() step
  	    	       	recorder.release(); // Now the object cannot be reused
  	    	       	recorder = null;
  	    	       	recording = false;
  	    		}
  	    		catch (Exception e) {
  	    			Toast.makeText(context, "error4" + e.getMessage(), Toast.LENGTH_LONG).show();
  	    			
  				}
  	    	}
  		}*/
  		
//  		private void sd()
//  		{
//  			File source = new File("H:\\work-temp\\file");
//  			File desc = new File("H:\\work-temp\\file2");
//  			try {
//  			    FileUtils.copyDirectory(source, desc);
//  			} catch (IOException e) {
//  			    e.printStackTrace();
//  			}
//  		}
  		
  		private static void copyFile(File sourceFile, File destFile)
	                throws IOException {
		        if (!sourceFile.exists()) {
		                return;
		        }
		        if (!destFile.exists()) {
		                destFile.createNewFile();
		        }
		        FileChannel source = null;
		        FileChannel destination = null;
		        source = new FileInputStream(sourceFile).getChannel();
		        destination = new FileOutputStream(destFile).getChannel();
		        if (destination != null && source != null) {
		                destination.transferFrom(source, 0, source.size());
		        }
		        if (source != null) {
		                source.close();
		        }
		        if (destination != null) {
		                destination.close();
		        }

  		}
  		
  		private void BlockRecordCalls(boolean block)
  		{
  			if (noNeedBlockingRecordsIsOff)
  				return;
  			
  			Prefs.setNeedRecCall(context, !block);
  			//SharedData._Paused = block;
  		}
  		
  		private boolean makeBackUpFiles(boolean deleteAfterCopy, String DestinationFolder)
  	    {//����������� ������ � ����� "call" � ����� "callbackup"
  	    	
  			 	BlockRecordCalls(true);  			 	
  			 	
  	            File dir = new File(SharedData._Path);  	            
  	            if (dir != null && !dir.exists() && !dir.mkdirs()) {
  	            	Toast.makeText(context, "�������� ����� ���������� ��� �� ���������� : " + SharedData._Path , Toast.LENGTH_SHORT).show();
  	            	BlockRecordCalls(false);
  					return false;
  				}
  	            
  	            File[] filesource = dir.listFiles();
  	            String remotepath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + DestinationFolder + "/";
  	            File remotefolder = new File(remotepath);
  	          
  	            if (remotefolder != null && !remotefolder.exists() && !remotefolder.mkdirs()) {
  	            	Toast.makeText(context, "����� ���������� ���������� ��� �� ���������� : " + remotepath , Toast.LENGTH_SHORT).show();
  	            	BlockRecordCalls(false);
  					return false;
  				}
  	            Toast.makeText(context, "���-�� ������ ��� ����������� : " + filesource.length , Toast.LENGTH_SHORT).show();
  	            
  	            for(int i = 0; i < filesource.length; i++) 
  	            {
  	                File from = filesource[i];
  	                if (from.isFile())//���������� �����
  	                {
  	                	try {                		 
  	                		Toast.makeText(context, "from: " + from.toString() , Toast.LENGTH_SHORT).show();
  	                		Toast.makeText(context, "to: " + remotepath + from.getName() , Toast.LENGTH_SHORT).show();
  	    					copyFile(from, new File(remotepath + from.getName()));
  	    					
  	    				} catch (IOException e) {
  	    					Toast.makeText(context, "error - copyFile = " + e.getMessage() , Toast.LENGTH_SHORT).show();
  	    					BlockRecordCalls(false);
  	    					return false;
  	    					}
  		                	if (deleteAfterCopy)
  		                	{
  			                		try {
  				                		if(!from.delete()){
  				                			Toast.makeText(context, "���� �� ������ = " + from.toString() , Toast.LENGTH_SHORT).show();
  				                		}
  			                	}
  			                	catch (Exception e) {
  			    					Toast.makeText(context, "error - deleteFile = " + e.getMessage() , Toast.LENGTH_SHORT).show();
  			    					BlockRecordCalls(false);
  			    					return false;
  			    				}
  		                	}                	
  	                }
  	            }//end cycle
  	            BlockRecordCalls(false);
  	            return true;
  	    }
  		
  		private String getData() {
	    // Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(GetServerURL());

	    try {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
	        nameValuePairs.add(new BasicNameValuePair("BDirection", "600"));
	        nameValuePairs.add(new BasicNameValuePair("deviceId", GetDeviceId()));
	        nameValuePairs.add(new BasicNameValuePair("action", "1"));
	        nameValuePairs.add(new BasicNameValuePair("mCallId", "0"));
	        
	        
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
	        
	        StringBuilder sb = inputStreamToString(response.getEntity().getContent());
	        	        
	        
	        //Toast.makeText(this, "sb = " + sb.toString() , Toast.LENGTH_LONG).show();
	        return sb.toString();
	        
	    } catch (ClientProtocolException e) {
	    	
	    	Toast.makeText(context, e.getMessage().toString() , Toast.LENGTH_LONG).show();
	    	return null;

	    } catch (IOException e) {
 
	    	Toast.makeText(context, e.getMessage().toString() , Toast.LENGTH_LONG).show();
	    	return null;
	    }    
	}
    
  	// Fast Implementation
  		private StringBuilder inputStreamToString(InputStream is) {		
  		    String line = "";
  		    StringBuilder total = new StringBuilder();
  		    
  		    // Wrap a BufferedReader around the InputStream
  		    BufferedReader rd = new BufferedReader(new InputStreamReader(is));

  		    // Read response until the end
  		    try {
  				while ((line = rd.readLine()) != null) { 
  				    total.append(line); 
  				}
  			} catch (IOException e) {			
  				e.printStackTrace();
  				Toast.makeText(context, e.getMessage().toString() , Toast.LENGTH_LONG).show();
  			}
  		    
  		    // Return full string
  		    return total;
  		}
  		
  		public  String getCurrentDateTime() 
		  {
		  	 Date date = new Date();
			 return  myDateFormat.format(date);
	     }
  		
  		private boolean sendSMS(){		
  			
  			
             if (phone.length()>0 && value.length()>0)
             {
            	 sendSMS(phone, value);
            	 return true;
             }else             
                 Toast.makeText(context, 
                     "������! ���������� ����� ��� ����� ���", 
                     Toast.LENGTH_SHORT).show();
             return false;
  		}
  		
  		 @SuppressWarnings("deprecation")
		private void sendSMS(String phoneNumber, String message)
 	    {        
 	        String SENT = "SMS_SENT";
 	        String DELIVERED = "SMS_DELIVERED";
 	        
 	       
 	        
 	        PendingIntent sentPI = PendingIntent.getActivity(context, 0,  intent, 0);
 	 
 	        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED), 0);
 	        
// 	       //---when the SMS has been sent---
// 	       context.registerReceiver(new BroadcastReceiver(){
// 	 	         @SuppressWarnings("deprecation")
// 	 			@Override
// 	 	         public void onReceive(Context arg0, Intent arg1) {
// 	 	             switch (getResultCode())
// 	 	             {
// 	 	                 case Activity.RESULT_OK:
// 	 	                     Toast.makeText(context, "SMS sent", 
// 	 	                             Toast.LENGTH_SHORT).show();
// 	 	                     break;
// 	 	                 case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
// 	 	                     Toast.makeText(context, "Generic failure", 
// 	 	                             Toast.LENGTH_SHORT).show();
// 	 	                     break;
// 	 	                 case SmsManager.RESULT_ERROR_NO_SERVICE:
// 	 	                     Toast.makeText(context, "No service", 
// 	 	                             Toast.LENGTH_SHORT).show();
// 	 	                     break;
// 	 	                 case SmsManager.RESULT_ERROR_NULL_PDU:
// 	 	                     Toast.makeText(context, "Null PDU", 
// 	 	                             Toast.LENGTH_SHORT).show();
// 	 	                     break;
// 	 	                 case SmsManager.RESULT_ERROR_RADIO_OFF:
// 	 	                     Toast.makeText(context, "Radio off", 
// 	 	                             Toast.LENGTH_SHORT).show();
// 	 	                     break;
// 	 	             }
// 	 	         }
// 	 	     }, new IntentFilter(SENT));
//
// 	 	     //---when the SMS has been delivered---
// 	      context.registerReceiver(new BroadcastReceiver(){
// 	 	         @Override
// 	 	         public void onReceive(Context arg0, Intent arg1) {
// 	 	             switch (getResultCode())
// 	 	             {
// 	 	                 case Activity.RESULT_OK:
// 	 	                     Toast.makeText(context, "SMS delivered", 
// 	 	                             Toast.LENGTH_SHORT).show();
// 	 	                     break;
// 	 	                 case Activity.RESULT_CANCELED:
// 	 	                     Toast.makeText(context, "SMS not delivered", 
// 	 	                             Toast.LENGTH_SHORT).show();
// 	 	                     break;                        
// 	 	             }
// 	 	         }
// 	 	     }, new IntentFilter(DELIVERED)); 
 	           
 	 
 	        SmsManager sms = SmsManager.getDefault();
 	        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI); 	     
 	       
 	    }

  	

		private void writetoDB()
  		{  		
  			boolean result = false;
  			int ret = -1;
  			if (phone != null && !phone.equals("") && mCallId != null && !mCallId.equals(""))
  			{
  				//mCallid, deviceId, phone, isActive, beginCall, endCall
  				db = new DBAdapter(this.context);
  		        lh = new LevelHelperDB();
  		        lh.updateCloseAllActiveCalls(db);//�������� ��� ������������� ��������� �� �������� active
  				ret = lh.SetCurrentCallInfo(db, mCallId, GetDeviceId(), phone, fio, true, getCurrentDateTime(), null);//���������� � ��������� �� ������ � ������ �� �������� isactive ��� ������������� mCallId ��������� ������ ��� ������������ ������� ��������� ��������� 
  				if (ret == 0)
  					result = true;
  				//Toast.makeText(context, "ret insert = " + ret , Toast.LENGTH_SHORT).show();
  			}
  			else
  			{
  				Toast.makeText(context, "������������ ������� ������--- phone != null - !phone.equals - mCallId != null - !mCallId.equals" , Toast.LENGTH_LONG).show();  				
  			}
  			
  			
  				//Toast.makeText(context, "records added to DB" , Toast.LENGTH_LONG).show();
  			if (!result)
  				Toast.makeText(context, "ERROR - records not added to local DB" , Toast.LENGTH_LONG).show();
  			
  		}
		
		private void writeSMStoDB()
  		{  		
  			boolean result = false;
  			int ret = -1;
  			if (phone != null && !phone.equals("") && mCallId != null && !mCallId.equals(""))
  			{
  				//mCallid, deviceId, phone, isActive, beginCall, endCall
  				db = new DBAdapter(this.context);
  		        lh = new LevelHelperDB();
  		        lh.updateCloseAllActiveCalls(db);//�������� ��� ������������� ��������� �� �������� active
  				ret = lh.SetCurrentCallInfo(db, mCallId, GetDeviceId(), phone, "SMS", false, getCurrentDateTime(), null);//���������� � ��������� �� ������ � ������ �� �������� isactive ��� ������������� mCallId ��������� ������ ��� ������������ ������� ��������� ��������� 
  				if (ret == 0)
  					result = true;
  				//Toast.makeText(context, "ret insert = " + ret , Toast.LENGTH_SHORT).show();
  			}
  			else
  			{
  				Toast.makeText(context, "������������ ������� ������--- phone != null - !phone.equals - mCallId != null - !mCallId.equals" , Toast.LENGTH_LONG).show();  				
  			}
  			
  			
  				//Toast.makeText(context, "records added to DB" , Toast.LENGTH_LONG).show();
  			if (!result)
  				Toast.makeText(context, "ERROR - records not added to local DB" , Toast.LENGTH_LONG).show();
  			
  		}
  		
  	//update record - set inactive by mCallId-------------------------------------------------------------
  		public void postData(String mCallId) {
  		    // Create a new HttpClient and Post Header
  		    HttpClient httpclient = new DefaultHttpClient();
  		    HttpPost httppost = new HttpPost(GetServerURL());
  		    
  		    
  		   // Toast.makeText(this, "mCallId = " + mCallId + " deviceId = " + deviceId  , Toast.LENGTH_LONG).show();

  		    try {
  		        // Add your data
  		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
  		        nameValuePairs.add(new BasicNameValuePair("BDirection", "600"));
  		        nameValuePairs.add(new BasicNameValuePair("deviceId", GetDeviceId()));
  		        nameValuePairs.add(new BasicNameValuePair("action", "2"));
  		        nameValuePairs.add(new BasicNameValuePair("mCallId", mCallId));
  		        
  		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

  		        // Execute HTTP Post Request
  		       httpclient.execute(httppost);	        
  		     
  		        
  		    } catch (ClientProtocolException e) {
  		    	
  		    	Toast.makeText(context, e.getMessage().toString() , Toast.LENGTH_LONG).show();

  		    } catch (IOException e) {
  	 
  		    	Toast.makeText(context, e.getMessage().toString() , Toast.LENGTH_LONG).show();
  		    }	    
  		    
  		}
  		
  		
  		 public void postOverData(String mCallId) {
 		    // Create a new HttpClient and Post Header
 		    HttpClient httpclient = new DefaultHttpClient();
 		    HttpPost httppost = new HttpPost(GetServerURL());
 		    
 		    
 		   // Toast.makeText(this, "mCallId = " + mCallId + " deviceId = " + deviceId  , Toast.LENGTH_LONG).show();

 		    try {
 		        // Add your data
 		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
 		        nameValuePairs.add(new BasicNameValuePair("BDirection", "600"));
 		        nameValuePairs.add(new BasicNameValuePair("deviceId", GetDeviceId()));
 		        nameValuePairs.add(new BasicNameValuePair("action", "3"));
 		        nameValuePairs.add(new BasicNameValuePair("mCallId", mCallId));
 		        
 		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

 		        // Execute HTTP Post Request
 		       httpclient.execute(httppost);	        
 		     
 		        
 		    } catch (ClientProtocolException e) {
 		    	
 		    	Toast.makeText(context, e.getMessage().toString() , Toast.LENGTH_LONG).show();

 		    } catch (IOException e) {
 	 
 		    	Toast.makeText(context, e.getMessage().toString() , Toast.LENGTH_LONG).show();
 		    }	    
 		    
 		}
  		
  		private void dialPhone(String phone) 
	    {   
  			//Toast.makeText(context, "phone = " + phone , Toast.LENGTH_SHORT).show(); 			
  			
  			Intent call = new Intent(Intent.ACTION_CALL);
  			call.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  		    call.setData(Uri.parse("tel:" + phone));
  		    context.getApplicationContext().startActivity(call);
	    }
  		
  		private void showDialPhone(String number)
	    {	
  			Intent call = new Intent(Intent.ACTION_DIAL);
  			call.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  		    call.setData(Uri.parse("tel:" + number));
  		    context.getApplicationContext().startActivity(call);
	    } 
  		
  		private String GetDeviceId()
  		{
  			return Prefs.getDeviceId(context);
  		}
  		
  		private String GetServerURL()
		{
  			String WIFI = "1";
    		if (Prefs.getIsNetTypeWifi(context).equals(WIFI))
    			return Prefs.getWifiServerURL(context);
    		
    		return Prefs.getInternetServerURL(context);
		}
}
