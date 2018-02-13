package by.sbb.wificallback;

///Работа с локальной базой

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;


public class LevelHelperDB {

	 public int SetCurrentCallInfo(DBAdapter db, String mCallid, String deviceId, String phone, String fio, boolean isActive, String beginCall, String endCall)
	    {
		 //0 insert
		 //1 update
		 //-1 error update
		 //-2 error insert
	    	int result = -2;	     	
	    	if (endCall == null) //insert new records
	    	{	
	    		db.open();
	    		long id = db.insertnewRecords(mCallid, deviceId, phone, fio, isActive, beginCall);
	    		//Toast.makeText(this, "id = " + id, Toast.LENGTH_LONG).show();//
	    		db.close();
	    		if (id != -1)//recods added to DB    		
	    			result = 0;
	    	}
	    		
	    	
	    	else //UPADTE RECORD
	    	{
	    		db.open();
	        	if (db.updateENDCALL(Integer.parseInt (mCallid)) )
	        	{
	        		result = 1;
	        		//Toast.makeText(this, "Update successful.", Toast.LENGTH_LONG).show();
	        	}
	        	
	        	else
	        	{
	        		result = -1;
	        		//Toast.makeText(this, "Update level failed.", Toast.LENGTH_LONG).show();        		
	        	}
	        	        	
	        	db.close();
	    	}
	    	
	    	return result;    	
	    }
	 
	 public void updateCloseAllActiveCalls(DBAdapter db)
	 {
		 db.open();
		 db.updateCloseAllActiveCalls();
		 db.close();
	 }
	 
	 public long GetCurrentCallid(DBAdapter db)
	    {
		 	//-1 not found!!!!!!!!!!!!!!!
		 	//-3 Exception!!!!!!!!!!!!!!!
	    	int callid = 0;
	    	db.open();
	    	try{
	    		Cursor c = db.getCursorByparamID(true);
		    	if (c == null)
		    		return -1;
		    	if (c.moveToFirst())
		    	{
		    		callid = Integer.parseInt( c.getString(1) ); 
		    	}
		    	else
		    		callid = -1;//not found!!!!!!!!!!!!!!!
		    	
		    	c.close();
		    	db.close();	
	    	}
	    	catch (Exception e) {
	    		callid = -3;
			}
	    		    	
	    	return callid;
	    }
	 
	 public List<String> GetAllRecords(DBAdapter db)
	    {
		 	List<String> list = new ArrayList<String>();
			 	try{
			 	db.open();
			 	Cursor cursor = db.getAllRecords();
			 	int num = 1;
			 	
			 			 if (cursor.moveToFirst()) {		 
			 			 do {
			 			 /*
			0	KEY_ROWID,
			1	KEY_CALLID,
			2	KEY_PHONE,
			3	KEY_FIO,
			4	KEY_ISACTIVE,
			5	KEY_BEGINCALL,
			6	KEY_ENDCALL
			7 	KEY_DEVICEID*/
			 				 if (num > 1)
			 					list.add("--------------------");
			 				 
			 			 list.add("№" + num + ": callID = " + cursor.getString(1) + "; deviceID = " + cursor.getString(7)  + ": \n" + cursor.getString(3) + "\nтел." + cursor.getString(2)  + ": \nc " + cursor.getString(5) + "\nпо " + cursor.getString(6));
			 			 ++num;
			 			 } while (cursor.moveToNext());
			 			 
			 			 }
			 			 
			 			 if (cursor != null && !cursor.isClosed()) {
			 			 
			 			 cursor.close();
			 			 
			 			 }
			 			 
			 			 db.close();
			 		}
		 	catch (Exception e) {
				list.clear();
	 			list.add(e.getMessage());
			} 
		 	
		 			 
		 			 return list;	
	    }
	 
	 
	
	 
	
	/* 
	  //--------------------------------------------------------------------------------------------------------------------------------------------------------
	    private void update(){
	    	db.open();
	    	if (db.updateLevel(1, "123"))
	    	Toast.makeText(this, "Update successful.",
	    	Toast.LENGTH_LONG).show();
	    	else
	    	Toast.makeText(this, "Update failed.",
	    	Toast.LENGTH_LONG).show();
	    	//-------------------
	    	//---retrieve the same title to verify---
	    	Cursor c = db.getLevel(1);
	    	if (c.moveToFirst())
	    	DisplayLevel(c);
	    	else
	    	Toast.makeText(this, "No title found",
	    	Toast.LENGTH_LONG).show();
	    	//-------------------
	    	db.close();
	    	}
	    
	    private void insert()
	    {
	    	db.open();
	    	long id;
	    	id = db.insertLevel("9");    	
	    	Toast.makeText(this, "id = " + id, Toast.LENGTH_LONG).show();    	
	    	db.close();
	   	}
	    
	    private void disAll(){
	    	db.open();
	    	
	    	try
	    	{
	    		Cursor c = db.getAllTitles();
		    	if (c.moveToFirst())
		    	{
			    	do 
			    	{
				    	System.out.println("bool2");
				    	DisplayLevel(c);
			    	} 
			    	while (c.moveToNext());
		    	}
	    	
	    	}
	    	
	    	catch(Exception e)
	    	{
	    		System.out.println(e);
		    }
	    	
	    		db.close();
		    }
	    
	    private void dis ( int j ){
	    	db.open();
	    	Cursor c = db.getLevel(j);
	    	if (c.moveToFirst())
	    		DisplayLevel(c);
	    	else
	    		Toast.makeText(this, "No level found", Toast.LENGTH_LONG).show();
	    	db.close();
	    	}
	    
	    private void del ( int j ){
	    	db.open();
	    	if (db.deleteLevel(j))
	    	Toast.makeText(this, "Delete successful.",
	    	Toast.LENGTH_LONG).show();
	    	else
	    	Toast.makeText(this, "Delete failed.",
	    	Toast.LENGTH_LONG).show();
	    	db.close();
	    	}
	    
	    public void DisplayLevel(Cursor c)
	    {
	    System.out.println("bool");
	    Toast.makeText(this,
	    "id: " + c.getString(0) + "\n" +
	    "level: " + c.getString(1) + "\n", 
	    Toast.LENGTH_LONG).show();
	    }   
	*/
}
