package com.dlsu.getbetter.getbetter.cryptoGB;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;

/**
 * Created by User on 10/18/2017.
 */

public class CryptoServiceReciever extends ResultReceiver {
    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */

    private ResultReceiver mRec;

    public static final String RESULT_RECEIEVER_EXTRA = "reciever";

    public CryptoServiceReciever(Handler handler) {
        super(handler);
    }

    public void setReceiver(ResultReceiver receiver){
        mRec = receiver;
    }

    public interface ResultReceiver{
        public void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData){
        if(mRec!=null) super.onReceiveResult(resultCode, resultData);
    }
}
