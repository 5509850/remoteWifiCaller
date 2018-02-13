package by.sbb.wificallback;

///сервис фиксирующий время окончания звонка и записи в БД на удаленном сервере. + запись разговоров


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;



import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class CustomPhoneStateListener extends PhoneStateListener {

    //private static final String TAG = "PhoneStateChanged";
    Context context; //Context to make Toast if required 
    
    private   DBAdapter db;
	private   LevelHelperDB lh;	
	
	private boolean needsetEndCallToDB = false;
	
	private static final int NOTIFYRec_ID = 2;
	private NotificationManager mNotificationManager;
    
    
    public CustomPhoneStateListener(Context context) {
        super();
        this.context = context;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
    

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
       // super.onCallStateChanged(state, incomingNumber);
    	//Toast.makeText(context, "CustomPhoneStateListener", Toast.LENGTH_SHORT).show();//TODO:------------------------------------------------dele
    	
        if (!isMyServiceRunning())
        	return;
        
        switch (state) {
        case TelephonyManager.CALL_STATE_IDLE:
            //when Idle i.e no call
            Toast.makeText(context, "ОТБОЙ", Toast.LENGTH_SHORT).show();//---------------delete
            //Toast.makeText(context, "needsetEndCallToDB = " + needsetEndCallToDB, Toast.LENGTH_SHORT).show();
            if (needsetEndCallToDB)
            	setENDCALL();
            try
            {
            	StopCurrentCallRecord();
            }
            catch (Exception e) {
            	Toast.makeText(context, "StopCurrentCallRecord - " + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
            
//            if ()
//            {
//            	Toast.makeText(context, "Окончание записи разговора", Toast.LENGTH_SHORT).show();
//            	stopRecords();
//            }
            break;
        case TelephonyManager.CALL_STATE_OFFHOOK:
            //when Off hook i.e in call
            //Make intent and start your service here
            Toast.makeText(context, "ИСХОДЯЩИЙ ЗВОНОК", Toast.LENGTH_SHORT).show();//TODO:-------------delete
            needsetEndCallToDB = true;
            try{
            	StartRecordDialingCall(state);	
            }
            catch (Exception e) {
            	Toast.makeText(context, "StartRecordDialingCall - " + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
            
//            if (Prefs.getNeedRecCall(context))
//            {
//            	startRecords();
//            	Toast.makeText(context, "Начало записи разговора", Toast.LENGTH_SHORT).show();
//            }
            break;
        case TelephonyManager.CALL_STATE_RINGING:
            //when Ringing
            Toast.makeText(context, "ВХОДЯЩИЙ ЗВОНОК", Toast.LENGTH_SHORT).show();
            try{
            	StartRecordIncomingCall(state, incomingNumber);	
            }
            catch (Exception e) {
            	Toast.makeText(context, "StartRecordIncomingCall - " + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
            break;           
        
        default:
            break;
        }
    }
    
    private void StartRecordDialingCall(int state)
    {
    	if (!Prefs.getNeedRecCall(context)) //если разрешена запись
			return;	
    	
    
		
		String CallDate = SanityDate();			
		String DeviceId = Prefs.getDeviceId(context);// + "_" + callID + ".mp3"
		long mCallId = GetCallId();			
		
		String RootDir = SharedData._Path;			
		String CallFile = SharedData._Path  + DeviceId + "_" + mCallId + "_" + CallDate;
		if (!Prefs.getIsRecCallNow(context)) {
			Prefs.setIsRecCallNow(context, true);
			String med_state = android.os.Environment
					.getExternalStorageState();
			if (!med_state.equals(android.os.Environment.MEDIA_MOUNTED)) {
				return;
			}

			File directory = null;
			directory = new File(RootDir + "text.txt").getParentFile();
			if (!directory.exists() && !directory.mkdirs()) {
				return;
			}
			

			Recoders_Init(CallFile);
			Recorder_Prepare();
			//Toast.makeText(context, "начало записи разговора!!!", Toast.LENGTH_SHORT).show();//-----------------------------------------
			startNotificationsRecordIcon();
		}
		else
		{
			Toast.makeText(context, "уже идет запись разговора!!!", Toast.LENGTH_SHORT).show();
		}
		Log.v("DEBUG", TelephonyManager.CALL_STATE_OFFHOOK
				+ " ITS.CallRecorder - Recording Started " + state);
    }
    
    private void StartRecordIncomingCall(int state, String incomingNumber)
    {
    	
		if (!Prefs.getNeedRecCallPlusIncoming(context) || !Prefs.getNeedRecCall(context))//если разрешена запись входящих и запись разговоров
			return;
		
		String CallDate = SanityDate();
		String CallNum = SanityNum(incomingNumber);
		String DeviceId = Prefs.getDeviceId(context);// + "_" + callID + ".mp3"						
		
		String RootDir = SharedData._Path;			
		String CallFile = SharedData._Path  + DeviceId + "_0_" + CallDate + "t"
				+ CallNum;
		if (!Prefs.getIsRecCallNow(context)) {
			Prefs.setIsRecCallNow(context, true);
			String med_state = android.os.Environment
					.getExternalStorageState();
			if (!med_state.equals(android.os.Environment.MEDIA_MOUNTED)) {
				return;
			}

			File directory = null;
			directory = new File(RootDir + "text.txt").getParentFile();
			if (!directory.exists() && !directory.mkdirs()) {
				return;
			}
			

			Recoders_Init(CallFile);
			Recorder_Prepare();
			//Toast.makeText(context, "начало записи разговора!!!", Toast.LENGTH_SHORT).show();//-----------------------------------------
			startNotificationsRecordIcon();
		}
		Log.v("DEBUG", TelephonyManager.CALL_STATE_OFFHOOK
				+ " ITS.CallRecorder - Recording Started " + state);
    }
    
    private void StopCurrentCallRecord()
    {
    	if (!Prefs.getNeedRecCall(context)) //если не разрешена запись - выходим
			return;	
		
		 if (!isMyServiceRunning() || !isCardMounted())//не включать проверку если не работет сервис проверки данных с сервера или нет SD карты в телефоне
	        	return;
		 
		 try{
			 //TODO сделать из Prefs.Get----*****************************************************!@!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				if (Prefs.getIsRecCallNow(context)) {				
					Recorders_Stop();		
					stopNotificationsRecordIcon();
					Toast.makeText(context, "окончание записи разговора!!!", Toast.LENGTH_SHORT).show();
											}
				else{
					Toast.makeText(context, "Запись разговора уже отключена", Toast.LENGTH_SHORT).show();
			 }
				}
				catch (Exception e) {
					Toast.makeText(context, "Recorders_Stop() = " + e.getMessage(), Toast.LENGTH_SHORT).show();
				}
		 
   
		 
    }
    
    private boolean isCardMounted()
	{
		String state = android.os.Environment.getExternalStorageState();
		 if(!state.equals(android.os.Environment.MEDIA_MOUNTED))  {
	        	Toast.makeText(context, "SD Card недоступна! Запись разговоров невозможна! -  " + state + ".", Toast.LENGTH_LONG).show();	        	
	            return false;
	        }
	        return true;
	}
    
    private void startNotificationsRecordIcon()
    {   //readme 	
    	//http://habrahabr.ru/post/111238/ ()
    	 //NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	 	//Toast.makeText(context, "startNotificationsRecordIcon ---", Toast.LENGTH_SHORT).show();
    	 int icon = android.R.drawable.ic_media_play;
    	 CharSequence tickerText = "Запись разговора!";
    	 long when = System.currentTimeMillis();//system time
    	 Notification notification = new Notification(icon, tickerText, when);
    	 notification.flags |= Notification.FLAG_ONGOING_EVENT;//blocking hide after click Clear
    	 CharSequence contentTitle = "Идет запись разговора";
    	 CharSequence contentText = "сбб автоматическая запись разговора";
    	 Intent notificationIntent = new Intent(context, WifiCallBackActivity.class);
    	 PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
    	 notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent); 
    	 if (mNotificationManager != null)
    	 {
    		 mNotificationManager.notify(NOTIFYRec_ID, notification);
    	 }	    		 
    	 else
    	 {
    		 mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    		 mNotificationManager.notify(NOTIFYRec_ID, notification);
    	 }
    }
    
    private void stopNotificationsRecordIcon()
    {
    	if (mNotificationManager != null)
    		mNotificationManager.cancel(NOTIFYRec_ID);
    }
    
    private String SanityDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd'_'HH'h'mm'm'ss's'");
		Date currentTime_1 = new Date();
		return formatter.format(currentTime_1);
	}
    
    private void Recorders_Stop() {
		try {
			/*
			 * try {
			if(!recording) return;
			ar.stop();
			ar.release();
			ar = null;
			recording = false;
			ui_updater.sendEmptyMessage(0);
		} catch (Exception e) {
			e.printStackTrace();
			errMsg(R.string.RecError);
		}
			 * */
			
			
			SharedData._recorder.stop();
			SharedData._recorder.reset();
			SharedData._recorder.release();
			SharedData._recorder = null;
		} catch (IllegalStateException e) {
			Toast.makeText(context, "Recorders_Stop() = " + e.getMessage(), Toast.LENGTH_SHORT).show();			
		}
		Prefs.setIsRecCallNow(context, false);
	}

    
    private void Recorder_Prepare() {
		try {
			SharedData._recorder.prepare();
			SharedData._recorder.start();		
		} catch (IllegalStateException e) {
			e.printStackTrace();
			Toast.makeText(context, "IllegalStateException - Recorder_Prepare() = " + e.getMessage(), Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(context, "IOException - Recorder_Prepare() = " + e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
    
    private void Recoders_Init(String path) {
		String _ext = ".mp3";
		//SharedData._recorder

		SharedData._recorder = new MediaRecorder();
		SharedData._recorder.setAudioSource(Integer.parseInt(Prefs.getRecSourceType(context)));
		SharedData._recorder.setOutputFormat(SharedData.out_format);
		SharedData._recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		SharedData._recorder.setOutputFile(path + _ext);
		
	}
    
    
    private String SanityNum(String numstr) {
		String out = "";
		for (char ch : numstr.toCharArray()) {
			switch (ch) {
			case ' ':
				break;
			case '~':
				break;
			case '!':
				break;
			case '@':
				break;
			case '#':
				break;
			case '$':
				break;
			case '%':
				break;
			case '^':
				break;
			case '&':
				break;
			case '*':
				break;
			case '(':
				break;
			case ')':
				break;
			case '-':
				break;
			case '+':
				break;
			case '_':
				break;
			case '=':
				break;
			case '|':
				break;
			default:
				out = out + ch;
			}
		}
		return out;
	}

    
    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("by.sbb.wificallback.ServiceExample".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    
    private long GetCallId()
    {    	
    	db = new DBAdapter(context);
    	lh = new LevelHelperDB();
    	return lh.GetCurrentCallid(db);
    }
    
    private void setENDCALL()    {
    			
    		
    			needsetEndCallToDB = false;
    	    	
    	    	int ret = 0;
    	    	boolean result = false;
    	    	db = new DBAdapter(context);
    	    	lh = new LevelHelperDB();
    	    	long callid = GetCallId();  
    	    	
    	    	if (callid > 0)
    	    	{    	    		
    	    		ret = lh.SetCurrentCallInfo(db, String.valueOf(callid), "", "", "", false, "", "");
    	    		result = (ret == 1);
    	    		postData(String.valueOf(callid));
    	    	}
    	    	//Toast.makeText(context, "records - UPADTE callend;  callid = " + callid, Toast.LENGTH_SHORT).show();
    	    	//Toast.makeText(context, "records - UPADTE callend;  ret = " + ret, Toast.LENGTH_SHORT).show();
    	    		
    	    	//Toast.makeText(context, "records - UPADTE callend;  callid = " + callid, Toast.LENGTH_SHORT).show();
    	       	if (ret == -3)
    	    		Toast.makeText(context, "ERROR - (CustomPhoneStateListener.setENDCALL - LevelHelperDB.GetCurrentCallid) - UPADTE callend; ret = " + ret, Toast.LENGTH_SHORT).show();
    	    		
    }
    
    //set datetime is call OVER (endcall)
    public void postData(String mCallId) {
		    // Create a new HttpClient and Post Header
    	
    	String GGG = "1";
				
		int typeNetwork = SharedData.haveNetworkConnectionType(context);
		
		if (typeNetwork == 0 && Prefs.getIsNetTypeWifi(context).equals(GGG)) //если сеть 3G и Интернет не доступен по EDGE  после звонка, то пауза 5 сек - чтобы восстановился интернет
		 {
			//Toast.makeText(context, "Pause", Toast.LENGTH_SHORT).show();
			try {
				Thread.sleep(5000);
				if (SharedData.haveNetworkConnectionType(context) == 0)
					Thread.sleep(10000);
			} catch (InterruptedException e) {
				Toast.makeText(context, "ERROR - Thread.sleep(5000); " + e.getMessage(), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		 }
    	
    	
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