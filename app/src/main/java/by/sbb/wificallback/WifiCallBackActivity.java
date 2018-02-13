package by.sbb.wificallback;

//http://1.1.1.1/RDov.aspx?BDirection=600&deviceId=7&action=1&mCallId=5
//http://1.1.1.1:45080/RDov.aspx
//fio|Sep 14 2012  4:55:38|mCallId|1|call|+375257777777|deviceId|7|typeDataId|1|value|empty


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

/**
��������� ��������� ������ ServiceExample, ������� ���������� ��������� ������ (RepeatingAlarmService) ����� � �������� � ��������� ������� �� ������
�� ����� ������ ������ ����� RepeatingAlarmService.postData ��������� �� ������� ����� ������ ������ � ������ ������ �� ������� ����������
������� CustomPhoneStateListener ������� ��������� ��������(������/�����) � �� ����� ���������� � ������� ��� �������� ������� ��������� ������
LevelHelperDB � DBAdapter ��������� ������ � ��������� �� ��� ���������� � ����������� mcallid ��� ������������� ��� ���������� �� ������� ������� ��������� ������
BootReceiver - �������������� ����� ������� ServiceExample, PhoneStateBroadcastReceiver - �������������� ����� CustomPhoneStateListener - ������������� ������� ��������(������ ������� ��������� ������)
Prefs.geTimeStart - Prefs.getTimeStop - ����� ������ �������.
������ ��� ������ ���������� - SharedData -> StartServicesAtStartUp -> CallStateListener
  
 * @author gorbunov
 *
 */
public class WifiCallBackActivity extends Activity implements OnClickListener {
	
	
    
	Button ButtonTest, buttonStart, buttonStop, buttonSetting, buttonlog, buttonftp;
	TextView text_log, textview_title;
	//String url = "http://10.47.0.23/RDov.aspx?BDirection=600&deviceId=7&action=1&mCallId=0";
	//fio|{0}|mCallId|{1}|call|{2}|deviceId|{3}|typeDataId|{4}|value|{5}----------------------------------------------------------------
	//String deviceId = "7";
	//String baseUrl = "http://10.47.0.23/RDov.aspx";
	//http://212.98.171.21:45080/RDov.aspx	
	
	private Context context = this;	
	private Vibrator mVibrator;
	private static final int VIBRATE_MILLIS = 150;
	
	private static final int calltype = 1;
	private static final int smstype = 2;
	private static final int takerecordstype = 3;
	
	private   DBAdapter db;
	private   LevelHelperDB lh;
	private NotificationManager mNotificationManager;
	
	private static final int NOTIFY_ID = 1;
	private static final int NOTIFYRec_ID = 2;
	
	private static final int no_network = 0; 
	private static final int wifi = 1;
	private static final int GGG = 2;	
	
	private MediaRecorder rec;	
	//AudioRecorder recorder; 
	
	//PowerManager.WakeLock wakeLock ;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ButtonTest = (Button)findViewById(R.id.button_test);
        buttonStart = (Button) findViewById(R.id.button_start);
        buttonStop = (Button) findViewById(R.id.button_stop);
        buttonSetting = (Button) findViewById(R.id.button_setting);
        buttonlog = (Button) findViewById(R.id.button_log);
        buttonftp = (Button) findViewById(R.id.button_ftp); 
        text_log = (TextView) findViewById(R.id.text_log);
        textview_title = (TextView) findViewById(R.id.textview_title);
        
        //text_log.setEnabled(false);
        
        text_log.setVisibility(View.INVISIBLE);
        

        buttonStart.setOnClickListener(this);
        buttonStop.setOnClickListener(this);        
        ButtonTest.setOnClickListener(this);
        buttonSetting.setOnClickListener(this);
        buttonlog.setOnClickListener(this);
        buttonftp.setOnClickListener(this);
        
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        CheckCorrectDeviceId();
        try {
			SetTitleDeviceId();
		} catch (NameNotFoundException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			
		}
        SetEnableButtonStartStopService();
        
        //PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		//wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "tag");//SCREEN_DIM_WAKE_LOCK//PARTIAL_WAKE_LOCK
		
        //recorder = new MediaRecorder();
        
