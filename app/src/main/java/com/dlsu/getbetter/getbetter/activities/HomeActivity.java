package com.dlsu.getbetter.getbetter.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.AddInstructionsCaseFragment;
import com.dlsu.getbetter.getbetter.ClosedCaseFragment;
import com.dlsu.getbetter.getbetter.DetailsFragment;
import com.dlsu.getbetter.getbetter.DiagnosedCaseFragment;
import com.dlsu.getbetter.getbetter.DirectoryConstants;
import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.UrgentCaseFragment;
import com.dlsu.getbetter.getbetter.cryptoGB.Serializator;
import com.dlsu.getbetter.getbetter.cryptoGB.aes;
import com.dlsu.getbetter.getbetter.cryptoGB.file_aes;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.sessionmanagers.SystemSessionManager;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;

import static android.os.Environment.DIRECTORY_DOCUMENTS;


public class HomeActivity extends AppCompatActivity implements View.OnClickListener,
        DiagnosedCaseFragment.OnCaseRecordSelected, UrgentCaseFragment.OnCaseRecordSelected,
        ClosedCaseFragment.OnCaseRecordSelected {

    private SystemSessionManager systemSessionManager;
    private DataAdapter getBetterDb;
    private boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        Log.w("sys", Boolean.toString(getIntent().getSerializableExtra("sys")!=null));
//        Log.w("key", String.valueOf(((aes)getIntent().getSerializableExtra("sys")).getKey().getEncoded()));
//        cryptoInit(new File("crypto.dat"));
        systemSessionManager = new SystemSessionManager(this);
        if(systemSessionManager.checkLogin())
            finish();

        HashMap<String, String> user = systemSessionManager.getUserDetails();
        HashMap<String, String> hc = systemSessionManager.getHealthCenter();

        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        String userNameLabel = user.get(SystemSessionManager.LOGIN_USER_NAME);
        String currentHealthCenter = hc.get(SystemSessionManager.HEALTH_CENTER_NAME);
        initializeDatabase();

        TextView welcomeText = (TextView)findViewById(R.id.home_welcome_text);
        Button viewCreatePatientBtn = (Button)findViewById(R.id.view_create_patient_records_btn);
        Button downloadAdditionalContentBtn = (Button)findViewById(R.id.download_content_btn);
        Button logoutBtn = (Button)findViewById(R.id.logout_btn);
        Button changeHealthCenterBtn = (Button)findViewById(R.id.change_health_center_btn);
        TextView healthCenter = (TextView)findViewById(R.id.home_current_health_center);

        welcomeText.append(" " + getUserName(userNameLabel) + "!");


        FragmentTabHost fragmentTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
//        FragmentTabHost fragmentTabHost = new FragmentTabHost(this);
        fragmentTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);


        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("urgent").setIndicator("Urgent Cases"),
                UrgentCaseFragment.class, null);

        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("additional instructions").setIndicator("Cases w/ Additional Instructions"),
                AddInstructionsCaseFragment.class, null);

        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("diagnosed").setIndicator("Diagnosed Cases"),
                DiagnosedCaseFragment.class, null);

        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("closed").setIndicator("Closed Cases"),
                ClosedCaseFragment.class, null);


        if (healthCenter != null) {
            healthCenter.setText(currentHealthCenter);
        }

        TextView userLabel = (TextView)findViewById(R.id.user_label);
        if (userLabel != null) {
            userLabel.setText(userNameLabel);
        }

        if (viewCreatePatientBtn != null) {
            viewCreatePatientBtn.setOnClickListener(this);
        }

        if (downloadAdditionalContentBtn != null) {
            downloadAdditionalContentBtn.setOnClickListener(this);
        }

        if (changeHealthCenterBtn != null) {
            changeHealthCenterBtn.setOnClickListener(this);
        }

        if (logoutBtn != null) {
            logoutBtn.setOnClickListener(this);
        }

    }

