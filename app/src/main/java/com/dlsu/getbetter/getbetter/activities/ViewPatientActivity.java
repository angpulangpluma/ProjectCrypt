package com.dlsu.getbetter.getbetter.activities;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.DirectoryConstants;
import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.adapters.CaseRecordAdapter;
import com.dlsu.getbetter.getbetter.cryptoGB.aes;
import com.dlsu.getbetter.getbetter.cryptoGB.file_aes;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.objects.CaseRecord;
import com.dlsu.getbetter.getbetter.objects.DividerItemDecoration;
import com.dlsu.getbetter.getbetter.objects.Patient;
import com.dlsu.getbetter.getbetter.sessionmanagers.NewPatientSessionManager;
import com.dlsu.getbetter.getbetter.sessionmanagers.SystemSessionManager;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.os.Environment.DIRECTORY_DOCUMENTS;


public class ViewPatientActivity extends AppCompatActivity implements View.OnClickListener {

    private DataAdapter getBetterDb;
    private long patientId;
    private ArrayList<CaseRecord> caseRecords;
    private NewPatientSessionManager newPatientSessionManager;
    private Patient patient;

    private static final String TAG = "ViewPatient";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_patient);

        SystemSessionManager systemSessionManager = new SystemSessionManager(this);
        if(systemSessionManager.checkLogin())
            finish();

        Bundle extras = getIntent().getExtras();
        if(extras != null)
            patientId = extras.getLong("patientId");

        Log.d(TAG, "onCreate: " + patientId);

        CircleImageView profileImage = (CircleImageView)findViewById(R.id.view_patient_profile_image);
        TextView patientName = (TextView)findViewById(R.id.view_patient_name);
        TextView age = (TextView)findViewById(R.id.view_patient_age);
        TextView gender = (TextView)findViewById(R.id.view_patient_gender);
        TextView civilStatus = (TextView)findViewById(R.id.view_patient_civil_status);
        TextView bloodType = (TextView)findViewById(R.id.view_patient_blood);
