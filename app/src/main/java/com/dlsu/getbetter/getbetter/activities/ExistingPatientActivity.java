package com.dlsu.getbetter.getbetter.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.DirectoryConstants;
import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.adapters.ExistingPatientAdapter;
import com.dlsu.getbetter.getbetter.cryptoGB.aes;
import com.dlsu.getbetter.getbetter.cryptoGB.file_aes;
import com.dlsu.getbetter.getbetter.cryptoGB.timealgo;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.objects.DividerItemDecoration;
import com.dlsu.getbetter.getbetter.objects.Patient;
import com.dlsu.getbetter.getbetter.sessionmanagers.SystemSessionManager;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import static android.os.Environment.DIRECTORY_DOCUMENTS;


public class ExistingPatientActivity extends AppCompatActivity implements View.OnClickListener {

    private DataAdapter getBetterDb;
    private Long selectedPatientId;
    private String patientFirstName;
    private String patientLastName;
    private ArrayList<Patient> existingPatients;
    private ProgressDialog pDialog;

    private boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_existing_patient);

//        Log.w("sys", Boolean.toString(getIntent().getSerializableExtra("sys")!=null));

        SystemSessionManager systemSessionManager = new SystemSessionManager(this);
        if(systemSessionManager.checkLogin())
            finish();

        HashMap<String, String> user = systemSessionManager.getUserDetails();
        HashMap<String, String> hc = systemSessionManager.getHealthCenter();
        int healthCenterId = Integer.parseInt(hc.get(SystemSessionManager.HEALTH_CENTER_ID));
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        Button newPatientRecBtn = (Button)findViewById(R.id.create_new_patient_btn);
        Button uploadCaseRecBtn = (Button)findViewById(R.id.upload_case_record);
        Button uploadPatientRecBtn = (Button) findViewById(R.id.upload_patient_record);
        Button backBtn = (Button)findViewById(R.id.existing_patient_back_btn);
        FrameLayout container = (FrameLayout)findViewById(R.id.existing_patient_container);


        RecyclerView existingPatientListView = (RecyclerView) findViewById(R.id.existing_patient_list);
        RecyclerView.LayoutManager existingPatientLayoutManager = new LinearLayoutManager(this);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(this);

        existingPatients = new ArrayList<>();
        initializeDatabase();
        new GetPatientListTask().execute(healthCenterId);

//        prepFilesDisplay();

        ExistingPatientAdapter existingPatientsAdapter = new ExistingPatientAdapter(existingPatients);

        existingPatientListView.setHasFixedSize(true);
        existingPatientListView.setLayoutManager(existingPatientLayoutManager);
        existingPatientListView.setAdapter(existingPatientsAdapter);
        existingPatientListView.addItemDecoration(dividerItemDecoration);
        existingPatientsAdapter.SetOnItemClickListener(new ExistingPatientAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {

                prepFilesStore();
                selectedPatientId = existingPatients.get(position).getId();
                Intent intent = new Intent(ExistingPatientActivity.this, ViewPatientActivity.class);
//                intent.putExtra("sys", getIntent().getSerializableExtra("sys"));
                intent.putExtra("patientId", selectedPatientId);
                startActivity(intent);
                ExistingPatientActivity.this.finish();

            }
        });
        existingPatientsAdapter.notifyDataSetChanged();
//        if(existingPatients.isEmpty()) {
//            TextView textView = new TextView(this);
//            textView.setText("Patient List Empty");
//            textView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//            container.addView(textView);
//            existingPatientListView.setVisibility(View.GONE);
//        }
//        else{

