package com.dlsu.getbetter.getbetter.cryptoGB;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.icu.util.Output;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.util.Log;

import com.dlsu.getbetter.getbetter.DirectoryConstants;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import javax.crypto.spec.SecretKeySpec;

import static android.os.Environment.DIRECTORY_DOCUMENTS;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class CryptoFileService extends IntentService{

    public static final String ACTION_ENC = "com.dlsu.getbetter.getbetter.cryptoGB.action.ENC";
    public static final String ACTION_DEC = "com.dlsu.getbetter.getbetter.cryptoGB.action.DEC";
    public static final String ACTION_STAT = "com.dlsu.getbetter.getbetter.cryptoGB.action.STAT";
    public static final String ACTION_NAME = "com.dlsu.getbetter.getbetter.cryptoGB.action.NAME";

    //from Activity to this
    private static final String CRYPTO_FILE = "com.dlsu.getbetter.getbetter.cryptoGB.extra.FILE";
    private static final String CRYPTO_HCID = "com.dlsu.getbetter.getbetter.cryptoGB.extra.HCID";
    private static final String CRYPTO_SERV = "com.dlsu.getbetter.getbetter.cryptoGB.extra.SERV";

    //from this to Activity
    static final String CRYPTO_STAT = "com.dlsu.getbetter.getbetter.crypto.extra.STAT";
    static final String CRYPTO_NAME = "com.dlsu.getbetter.getbetter.crypto.extra.NAME";

    //status updates
    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;

    private static final String TAG = "CryptoFileService";
    private transient CryptoBroadcastReceiver cryptoBroadcaster;

    private String CRYPTO_RSLT;

//    private transient aes master;

    public CryptoFileService() {
        super("CryptoFileService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     * TODO: send encrypted file back
     */
    public void cryptoAskEncrypt(Context context, String sel, int hcID, aes master, CryptoServiceReciever receiver) {
        Log.w("cryptoaskencrypt", "yes");
//        Intent intent = new Intent(context, CryptoFileService.class);
        Log.w("cryptoaskencrypt", Boolean.toString(receiver.getReceiver()!=null));
        Intent intent = new Intent(context, CryptoFileService.class);
        intent.setAction(ACTION_ENC);
        intent.putExtra(CRYPTO_HCID, hcID);
        intent.putExtra(CRYPTO_FILE, sel);
        intent.putExtra(CRYPTO_SERV, master);
        intent.putExtra(CryptoServiceReciever.RESULT_RECEIEVER_EXTRA, receiver);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public void cryptoAskDecrypt(Context context, String sel, int hcID, aes master, CryptoServiceReciever receiver) {
//        Intent intent = new Intent(context, CryptoFileService.class);
        Log.w("cryptoaskdecrypt", Boolean.toString(receiver.getReceiver()!=null));
        Intent intent = new Intent(context, CryptoFileService.class);
        intent.setAction(ACTION_DEC);
        intent.putExtra(CRYPTO_HCID, hcID);
        intent.putExtra(CRYPTO_FILE, sel);
        intent.putExtra(CRYPTO_SERV, master);
        intent.putExtra(CryptoServiceReciever.RESULT_RECEIEVER_EXTRA, receiver);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.w("cryptofilehandleintent", "yes");
//        if (intent != null) {
        final android.support.v4.os.ResultReceiver receiver = intent.getParcelableExtra(CryptoServiceReciever.RESULT_RECEIEVER_EXTRA);
        Bundle bund = new Bundle();

        Log.w("cryptofilehandleintent", Boolean.toString(receiver!=null));

        final String action = intent.getAction();
        File src = new File(intent.getStringExtra(CRYPTO_FILE));
        Log.w("input file size", Long.toString(src.length()));
        File in = new File(Environment.getExternalStoragePublicDirectory(DirectoryConstants.CRYPTO_FOLDER),
                src.getName());
        try{
            if(in.createNewFile()) {
                receiver.send(STATUS_RUNNING, Bundle.EMPTY);
                FileOutputStream os = new FileOutputStream(in);
                os.write(read(src));
                os.close();
                Log.w("input file size", Long.toString(in.length()));
                Log.w("input file loc", in.getPath());
            } else throw new Exception("cannot create new file for storing");

        } catch (Exception e){
//            e.printStackTrace();
            bund.putString(Intent.EXTRA_TEXT, e.toString());
            receiver.send(STATUS_ERROR, bund);
        }

        if (ACTION_ENC.equals(action)) {
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
            this.handleFileEncryption(in,
                    (aes)intent.getSerializableExtra(CRYPTO_SERV),
                    intent);
        } else if (ACTION_DEC.equals(action)) {
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
            this.handleFileDecryption(in,
                    (aes)intent.getSerializableExtra(CRYPTO_SERV),
                    intent);
        }

    }

    protected void handleFileEncryption(File sel, aes m, Intent i){
        final android.support.v4.os.ResultReceiver receiver = i.getParcelableExtra(CryptoServiceReciever.RESULT_RECEIEVER_EXTRA);
        Bundle bund = new Bundle();
//        boolean result = false;

        Log.w("handlefileencryption", Boolean.toString(receiver!=null));

//    Log.w("file enc serv", "yes");
//        Log.w("sel size", Long.toString(sel.length()));
        file_aes mastercry = null;
//        File path = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS),
//                DirectoryConstants.CRYPTO_FOLDER);
////        path.mkdirs();
//        File output = new File(path.getPath() +"/" + sel.getName());
//        Log.d("output", output.getAbsolutePath());
        Log.w("sel file size", Long.toString(sel.length()));
        try {
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
            mastercry = new file_aes(m);
            mastercry.encryptFile(sel);
            handleFileDecryption(sel, m, i);
        } catch(Exception e){
            Log.e("error", e.toString());
            bund.putString("result", null);
            bund.putString(Intent.EXTRA_TEXT, e.toString());
            receiver.send(STATUS_ERROR, bund);
            CRYPTO_RSLT = null;
        }
        Log.w("enc", "done");
        bund.putString("result", sel.getPath());
        receiver.send(STATUS_FINISHED, bund);
        CRYPTO_RSLT = sel.getPath();
        Log.w("result", sel.getPath());
    }

    private void handleFileDecryption(File sel, aes m, Intent i){
        final android.support.v4.os.ResultReceiver receiver = i.getParcelableExtra(CryptoServiceReciever.RESULT_RECEIEVER_EXTRA);
        Bundle bund = new Bundle();

        Log.w("cryptofiledecryption", Boolean.toString(receiver!=null));

        file_aes mastercry;
        Log.w("sel file size", Long.toString(sel.length()));
        try {
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
            mastercry = new file_aes(m);
            mastercry.decryptFile(sel);
        } catch(Exception e){
            Log.e("error", e.toString());
            bund.putString("result", null);
            bund.putString(Intent.EXTRA_TEXT, e.toString());
            receiver.send(STATUS_ERROR, bund);
            CRYPTO_RSLT = null;
        }
        Log.w("enc", "done");
        bund.putString("result", sel.getPath());
        receiver.send(STATUS_FINISHED, bund);
        CRYPTO_RSLT = sel.getPath();
        Log.w("result", sel.getPath());
//        File path = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS),
//                DirectoryConstants.CRYPTO_TEST);
////        path.mkdirs();
//        File output = new File(path.getPath() +"/" + sel.getName());
//        Log.d("output", output.getAbsolutePath());
//        try {
//            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
////            FileOutputStream fos = new FileOutputStream(output);
////            fos.write(read(sel));
////            fos.flush();
////            fos.close();
//            mastercry = new file_aes(m);
//            mastercry.decryptFile(output);
//        } catch(Exception e){
//            Log.e("error", e.toString());
//            bund.putString("result", null);
//            bund.putString(Intent.EXTRA_TEXT, e.toString());
//            receiver.send(STATUS_ERROR, bund);
//            CRYPTO_RSLT = null;
//        }
//        Log.w("enc", "done");
//        bund.putString("result", output.getPath());
//        receiver.send(STATUS_FINISHED, bund);
//        CRYPTO_RSLT = output.getPath();
//        Log.w("result", output.getPath());
    }

    private static byte[] read(File file) throws IOException {
        byte[] buffer = null;
        InputStream ios = null;
        ios = new FileInputStream(file);
        buffer = IOUtils.toByteArray(ios);
        ios.close();
        return buffer;
    }

    public void saveKey(String fileloc, SecretKeySpec secretkey) throws Exception {
        //save key to file
        System.out.println("Trying to save key in " + fileloc);
        OutputStream output = null;
        try {
            System.out.println("Saving key in file");
            output = new BufferedOutputStream(new FileOutputStream(fileloc));
            output.write(secretkey.getEncoded());
        } finally{
            output.close();
            System.out.println("Successfully saved key");
        }

    }

    public void getKey(String fileloc, int in) throws Exception {
        //get key from file
        System.out.println("Trying to get key from " + fileloc);
        byte[] result = new byte[(int)new File(fileloc).length()];
        try{
            //TODO: get nth key from file
            InputStream input = new BufferedInputStream(new FileInputStream(fileloc));
            input.read(result);
        } finally{

//            this.secretkey = new SecretKeySpec(result, 0, result.length, "AES");
//            System.out.println("Key successfully retrieved!");
        }

    }

}
