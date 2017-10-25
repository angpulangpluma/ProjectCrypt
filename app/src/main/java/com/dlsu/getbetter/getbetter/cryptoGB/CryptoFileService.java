package com.dlsu.getbetter.getbetter.cryptoGB;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.icu.util.Output;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.dlsu.getbetter.getbetter.DirectoryConstants;
import com.dlsu.getbetter.getbetter.activities.UpdatePatientRecordActivity;

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
//    private transient CryptoBroadcastReceiver cryptoBroadcaster;

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
     */
    public void cryptoAskEncrypt(Context context, String sel, int hcID, aes master) {
        Log.w("cryptoaskencrypt", "yes");
//        Intent intent = new Intent(context, CryptoFileService.class);
        Intent intent = new Intent(context, CryptoFileService.class);
        intent.setAction(ACTION_ENC);
        intent.putExtra(CRYPTO_HCID, hcID);
        intent.putExtra(CRYPTO_FILE, sel);
        String f = sel;
        Log.w("file", f);
        checkPermissions(context);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public void cryptoAskDecrypt(Context context, String sel, int hcID, aes master) {
//        Intent intent = new Intent(context, CryptoFileService.class);
        Intent intent = new Intent(context, CryptoFileService.class);
        intent.setAction(ACTION_DEC);
        intent.putExtra(CRYPTO_HCID, hcID);
        intent.putExtra(CRYPTO_FILE, sel);
        checkPermissions(context);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.w("cryptofilehandleintent", "yes");
//        if (intent != null) {
//        final android.support.v4.os.ResultReceiver receiver = intent.getParcelableExtra(CryptoServiceReciever.RESULT_RECEIEVER_EXTRA);
//        Bundle bund = new Bundle();

//        Log.w("cryptofilehandleintent", Boolean.toString(receiver!=null));

        final String action = intent.getAction();
        String fi = intent.getStringExtra(CRYPTO_FILE);
        Log.w("file", fi);
        File src = new File(fi);
        File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                DirectoryConstants.CRYPTO_TEST);
        File f = null;
        InputStream in;
        OutputStream out;
        boolean isFileUnlocked = false;

        if (path.mkdirs()){
            Log.w("file?", "yes");
            f = new File(path, src.getName());
            try {
                if (f.createNewFile()){
                    in = new FileInputStream(src);
                    out = new FileOutputStream(f);
                    IOUtils.copy(in, out);
                    out.close();
                    in.close();
                    if(f.setReadable(true)){
                        Log.w("exists?", "yes");
                        try {
                            long lastmod = f.lastModified();
                            Log.w("last modified", Long.toString(lastmod));
                            org.apache.commons.io.FileUtils.touch(f);
                            isFileUnlocked = true;
                        } catch (IOException e) {
//                            isFileUnlocked = false;
                            Log.w("error", e.getMessage());
                        }
                    } else Log.w("exists?", "no");
                } else Log.w("exists?", "no");
            } catch (IOException e) {
//                e.printStackTrace();
                Log.w("error", e.getMessage());
            }
        } else Log.w("file?", "no");
//        File srcnew = new File(src.getPath());

        if (isFileUnlocked){
            Log.w("got it?", "yes");
        } else Log.w("got it?", "no");
//        InputStream in;
//        try{
//            in = new FileInputStream(src);
//            Log.w("got it?", "yes");
//        } catch(Exception e){
//            Log.w("got it?", "no");
//            Log.w("error", e.getMessage());
//        }
        Log.w("input file size", Long.toString(src.length()));


//        File in = new File(Environment.getExternalStoragePublicDirectory(DirectoryConstants.CRYPTO_FOLDER),
//                src.getName());
//        try{
//            if(in.createNewFile()) {
//                receiver.send(STATUS_RUNNING, Bundle.EMPTY);
//                FileOutputStream os = new FileOutputStream(in);
//                os.write(read(src));
//                os.close();
//                Log.w("input file size", Long.toString(in.length()));
//                Log.w("input file loc", in.getPath());
//            } else throw new Exception("cannot create new file for storing");
//
//        } catch (Exception e){
////            e.printStackTrace();
//            bund.putString(Intent.EXTRA_TEXT, e.toString());
//            receiver.send(STATUS_ERROR, bund);
//        }

//        if (ACTION_ENC.equals(action)) {
//            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
//            this.handleFileEncryption(src,
//                    (aes)intent.getSerializableExtra(CRYPTO_SERV),
//                    intent);
//        } else if (ACTION_DEC.equals(action)) {
//            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
//            this.handleFileDecryption(src,
//                    (aes)intent.getSerializableExtra(CRYPTO_SERV),
//                    intent);
//        }

    }

    protected void handleFileEncryption(File sel, aes m, Intent i){
//        final android.support.v4.os.ResultReceiver receiver = i.getParcelableExtra(CryptoServiceReciever.RESULT_RECEIEVER_EXTRA);
//        Bundle bund = new Bundle();
//        boolean result = false;

//        Log.w("handlefileencryption", Boolean.toString(receiver!=null));

//    Log.w("file enc serv", "yes");
//        Log.w("sel size", Long.toString(sel.length()));
        file_aes mastercry = null;
        File path = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS),
                DirectoryConstants.CRYPTO_FOLDER);
