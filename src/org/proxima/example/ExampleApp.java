package org.proxima.example;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.proxima.Channel;
import org.proxima.Channel.ChannelListener;
import org.proxima.NativeTools;
import org.proxima.ProximityManager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ExampleApp extends Activity
{
    private static final String TAG = "ExampleApp";

    private ProximityManager mProximityManager;
    private ListView mPeerListView;
    private PeerListAdapter mPeerListAdapter;
    private IntentFilter mIntentFilter;
    private Channel mChannel;
    private PeerListListener mPeerListListener;
    private ExampleBroadcastReceiver mBroadcastReceiver;
    private ProgressDialog mLoadingPeersDialog;

    private boolean mIsProximityEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProximityManager = ProximityManager.getInstance();
        // Must call this before anything else
        mChannel = mProximityManager.initialize(getApplicationContext(),
                new ChannelListener()
                {

                    @Override
                    public void onChannelDisconnected()
                    {
                        // Code to run when the channel is disconnected
                    }
                });

        mPeerListView = (ListView) findViewById(R.id.peerListView);

        mPeerListListener = new PeerListListener();

        mIntentFilter = new IntentFilter();

        // Indicates a change in the proximity status.
        mIntentFilter
                .addAction(ProximityManager.PROXIMITY_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        mIntentFilter
                .addAction(ProximityManager.PROXIMITY_PEERS_CHANGED_ACTION);

        mLoadingPeersDialog = new ProgressDialog(this);
        mLoadingPeersDialog.setCancelable(false);
        mLoadingPeersDialog.setMessage("Looking for peers ...");
        mLoadingPeersDialog.show();

        Button refreshButton = (Button) findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mIsProximityEnabled)
                {
                    mPeerListAdapter.clear();
                    mProximityManager.getPeers(mChannel, mPeerListListener);

                    InetAddress ipaddr = NativeTools.getIpAddress();

                    Log.i(TAG, ipaddr.toString() + " "
                            + NativeTools.getBroadcast(ipaddr).toString());

                }
            }
        });

        mPeerListView.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                TextView tv = (TextView) view;

                String destination = tv.getText().toString();
                String msg = "test\n";

                String retval = null;
                try
                {
                    SendMessageTask task = new SendMessageTask();
                    task.execute(new String[] { destination, msg });
                    retval = task.get();
                }
                catch (Exception e)
                {
                    retval = "Error: " + e.getMessage();
                }

                try
                {
                    String url = "http://" + destination + ":8080"; // your URL
                                                                    // here
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(url);
                    mediaPlayer.prepare(); // might take long! (for buffering,
                                           // etc)
                    mediaPlayer.start();
                }
                catch (IllegalArgumentException e)
                {
                    e.printStackTrace();
                }
                catch (SecurityException e)
                {
                    e.printStackTrace();
                }
                catch (IllegalStateException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

            }
        });

        // start messenger service so that it runs even if no active activities
        // are bound to it
        startService(new Intent(this, MessageService.class));

        // start audio service
        startService(new Intent(this, AudioService.class));
    }

    public void onProximityEnabled()
    {
        if (mIsProximityEnabled)
        {
            /**
             * We got the broadcast, so the service is connected, so we can do
             * this
             */
            mProximityManager.getPeers(mChannel, mPeerListListener);

            mPeerListAdapter = new PeerListAdapter(this,
                    android.R.layout.simple_list_item_1,
                    mPeerListListener.getPeers());
            mPeerListView.setAdapter(mPeerListAdapter);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mBroadcastReceiver = new ExampleBroadcastReceiver(mProximityManager,
                mChannel, this);
        registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    private class PeerListListener implements ProximityManager.PeerListListener
    {
        public ArrayList<String> peers = new ArrayList<String>();

        @Override
        public void onPeerListAvailable(ArrayList<String> peers)
        {
            Log.d(TAG, "Peer list available");
            this.peers = peers;
            mPeerListAdapter.clear();

            for (int i = 0; i < peers.size(); i++)
            {
                mPeerListAdapter.add(peers.get(i));
            }

            mPeerListAdapter.notifyDataSetChanged();

            mLoadingPeersDialog.dismiss();
        }

        public ArrayList<String> getPeers()
        {
            return peers;
        }
    };

    private class PeerListAdapter extends ArrayAdapter<String>
    {
        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public PeerListAdapter(Context context, int textViewResourceId,
                List<String> objects)
        {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i)
            {
                mIdMap.put(objects.get(i), i);
            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void setIsProximityEnabled(boolean enabled)
    {
        mIsProximityEnabled = enabled;
    }

}
