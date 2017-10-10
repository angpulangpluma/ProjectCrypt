package com.dlsu.getbetter.getbetter.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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

import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.adapters.ExistingPatientAdapter;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.objects.DividerItemDecoration;
import com.dlsu.getbetter.getbetter.objects.Patient;
import com.dlsu.getbetter.getbetter.sessionmanagers.SystemSessionManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;


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

        ExistingPatientAdapter existingPatientsAdapter = new ExistingPatientAdapter(existingPatients);

        existingPatientListView.setHasFixedSize(true);
        existingPatientListView.setLayoutManager(existingPatientLayoutManager);
        existingPatientListView.setAdapter(existingPatientsAdapter);
        existingPatientListView.addItemDecoration(dividerItemDecoration);
        existingPatientsAdapter.SetOnItemClickListener(new ExistingPatientAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {

                selectedPatientId = existingPatients.get(position).getId();
                Intent intent = new Intent(ExistingPatientActivity.this, ViewPatientActivity.class);
                intent.putExtra("patientId", selectedPatientId);
                startActivity(intent);
                ExistingPatientActivity.this.finish();

            }
        });
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

            Intent intent = new Intent(this, NewPatientInfoActivity.class);
            startActivity(intent);

        } else if (id == R.id.upload_patient_record) {

            if(isConnected) {
                Intent intent = new Intent(this, UploadPatientToServerActivity.class);
                startActivity(intent);
                finish();
            } else {
                featureAlertMessage("No Internet connection detected. Please make sure you are connected to the internet.");
            }


        } else if (id == R.id.upload_case_record) {

            if(isConnected) {
                Intent intent = new Intent(this, UploadCaseRecordToServerActivity.class);
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


}