//    private void doSomethingCryptFile(String dec, File input){
//
//        Log.w("service in", "yes");
//
//        file_aes mastercry = new file_aes(cryptoInit(new File("crypto.dat")));
////        File path = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS),
////                DirectoryConstants.CRYPTO_FOLDER);
////        path.mkdirs();
////        File output = new File(path.getPath() +"/" + input.getName());
////        File output = new File(path.getPath() +"/" + input.getName());
////        Log.w("output", output.getAbsolutePath());
////        try {
////            FileOutputStream fos = new FileOutputStream(input);
////            fos.write(read(input));
////            fos.flush();
////            fos.close();
////        } catch(Exception e){
////            Log.w("error", e.toString());
////        }
//        switch(dec){
//            case "enc":{
//                mastercry.encryptFile(input);
//                Log.d("Action", "enc");
//            }; break;
//            case "dec":{
//                mastercry.decryptFile(input);
//                Log.d("Action", "dec");
//            }; break;
//        }
////
//    }
//
//    private aes cryptoInit(File set) {
//        checkPermissions(this);
////        File set = null;
////        OutputStream in = null;
////        DataOutputStream dos = null;
//        set = createFile(this, "crypto.dat");
//        aes master = null;
//        if(set!=null){
//            try{
//                master = new aes();
//                master.loadKey(set);
////                master.saveKey(master.getKey(), set);
////                in = new FileOutputStream(set);
////                dos = new DataOutputStream(in);
////                dos.write(master.getKey().getEncoded());
//            } catch(Exception e){
//                Log.w("error", e.getMessage());
//            }
//        }
//
//        return master;
//    }
//
////    private void cryptoInit(){
////        checkPermissions(this);
////        File set = null;
////        set = createFile(this, "datadb.dat");
////        if(set!=null){
////            aes f = Serializator.deserialize(set.getPath(), aes.class);
////            Log.w("crypto", Boolean.toString(f!=null));
////            Log.w("key", String.valueOf(f.getKey().getEncoded()));
////            Log.w("cipher", Boolean.toString(f.getCipher()!=null));
////        }
////    }
//
//    private File createFile(Context con, String newname){
//        checkPermissions(this);
//        File f = null;
//        InputStream in;
//        OutputStream out;
//        boolean isFileUnlocked = false;
//        try {
//            f = con.getFileStreamPath(newname);
//            if (!f.exists()) {
//                if (f.createNewFile()) {
//                    Log.w("file?", "new");
//                    in = new FileInputStream(f);
//                    out = new FileOutputStream(f);
//                    if (IOUtils.copy(in, out)>0) {
//                        Log.w("copy?", "yes");
//                        out.close();
//                        in.close();
//                        if (f.canRead()) {
//                            Log.w("read?", "yes");
//                            try {
//                                long lastmod = f.lastModified();
//                                Log.w("last modified", Long.toString(lastmod));
//                                org.apache.commons.io.FileUtils.touch(f);
//                                isFileUnlocked = true;
//                            } catch (IOException e) {
//                                //                            isFileUnlocked = false;
//                                Log.w("error", e.getMessage());
//                            }
//                        } else Log.w("read?", "no");
//                    } else Log.w("copy?", "no");
//                } else Log.w("file?", "no");
//            } else Log.w("exists?", "yes");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return f;
//    }
////
//    private void checkPermissions(Context context){
//        int readStuff = ContextCompat.checkSelfPermission(context,
//                Manifest.permission.READ_EXTERNAL_STORAGE);
//        int writeStuff = ContextCompat.checkSelfPermission(context,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE);
////        Log.w("read?", Integer.toString(readStuff));
////        Log.w("write?", Integer.toString(writeStuff));
//        //for read stuff
//        if(readStuff == PackageManager.PERMISSION_GRANTED)
//            Log.w("read?", "yes");
//        else if(readStuff == PackageManager.PERMISSION_DENIED)
//            Log.w("read?", "no");
//
//        //for write stuff
//        if(writeStuff == PackageManager.PERMISSION_GRANTED)
//            Log.w("write?", "yes");
//        else if(writeStuff == PackageManager.PERMISSION_DENIED)
//            Log.w("write?", "no");
//    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if(id == R.id.view_create_patient_records_btn) {

//            systemSessionManager.setHealthCenter(healthCenterName, String.valueOf(healthCenterId));
            Intent intent = new Intent(this, ExistingPatientActivity.class);
//            intent.putExtra("sys", getIntent().getSerializableExtra("sys"));
            startActivity(intent);


        } else if (id == R.id.download_content_btn) {

            if(getInternetConnectivityStatus()) {

                //            systemSessionManager.setHealthCenter(healthCenterName, String.valueOf(healthCenterId));
                Intent intent = new Intent(this, DownloadContentActivity.class);
//                intent.putExtra("sys", getIntent().getSerializableExtra("sys"));
                startActivity(intent);

            } else {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Feature under development.");
                builder.setMessage("Sorry. This feature is still being fixed.");

                builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.show();
            }



        } else if (id == R.id.logout_btn) {

            systemSessionManager.logoutUser();

        } else if (id == R.id.change_health_center_btn) {

            Intent intent = new Intent(this, HealthCenterActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initializeDatabase () {

        getBetterDb = new DataAdapter(this);

        try {
            getBetterDb.createDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    @Override
    public void onCaseRecordSelected(int caseRecordId) {

        if(findViewById(R.id.case_detail) != null) {
            Log.d("choice", caseRecordId + "");
            Bundle arguments = new Bundle();
            arguments.putInt("case record id", caseRecordId);
            DetailsFragment fragment = new DetailsFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction().replace(R.id.case_detail, fragment).commit();
        }
    }

    private boolean getInternetConnectivityStatus() {

        ConnectivityManager cm =
                (ConnectivityManager)getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    private void initializeUpdatedTabs(HomeActivity activity) {


    }
}
