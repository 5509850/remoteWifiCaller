package by.sbb.wificallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public final class SharedData {

	static int _Rec_Type = android.media.MediaRecorder.AudioSource.MIC;//VOICE_CALL VOICE_CALL VOICE_RECOGNITION AudioSource.MIC   AudioSource.VOICE_CALL
	static int out_format = MediaRecorder.OutputFormat.MPEG_4; //MediaRecorder.OutputFormat.MPEG_4                  MediaRecorder.OutputFormat.RAW_AMR
	//TODO: check quality of record

	static String folderForSaveCallsRecords = "calls"; 
	static String folderForBackUpCallsRecords = "callarch";
	static String folderTempCallsRecords = "calltemp";	
	
	static boolean inRecordMode;
	
	static String _Path = android.os.Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/" + folderForSaveCallsRecords + "/";
	
	
		
	static boolean _Started = false;
	static boolean _AutoStart = true;
	//static boolean _Recording = false;	
	//static String mCallId = "-1";

	static MediaRecorder _recorder = new MediaRecorder();
	static MediaRecorder _recorder_down = new MediaRecorder();
	static MediaRecorder _recorder_up = new MediaRecorder();

	SharedData() {
	}
	
	public OnRecordPositionUpdateListener mListner = new OnRecordPositionUpdateListener() {
		
		public void onPeriodicNotification(AudioRecord recorder) {
			// TODO Auto-generated method stub
			
		}
		
		public void onMarkerReached(AudioRecord recorder) {
			inRecordMode = false;
			
		}
	};
	
	static double GetFreeSpaceOfSdMemory()
	{	
		if (!android.os.Environment
				.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			return 0;
		}
		
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		double sdAvailSize = (double)stat.getAvailableBlocks()
		                   * (double)stat.getBlockSize();
		//One binary gigabyte equals 1,073,741,824 bytes.
		double megaAvailable = sdAvailSize / 1048576;//1073741824;
		
		return megaAvailable;
		
	}
	
	static int  haveNetworkConnectionType(Context context)
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
	
	private static String GetServerURL(Context context)
	{
			String WIFI = "1";
		if (Prefs.getIsNetTypeWifi(context).equals(WIFI))
			return Prefs.getWifiServerURL(context);
		
		return Prefs.getInternetServerURL(context);
	}
	
	private static String GetDeviceId(Context context)
		{
			return Prefs.getDeviceId(context);
		}
	
	
	 public static boolean isMyServiceRunning(Context context) {
	        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	            if ("by.sbb.wificallback.ServiceExample".equals(service.service.getClassName()) 
	            		) {
	                return true;
	            }
	        }
	        return false;
	    }
	
	static public void postData(String mCallId, Context context) {
		    // Create a new HttpClient and Post Header
		    HttpClient httpclient = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost(GetServerURL(context));
		    
		    
		   // Toast.makeText(this, "mCallId = " + mCallId + " deviceId = " + deviceId  , Toast.LENGTH_LONG).show();

		    try {
		        // Add your data
		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		        nameValuePairs.add(new BasicNameValuePair("BDirection", "600"));
		        nameValuePairs.add(new BasicNameValuePair("deviceId", GetDeviceId(context)));
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
	
	//withou block records - only for testing!
	public static boolean makeBackUpFiles(boolean deleteAfterCopy, String DestinationFolder, Context context)
	    {//копирование файлов с папки "call" в папку "callbackup"
	    	
			 	  			 	
			 	
	            File dir = new File(SharedData._Path);  	            
	            if (dir != null && !dir.exists() && !dir.mkdirs()) {
	            	Toast.makeText(context, "исходная папка недоступна или не существует : " + SharedData._Path , Toast.LENGTH_SHORT).show();	            	
					return false;
				}
	            
	            File[] filesource = dir.listFiles();
	            String remotepath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + DestinationFolder + "/";
	            File remotefolder = new File(remotepath);
	          
	            if (remotefolder != null && !remotefolder.exists() && !remotefolder.mkdirs()) {
	            	Toast.makeText(context, "папка назначения недоступна или не существует : " + remotepath , Toast.LENGTH_SHORT).show();	            	
					return false;
				}
	            Toast.makeText(context, "кол-во файлов для копирования : " + filesource.length , Toast.LENGTH_SHORT).show();
	            
	            for(int i = 0; i < filesource.length; i++) 
	            {
	                File from = filesource[i];
	                if (from.isFile())//пропускаем папки
	                {
	                	try {                		 
	                		Toast.makeText(context, "from: " + from.toString() , Toast.LENGTH_SHORT).show();
	                		Toast.makeText(context, "to: " + remotepath + from.getName() , Toast.LENGTH_SHORT).show();
	    					copyFile(from, new File(remotepath + from.getName()));
	    					
	    				} catch (IOException e) {
	    					Toast.makeText(context, "error - copyFile = " + e.getMessage() , Toast.LENGTH_SHORT).show();
	    					
	    					return false;
	    					}
		                	if (deleteAfterCopy)
		                	{
			                		try {
				                		if(!from.delete()){
				                			Toast.makeText(context, "файл не удален = " + from.toString() , Toast.LENGTH_SHORT).show();
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
	
	 public static boolean FtpUploadFolder(String FolderName, boolean deleteAfterCopy, Context context)
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
	
	 public static void postOverData(String mCallId, Context context) {
		    // Create a new HttpClient and Post Header
		    HttpClient httpclient = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost(GetServerURL(context));
		    
		    
		   // Toast.makeText(this, "mCallId = " + mCallId + " deviceId = " + deviceId  , Toast.LENGTH_LONG).show();

		    try {
		        // Add your data
		    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		        nameValuePairs.add(new BasicNameValuePair("BDirection", "600"));
		        nameValuePairs.add(new BasicNameValuePair("deviceId", GetDeviceId(context)));
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
		
}