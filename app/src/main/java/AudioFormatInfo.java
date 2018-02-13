import by.sbb.wificallback.Prefs;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

//клас описание аудиоформата для использования в AudioReciever

public class AudioFormatInfo {
	
	//static int _Rec_Type = android.media.MediaRecorder.AudioSource.MIC;//VOICE_CALL VOICE_CALL VOICE_RECOGNITION AudioSource.MIC   AudioSource.VOICE_CALL
	static int audioFormat = MediaRecorder.OutputFormat.MPEG_4; //MediaRecorder.OutputFormat.MPEG_4                  MediaRecorder.OutputFormat.RAW_AMR
	static int sampleRateInHz =  android.media.AudioFormat.ENCODING_PCM_16BIT;
	static int channelConfig  = AudioFormat.CHANNEL_IN_MONO;	
	
	public int getChannelConfig() {
		// TODO Auto-generated method stub
		return channelConfig;
	}
	public int getSampleRateInHz() {
		// TODO Auto-generated method stub
		return sampleRateInHz;
	}
	public int getAudioFormat() {
		// TODO Auto-generated method stub
		return audioFormat;
	}
	
	public int getRecordType(Context context) {
		// TODO Auto-generated method stub
		//return _Rec_Type;
		return Integer.parseInt(Prefs.getRecSourceType(context));
	}
	
	

}
