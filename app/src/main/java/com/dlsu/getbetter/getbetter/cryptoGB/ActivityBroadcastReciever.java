package com.dlsu.getbetter.getbetter.cryptoGB;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//from activity to cryptoservice
//TODO: finish this
public class ActivityBroadcastReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch(action){
            case CryptoFileService.ACTION_ENC:

                break;
            case CryptoFileService.ACTION_DEC:
                break;
        }
    }
}
