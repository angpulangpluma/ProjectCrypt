package com.dlsu.getbetter.getbetter.cryptoGB;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CryptoFileService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_ENC = "com.dlsu.getbetter.getbetter.cryptoGB.action.ENC";
    private static final String ACTION_DEC = "com.dlsu.getbetter.getbetter.cryptoGB.action.DEC";

    // TODO: Rename parameters
    private static final String CRYPTO_FILE = "com.dlsu.getbetter.getbetter.cryptoGB.extra.FILE";
    private static final String CRYPTO_HCID = "com.dlsu.getbetter.getbetter.cryptoGB.extra.HCID";

    public CryptoFileService() {
        super("CryptoFileService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void cryptoAskEncrypt(Context context, File sel, int hcID) {
        Intent intent = new Intent(context, CryptoFileService.class);
        intent.setAction(ACTION_ENC);
        intent.putExtra(CRYPTO_FILE, sel);
        intent.putExtra(CRYPTO_HCID, hcID);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void cryptoAskDecrypt(Context context, File sel, int hcID) {
        Intent intent = new Intent(context, CryptoFileService.class);
        intent.setAction(ACTION_DEC);
        intent.putExtra(CRYPTO_FILE, sel);
        intent.putExtra(CRYPTO_HCID, hcID);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_ENC.equals(action)) {
//                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
//                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
//                handleActionFoo(param1, param2);


            } else if (ACTION_DEC.equals(action)) {
//                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
//                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
//                handleActionBaz(param1, param2);
            }
        }
    }

    private void handleFileEncryption(File sel){

    }

    private void handleFileDecryption(File sel){

    }

    private byte[] read(File file) throws IOException {
        byte[] buffer = new byte[(int) file.length()];
        InputStream ios = null;
        try{
            ios = new FileInputStream(file);
            if(ios.read(buffer)==-1){
                throw new IOException(
                        "EOF reached while trying to read the whole file.");
            }
        } finally{
            try {
                if (ios != null) ios.close();
            } catch (IOException e){

            }
        }
        return buffer;
    }



//    /**
//     * Handle action Foo in the provided background thread with the provided
//     * parameters.
//     */
//    private void handleActionFoo(String param1, String param2) {
//        // TODO: Handle action Foo
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
//
//    /**
//     * Handle action Baz in the provided background thread with the provided
//     * parameters.
//     */
//    private void handleActionBaz(String param1, String param2) {
//        // TODO: Handle action Baz
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
}
