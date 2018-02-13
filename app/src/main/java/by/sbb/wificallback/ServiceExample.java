package by.sbb.wificallback;

//http://blog.divenvrsk.org/2010/09/android.html

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class ServiceExample extends Service {

    public static final int FIRST_RUN = 5000; // 5 seconds
    int REQUEST_CODE = 11223344;

    AlarmManager alarmManager;

    @Override
    public void onCreate() {
        super.onCreate();

        startService();
        Log.v(this.getClass().getName(), "onCreate(..)");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(this.getClass().getName(), "onBind(..)");
        return null;
    }

    @Override
    public void onDestroy() {
        if (alarmManager != null) {
            Intent intent = new Intent(this, RepeatingAlarmService.class);
            alarmManager.cancel(PendingIntent.getBroadcast(this, REQUEST_CODE, intent, 0));
        }
        Toast.makeText(this, "Сервис звонков Остановлен!", Toast.LENGTH_LONG).show();
        Log.v(this.getClass().getName(), "Service onDestroy(). Stop AlarmManager at " + new java.sql.Timestamp(System.currentTimeMillis()).toString());
    }
    
    private long GetInterval()
    {
    	long i = Integer.parseInt(Prefs.getInterval(this));
    	if (i > 0)
    	{
    		return 1000 * i;
    	}
    	return 10000; 
    }
    

    private void startService() {

        Intent intent = new Intent(this, RepeatingAlarmService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, intent, 0);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        
        alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + FIRST_RUN,
                GetInterval(),
                pendingIntent);

        Toast.makeText(this, "Сервис звонков Запущен. \n(время работы с " + Prefs.geTimeStart(this) + "ч. по " +  Prefs.getTimeStop(this) + "ч.) \nc интервалом проверки каждые " + Prefs.getInterval(this) + "c", Toast.LENGTH_LONG).show();
        Log.v(this.getClass().getName(), "AlarmManger started at " + new java.sql.Timestamp(System.currentTimeMillis()).toString());
    }
}
