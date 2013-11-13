package org.proxima.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.proxima.example.NanoHTTPD.Response.Status;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AudioService extends Service
{
    private static final String TAG = "AudioService";

    private static Thread httpServerThread = null;

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d(TAG, "Audio service started");

        /**
         * This service just listens on port 8080 and spits out an audio file
         */
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(TAG, "Received start id " + startId + ": " + intent);

        if (httpServerThread == null)
        {
            httpServerThread = new HttpServerThread();
            httpServerThread.start();
        }

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0)
    {

        return null;
    }

    class HttpServerThread extends Thread
    {
        @Override
        public void run()
        {
            AudioServer audioServer = new AudioServer(getApplicationContext());
            try
            {
                audioServer.start();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}

/**
 * An example of subclassing NanoHTTPD to make a custom HTTP server.
 */
class AudioServer extends NanoHTTPD
{
    private final Context context;

    public AudioServer(Context context)
    {
        super(8080);
        this.context = context;
    }

    @Override
    public Response serve(String uri, Method method,
            Map<String, String> header, Map<String, String> parms,
            Map<String, String> files)
    {
        System.out.println(method + " '" + uri + "' ");


        try
        {
            InputStream in;
            in = context.getAssets().open("01-Come Together.mp3");
            return new NanoHTTPD.Response(Status.OK, "application/octet-stream", in);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return new NanoHTTPD.Response(Status.NOT_FOUND, "not found!", "???");
    }
}