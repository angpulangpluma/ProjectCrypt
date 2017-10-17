package com.dlsu.getbetter.getbetter.cryptoGB;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.icu.util.Output;
import android.os.Environment;
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
    public static final String ACTION_ENC = "com.dlsu.getbetter.getbetter.cryptoGB.action.ENC";
    public static final String ACTION_DEC = "com.dlsu.getbetter.getbetter.cryptoGB.action.DEC";

    // TODO: Rename parameters
    public static final String CRYPTO_FILE = "com.dlsu.getbetter.getbetter.cryptoGB.extra.FILE";
    public static final String CRYPTO_HCID = "com.dlsu.getbetter.getbetter.cryptoGB.extra.HCID";
    public static final String CRYPTO_SERV = "com.dlsu.getbetter.getbetter.cryptoGB.extra.SERV";

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
     */
    public void cryptoAskEncrypt(Context context, String sel, int hcID, aes master) {
        Log.w("cryptoaskencrypt", "yes");
        Intent intent = new Intent(context, CryptoFileService.class);
        intent.setAction(ACTION_ENC);
        intent.putExtra(CRYPTO_HCID, hcID);
        intent.putExtra(CRYPTO_FILE, sel);
        intent.putExtra(CRYPTO_SERV, master);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public void cryptoAskDecrypt(Context context, String sel, int hcID, aes master) {
        Intent intent = new Intent(context, CryptoFileService.class);
        intent.setAction(ACTION_DEC);
        intent.putExtra(CRYPTO_HCID, hcID);
        intent.putExtra(CRYPTO_FILE, sel);
        intent.putExtra(CRYPTO_SERV, master);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.w("cryptofilehandleintent", "yes");
        if (intent != null) {
            final String action = intent.getAction();
            File src = new File(intent.getStringExtra(CRYPTO_FILE));
            File in = new File(Environment.getExternalStoragePublicDirectory(DirectoryConstants.CRYPTO_FOLDER),
                    src.getName());
            try{
//                in.createNewFile();
                OutputStream os = new FileOutputStream(in);
                os.write(read(src));
                os.close();
            } catch (Exception e){
                e.printStackTrace();
            }

            if (ACTION_ENC.equals(action)) {
                this.handleFileEncryption(in, (aes)intent.getSerializableExtra(CRYPTO_SERV));
            } else if (ACTION_DEC.equals(action)) {
                this.handleFileDecryption(in, (aes)intent.getSerializableExtra(CRYPTO_SERV));
            }
        }
    }

    protected void handleFileEncryption(File sel, aes m){
    Log.w("file enc serv", "yes");
        file_aes mastercry = new file_aes(m);
        File path = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS),
                DirectoryConstants.CRYPTO_FOLDER);
        path.mkdirs();
        File output = new File(path.getPath() +"/" + sel.getName());
        Log.d("output", output.getAbsolutePath());
        try {
            FileOutputStream fos = new FileOutputStream(output);
            fos.write(read(sel));
            fos.flush();
            fos.close();
        } catch(Exception e){
            Log.e("error", e.toString());
        }
        mastercry.encryptFile(output);
        Log.w("enc", "done");
    }

    private void handleFileDecryption(File sel, aes m){

        file_aes mastercry = new file_aes(m);
        File path = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS),
                DirectoryConstants.CRYPTO_FOLDER);
        path.mkdirs();
        File output = new File(path.getPath() +"/" + sel.getName());
        Log.d("output", output.getAbsolutePath());
        try {
            FileOutputStream fos = new FileOutputStream(output);
            fos.write(read(sel));
            fos.flush();
            fos.close();
        } catch(Exception e){
            Log.e("error", e.toString());
        }
        mastercry.decryptFile(output);
        Log.w("dec", "done");
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
