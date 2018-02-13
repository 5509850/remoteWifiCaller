package by.sbb.wificallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import android.content.Context;
import android.widget.Toast;


public class FtpLibrary {
	
	private FTPClient mFtp; 
	
	public FtpLibrary()
	{
		mFtp = new FTPClient();
	}
	
	public void connect(Context context) throws SocketException, IOException
	{
		
		String userid = Prefs.getFtpLogin(context);
		String pwd = Prefs.getFtpPass(context);;
		InetAddress server = null;
		try {
			//server = InetAddress.getLocalHost();
			server = InetAddress.getByName(Prefs.getFtpServer(context));
		} catch (UnknownHostException e) {
			Toast.makeText(context, "UnknownHostException = " + e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		int port = Integer.valueOf(Prefs.getFtpPort(context));			
		
		mFtp.connect(server, port); // Using port no=21
		mFtp.login(userid, pwd);
	}
	
	//upload one file
	public boolean upload(String remoteFileName, InputStream aInputStream) throws Exception
	{
		mFtp.setFileType(FTP.BINARY_FILE_TYPE);
		mFtp.enterLocalPassiveMode();		
		boolean aRtn= mFtp.storeFile(remoteFileName, aInputStream);
		aInputStream.close();
		return aRtn;
	}
	
	//upload all files from folder in SD card!
	public void upload(String SourceFolderName, boolean deleteAfterCopy) throws Exception
	{
		 File dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + SourceFolderName + "/");  	            
         if (dir != null && !dir.exists() && !dir.mkdirs()) {
        	 throw new Exception("локальная папка не найдена" + SourceFolderName + " не найдена");
			}         
         File[] filesource = dir.listFiles();
         
		 for(int i = 0; i < filesource.length; i++) 
         {
             File from = filesource[i];
             if (from.isFile())//пропускаем папки
             {
            	String fileName = from.getName();		 
         		File source = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + SourceFolderName + "/" + fileName);		
         		InputStream aInputStream = null;
         		try {
         			aInputStream = new FileInputStream(source);
         		} catch (FileNotFoundException e) {
         			e.printStackTrace();
         			throw new Exception("aInputStream = new FileInputStream(source) ");			
         		}		
         		mFtp.setFileType(FTP.BINARY_FILE_TYPE);
         		mFtp.enterLocalPassiveMode();		
         		boolean aRtn= mFtp.storeFile(fileName, aInputStream);
         		aInputStream.close();
         		if (!aRtn)
         			throw new Exception("boolean aRtn= mFtp.storeFile(fileName, aInputStream)");
         		
         		if (deleteAfterCopy)
              	{
	                		try {
		                		if(!from.delete()){
		                			//файл не удален!
		                		}
	                	}
	                	catch (Exception e) {
	                		throw new Exception("ошибка при удалении файла");
	    				}
              	}          
             }
         }//end cycl		
		
	}
	
	public void disconnect() throws Exception
	{
		mFtp.disconnect();
	}

}