//        }

        newPatientRecBtn.setOnClickListener(this);
        uploadPatientRecBtn.setOnClickListener(this);
        uploadCaseRecBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
    }

    private void initializeDatabase () {

        getBetterDb = new DataAdapter(this);

        try {
            getBetterDb.createDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void getExistingPatients(int healthCenterId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        existingPatients.addAll(getBetterDb.getPatients(healthCenterId));

        getBetterDb.closeDatabase();

    }

    private void getLatestCaseRecordHistory(int patientId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // TODO: 16/11/2016 get last consultation date

        getBetterDb.closeDatabase();
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.create_new_patient_btn) {

            prepFilesStore();
            Intent intent = new Intent(this, NewPatientInfoActivity.class);
//            intent.putExtra("sys", getIntent().getSerializableExtra("sys"));
            startActivity(intent);

        } else if (id == R.id.upload_patient_record) {

            if(isConnected) {
                prepFilesStore();
                Intent intent = new Intent(this, UploadPatientToServerActivity.class);
//                intent.putExtra("sys", getIntent().getSerializableExtra("sys"));
                startActivity(intent);
                finish();
            } else {
                featureAlertMessage("No Internet connection detected. Please make sure you are connected to the internet.");
            }


        } else if (id == R.id.upload_case_record) {

            if(isConnected) {
                prepFilesStore();
                Intent intent = new Intent(this, UploadCaseRecordToServerActivity.class);
//                intent.putExtra("sys", getIntent().getSerializableExtra("sys"));
                startActivity(intent);
                finish();
            } else {
                featureAlertMessage("No Internet connection detected. Please make sure you are connected to the internet.");
            }


        } else if (id == R.id.existing_patient_back_btn) {
            finish();
        }
    }

    private class GetPatientListTask extends AsyncTask<Integer, Void, String> {


        @Override
        protected void onPreExecute () {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected String doInBackground(Integer... params) {

            getExistingPatients(params[0]);
            prepFilesDisplay();

            return "Done!";
        }

        @Override
        protected void onPostExecute (String results) {

            super.onPostExecute(results);
            dismissProgressDialog();

        }
    }

    private void showProgressDialog() {
        if(pDialog == null) {
            pDialog = new ProgressDialog(ExistingPatientActivity.this);
            pDialog.setTitle("Please wait for a few moments");
            pDialog.setMessage("Populating patient list");
            pDialog.setIndeterminate(true);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        pDialog.show();
    }

    private void dismissProgressDialog() {

        if(pDialog != null && pDialog.isShowing()) {
            pDialog.hide();
            pDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    private void featureAlertMessage(String result) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("INTERNET CONNECTION");
        builder.setMessage(result);

        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    //decrypt files for display
    private void prepFilesDisplay(){
        if (existingPatients.size()>0){
            Log.w("dec time?", "yes!");
            for(int i=0; i<existingPatients.size(); i++){
                String prof = existingPatients.get(i).getProfileImageBytes();
                Log.w("file", prof);
                doSomethingCryptFile("dec", new File(prof));
            }
        } else Log.w("dec time?", "no!");
//        doSomethingCryptFile("dec", new File(patientProfileImage));
//        if(attachments.size()>0){
//            for(int i=0; i<attachments.size(); i++)
//                doSomethingCryptFile("dec", new File(attachments.get(i).getAttachmentPath()));
//        }
    }

    //encrypt files for storage
    private void prepFilesStore(){
        if (existingPatients.size()>0){
            for(int i=0; i<existingPatients.size(); i++){
                String prof = existingPatients.get(i).getProfileImageBytes();
                doSomethingCryptFile("enc", new File(prof));
            }
        }
//        doSomethingCryptFile("enc", new File(patientProfileImage));
//        if(attachments.size()>0){
//            for(int i=0; i<attachments.size(); i++)
//                doSomethingCryptFile("enc", new File(attachments.get(i).getAttachmentPath()));
//        }
    }

    private void doSomethingCryptFile(String dec, File input){

        Log.w("service in", "yes");

        file_aes mastercry = new file_aes(cryptoInit(new File("crypto.dat")));
//        File path = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS),
//                DirectoryConstants.CRYPTO_FOLDER);
//        path.mkdirs();
//        File output = new File(path.getPath() +"/" + input.getName());
//        File output = new File(path.getPath() +"/" + input.getName());
//        Log.w("output", output.getAbsolutePath());
//        try {
//            FileOutputStream fos = new FileOutputStream(input);
//            fos.write(read(input));
//            fos.flush();
//            fos.close();
//        } catch(Exception e){
//            Log.w("error", e.toString());
//        }
        timealgo tester = new timealgo(mastercry);
        File testpath = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS),
                DirectoryConstants.CRYPTO_FOLDER);
        File test = new File(testpath.getPath(), "test_log.txt");
        try {
            if (testpath.mkdirs() && (test.createNewFile() || test.exists())) {
                FileWriter fw = new FileWriter(test, true);
                tester.setFileLog(fw);
                switch (dec) {
                    case "enc": {
//                        mastercry.encryptFile(input);
                        tester.writeEncTime(3, input, null);
                        Log.d("Action", "enc");
                    }
                    ;
                    break;
                    case "dec": {
//                        mastercry.decryptFile(input);
                        tester.writeDecTime(3, input, null);
                        Log.d("Action", "dec");
                    }
                    ;
                    break;
                }
                tester.finishTest();
            }
        } catch(Exception e){
            Log.w("error", e.toString());
        }
//
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
                Log.w("error", e.getMessage());
            }
        }
        return buffer;
    }

    private aes cryptoInit(File set) {
        checkPermissions(this);
//        File set = null;
//        OutputStream in = null;
//        DataOutputStream dos = null;
        set = createFile(this, "crypto.dat");
        aes master = null;
        if(set!=null){
            try{
                master = new aes();
                master.loadKey(set);
                master.setCipher();
//                master.saveKey(master.getKey(), set);
//                in = new FileOutputStream(set);
//                dos = new DataOutputStream(in);
//                dos.write(master.getKey().getEncoded());
            } catch(Exception e){
                Log.w("error", e.getMessage());
            }
        }
        return master;
    }

//    private aes cryptoInit(){
////        Serializator str = new Serializator();
//        checkPermissions(this);
//        File set = null;
//        aes mstr = null;
//        set = createFile(this, "datadb.dat");
//        if(set!=null){
//            mstr = Serializator.deserialize(set.getPath(), aes.class);
//            Log.w("crypto", Boolean.toString(mstr!=null));
//            Log.w("key", String.valueOf(mstr.getKey().getEncoded()));
//            Log.w("cipher", Boolean.toString(mstr.getCipher()!=null));
//        }
//        return mstr;
//    }

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

    private File createFileDuplicate(String path, String newname, String oldfile){
        checkPermissions(this);
        File f = null;
        InputStream in;
        OutputStream out;
        boolean isFileUnlocked = false;
        try {
            f = new File(path, newname + "." +
                    FilenameUtils.getExtension(oldfile));
            if(f.createNewFile()) {
                Log.w("file?", "yes");
                in = new FileInputStream(new File(oldfile));
                out = new FileOutputStream(f);
                if (IOUtils.copy(in, out)>-1) {
                    Log.w("copy?", "yes");
                    out.close();
                    in.close();
                    if (f.canRead()) {
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
                } else Log.w("copy?", "no");
            } else {
                f = null;
                Log.w("file?", "no");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
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
