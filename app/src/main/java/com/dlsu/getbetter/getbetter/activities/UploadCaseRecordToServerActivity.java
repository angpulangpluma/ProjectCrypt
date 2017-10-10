package com.dlsu.getbetter.getbetter.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.DirectoryConstants;
import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.adapters.CaseRecordUploadAdapter;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.objects.Attachment;
import com.dlsu.getbetter.getbetter.objects.CaseRecord;
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


public class UploadCaseRecordToServerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "UPLOADCASERECORD";

    private static final String CASE_RECORD_ID_KEY = "case_record_id";
    private static final String CASE_TYPE_ID_KEY = "case_id";
    private static final String USER_ID_KEY = "user_id";
    private static final String HEALTH_CENTER_ID_KEY = "health_center_id";
    private static final String COMPLAINT_KEY = "complaint";
    private static final String CONTROL_NUMBER_KEY = "control_number";
    private static final String ADDITIONAL_NOTES_KEY = "additional_notes";
    private static final String CASE_RECORD_STATUS_ID_KEY = "record_status_id";
    private static final String UPDATED_BY_KEY = "updated_by";
    private static final String UPDATED_ON_KEY = "updated_on";

    private static final String ATTACHMENT_DESCRIPTION_KEY = "description";
    private static final String ATTACHMENT_NAME_KEY = "attachment_name";
    private static final String ATTACHMENT_TYPE_ID_KEY = "case_record_attachment_type_id";
    private static final String UPLOADED_ON_KEY = "uploaded_on";

    private static final int TIMEOUT_VALUE = 60 * 1000;

    private ArrayList<CaseRecord> caseRecordsUpload;
    private int userId;
    private int healthCenterId;
    private ProgressDialog pDialog = null;

    private DataAdapter getBetterDb;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_case_record_to_server);

        SystemSessionManager systemSessionManager = new SystemSessionManager(this);

        if(systemSessionManager.checkLogin())
            finish();

        HashMap<String, String> user = systemSessionManager.getUserDetails();
        HashMap<String, String> hc = systemSessionManager.getHealthCenter();
        healthCenterId = Integer.parseInt(hc.get(SystemSessionManager.HEALTH_CENTER_ID));

        Bundle extras = getIntent().getExtras();
        caseRecordsUpload = new ArrayList<>();

        Button backBtn = (Button)findViewById(R.id.upload_caserecord_back_btn);
        Button uploadBtn = (Button)findViewById(R.id.upload_caserecord_upload_btn);
        ListView caseRecordList = (ListView)findViewById(R.id.upload_page_case_record_list);


        uploadBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);

        initializeDatabase();
        userId = getUserId(user.get(SystemSessionManager.LOGIN_USER_NAME));
        new PopulateCaseRecordListTask().execute();

        CaseRecordUploadAdapter caseRecordUploadAdapter = new CaseRecordUploadAdapter(this,
                R.layout.case_record_item_checkbox, caseRecordsUpload);

        caseRecordList.setAdapter(caseRecordUploadAdapter);
    }

    private void initializeDatabase () {

        getBetterDb = new DataAdapter(this);

        try {
            getBetterDb.createDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getCaseRecordsUpload () {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        caseRecordsUpload.addAll(getBetterDb.getCaseRecordsUpload());
        for(int i = 0; i < caseRecordsUpload.size(); i++) {
            String patientName = getPatientName(caseRecordsUpload.get(i).getUserId());
            caseRecordsUpload.get(i).setPatientName(patientName);
            caseRecordsUpload.get(i).setAttachments(getCaseRecordAttachments(caseRecordsUpload.get(i).getCaseRecordId()));
        }

        getBetterDb.closeDatabase();

    }

    private String getPatientName (int userId) {

        String patientName;

        patientName = getBetterDb.getPatientName(userId);

        return patientName;

    }

    private int getUserId (String username) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int id = getBetterDb.getUserId(username);

        getBetterDb.closeDatabase();

        return id;

    }

    private ArrayList<Attachment> getCaseRecordAttachments(int caseRecordId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ArrayList<Attachment> caseAttachments = new ArrayList<>();
        caseAttachments.addAll(getBetterDb.getAttachments(caseRecordId));

        getBetterDb.closeDatabase();

        return caseAttachments;

    }

    private void removeCaseRecordsUpload (int caseRecordId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getBetterDb.removeCaseRecordUpload(caseRecordId);
        getBetterDb.closeDatabase();
    }

    private void updateCaseRecordId (int newId, int oldId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getBetterDb.updateCaseRecordId(newId, oldId);

        getBetterDb.closeDatabase();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.upload_caserecord_back_btn) {

            Intent intent = new Intent(this, ExistingPatientActivity.class);
            startActivity(intent);
            finish();

        } else if(id == R.id.upload_caserecord_upload_btn) {

            ArrayList<CaseRecord> selectedCaseRecordsList = new ArrayList<>();


            for (int i = 0; i < caseRecordsUpload.size(); i++) {

                CaseRecord selectedCaseRecord = caseRecordsUpload.get(i);

                if (selectedCaseRecord.isChecked()) {
//                    selectedCaseRecordsList.add(selectedCaseRecord);
                    uploadCaseRecord(selectedCaseRecord);
                }
            }

//            uploadCaseRecord(selectedCaseRecordsList);

        }
    }

    private class PopulateCaseRecordListTask extends AsyncTask<Void, Void, Void> {



        @Override
        protected void onPreExecute() {
            showProgressDialog();

        }

        @Override
        protected Void doInBackground(Void... params) {
            getCaseRecordsUpload();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dismissProgressDialog();
        }
    }

    private void uploadCaseRecord(final CaseRecord caseRecordsUpload) {

        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.setTimeout(TIMEOUT_VALUE);
        RequestParams params = new RequestParams();
        final String contentType = RequestParams.APPLICATION_OCTET_STREAM;
        List<Map<String, String>> attachments = new ArrayList<Map<String, String>>();
        ArrayList<Attachment> attachmentList = caseRecordsUpload.getAttachments();

        String caseRecordId = String.valueOf(caseRecordsUpload.getCaseRecordId());

        Map<String, String> record = new HashMap<>();
        record.put(CASE_RECORD_ID_KEY, caseRecordId);
        record.put(CASE_TYPE_ID_KEY, String.valueOf(caseRecordsUpload.getCaseId()));
        record.put(USER_ID_KEY, String.valueOf(caseRecordsUpload.getUserId()));
        record.put(HEALTH_CENTER_ID_KEY, String.valueOf(healthCenterId));
        record.put(COMPLAINT_KEY, caseRecordsUpload.getCaseRecordComplaint());
        record.put(CONTROL_NUMBER_KEY, caseRecordsUpload.getCaseRecordControlNumber());
        record.put(ADDITIONAL_NOTES_KEY, caseRecordsUpload.getCaseRecordAdditionalNotes());
//        record.put(CASE_RECORD_STATUS_ID_KEY, String.valueOf(caseRecordsUpload.getCaseRecordStatusId()));
        record.put(UPDATED_BY_KEY, String.valueOf(userId));
//        caseRecords.add(record);

        for (int i = 0; i < attachmentList.size(); i++) {

            Map<String, String> attachment = new HashMap<>();
            attachment.put(ATTACHMENT_DESCRIPTION_KEY, attachmentList.get(i).getAttachmentDescription());
            attachment.put(ATTACHMENT_TYPE_ID_KEY, String.valueOf(attachmentList.get(i).getAttachmentType()));
            attachments.add(attachment);

            String attachmentName = attachmentList.get(i).getAttachmentDescription();
            File attachmentFile = new File(attachmentList.get(i).getAttachmentPath());
            if(attachmentList.get(i).getAttachmentType() == 1) {
                attachmentName = attachmentName.concat(".jpg");
            } else if (attachmentList.get(i).getAttachmentType() == 2) {
                attachmentName = attachmentName.concat(".mp4");
            } else if (attachmentList.get(i).getAttachmentType() == 3 || attachmentList.get(i).getAttachmentType() == 5) {
                attachmentName = attachmentName.concat(".3gp");
            }

            try {
                params.put(caseRecordId + attachmentName.substring(0,1), attachmentFile, contentType, attachmentName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        params.put("case_record", record);
        params.put("attachments", attachments);
        params.setHttpEntityIsRepeatable(true);
        params.setUseJsonStreamer(false);


        asyncHttpClient.post(UploadCaseRecordToServerActivity.this,
                DirectoryConstants.TEST_URL_POST, params, new TextHttpResponseHandler() {

                    @Override
                    public void onStart() {

                        super.onStart();
                        showProgressDialog();

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.d(TAG, "onFailure: " + responseString);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {

                        Log.d(TAG, responseString);
                        featureAlertMessage("Upload Complete");
//                        removeCaseRecordsUpload(caseRecordsUpload.getCaseRecordId());
                    }

                    @Override
                    public void onFinish() {

                        super.onFinish();
                        dismissProgressDialog();


                    }

                    @Override
                    public void onProgress(long bytesWritten, long totalSize) {
                        super.onProgress(bytesWritten, totalSize);
                        pDialog.setProgress((int) bytesWritten);
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

    Runnable UpdateRecordTime = new Runnable() {
        @Override
        public void run() {


            handler.postDelayed(this, 1000);
        }
    };

    private void showProgressDialog() {
        if(pDialog == null) {
            pDialog = new ProgressDialog(UploadCaseRecordToServerActivity.this);
            pDialog.setMessage("Uploading case record");
            pDialog.setProgress(0);
            pDialog.incrementProgressBy(1);
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
