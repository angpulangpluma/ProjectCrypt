package com.dlsu.getbetter.getbetter.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.DirectoryConstants;
import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.activities.HomeActivity;
import com.dlsu.getbetter.getbetter.adapters.HealthCenterListAdapter;
import com.dlsu.getbetter.getbetter.cryptoGB.KeySetter;
import com.dlsu.getbetter.getbetter.cryptoGB.Serializator;
import com.dlsu.getbetter.getbetter.cryptoGB.aes;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.objects.DividerItemDecoration;
import com.dlsu.getbetter.getbetter.objects.HealthCenter;
import com.dlsu.getbetter.getbetter.sessionmanagers.SystemSessionManager;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import static android.os.Environment.DIRECTORY_DOCUMENTS;

public class HealthCenterActivity extends AppCompatActivity {

    private DataAdapter getBetterDb;
    private SystemSessionManager systemSessionManager;
    private ArrayList<HealthCenter> healthCenters;
    private KeySetter ks = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_center);

        systemSessionManager = new SystemSessionManager(this);
        if(systemSessionManager.checkLogin())
            finish();

        HashMap<String, String> user = systemSessionManager.getUserDetails();
        TextView userLabel = (TextView)findViewById(R.id.hc_welcome_text);
        RecyclerView healthCenterRecycler = (RecyclerView)findViewById(R.id.hc_recycler_view);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(this);


        String email = user.get(SystemSessionManager.LOGIN_USER_NAME);
        healthCenters = new ArrayList<>();
        initializeDatabase();
        getHealthCenterList();

        userLabel.append(" " + getUserName(email) + "!");
        HealthCenterListAdapter healthCenterListAdapter = new HealthCenterListAdapter(healthCenters);

        if (healthCenterRecycler != null) {
            healthCenterRecycler.setHasFixedSize(true);
            healthCenterRecycler.setLayoutManager(new LinearLayoutManager(this));
            healthCenterRecycler.setAdapter(healthCenterListAdapter);
            healthCenterRecycler.addItemDecoration(dividerItemDecoration);
        }

        healthCenterListAdapter.SetOnItemClickListener(new HealthCenterListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                String healthCenterName = healthCenters.get(position).getHealthCenterName();
                String healthCenterId = String.valueOf(healthCenters.get(position).getHealthCenterId());

                systemSessionManager.setHealthCenter(healthCenterName, healthCenterId);

                ks = new KeySetter(view.getContext());
                ks.read(Integer.parseInt(systemSessionManager.getHealthCenter().get(SystemSessionManager.HEALTH_CENTER_ID)));
                Log.w("crypt", Boolean.toString(ks.getCrypto()!=null));
                byte[] ky = ks.getCrypto().getKey().getEncoded();
                char[] ch = new char[ky.length];
                for(int i=0; i<ch.length; i++)
                    ch[i] = Byte.valueOf(ky[i]).toString().charAt(0);
                Log.w("key", String.valueOf(ch));
//                    ch[i] = Byte.toString(ky[i]).;
//                Log.w("key", String.valueOf(ks.getCrypto().getKey().getEncoded()));
                cryptoPrep(ks.getCrypto());

                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
//                intent.putExtra("sys", ks.getCrypto());
                startActivity(intent);
                finish();
            }
        });
    }

    private void cryptoPrep(aes master) {
        checkPermissions(this);
        File set = null;
//        OutputStream in = null;
//        DataOutputStream dos = null;
        set = createFile(this, "crypto.dat");
        if(set!=null){
            try{
                master.saveKey(master.getKey(), set);
                File path = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS),
                    DirectoryConstants.CRYPTO_FOLDER);
                copyFiles(set, new File(path, "datadb.dat"));
//                in = new FileOutputStream(set);
//                dos = new DataOutputStream(in);
//                dos.write(master.getKey().getEncoded());
            } catch(Exception e){
                Log.w("error", e.getMessage());
            }
        }
    }

//    private void cryptoPrep(Serializable ser){
//        checkPermissions(this);
//        File set = null;
//        set = createFile(this, "datadb.dat");
////        set = createFile(
////                new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS),
////                        DirectoryConstants.CRYPTO_FOLDER).getPath(),
////                "data.dat");
//        if(set!=null){
//            Log.w("serializing?", "file set!");
//            Serializator.serialize(ser, set.getPath());
////            File path = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS),
////                        DirectoryConstants.CRYPTO_FOLDER);
////            copyFiles(set, new File(path, "datadb.dat"));
//        } else Log.w("serializing?", "file not set...");
//
////        Log.w("crypt", Boolean.toString(ser!=null));
//
//    }

    private void initializeDatabase () {

        getBetterDb = new DataAdapter(this);

        try {
            getBetterDb.createDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void getHealthCenterList() {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        healthCenters.addAll(getBetterDb.getHealthCenters());

        getBetterDb.closeDatabase();

    }

    private String getUserName(String userEmail) {


        try {
            getBetterDb.openDatabase();
        }catch (SQLException e) {
            e.printStackTrace();
        }

        String username = getBetterDb.getUserName(userEmail);

        getBetterDb.closeDatabase();

        return username;
    }

    private File createFile(Context con, String newname){
        checkPermissions(this);
        File f = null;
        InputStream in;
        OutputStream out;
        boolean isFileUnlocked = false;
        try {
            f = con.getFileStreamPath(newname);
            if (!f.exists()) {
                if (f.createNewFile()) {
                    Log.w("file?", "new");
                    in = new FileInputStream(f);
                    out = new FileOutputStream(f);
                    if (IOUtils.copy(in, out)>0) {
                        Log.w("copy?", "yes");
                        out.close();
                        in.close();
                        if (f.canRead()) {
                            Log.w("read?", "yes");
                            try {
                                long lastmod = f.lastModified();
                                Log.w("last modified", Long.toString(lastmod));
                                org.apache.commons.io.FileUtils.touch(f);
                                isFileUnlocked = true;
                            } catch (IOException e) {
                                //                            isFileUnlocked = false;
                                Log.w("error", e.getMessage());
                            }
                        } else Log.w("read?", "no");
                    } else Log.w("copy?", "no");
                } else Log.w("file?", "no");
            } else Log.w("exists?", "yes");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    private void copyFiles(File oldfile, File newfile){
        checkPermissions(this);
        File f = null;
        InputStream in;
        OutputStream out;
        boolean isFileUnlocked = false;
        try {
            if(oldfile.exists() && newfile.exists()){
                Log.w("copy", "start!");
                in = new FileInputStream(oldfile);
                out = new FileOutputStream(newfile);
                if(IOUtils.copy(in, out)>0){
                    Log.w("copy?", "yes!");
                } else Log.w("copy?", "no!");
                out.close();
                in.close();
            }
        } catch (IOException e) {
            Log.w("error", e.getMessage());
        }
    }

    private void checkPermissions(Context context){
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
