package by.sbb.wificallback;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;


public class Prefs extends PreferenceActivity 
{
	private static final String OPT_CALL = "callnow" ;
	private static final boolean OPT_CALL_DEF = true;
	
	private static final String OPT_WIFI = "wifi" ;
	private static final String OPT_WIFI_DEF = "1";
	
	private static final String OPT_RECSOURCETYPE = "recsourcetype" ;
	private static final String OPT_RECSOURCETYPE_DEF = "0";
	
	private static final String OPT_VIBRO = "vibro" ;
	private static final boolean OPT_VIBRO_DEF = false;
	
	private static final String OPT_AUTORUN = "autorun" ;
	private static final boolean OPT_AUTORUN_DEF = true;
	
	private static final String OPT_LOCK = "lock" ;
	private static final boolean OPT_LOCK_DEF = true;
	
	private static final String OPT_REC = "recordecall" ;
	private static final boolean OPT_REC_DEF = true;
	
	private static final String OPT_RECIN = "recordecallplusin" ;
	private static final boolean OPT_RECIN_DEF = true;
	
	private static final String OPT_BACK = "back" ;
	private static final boolean OPT_BACK_DEF = true;	
	
	
	
	
	private static final String OPT_FTP = "serverftp" ;
	private static final String OPT_FTP_DEF = "1.1.1.121";
	
	private static final String OPT_PORTFTP = "portftp" ;
	private static final String OPT_PORTFTP_DEF = "21";
	
	private static final String OPT_NAMEFTP = "nameftp" ;
	private static final String OPT_NAMEFTP_DEF = "android";
	
	private static final String OPT_PASSFTP = "passftp" ;
	private static final String OPT_PASSFTP_DEF = "1234";
	
	
	
	private static final String OPT_DEVICEID = "deviceid" ;
	private static final String OPT_DEVICEID_DEF = "0";
	
	private static final String OPT_TIME = "interval" ;
	private static final String OPT_TIME_DEF = "10";
	
	private static final String OPT_LOG = "log" ;
	private static final String OPT_LOG_DEF = "5";
	
	private static final String OPT_TIMESTART = "timestart" ;
	private static final String OPT_TIMESTART_DEF = "8";
	
	private static final String OPT_TIMESTOP = "timestop" ;
	private static final String OPT_TIMESTOP_DEF = "21";
	
	
	private static final String OPT_URLWIFI = "serverurlwifi" ;
	private static final String OPT_URLWIFI_DEF = "http://1.1.1.1/RDov.aspx";
	
	private static final String OPT_URL3G = "serverurl3g" ;
	private static final String OPT_URL3G_DEF = "http://1.1.1.1:45080/RDov.aspx";
	
