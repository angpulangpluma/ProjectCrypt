package com.dlsu.getbetter.getbetter.cryptoGB;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.dlsu.getbetter.getbetter.DirectoryConstants;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.objects.HealthCenter;
import com.dlsu.getbetter.getbetter.sessionmanagers.SystemSessionManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.crypto.spec.SecretKeySpec;

import static android.os.Environment.DIRECTORY_DOCUMENTS;

/**
 * Created by student on 10/11/2017.
 */

public class KeySetter{

//    private transient SystemSessionManager mngr;
    private transient aes mscrypto;
    private transient Context dCtxt;

    public KeySetter(Context c){
        Log.w("key setter", "set");
        this.dCtxt = c;
//        this.mngr = sys;
        this.mscrypto = null;
    }

//    public SystemSessionManager getSSM() { return this.mngr; }

    public void init(){
        Log.w("key setter", "init start");
        aes master = new aes();
        File out = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS),
                "tpyrc");
        DataAdapter gbDatabase = null;
        PrintWriter on = null;

        try {
            on = new PrintWriter(out);
            gbDatabase = new DataAdapter(dCtxt);
            gbDatabase.createDatabase();
            gbDatabase.openDatabase();
            Log.w("key setter", "init success");
        } catch(Exception e){
            e.printStackTrace();
        }

        ArrayList<HealthCenter> hcs = new ArrayList<HealthCenter>();
        hcs.addAll(gbDatabase.getHealthCenters());
        for (HealthCenter hc : hcs){
            master.setKey();
            on.println(hc.getHealthCenterId() + " " + master.getKey().getEncoded());
        }

        try {
            on.close();
            Log.w("key setter", "finished");
        } catch(Exception e) { e.printStackTrace(); }
        //get number of health centers
        //for each health center
    }

    public void read(int sel){
        Log.w("key setter", "read start");
        Log.w("sel val", String.valueOf(sel));

//        HashMap<String, String> selHC = mngr.getHealthCenter();
//        for (String key : selHC.keySet()){
//            String tag = key.toString();
//            String value = selHC.get(key).toString();
//            Log.w(tag, value);
//        }
        String buffer = "";
        File src = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS),
                "tpyrc");
        FileReader fr = null;
        BufferedReader ins = null;
        int total = 0;
        int nRead = 0;
        ArrayList<String> info = new ArrayList<String>();
        try {
            fr = new FileReader(src);
            ins = new BufferedReader(fr);
            while((buffer = ins.readLine())!= null){
                Log.w("read", buffer);
//                total += nRead;
                info.add(buffer);
            }
            ins.close();
            fr.close();
//            System.out.println("read " + total + " bytes");
        } catch(Exception e){ e.printStackTrace(); Log.e("error", e.toString()); }

        for (String s : info){
            Log.w("comparison", s);
            if (s.contains(Integer.toString(sel))){
                String[] input = s.split(" ");
                Log.w("key", input[1]);
                cryptoInit(input[1]);
            }
        }
    }

    public void cryptoInit(String key){
        mscrypto = new aes(new SecretKeySpec(key.getBytes(), 0, key.length(), "AES"));
        mscrypto.setCipher();
    }

    public aes getCrypto(){ return mscrypto; }

}
