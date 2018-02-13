package by.sbb.wificallback;

///регистрация сервиса CustomPhoneStateListener для записи времени окончания разговора в БД на удаленном сервере

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class PhoneStateBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		if (isMyServiceRunning(context)) {
			// start service CustomPhoneStateListener - check status call
			TelephonyManager telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			telephonyManager.listen(new CustomPhoneStateListener(context),
					PhoneStateListener.LISTEN_CALL_STATE);
			Log.v(this.getClass().getName(),
					"Service PhoneStateBroadcastReceiver loaded while device boot.");
		} else {
			//Toast.makeText(context, "сервис1 не включен", Toast.LENGTH_SHORT).show();
		}
	}

	private boolean isMyServiceRunning(Context context) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ("by.sbb.wificallback.ServiceExample".equals(service.service
					.getClassName())) {
				return true;
			}
		}
		return false;
	}

}