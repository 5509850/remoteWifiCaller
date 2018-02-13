package by.sbb.wificallback;

//работа с локальной базой

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/* DB = wificallback.db
 * Table = records
 * row = _id, mcallid, deviceid, phone, fio, isactive, begincall, endcall*/

public class DBAdapter {
	
	
	public static final String KEY_ROWID = "_id";
	public static final String KEY_CALLID = "mcallid";	
	public static final String KEY_DEVICEID = "deviceid";
	public static final String KEY_PHONE = "phone";
	public static final String KEY_FIO = "fio";
	public static final String KEY_ISACTIVE = "isactive";
	public static final String KEY_BEGINCALL = "begincall";
	public static final String KEY_ENDCALL = "endcall";
	
	private static final String TAG = "DBAdapter";
	private static final String DATABASE_NAME = "wificallback.db";
	private static final String DATABASE_TABLE = "records";
	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_CREATE =
	"create table IF NOT EXISTS records (_id integer primary key autoincrement, "
	+ "mcallid integer, "
	+ "deviceid integer, "
	+ "phone text not null, "
	+ "fio text not null, "
	+ "isactive integer, "
	+ "begincall text not null, "
	+ "endcall text not null);";
	private final Context context;
	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;	
	
	private final SimpleDateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	
	public DBAdapter(Context ctx)
	{
	this.context = ctx;
	DBHelper = new DatabaseHelper(context);
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		DatabaseHelper(Context context)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
	
		@Override
		public void onCreate(SQLiteDatabase db)
		{
			db.execSQL(DATABASE_CREATE);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion,	int newVersion)
		{
			Log.w(TAG, "Upgrading database from version " + oldVersion
			+ " to "
			+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS records");
			onCreate(db);
		}
	}
	
	//---opens the database---
	public DBAdapter open() throws SQLException
	{
		db = DBHelper.getWritableDatabase();
	return this;
	}
	
	//---closes the database---
	public void close()
	{
		DBHelper.close();
	}
	
	private int ConvertBooleanToInt(boolean bit)
	{
		if (bit)
			return 1;
		return 0;
	}
	
	private boolean ConvertIntToBoolean(int vol)
	{		
		return (1 == vol);		
	}
	
	//---insert a level into the database---
	public long insertnewRecords(String mCallid, String deviceId, String phone, String fio, boolean isActive, String beginCall)
	{
	ContentValues dataToInsert = new ContentValues();
	dataToInsert.put(KEY_CALLID, Integer.parseInt( mCallid ));
	dataToInsert.put(KEY_DEVICEID, Integer.parseInt( deviceId ));
	dataToInsert.put(KEY_PHONE, phone);
	dataToInsert.put(KEY_FIO, fio);	
	dataToInsert.put(KEY_ISACTIVE, ConvertBooleanToInt(isActive) );
	dataToInsert.put(KEY_BEGINCALL, beginCall);
	dataToInsert.put(KEY_ENDCALL, "");		
	
	return db.insert(DATABASE_TABLE, null, dataToInsert);
	}
	
	//---deletes a particular title---
	public boolean deleteRecords(long rowId)
	{
	return db.delete(DATABASE_TABLE, KEY_ROWID +
	"=" + rowId, null) > 0;
	}
	
	//---retrieves all the titles---
	public Cursor getAllRecords()
	{
	return db.query(true, DATABASE_TABLE, new String[] {
			KEY_ROWID,
			KEY_CALLID,
			KEY_PHONE,
			KEY_FIO,
			KEY_ISACTIVE,
			KEY_BEGINCALL,
			KEY_ENDCALL,
			KEY_DEVICEID
			},
			null,
	null,
	null,
	null,
	KEY_ROWID + " DESC ",
	Prefs.getCountLastCalls(context) 
				);
	}
	
	//---retrieves a particular title---
	public Cursor getCursorByparamID(boolean isActive) throws SQLException
	{
	Cursor mCursor =
			db.query(true, DATABASE_TABLE, new String[] 
					{
					KEY_ROWID,
					KEY_CALLID,
					KEY_ISACTIVE,
					KEY_BEGINCALL,
					KEY_ENDCALL
					},
					KEY_ISACTIVE + "= " + ConvertBooleanToInt(isActive),
				null,
				null,
				null,
				null,
				null);
		if (mCursor != null) 
		{
			mCursor.moveToFirst();
		}		
	return mCursor;
	}
	
	private  String getCurrentDateTime() 
	  {
	  	 Date date = new Date();
		 return  myDateFormat.format(date);
   }
	
	
	//---updates a level---
	public boolean updateENDCALL(long callid)	
	{			
		ContentValues dataToUpdate = new ContentValues();
		dataToUpdate.put(KEY_ISACTIVE, ConvertBooleanToInt(false) );
		dataToUpdate.put(KEY_ENDCALL, getCurrentDateTime() );
		
		return db.update(DATABASE_TABLE, dataToUpdate,
				KEY_CALLID + "=" + callid, null) > 0;
	}
	
	public boolean updateCloseAllActiveCalls()	
	{			
		ContentValues dataToUpdate = new ContentValues();
		dataToUpdate.put(KEY_ISACTIVE, ConvertBooleanToInt(false) );
		dataToUpdate.put(KEY_ENDCALL, getCurrentDateTime() );
		
		return db.update(DATABASE_TABLE, dataToUpdate,
				KEY_ISACTIVE + " = 1"  , null) > 0;
	}
}