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
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_ENC = "com.dlsu.getbetter.getbetter.cryptoGB.action.ENC";
    private static final String ACTION_DEC = "com.dlsu.getbetter.getbetter.cryptoGB.action.DEC";

    // TODO: Rename parameters
    private static final String CRYPTO_FILE = "com.dlsu.getbetter.getbetter.cryptoGB.extra.FILE";
    private static final String CRYPTO_HCID = "com.dlsu.getbetter.getbetter.cryptoGB.extra.HCID";
    private static final String CRYPTO_SERV = "com.dlsu.getbetter.getbetter.cryptoGB.extra.SERV";
    private static final String CRYPTO_RECV = "com.dlsu.getbetter.getbetter.cryptoGB.extra.RECV";

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;

    private static final String TAG = "CryptoFileService";

//    private transient aes master;

    public CryptoFileService() {
        super("CryptoFileService");
    }

//    public CryptoFileService(aes m){
//        super("CryptoFileService");
//        this.master = m;
//        Log.w("init", Boolean.toString(m!=null));
//    }


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
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, CryptoFileService.class);
        intent.setAction(ACTION_ENC);
        intent.putExtra(CRYPTO_HCID, hcID);
        intent.putExtra(CRYPTO_FILE, sel);
        intent.putExtra(CRYPTO_SERV, master);
        intent.putExtra(CRYPTO_RECV, receiver);
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
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, CryptoFileService.class);
        intent.setAction(ACTION_DEC);
        intent.putExtra(CRYPTO_HCID, hcID);
        intent.putExtra(CRYPTO_FILE, sel);
        intent.putExtra(CRYPTO_SERV, master);
        intent.putExtra(CRYPTO_RECV, receiver);
        context.startService(intent);
    }

    //TODO: send file name back, how to do this though?
    @Override
    protected void onHandleIntent(Intent intent) {

        Log.w("cryptofilehandleintent", "yes");
//        if (intent != null) {
        final ResultReceiver receiver = intent.getParcelableExtra(CRYPTO_RECV);
        Bundle bund = new Bundle();

        final String action = intent.getAction();
        File src = new File(intent.getStringExtra(CRYPTO_FILE));
        File in = new File(Environment.getExternalStoragePublicDirectory(DirectoryConstants.CRYPTO_FOLDER),
                src.getName());
        try{
//                in.createNewFile();
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
            OutputStream os = new FileOutputStream(in);
            os.write(read(src));
            os.close();

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
        final ResultReceiver receiver = i.getParcelableExtra(CRYPTO_RECV);
        Bundle bund = new Bundle();
//        boolean result = false;

    Log.w("file enc serv", "yes");
        file_aes mastercry = null;
        File path = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS),
                DirectoryConstants.CRYPTO_FOLDER);
//        path.mkdirs();
        File output = new File(path.getPath() +"/" + sel.getName());
        Log.d("output", output.getAbsolutePath());
        try {
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
            FileOutputStream fos = new FileOutputStream(output);
            fos.write(read(sel));
            fos.flush();
            fos.close();
            mastercry = new file_aes(m);
            mastercry.encryptFile(output);
        } catch(Exception e){
            Log.e("error", e.toString());
            bund.putString("result", null);
            bund.putString(Intent.EXTRA_TEXT, e.toString());
            receiver.send(STATUS_ERROR, bund);
        }
        Log.w("enc", "done");
        bund.putString("result", output.getPath());
        receiver.send(STATUS_FINISHED, bund);
    }

    private void handleFileDecryption(File sel, aes m, Intent i){
        final ResultReceiver receiver = i.getParcelableExtra(CRYPTO_RECV);
        Bundle bund = new Bundle();

        file_aes mastercry;
        File path = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS),
                DirectoryConstants.CRYPTO_FOLDER);
//        path.mkdirs();
        File output = new File(path.getPath() +"/" + sel.getName());
        Log.d("output", output.getAbsolutePath());
        try {
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
            FileOutputStream fos = new FileOutputStream(output);
            fos.write(read(sel));
            fos.flush();
            fos.close();
            mastercry = new file_aes(m);
            mastercry.encryptFile(output);
        } catch(Exception e){
            Log.e("error", e.toString());
            bund.putString("result", null);
            bund.putString(Intent.EXTRA_TEXT, e.toString());
            receiver.send(STATUS_ERROR, bund);
        }
        Log.w("enc", "done");
        bund.putString("result", output.getPath());
        receiver.send(STATUS_FINISHED, bund);
    }

    private static byte[] read(File file) throws IOException {
        byte[] buffer = new byte[(int) file.length()];
        InputStream ios = null;
        try{
            ios = new FileInputStream(file);
            if(ios.read(buffer)==-1){
                throw new IOException("EOF reached while trying to read the whole file.");
            }
        } finally{
            try {
                if (ios != null) ios.close();
            } catch (IOException e){

            }
        }
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