////        path.mkdirs();
//        File output = new File(path.getPath() +"/" + sel.getName());
//        Log.d("output", output.getAbsolutePath());
        Log.w("sel file size", Long.toString(sel.length()));
//        final String action = intent.getAction();
//        File src = new File(intent.getStringExtra(CRYPTO_FILE));
//        Log.w("input file size", Long.toString(src.length()));
        File in = new File(path, sel.getName());
        try{
            if( path.mkdirs() && in.createNewFile()) {
//                receiver.send(STATUS_RUNNING, Bundle.EMPTY);
                FileOutputStream os = new FileOutputStream(in);
                os.write(read(sel));
                os.close();
                Log.w("input file size", Long.toString(in.length()));
                Log.w("input file loc", in.getPath());
//                receiver.send(STATUS_RUNNING, Bundle.EMPTY);
//                mastercry = new file_aes(m);
//                mastercry.encryptFile(in);
//                handleFileDecryption(in, m, i);
            }

        } catch (Exception e){
//            e.printStackTrace();
//            bund.putString(Intent.EXTRA_TEXT, e.toString());
//            receiver.send(STATUS_ERROR, bund);
            Log.w("error", e.toString());
        }
//        try {
//            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
//            mastercry = new file_aes(m);
//            mastercry.encryptFile(sel);
//            handleFileDecryption(sel, m, i);
//        } catch(Exception e){
//            Log.e("error", e.toString());
//            bund.putString("result", null);
//            bund.putString(Intent.EXTRA_TEXT, e.toString());
//            receiver.send(STATUS_ERROR, bund);
//            CRYPTO_RSLT = null;
//        }
        Log.w("enc", "done");
//        bund.putString("result", in.getPath());
//        receiver.send(STATUS_FINISHED, bund);
        CRYPTO_RSLT = in.getPath();
        Log.w("result", in.getPath());
    }

    private void handleFileDecryption(File sel, aes m, Intent i){
//        final android.support.v4.os.ResultReceiver receiver = i.getParcelableExtra(CryptoServiceReciever.RESULT_RECEIEVER_EXTRA);
        Bundle bund = new Bundle();

//        Log.w("cryptofiledecryption", Boolean.toString(receiver!=null));

        file_aes mastercry;
        Log.w("sel file size", Long.toString(sel.length()));
        File path = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS),
                DirectoryConstants.CRYPTO_TEST);
        File in = new File(path, sel.getName());
        try{
            if(path.mkdirs() && in.createNewFile()) {
//                receiver.send(STATUS_RUNNING, Bundle.EMPTY);
                FileOutputStream os = new FileOutputStream(in);
                os.write(read(sel));
                os.close();
                Log.w("input file size", Long.toString(in.length()));
                Log.w("input file loc", in.getPath());
//                receiver.send(STATUS_RUNNING, Bundle.EMPTY);
                mastercry = new file_aes(m);
                mastercry.decryptFile(in);
            }

        } catch (Exception e){
//            e.printStackTrace();
//            bund.putString(Intent.EXTRA_TEXT, e.toString());
//            receiver.send(STATUS_ERROR, bund);
            Log.w("error", e.getMessage());
        }
//        try {
//            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
//            mastercry = new file_aes(m);
//            mastercry.decryptFile(sel);
//        } catch(Exception e){
//            Log.e("error", e.toString());
//            bund.putString("result", null);
//            bund.putString(Intent.EXTRA_TEXT, e.toString());
//            receiver.send(STATUS_ERROR, bund);
//            CRYPTO_RSLT = null;
//        }
        Log.w("enc", "done");
//        bund.putString("result", in.getPath());
//        receiver.send(STATUS_FINISHED, bund);
        CRYPTO_RSLT = in.getPath();
        Log.w("result", in.getPath());
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

    public void checkPermissions(Context context){
        int readStuff = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeStuff = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        Log.w("read?", Integer.toString(readStuff));
//        Log.w("write?", Integer.toString(writeStuff));
        //for read stuff
        if(readStuff == PackageManager.PERMISSION_GRANTED)
            Log.w("read?", "yes");
        else if(readStuff == PackageManager.PERMISSION_DENIED)
            Log.w("read?", "no");

        //for write stuff
        if(writeStuff == PackageManager.PERMISSION_GRANTED)
            Log.w("write?", "yes");
        else if(writeStuff == PackageManager.PERMISSION_DENIED)
            Log.w("write?", "no");
    }

}