      //  UpdateRecorderState();//-----------------------------------------------------------------del
        
        
        String state = android.os.Environment.getExternalStorageState();
        if(!state.equals(android.os.Environment.MEDIA_MOUNTED))  {
        	Toast.makeText(context, "SD Card ����������! ������ ���������� ����������!   state = " + state + ".", Toast.LENGTH_LONG).show();        	
            return;
        }
        //��������� ����� �� SD
        int freeMemorySDcard = (int) SharedData.GetFreeSpaceOfSdMemory();
        if (freeMemorySDcard < 300)
        {
        	Toast.makeText(context, "�� SD Card ������ 300��! ����� ������� ������ �����! (�������� " + freeMemorySDcard + "��.)", Toast.LENGTH_LONG).show();
        }
        else
        {
        	Toast.makeText(context, "�� SD Card �������� " + freeMemorySDcard + "��.", Toast.LENGTH_LONG).show();
        }
    }   
    
    @Override
    protected void onResume() 
    {
    	//Toast.makeText(context, "ONResume" , Toast.LENGTH_LONG).show();
    	
    	  try {
  			SetTitleDeviceId();
  		} catch (NameNotFoundException e) {
  			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
  		}
    	  super.onResume();
    }
    
    @Override
    protected void onRestart()
    {
    	//Toast.makeText(context, "onRestart" , Toast.LENGTH_LONG).show();
    	
  	  try {
			SetTitleDeviceId();
		} catch (NameNotFoundException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}	
  	super.onRestart();
    }
    
    public void onClick(View v) 
	{	
		switch(v.getId())
    	{
		case R.id.button_ftp:
		{
			//����������� �� ���			
			TESTFTPLIBRARY2();
			break;
		}
		
    	case R.id.button_test: 
    		text_log.setVisibility(View.INVISIBLE);
    		//TESTFTPLIBRARY2();
    		//TestFtpLibraryFolder();
    		//TestFtpLibraryOne();
    		//TestPrefSetRecods();
    		//TestFTP();
    		//TestFTPbad();
    		//TEST_Copy(true);
    		//TEST();
    		//newTest(); 		
    		
    		CheckSiteInfo();    		    		
    		break;
    		
    	case R.id.button_start:
    		text_log.setVisibility(View.INVISIBLE);
    		if (!GetDeviceId().equals("0")){
    			Log.v(this.getClass().getName(), "onClick: Starting service.");
                startService(new Intent(this, ServiceExample.class));
                startNotificationsIcon();
                SetEnableButtonStartStopService();
               /* if (cardReady && Prefs.getNeedRecCall(context))
                {
                	if(!SharedData._Started) {                		
                		
                		SharedData._AutoStart = true;
                		StartServicesAtStartUp.Start_CallRec(getBaseContext()); }
                }
                else
                {
                	  SharedData._AutoStart = false;
                	  if(SharedData._Started)  { StartServicesAtStartUp.Stop_CallRec(getBaseContext()); }
                }*/
                
               /* if (Prefs.getWakeLock(context))              
                	wakeLock.acquire();
                	*/
                finish();
    		}
    		else
    		{
    			Toast.makeText(this, "�� ���������� DeviceId = " + GetDeviceId(), Toast.LENGTH_LONG).show();
    			AdminActivityOpen();
    		}
            
            break;
            
        case R.id.button_stop:
            Log.v(this.getClass().getName(), "onClick: Stopping service.");
            stopService(new Intent(this, ServiceExample.class));            
            stopNotificationsIcon();
            SetEnableButtonStartStopService();
            
         
            /*if (wakeLock != null && Prefs.getWakeLock(context))
            	wakeLock.release();
            	*/
                        
            break;
            
        case R.id.button_setting:
            Log.v(this.getClass().getName(), "onClick: Setting.");
            AdminActivityOpen();
            break;
            
        case R.id.button_log:
            Log.v(this.getClass().getName(), "onClick: Log.");
            ShowRecordsFtomLocalDb();
            //	stopNotificationsRecordIcon();
            //	stopService(new Intent(context, ServiceRecords.class));
            //	TEST_OVER();
            //stopRecords();//-----------------------------------------------------------------------------------------------------------------------------
            break;
            
            
    	}		
	}
	
    
    private String getTypeNet()
    {
    	String WIFI = "1";
    	if (Prefs.getIsNetTypeWifi(context).equals(WIFI))
    		return "local - WiFi";
    	else
    		return "Internet - 3G";
    }
    
    private void SetTitleDeviceId() throws NameNotFoundException
    {
    	PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
    	int versionNumber = pinfo.versionCode;
    	String versionName = pinfo.versionName;
    	    	
    	//Toast.makeText(this, "Prefs.getIsNetTypeWifi = " + Prefs.getIsNetTypeWifi(context) , Toast.LENGTH_LONG).show();
    	textview_title.setText("����������. \ndeviceId = " + GetDeviceId() + "\n��� ���� - " + getTypeNet() + "\nver. � " + versionNumber);
    }
    
    private void CheckCorrectDeviceId()
    {
    	if (GetDeviceId().equals("0"))
    	{    		
    		AdminActivityOpen();
    		Toast.makeText(this, "����� ���������� DeviceId" , Toast.LENGTH_LONG).show();
    	}    	
    }
    
    private void startNotificationsIcon()    
    {   
    	
    	//readme 	
    	//http://habrahabr.ru/post/111238/ ()
    	 //NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	
    	
    	 int icon = android.R.drawable.sym_call_incoming;
    	 CharSequence tickerText = "������� ������ ����������";
    	 long when = System.currentTimeMillis();//system time
    	 Notification notification = new Notification(icon, tickerText, when);
    	 notification.flags |= Notification.FLAG_ONGOING_EVENT;//blocking hide after click Clear
    	 CharSequence contentTitle = "������ ��� ��������";
    	 CharSequence contentText = "c����� �������������� �������";
    	 Intent notificationIntent = new Intent(this, WifiCallBackActivity.class);
    	 PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
    	 notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent); 
    	 mNotificationManager.notify(NOTIFY_ID, notification);
    }
    
    private void startNotificationsRecordIcon()
    {   //readme 	
    	//http://habrahabr.ru/post/111238/ ()
    	 //NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	
    	int icon = android.R.drawable.ic_media_play;
    	 CharSequence tickerText = "������ ���������!";
    	 long when = System.currentTimeMillis();//system time
    	 Notification notification = new Notification(icon, tickerText, when);
    	 notification.flags |= Notification.FLAG_ONGOING_EVENT;//blocking hide after click Clear
    	 CharSequence contentTitle = "���� ������ ���������";
    	 CharSequence contentText = "��� �������������� ������ ���������";
    	 Intent notificationIntent = new Intent(this, WifiCallBackActivity.class);
    	 PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
    	 notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent); 
    	 mNotificationManager.notify(NOTIFYRec_ID, notification);
    }
    
    private void stopNotificationsRecordIcon()
    {
    	mNotificationManager.cancel(NOTIFYRec_ID);
    }
    
    private void stopNotificationsIcon()
    {
    	mNotificationManager.cancel(NOTIFY_ID);
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
    
    private void TestPrefSetRecods()
    {
    	if (Prefs.getNeedRecCall(context))
    		Toast.makeText(context, "getNeedRecCall = true", Toast.LENGTH_LONG).show();
    	else
    		Toast.makeText(context, "getNeedRecCall = false", Toast.LENGTH_LONG).show();
    	
    	Prefs.setNeedRecCall(context, !Prefs.getNeedRecCall(context));
    	
    }
    
    private String mCallId = "1";
    
    private void TESTFTPLIBRARY2()
    {
    	
    	//SharedData.postData(mCallId, context);
    	if (haveNetworkConnectionType(context) == no_network || haveNetworkConnectionType(context) == GGG)
    	{
    		Toast.makeText(context, "������: ��� ����������� � ���� wifi!", Toast.LENGTH_LONG).show();
    		return;
    	}
    		
			if (Prefs.getSaveBackUp(context)) //��������� ������
			{
				if (SharedData.makeBackUpFiles(false, SharedData.folderForBackUpCallsRecords, context))
				{
					if (!SharedData.FtpUploadFolder(SharedData.folderForSaveCallsRecords, true, context))
						return;
				}  	  								
				else
					return;
			}
			else{//�� ��������� ������
				if (SharedData.makeBackUpFiles(true, SharedData.folderTempCallsRecords, context))
					{
					if (!SharedData.FtpUploadFolder(SharedData.folderTempCallsRecords, true, context))
						return;
				}  	  								
				else
					return;
			}
			
			
			//TODO - ������� ����� - ���� true - �������� �� ftp � ������� �����
			//SharedData.postOverData(mCallId, context);
    }
    
    private void TestFtpLibraryFolder()
    {
    	FtpLibrary ftp = new FtpLibrary();
		try {
			ftp.connect(context);			
			ftp.upload("calls", true);
			ftp.disconnect();
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
    }
    
    private void TestFtpLibraryOne()
    {
    	String fileName = "t7.jpg";
    	String remoteFileName = fileName;
    	File source = new File(SharedData._Path + fileName);
		
		InputStream aInputStream = null;
		try {
			aInputStream = new FileInputStream(source);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Toast.makeText(context, "FileNotFoundException = " + e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		
		FtpLibrary ftp = new FtpLibrary();
		try {
			ftp.connect(context);			
			ftp.upload(remoteFileName, aInputStream);
			ftp.disconnect();
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
    }
    
    //--------------------------------------------------------------------------------------------------------------
    
   
    
    private boolean TEST_Copy(boolean needdelete)
    {        
    		//����������� ������ � ����� "call" � ����� "callbackup"
    	
            File dir = new File(SharedData._Path);
            
            if (dir != null && !dir.exists() && !dir.mkdirs()) {
            	Toast.makeText(context, "�������� ����� ���������� ��� �� ���������� : " + SharedData._Path , Toast.LENGTH_SHORT).show();
				return false;
			}
            
            File[] filesource = dir.listFiles();
            String remotepath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/callbackup/";
            File remotefolder = new File(remotepath);
          
            if (remotefolder != null && !remotefolder.exists() && !remotefolder.mkdirs()) {
            	Toast.makeText(context, "����� ���������� ���������� ��� �� ���������� : " + remotepath , Toast.LENGTH_SHORT).show();
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
    					return false;
    				}
	                	if (needdelete)
	                	{
		                		try {
			                		if(!from.delete()){
			                			Toast.makeText(context, "���� �� ������ = " + from.toString() , Toast.LENGTH_SHORT).show();
			                		}
		                	}
		                	catch (Exception e) {
		    					Toast.makeText(context, "error - deleteFile = " + e.getMessage() , Toast.LENGTH_SHORT).show();
		    					return false;
		    				}
	                	}                	
                }
            }//end cycle
            return true;
    }
    
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
    
    private void UpdateRecorderState() 
    {
		            if(SharedData._Started)
		    {
		            	ButtonTest.setText("Stop Recording");
		    }
		    else
		    {		    	ButtonTest.setText("Start Recording");
		    }
    }
    
    
   
    private long GetCallId()
    {    	
    	db = new DBAdapter(context);
    	lh = new LevelHelperDB();
    	return lh.GetCurrentCallid(db);
    }
    
  
	
	 
	 private void SetEnableButtonStartStopService()
	 {
		 if (SharedData.isMyServiceRunning(context))
		 {
			 buttonStart.setEnabled(false);
			 buttonStop.setEnabled(true);
		 }
		 else
		 {
			 buttonStart.setEnabled(true);
			 buttonStop.setEnabled(false);
		 }
	 }
	
	private void ShowRecordsFtomLocalDb()
	{
		text_log.setVisibility(View.VISIBLE);
		db = new DBAdapter(this.context);
	        lh = new LevelHelperDB();
	        
		List<String> names = lh.GetAllRecords(db);
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("������ ��������� " + Prefs.getCountLastCalls(context) + " ������� � ��:\n");
		 
		for (int i = 0; i < names.size(); i++)
		{
			sb.append(names.get(i) + "\n");
		}
		
		text_log.setText(sb);// .setText(sb);
		
	}
	
	private void AdminActivityOpen()
	{		
		startActivity(new Intent(this, Prefs.class));
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
	
	private int  haveNetworkConnectionType(Context context)
    {
		//return int
		//0 - no network
		//1 - only wifi
		//2 - only 3G
		int WIFI = 1;
		int GGG = 2;
		int type = 0;
		
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo)
        {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
            {
                if (ni.isConnected())
                {
                    haveConnectedWifi = true;
                    Log.v("WIFI CONNECTION ", "AVAILABLE");
                    //Toast.makeText(this,"WIFI CONNECTION is Available", Toast.LENGTH_LONG).show();
                    type = WIFI; 
                } else
                {
                    Log.v("WIFI CONNECTION ", "NOT AVAILABLE");
                    //Toast.makeText(this,"WIFI CONNECTION is NOT AVAILABLE", Toast.LENGTH_LONG).show();
                }
            }
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
            {
                if (ni.isConnected())
                {
                    haveConnectedMobile = true;
                    Log.v("MOBILE INTERNET CONNECTION ", "AVAILABLE");
                    //Toast.makeText(this,"MOBILE INTERNET CONNECTION - AVAILABLE", Toast.LENGTH_LONG).show();
                    type = GGG;
                } else
                {
                    Log.v("MOBILE INTERNET CONNECTION ", "NOT AVAILABLE");
                    //Toast.makeText(this,"MOBILE INTERNET CONNECTION - NOT AVAILABLE", Toast.LENGTH_LONG).show();
                }
            }
        }
        if (!haveConnectedWifi && !haveConnectedMobile)
        	return 0;
        
        return type;
    }
	
	//fio|{0}|mCallId|{1}|call|{2}|deviceId|{3}|typeDataId|{4}|value|{5}----------------------------------------------------------------
	//typeDataId = 1 call 2 sms
	private void DoProcessing(String[] params)
	{
		//deviceId|{0}|mCallId|{1}|call|+375291234567
		
		//Toast.makeText(this, "params.length = " + params.length , Toast.LENGTH_LONG).show();
		try {
			
               
               String fio = "";
               String mCallId = ""; 
               String phone = "";
               String deviceId = "";
               int typeDataId = 0;
               String value = "";
				
				for (int i = 0; i < params.length; i++)
				{
					if (params[i].equals("fio"))
					{
						fio = String.valueOf(params[i + 1]);
					}
					
					if (params[i].equals("mCallId"))
					{
						mCallId = String.valueOf(params[i + 1]);
						Toast.makeText(this, "� ������ ����� ������ �� ���������� � ����� ������ �� �����������! (mCallId = " + params[i + 1] + ")" , Toast.LENGTH_LONG).show();
					}
					
					if (params[i].equals("call"))
					{
						phone = String.valueOf(params[i + 1]);
					}
					
					if (params[i].equals("deviceId"))
					{
						deviceId = String.valueOf(params[i + 1]);
					}
					
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
						Vibrate();
						
						if (Prefs.getCallType(context))
		            		dialPhone(phone);
		            	else
		            		showDialPhone(phone);
						break;
					}
					
					case smstype:
					{
						//sendSMS
						break;
					}
					
					case takerecordstype:
					{
						//takerecordstype();
						break;
					}
					
					
				}
				
				
				
			}
			 catch (Exception e)
			 {
				 Toast.makeText(this, "DoItNow() - " + e.getMessage().toString() , Toast.LENGTH_LONG).show(); 
			 }			
	}
	
	
	
	//return xml as string from server
	public String getData() {
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
	    	
	    	Toast.makeText(this, e.getMessage().toString() , Toast.LENGTH_LONG).show();
	    	return null;

	    } catch (IOException e) {
 
	    	Toast.makeText(this, e.getMessage().toString() , Toast.LENGTH_LONG).show();
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
			Toast.makeText(this, e.getMessage().toString() , Toast.LENGTH_LONG).show();
		}
	    
	    // Return full string
	    return total;
	}

		
	//parse txt from string by '|'
		private String[] ParseTXT(String text)
		{	
			if (text == null)
				return null;
		
			return text.split("\\|"); 				
		}
		
		private void dialPhone(String phone) 
	    {    	
	    	startActivity(new Intent("android.intent.action.CALL",	Uri.parse("tel:" + phone)));
	    }
		
		private void showDialPhone(String number)
		    {	
		    	Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));		    	
		    	startActivity(dialIntent);
		    } 
	
		
		private void ShowUrl(String URL)
	    {   	
	    		
	    	Intent UrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
	    	startActivity(UrlIntent);   	
	    	
	    }
		
		private void CheckSiteInfo()
		{		
			int WIFI = 1;
			int GGG = 2;
			int typeNetwork = haveNetworkConnectionType(this);
			
			 if (typeNetwork != 0)
			 //if (isInternetOn()) 
			 {
				         // INTERNET IS AVAILABLE, DO STUFF..
				        // Toast.makeText(this,"Network is Available", Toast.LENGTH_LONG).show();
				 		if (Integer.valueOf(Prefs.getIsNetTypeWifi(context)) != null && Integer.valueOf(Prefs.getIsNetTypeWifi(context)) == WIFI)
				 		{
				 			if (typeNetwork == WIFI)
							 {
				
				 				String[] params = ParseTXT(getData());
						         if (params != null)
						         {
						        	 if (params.length > 1)
										{    			
											Toast.makeText(this, "����� ������ ", Toast.LENGTH_LONG).show();
											DoProcessing(params);
										}
										else
										{
											Toast.makeText(this, "��� ������ " , Toast.LENGTH_LONG).show();
										}
						         }
						         else
						         {
						     		Toast.makeText(this, "���������� ����������� � �������� �� WIFI" , Toast.LENGTH_LONG).show();
						         }
							 }
							 else
							 {
								 Toast.makeText(this,"������ ��� ���� WIFI, �� �������� 3G!", Toast.LENGTH_LONG).show();
							 }
				 		}
				 		if (Integer.valueOf(Prefs.getIsNetTypeWifi(context)) != null && Integer.valueOf(Prefs.getIsNetTypeWifi(context)) == GGG)
				 		{
				 			if (typeNetwork == GGG)
							 {
				
						         String[] params = ParseTXT(getData());
						         if (params != null)
						         {
						        	 if (params.length > 1)
										{    			
											Toast.makeText(this, "����� ������ ", Toast.LENGTH_LONG).show();
											DoProcessing(params);
										}
										else
										{
											Toast.makeText(this, "��� ������ " , Toast.LENGTH_LONG).show();
										}
						         }
						         else
						         {
						     		Toast.makeText(this, "���������� ����������� � �������� ����� 3G" , Toast.LENGTH_LONG).show();
						         }
						        	 
							 }
							 else
							 {
								 Toast.makeText(this,"������ ��� ���� 3G, �� �������� WIFI!", Toast.LENGTH_LONG).show();
							 }
				 		}	         }
		    else {
		         // NO INTERNET AVAILABLE, DO STUFF..
		        Toast.makeText(this,"C��� ����������!", Toast.LENGTH_LONG).show();

		        }
			
			
		
		}
		
		private boolean isConnected() {
	        HttpURLConnection urlConnection = null;
	        try {
	            URL url = new URL("http://google.com");
	            urlConnection = (HttpURLConnection) url.openConnection();
	            urlConnection.setInstanceFollowRedirects(false);
	            urlConnection.setConnectTimeout(10000);
	            urlConnection.setReadTimeout(10000);
	            urlConnection.setUseCaches(false);
	            urlConnection.getInputStream();
	            return urlConnection.getResponseCode() == 204;
	        } catch (IOException e) {
	            
	            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
	            return false;
	        } finally {
	            if (urlConnection != null) urlConnection.disconnect();
	        }
	}
	
		
		 private void TEST()
		    {
		    	
		    	rec = new MediaRecorder();
		    	
		    	long callID = GetCallId();
		     	Toast.makeText(context, "callID = " + callID, Toast.LENGTH_LONG).show();
		     	String path = Environment.getExternalStorageDirectory().toString() + "/calls/" + Prefs.getDeviceId(context) + "_" + callID + ".mp3";
		     

		         // make sure the directory we plan to store the recording in exists
		         File directory = new File(path).getParentFile();
		         if (!directory.exists() && !directory.mkdirs()) {
		         	Toast.makeText(context, "Path to file could not be created.", Toast.LENGTH_LONG).show();
		             return;          
		         }  
		         
		         File file = new File(path);        
		         if (file.exists() && file.isFile())
		         {
		         	try{
		         		file.delete();
		         		Toast.makeText(context, "File has been deleted", Toast.LENGTH_LONG).show();
		         	}
		         	catch (Exception e) {
		         		Toast.makeText(context, "error1" + e.getMessage(), Toast.LENGTH_LONG).show();
		 			}
		         }
		         
		         if (isCallActive(context))
		         {
				        	  try {
				    		 rec.setAudioSource(Integer.parseInt(Prefs.getRecSourceType(context)));
				        	 rec.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
				        	 rec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
				        	 rec.setOutputFile(path);
							 rec.prepare();
							 rec.start();   // Recording is now started
							 startNotificationsRecordIcon();
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							Toast.makeText(context, "error2 " + e.getMessage(), Toast.LENGTH_LONG).show();
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							Toast.makeText(context, "error3 " +e.getMessage(), Toast.LENGTH_LONG).show();
							e.printStackTrace();
						}
		         }
		         else
		        	 Toast.makeText(context, "��� ��������� ������", Toast.LENGTH_LONG).show();
		     	
		        

		         /*recorder = new AudioRecorder(path);  
		         
		         try {    		
		 			recorder.start();			
		 		} catch (IOException e) {
		 			// TODO Auto-generated catch block
		 			Toast.makeText(context, "error21" + e.getMessage(), Toast.LENGTH_LONG).show();
		 		}*/
		    	//startService(new Intent(context, ServiceRecords.class));
		    	//startRecords();
		    }
		    
		    private void TEST_OVER()
		    {
		    	/*if (recorder != null) {
		        	try {
						recorder.stop();
					} catch (IOException e) {				
						Toast.makeText(context, "error1" + e.getMessage(), Toast.LENGTH_LONG).show();
						e.printStackTrace();
					}
		    	}*/
		    	
		    	
		    	if (rec != null)
		    	{
		    		try{
		    			stopNotificationsRecordIcon();
		    			rec.stop();
		    	       	 rec.reset();   // You can reuse the object by going back to setAudioSource() step
		    	       	 rec.release(); // Now the object cannot be reused
		    	       	 rec = null;
		    		}
		    		catch (Exception e) {
		    			Toast.makeText(context, "error4" + e.getMessage(), Toast.LENGTH_LONG).show();
		    			
					}
		    	 	
		    	}    	 
		    }
		    
		
		 /* private void startRecords()
	    {   	 
	    		
	    	String state = android.os.Environment.getExternalStorageState();
	        if(!state.equals(android.os.Environment.MEDIA_MOUNTED))  {
	        	Toast.makeText(context, "SD Card is not mounted.  It is " + state + ".", Toast.LENGTH_LONG).show();
	            return;
	        }
	        
	        int callID = GetCallId();
	    	Toast.makeText(context, "callID = " + callID, Toast.LENGTH_LONG).show();
	    	String path = Environment.getExternalStorageDirectory().toString() + "/calls/" + Prefs.getDeviceId(context) + "_" + callID + ".mp3";
	    

	        // make sure the directory we plan to store the recording in exists
	        File directory = new File(path).getParentFile();
	        if (!directory.exists() && !directory.mkdirs()) {
	        	Toast.makeText(context, "Path to file could not be created.", Toast.LENGTH_LONG).show();
	            return;          
	        }  
	        
	        File file = new File(path);        
	        if (file.exists() && file.isFile())
	        {
	        	try{
	        		file.delete();
	        		Toast.makeText(context, "File has been deleted", Toast.LENGTH_LONG).show();
	        	}
	        	catch (Exception e) {
	        		Toast.makeText(context, "error1" + e.getMessage(), Toast.LENGTH_LONG).show();
				}
	        	
	        	
	        }
	      
	    	 try {
	    		 recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
	        	 recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
	        	 recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
	        	 recorder.setOutputFile(path);
				 recorder.prepare();
				 recorder.start();   // Recording is now started
				 startNotificationsRecordIcon();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				Toast.makeText(context, "error2 " + e.getMessage(), Toast.LENGTH_LONG).show();
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(context, "error3 " +e.getMessage(), Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
	    }
	    */

	   /* private void stopRecords()
	    {
	    	if (recorder != null)
	    	{
	    		try{
	    			stopNotificationsRecordIcon();
	    			recorder.stop();
	    	       	 recorder.reset();   // You can reuse the object by going back to setAudioSource() step
	    	       	 recorder.release(); // Now the object cannot be reused
	    		}
	    		catch (Exception e) {
	    			Toast.makeText(context, "error4" + e.getMessage(), Toast.LENGTH_LONG).show();
	    			
				}
	    	 	
	    	}    	 
	    }*/
		
		
}