package org.proxima.example;

import org.proxima.Channel;
import org.proxima.ProximityManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ExampleBroadcastReceiver extends BroadcastReceiver
{
    private static final String TAG = "ExampleBroadcastReceiver";

    private final ProximityManager mProximityManager;
    private final Channel mChannel;
    private final ExampleApp mActivity;

    public ExampleBroadcastReceiver(ProximityManager manager, Channel channel,
            ExampleApp activity)
    {
        super();

        mProximityManager = manager;
        mChannel = channel;
        mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(TAG, "Broadcast message received");
        String action = intent.getAction();

        if (ProximityManager.PROXIMITY_STATE_CHANGED_ACTION.equals(action))
        {
            // Determine if proximity mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(
                    ProximityManager.EXTRA_PROXIMITY_STATE, -1);
            if (state == ProximityManager.PROXIMITY_STATE_ENABLED)
            {
                mActivity.setIsProximityEnabled(true);
                mActivity.onProximityEnabled();
            }
            else
            {
                mActivity.setIsProximityEnabled(false);
            }
        }
        else if (ProximityManager.PROXIMITY_PEERS_CHANGED_ACTION.equals(action))
        {

            // The peer list has changed! We should probably do something about
            // that.

        }
    }

}