	private static final String OPT_RECORDING = "recordingcall" ;
	private static final boolean OPT_RECORDING_DEF = false;
	
	
	
	
	@Override
   protected void onCreate(Bundle savedInstanceState) 
	{
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.settings);
    }
	
	/** Get the current value of the music option */
	
	public static String geTimeStart(Context context) 
	{
	   return PreferenceManager.getDefaultSharedPreferences(context)
	         .getString(OPT_TIMESTART, OPT_TIMESTART_DEF);
	}
	
	public static String getTimeStop(Context context) 
	{
	   return PreferenceManager.getDefaultSharedPreferences(context)
	         .getString(OPT_TIMESTOP, OPT_TIMESTOP_DEF);
	}
	
	public static boolean getCallType(Context context) 
	{
	   return PreferenceManager.getDefaultSharedPreferences(context)
	         .getBoolean(OPT_CALL, OPT_CALL_DEF);
	}
	
	public static boolean getWakeLock(Context context) 
	{
	   return PreferenceManager.getDefaultSharedPreferences(context)
	         .getBoolean(OPT_LOCK, OPT_LOCK_DEF);
	}
	
	public static boolean getVibro(Context context) 
	{
	   return PreferenceManager.getDefaultSharedPreferences(context)
	         .getBoolean(OPT_VIBRO, OPT_VIBRO_DEF);
	}
	
	public static String getDeviceId(Context context) 
	{
	   return PreferenceManager.getDefaultSharedPreferences(context)
	         .getString(OPT_DEVICEID, OPT_DEVICEID_DEF);
	}
	
	public static String getWifiServerURL(Context context) 
	{
	   return PreferenceManager.getDefaultSharedPreferences(context)
	         .getString(OPT_URLWIFI, OPT_URLWIFI_DEF);
	}
	
	public static String getInternetServerURL(Context context) 
	{
	   return PreferenceManager.getDefaultSharedPreferences(context)
	         .getString(OPT_URL3G, OPT_URL3G_DEF);
	}
	
	public static String getInterval(Context context) 
	{
	   return PreferenceManager.getDefaultSharedPreferences(context)
	         .getString(OPT_TIME, OPT_TIME_DEF);
	}
	
	public static String getCountLastCalls(Context context) 
	{
	   return PreferenceManager.getDefaultSharedPreferences(context)
	         .getString(OPT_LOG, OPT_LOG_DEF);
	}
	
	public static boolean getAutorun(Context context) 
	{
	   return PreferenceManager.getDefaultSharedPreferences(context)
	         .getBoolean(OPT_AUTORUN, OPT_AUTORUN_DEF);
	}
	
	public static String getIsNetTypeWifi(Context context) 
	{
	   return PreferenceManager.getDefaultSharedPreferences(context)
	         .getString(OPT_WIFI, OPT_WIFI_DEF);
	}
	
	
	public static boolean getNeedRecCall(Context context) 
	{
	   return PreferenceManager.getDefaultSharedPreferences(context)
	         .getBoolean(OPT_REC, OPT_REC_DEF);
	}
	
	public static boolean getNeedRecCallPlusIncoming(Context context) 
	{
	   return PreferenceManager.getDefaultSharedPreferences(context)
	         .getBoolean(OPT_RECIN, OPT_RECIN_DEF);
	}
	
	public static void setNeedRecCall(Context context, boolean NeedRecCall) 
	{
	   // PreferenceManager.getDefaultSharedPreferences(context)
	     //    .getBoolean(OPT_REC, OPT_REC_DEF);
	    
		Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putBoolean(OPT_REC, NeedRecCall);
		editor.commit();
	}
	
	public static void setIsRecCallNow(Context context, boolean isRecordingNow) 
	{
	   // PreferenceManager.getDefaultSharedPreferences(context)
	     //    .getBoolean(OPT_REC, OPT_REC_DEF);
	    
		Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putBoolean(OPT_RECORDING, isRecordingNow);
		editor.commit();
	}
	
	public static boolean getIsRecCallNow(Context context) 
	{
	   return PreferenceManager.getDefaultSharedPreferences(context)
	         .getBoolean(OPT_RECORDING, OPT_RECORDING_DEF);
	}	
	
	//------------------------------------------------------------------------
	
	
	public static String getFtpServer(Context context) 
	{
	   return PreferenceManager.getDefaultSharedPreferences(context)
	         .getString(OPT_FTP, OPT_FTP_DEF);
	}
	
	public static String getFtpPort(Context context) 
	{
	   return PreferenceManager.getDefaultSharedPreferences(context)
	         .getString(OPT_PORTFTP, OPT_PORTFTP_DEF);
	}
	
	public static String getFtpLogin(Context context) 
	{
	   return PreferenceManager.getDefaultSharedPreferences(context)
	         .getString(OPT_NAMEFTP, OPT_NAMEFTP_DEF);
	}
	
	public static String getFtpPass(Context context) 
	{
	   return PreferenceManager.getDefaultSharedPreferences(context)
	         .getString(OPT_PASSFTP, OPT_PASSFTP_DEF);
	}
	
	
	public static boolean getSaveBackUp(Context context) 
	{
	   return PreferenceManager.getDefaultSharedPreferences(context)
	         .getBoolean(OPT_BACK, OPT_BACK_DEF);
	}
	
	public static String getRecSourceType(Context context) 
	{
	   return PreferenceManager.getDefaultSharedPreferences(context)
	         .getString(OPT_RECSOURCETYPE, OPT_RECSOURCETYPE_DEF);
	}
	
	
}