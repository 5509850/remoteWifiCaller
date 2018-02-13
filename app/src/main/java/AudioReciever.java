import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Handler;
import android.os.Process;

//класс для записи аудио!!!!!!!!!!!!!

public class AudioReciever implements Runnable
{
    private boolean mIsRunning;
    private List<Handler> handlers;
    private AudioFormatInfo format;
    private AudioRecord mRecord;
    private Context context;
    
    private int MSG_DATA = 0;
    
    private final int BUFF_COUNT = 32;
    
    public AudioReciever(AudioFormatInfo format, Context context)
    {
        this.format = format;
        this.context = context;
        handlers = new ArrayList<Handler>();
        mIsRunning = true;
        mRecord = null;
    }
    
    public void addHandler(Handler handler)
    {
        handlers.add(handler);
    }

    public void stop()
    {
        mIsRunning = false;
    }
    
    public void run()
    {
        // приоритет для потока обработки аудио
        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
        mIsRunning = true;
        
        int buffSize = AudioRecord.getMinBufferSize(format.getSampleRateInHz(), 
                format.getChannelConfig(), format.getAudioFormat());
        
        if(buffSize == AudioRecord.ERROR)
        {
            System.err.println("getMinBufferSize returned ERROR");
            return;
        }
        
        if(buffSize == AudioRecord.ERROR_BAD_VALUE)
        {
            System.err.println("getMinBufferSize returned ERROR_BAD_VALUE");
            return;
        }
        
        // здесь работаем с short, поэтому требуем 16-bit
        if(format.getAudioFormat() != AudioFormat.ENCODING_PCM_16BIT)
        {
            System.err.println("unknown format");
            return;
        }
        
        // циклический буфер буферов. Чтобы не затереть данные,
        // пока главный поток их обрабатывает
        short[][] buffers = new short[BUFF_COUNT][buffSize >> 1];
        
        mRecord = new AudioRecord(format.getRecordType(context),
                format.getSampleRateInHz(), 
                format.getChannelConfig(), format.getAudioFormat(),
                buffSize * 10);
        
        if(mRecord.getState() != AudioRecord.STATE_INITIALIZED)
        {
            System.err.println("getState() != STATE_INITIALIZED");
            return;
        }
        
        try
        {
            mRecord.startRecording();
        }
        catch(IllegalStateException e)
        {
            e.printStackTrace();
            return;
        }
        
        int count = 0;
        
        while(mIsRunning)
        {
            int samplesRead = mRecord.read(buffers[count], 0, buffers[count].length);
            
            if(samplesRead == AudioRecord.ERROR_INVALID_OPERATION)
            {
                System.err.println("read() returned ERROR_INVALID_OPERATION");
                return;
            }
            
            if(samplesRead == AudioRecord.ERROR_BAD_VALUE)
            {
                System.err.println("read() returned ERROR_BAD_VALUE");
                return;
            }
            
            // посылаем оповещение обработчикам
            sendMsg(buffers[count]);
            
            count = (count + 1) % BUFF_COUNT;
        }

        try
        {
            try
            {
                mRecord.stop();
            }
            catch(IllegalStateException e)
            {
                e.printStackTrace();
                return;
            }
        }
        finally
        {
            // освобождаем ресурсы
            mRecord.release();
            mRecord = null;
        }
        
    }

    private void sendMsg(short[] data)
    {
    	for(Handler handler : handlers)
        {
            handler.sendMessage(handler.obtainMessage(MSG_DATA, data));
        }
    }
}