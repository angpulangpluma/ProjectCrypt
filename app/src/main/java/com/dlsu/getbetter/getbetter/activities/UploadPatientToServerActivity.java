package com.dlsu.getbetter.getbetter.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.dlsu.getbetter.getbetter.DirectoryConstants;
import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.adapters.PatientUploadAdapter;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.objects.Patient;
import com.dlsu.getbetter.getbetter.sessionmanagers.SystemSessionManager;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;

public class UploadPatientToServerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "UploadPatientActivity";

    private static final String ID_KEY = "user_id";
    private static final String FIRST_NAME_KEY = "first_name";
    private static final String MIDDLE_NAME_KEY = "middle_name";
    private static final String LAST_NAME_KEY = "last_name";
    private static final String BIRTHDATE_KEY = "birthdate";
    private static final String GENDER_ID_KEY = "gender_id";
    private static final String CIVIL_STATUS_KEY = "civil_status_id";
    private static final String IMAGE_NAME_KEY = "image_name";
    private static final String HEALTH_CENTER_KEY = "default_health_center";
    private static final String BLOOD_TYPE_KEY = "blood_type";
    private static final String PROFILE_URL_KEY = "profile_url";

    private static final int TIMEOUT_VALUE = 60 * 1000;

    private ArrayList<Patient> patientsUpload;
    private DataAdapter getBetterDb;
    private int healthCenterId;
    private ProgressDialog pDialog = null;
    private long newUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_to_server);

        SystemSessionManager systemSessionManager = new SystemSessionManager(this);
        if(systemSessionManager.checkLogin())
            finish();

        HashMap<String, String> user = systemSessionManager.getUserDetails();
        HashMap<String, String> hc = systemSessionManager.getHealthCenter();
        healthCenterId = Integer.parseInt(hc.get(SystemSessionManager.HEALTH_CENTER_ID));

        patientsUpload = new ArrayList<>();
        ListView patientList = (ListView)findViewById(R.id.upload_page_patient_list);
        Button uploadBtn = (Button)findViewById(R.id.upload_patient_upload_btn);
        Button backBtn = (Button)findViewById(R.id.upload_patient_back_btn);

        initializeDatabase();
        new GetPatientListTask().execute();

        PatientUploadAdapter patientUploadAdapter = new PatientUploadAdapter(this, R.layout.patient_list_item_checkbox, patientsUpload);
        patientList.setAdapter(patientUploadAdapter);

        uploadBtn.setOnClickListener(this);
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

    private void getPatientListUpload (int healthCenterId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        patientsUpload.addAll(getBetterDb.getPatientsUpload(healthCenterId));
        Log.e("patient list size", patientsUpload.size() + "");

        getBetterDb.closeDatabase();

    }

    private int getGenderId(String genderName) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int result = getBetterDb.getGenderId(genderName);

        getBetterDb.closeDatabase();

        return result;
    }

    private int getCivilStatusId(String civilStatusName) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int result = getBetterDb.getCivilStatusId(civilStatusName);

        getBetterDb.closeDatabase();

        return result;
    }

    private void removePatientUpload (long userId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getBetterDb.removePatientUpload(userId);
        getBetterDb.closeDatabase();

    }

    private void updateUserId (long newId, long oldId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "updateUserId: " + newId);
        getBetterDb.updateUserId(newId, oldId);

        getBetterDb.closeDatabase();

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.upload_patient_upload_btn) {

//            ArrayList<Patient> selectedPatientsList = new ArrayList<>();

            for(int i = 0; i < patientsUpload.size(); i++) {
                Patient selectedPatient = patientsUpload.get(i);

                if(selectedPatient.isChecked()) {
//                    selectedPatientsList.add(selectedPatients);
                    uploadPatient(selectedPatient);
                }
            }
//            uploadPatient(selectedPatientsList);

//            for(int i = 0; i < selectedPatientsList.size(); i++) {
//                getStringImage(selectedPatientsList.get(i).getProfileImageBytes());
//                uploadPatient(selectedPatientsList.get(i));
//            }



        } else if (id == R.id.upload_patient_back_btn) {

            Intent intent = new Intent(this, ExistingPatientActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private class GetPatientListTask extends AsyncTask<Void, Void, Void> {

//        ProgressDialog progressDialog = null;
//        Context context;
//
//        public GetPatientListTask(AppCompatActivity activity) {
//            context = activity;
//            progressDialog = new ProgressDialog(context);
//        }

        @Override
        protected void onPreExecute() {
//            progressDialog.setMessage("Populating patient list");
//            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//            progressDialog.setIndeterminate(true);
//            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            getPatientListUpload(healthCenterId);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

//            if(progressDialog.isShowing()) {
//                progressDialog.dismiss();
//            }

        }
    }

    private void uploadPatient(final Patient patientUpload) {

        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.setTimeout(TIMEOUT_VALUE);
        RequestParams params = new RequestParams();
        final String contentType = RequestParams.APPLICATION_OCTET_STREAM;
        params.setForceMultipartEntityContentType(true);
//        List<Map<String, String>> patients = new ArrayList<Map<String, String>>();

//        Map<String, String> patient = new HashMap<>();

        String imageFileName = patientUpload.getFirstName().toLowerCase() + "_" +
                patientUpload.getLastName().toLowerCase() + ".jpg";
        String patientId = String.valueOf(patientUpload.getId());

        params.put(ID_KEY, patientId);
        params.put(FIRST_NAME_KEY, patientUpload.getFirstName());
        params.put(MIDDLE_NAME_KEY, patientUpload.getMiddleName());
        params.put(LAST_NAME_KEY, patientUpload.getLastName());
        params.put(BIRTHDATE_KEY, patientUpload.getBirthdate());
        params.put(GENDER_ID_KEY, getGenderId(patientUpload.getGender()));
        params.put(CIVIL_STATUS_KEY, getCivilStatusId(patientUpload.getCivilStatus()));
        params.put(BLOOD_TYPE_KEY, patientUpload.getBloodType());
        params.put(HEALTH_CENTER_KEY, healthCenterId);

//        patient.put(ID_KEY, patientId);
//        patient.put(FIRST_NAME_KEY, patientUpload.getFirstName());
//        patient.put(MIDDLE_NAME_KEY, patientUpload.getMiddleName());
//        patient.put(LAST_NAME_KEY, patientUpload.getLastName());
//        patient.put(BIRTHDATE_KEY, patientUpload.getBirthdate());
//        patient.put(GENDER_ID_KEY, String.valueOf(getGenderId(patientUpload.getGender())));
//        patient.put(CIVIL_STATUS_KEY, String.valueOf(getCivilStatusId(patientUpload.getCivilStatus())));
//        patient.put(IMAGE_NAME_KEY, imageFileName);
//        patient.put(HEALTH_CENTER_KEY, String.valueOf(healthCenterId));
//        patients.add(patient);

        File profileImage = new File(patientUpload.getProfileImageBytes());

        try {
            params.put(PROFILE_URL_KEY, profileImage, contentType, imageFileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

//        for(int i = 0; i < patientsUpload.size(); i++) {
//
//
//        }

//        params.put("patients", patient);
        params.setHttpEntityIsRepeatable(true);
        params.setUseJsonStreamer(false);


        asyncHttpClient.post(this, DirectoryConstants.UPLOAD_PATIENT_SERVER_SCRIPT_URL , params, new TextHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                showProgressDialog();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseBody) {

                featureAlertMessage("Upload Success");
                Log.d(TAG, responseBody);

                newUserId = Long.parseLong(responseBody);
                updateUserId(newUserId, patientUpload.getId());
                removePatientUpload(patientUpload.getId());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {

                featureAlertMessage("Upload Failed");
                Log.d(TAG, "onFailure: " + responseBody);
                Log.d(TAG, "onFailure: " + statusCode);

            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                pDialog.setProgress((int)bytesWritten);


            }

            @Override
            public void onFinish() {
                super.onFinish();
                dismissProgressDialog();
            }
        });
    }

    private void featureAlertMessage(String result) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("STATUS");
        builder.setMessage(result);

        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        builder.show();
    }

    private void showProgressDialog() {
        if(pDialog == null) {
            pDialog = new ProgressDialog(UploadPatientToServerActivity.this);
            pDialog.setMessage("Uploading patient");
            pDialog.setProgress(0);
            pDialog.setMax(100);
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }
        pDialog.show();
    }

    private void dismissProgressDialog() {

        if(pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }
}