//        TextView contactInfo = (TextView)findViewById(R.id.view_patient_contact);
//        TextView addressInfo = (TextView)findViewById(R.id.view_patient_address);
//        TextView caseRecordCount = (TextView)findViewById(R.id.view_patient_case_record_count);
        RecyclerView caseRecordList = (RecyclerView)findViewById(R.id.view_patient_case_recycler);
        Button backBtn = (Button)findViewById(R.id.view_patient_back_btn);
        Button updatePatientBtn = (Button)findViewById(R.id.view_patient_update_btn);
        Button newCaseRecordBtn = (Button)findViewById(R.id.view_patient_create_case_btn);

        initializeDatabase();
        caseRecords = new ArrayList<>();
        patient = getPatientInfo();
        getCaseRecords();
        createPatientSession(this);

        File profpic = checkFile("", FilenameUtils.getName(new File(patient.getProfileImageBytes()).getPath()));
        if (profpic !=null){
            Log.w("got inner pic", profpic.getPath());
            setPic(profileImage, profpic.getPath());
        } else{
            Log.w("did not get inner pic", patient.getProfileImageBytes());
            doSomethingCryptFile("dec", new File(patient.getProfileImageBytes()));
            setPic(profileImage, patient.getProfileImageBytes());
        }

        patientName.setText(patientFullName(patient.getLastName() + ", ", patient.getFirstName(), patient.getMiddleName()));
        age.append(": " + patient.getAge() + " Years Old");
        gender.append(": " + patient.getGender());
        civilStatus.append(": " + patient.getCivilStatus());
        bloodType.append(": " + patient.getBloodType());

        RecyclerView.LayoutManager caseRecordsLayoutManager = new LinearLayoutManager(this);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(this);
        CaseRecordAdapter caseRecordAdapter = new CaseRecordAdapter(caseRecords);
        caseRecordList.setHasFixedSize(true);
        caseRecordList.setLayoutManager(caseRecordsLayoutManager);
        caseRecordList.setAdapter(caseRecordAdapter);
        caseRecordList.addItemDecoration(dividerItemDecoration);
        caseRecordAdapter.SetOnItemClickListener(new CaseRecordAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                int selectedCaseRecordId = caseRecords.get(position).getCaseRecordId();
                Intent intent = new Intent(ViewPatientActivity.this, ViewCaseRecordActivity.class);
                intent.putExtra("caseRecordId", selectedCaseRecordId);
                intent.putExtra("patientId", patientId);
                startActivity(intent);
            }
        });

        backBtn.setOnClickListener(this);
        updatePatientBtn.setOnClickListener(this);
        newCaseRecordBtn.setOnClickListener(this);
    }

    private void initializeDatabase () {

        getBetterDb = new DataAdapter(this);

        try {
            getBetterDb.createDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Patient getPatientInfo() {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Patient patient = getBetterDb.getPatient(patientId);
        getBetterDb.closeDatabase();

        return patient;

    }

    private void getCaseRecords() {

        class GetCaseRecordsTask extends AsyncTask<Void, Void, Void> {

//        private ProgressDialog progressDialog = new ProgressDialog(CreateUpdateCaseRecordActivity.this);

            @Override
            protected void onPreExecute () {
                super.onPreExecute();
//            progressDialog.setMessage("Populating Case Records List...");
//            progressDialog.show();

            }

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    getBetterDb.openDatabase();
                } catch (SQLException e) {
                    e.printStackTrace();
                }


                caseRecords.addAll(getBetterDb.getCaseRecords(patientId));
                getBetterDb.closeDatabase();
//            getCaseRecordStatus();

                return null;
            }

            @Override
            protected void onPostExecute (Void aVoid) {

                super.onPostExecute(aVoid);
//            progressDialog.hide();
//            progressDialog.dismiss();
            }
        }

        GetCaseRecordsTask getCaseRecordsTask = new GetCaseRecordsTask();
        getCaseRecordsTask.execute();

    }

    private void createPatientSession(ViewPatientActivity activity) {

        newPatientSessionManager = new NewPatientSessionManager(activity);
//        newPatientSessionManager.createNewPatientSession(patient.getFirstName(),
//                patient.getMiddleName(), patient.getLastName(), patient.getBirthdate(),
//                patient.getGender(), patient.getCivilStatus(), patient.getProfileImageBytes());
        newPatientSessionManager.setPatientInfo(Long.toString(patient.getId()), patient.getFirstName(),
                patient.getLastName(), patient.getAge(), patient.getGender(), patient.getCivilStatus(),
                patient.getBloodType(), patient.getProfileImageBytes());

    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        if(id == R.id.view_patient_back_btn) {

            newPatientSessionManager.endSession();
            doSomethingCryptFile("enc", new File(patient.getProfileImageBytes()));
//            Intent intent = new Intent(ViewPatientActivity.this, ExistingPatientActivity.class);
//            startActivity(intent);
            finish();

        } else if(id == R.id.view_patient_update_btn) {

            Intent intent = new Intent(this, UpdatePatientRecordActivity.class);
            intent.putExtra("selectedPatient", patientId);
            startActivity(intent);
            doSomethingCryptFile("enc", new File(patient.getProfileImageBytes()));
            finish();

        } else if(id == R.id.view_patient_create_case_btn) {

            Intent intent = new Intent(this, CaptureDocumentsActivity.class);
            startActivity(intent);
            finish();

        }

    }

    private String patientFullName (String firstName, String middleName, String lastName) {

        return firstName +
                " " +
                middleName +
                " " +
                lastName;
    }

    private void setPic(ImageView mImageView, String mCurrentPhotoPath) {
        // Get the dimensions of the View
        int targetW = 300;//mImageView.getWidth();
        int targetH = 250;//mImageView.getHeight();
        Log.d("width and height", targetW + targetH + "");

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);
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
        switch(dec){
            case "enc":{
                mastercry.encryptFile(input);
                Log.d("Action", "enc");
            }; break;
            case "dec":{
                mastercry.decryptFile(input);
                Log.d("Action", "dec");
            }; break;
        }
//
    }

    private byte[] read(File file) throws IOException{
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

    private File checkFile(String path, String name){
        File result;
        try {
            result = new File(path, name);
        } catch(NullPointerException ex){
            result = null;
        }
        return result;
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
