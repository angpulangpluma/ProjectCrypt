package com.dlsu.getbetter.getbetter.cryptoGB;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import javax.crypto.spec.SecretKeySpec;

/**
 * Created by User on 10/26/2017.
 */

public class CryptFile
        extends AsyncTask<String, Void, String>{

    private String result = null;
    private final TaskListener listener;
    private aes master;

    public CryptFile(TaskListener listener){
        this.listener = listener;
    }

    @Override
    protected void onPreExecute(){
        listener.onTaskStarted();
    }

    @Override
    protected String doInBackground(String... strings) {
        //TODO: encrypt/decrypt here
        String file = strings[0];
        String base = strings[2];
        SecretKeySpec k =
                new SecretKeySpec(
                        Base64.decode(strings[1], Base64.DEFAULT),
                        0,
                        Base64.decode(strings[1], Base64.DEFAULT).length,
                        "AES");
        Log.w("f", file);
        Log.w("e", base);
        return "Done!";
    }

    @Override
    protected void onPostExecute(String string){
        listener.onTaskFinished(string);
    }
}
