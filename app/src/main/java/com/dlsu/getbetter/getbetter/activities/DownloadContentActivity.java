package com.dlsu.getbetter.getbetter.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.dlsu.getbetter.getbetter.DirectoryConstants;
import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.RequestHandler;
import com.dlsu.getbetter.getbetter.adapters.CaseRecordDownloadAdapter;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.objects.Attachment;
import com.dlsu.getbetter.getbetter.objects.CaseRecord;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class DownloadContentActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG_CASE_RECORD = "case_records";
    private static final String TAG_CASE_ATTACHMENTS = "case_attachments";
    private static final String TAG_CASE_RECORD_ID = "case_record_id";
    private static final String TAG_USER_ID = "user_id";
    private static final String TAG_COMPLAINT = "complaint";
    private static final String TAG_ADDITIONAL_NOTES = "additional_notes";
    private static final String TAG_HEALTH_CENTER_ID = "health_center_id";
    private static final String TAG_RECORD_STATUS_ID = "record_status_id";
    private static final String TAG_UPDATED_ON = "updated_on";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_FILE_PATH = "file_path";
    private static final String TAG_CASE_ATTACHMENT_TYPE = "case_attachment_type";
    private static final String TAG_UPLOADED_ON = "uploaded_on";
    private static final String TAG_UPLOADED_BY = "uploaded_by";

    private String myJSON;
    private String myJSONAttachments;
    private ArrayList<CaseRecord> caseRecordsData;
    private ArrayList<Long> patientIds;

    private DataAdapter getBetterDb;
    private ProgressDialog dDialog = null;
    private ListView caseRecordList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_content);

        Button backBtn = (Button)findViewById(R.id.download_back_btn);
        Button downloadBtn = (Button)findViewById(R.id.download_selected_btn);
        caseRecordList = (ListView)findViewById(R.id.download_page_case_record_list);

        backBtn.setOnClickListener(this);
        downloadBtn.setOnClickListener(this);


        caseRecordsData = new ArrayList<>();

        ArrayList<Attachment> caseRecordAttachments = new ArrayList<>();

        initializeDatabase();
        getPatientIds();
        getDownloadList();
//        getDownloadableData();
    }

    private void initializeDatabase () {

        getBetterDb = new DataAdapter(this);

        try {
            getBetterDb.createDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.download_back_btn) {
            finish();
        } else if (id == R.id.download_selected_btn) {

            ArrayList<CaseRecord> selectedCaseRecordList = new ArrayList<>();

            for(int i = 0; i < caseRecordsData.size(); i++) {

                CaseRecord selectedCaseRecord = caseRecordsData.get(i);

                if(selectedCaseRecord.isChecked()) {
                    selectedCaseRecordList.add(selectedCaseRecord);
                }
            }

            updateCaseRecord(selectedCaseRecordList);
//            updateCaseRecordAdditionalNotes(selectedCaseRecordList);
//            updateLocalCaseRecordHistory(selectedCaseRecordList);
//            downloadSelectedData(selectedCaseRecordList);

        }
    }

    private void getPatientIds() {

        try {
            getBetterDb.openDatabase();
        }catch (SQLException e) {
            e.printStackTrace();
        }

        patientIds = getBetterDb.getPatientIds();
        getBetterDb.closeDatabase();

    }

    private String getHealthCenterName(int healthCenterId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String result;
        result = getBetterDb.getHealthCenterString(healthCenterId);

        getBetterDb.closeDatabase();

        return result;
    }

    private void getDownloadList() {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("patientIds", patientIds);
        client.get(DirectoryConstants.DOWNLOAD_CASE_RECORD_LIST_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                myJSON = responseString;
                populateCaseRecordsList();
            }
        });
    }

    private void populateCaseRecordsList() {

        try {
            JSONObject jsonObject = new JSONObject(myJSON);
            JSONArray caseRecords = jsonObject.getJSONArray(TAG_CASE_RECORD);

            for(int i = 0; i < caseRecords.length(); i++) {
                JSONObject c = caseRecords.getJSONObject(i);
                int caseRecordId = Integer.parseInt(c.getString(TAG_CASE_RECORD_ID));
                String patientName = getUserName(Integer.parseInt(c.getString(TAG_USER_ID)));
                String complaint = c.getString(TAG_COMPLAINT);
                String additionalNotes = c.getString(TAG_ADDITIONAL_NOTES);
                String healthCenter = getHealthCenterName(Integer.parseInt(c.getString(TAG_HEALTH_CENTER_ID)));
                String recordStatus = getCaseRecordStatusString(Integer.parseInt(c.getString(TAG_RECORD_STATUS_ID)));
                String updatedOn = c.getString(TAG_UPDATED_ON);

                CaseRecord caseRecord = new CaseRecord(caseRecordId, patientName, complaint, additionalNotes,
                        healthCenter, recordStatus, updatedOn);
                caseRecord.setCaseRecordStatusId(Integer.parseInt(c.getString(TAG_RECORD_STATUS_ID)));
                caseRecordsData.add(caseRecord);

                Log.e("case records data", caseRecordsData.size() + "");
            }

            CaseRecordDownloadAdapter caseRecordDownloadAdapter = new CaseRecordDownloadAdapter(this, R.layout.case_record_item_checkbox, caseRecordsData);
            caseRecordList.setAdapter(caseRecordDownloadAdapter);

        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateCaseRecord(ArrayList<CaseRecord> caseRecords) {


        class UpdateCaseRecords extends AsyncTask<ArrayList<CaseRecord>, Integer, Integer> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgressDialog("Updating Case Record");
            }

            @Override
            protected Integer doInBackground(ArrayList<CaseRecord>... arrayLists) {
                ArrayList<CaseRecord> params = arrayLists[0];

                try {
                    getBetterDb.openDatabase();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                int totalRecords = params.size();

                for (int i = 0; i < params.size(); i++) {
                    getBetterDb.updateCaseRecordAdditionalNotes(params.get(i).getCaseRecordId(),
                            params.get(i).getCaseRecordAdditionalNotes());
                    getBetterDb.updateLocalCaseRecordHistory(params.get(i));
                    publishProgress((i/totalRecords) * 100);
                    if (isCancelled()) break;
                }

                getBetterDb.closeDatabase();

                return totalRecords;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values[0]);
            }

            @Override
            protected void onPostExecute(Integer s) {
                super.onPostExecute(s);
                dismissProgressDialog();
                featureAlertMessage("Updated " + s + " case records.");
            }
        }

        new UpdateCaseRecords().execute(caseRecords);
    }

    private void downloadSelectedData(ArrayList<CaseRecord> caseRecords) { //decrypt here

        final ArrayList<String> fileUrl = new ArrayList<>();

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("caseRecords", caseRecords);
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                DirectoryConstants.CASE_RECORD_ATTACHMENT_DIRECTORY_NAME);
        client.post(DirectoryConstants.DOWNLOAD_CASE_RECORD_NEW_ATTACHMENTS_SERVER_SCRIPT_URL, params, new TextHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                fileUrl.add(responseString);
            }
        });


        for (int i = 0; i < fileUrl.size(); i++) {

            AsyncHttpClient fileClient = new AsyncHttpClient();
            fileClient.get(fileUrl.get(i), new FileAsyncHttpResponseHandler(this) {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, File file) {
                    FileInputStream inputStream = null;
                    FileOutputStream outputStream = null;


                    try {
                        inputStream = new FileInputStream(file);
                        outputStream = openFileOutput(file.getName(), Context.MODE_PRIVATE);
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                    }catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }




//        class DownloadSelectedData extends AsyncTask<ArrayList<CaseRecord>, Void, String> {
//
//            RequestHandler rh = new RequestHandler();
//
//            @Override
//            protected void onPreExecute() {
//                showDownloadingDialog("Downloading data...");
//            }
//
//            @SafeVarargs
//            @Override
//            protected final String doInBackground(ArrayList<CaseRecord>... params) {
//
//                ArrayList<CaseRecord> selectedCaseRecords = params[0];
//                String result = null;
////                File mediaStorageDir = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "sample_audio");
////                File profileImageFile = new File (mediaStorageDir.getPath() + File.pathSeparator + "audio2.mp3");
//
//                for(int i = 0; i < selectedCaseRecords.size(); i++) {
//
//                    HashMap<String, String> data = new HashMap<>();
//                    data.put(TAG_CASE_RECORD_ID, String.valueOf(selectedCaseRecords.get(i).getCaseRecordId()));
////                    rh.getAudioFile(profileImageFile.getPath());
//                    result = rh.sendPostRequest(DirectoryConstants.DOWNLOAD_CASE_RECORD_NEW_ATTACHMENTS_SERVER_SCRIPT_URL, data);
//                }
//
//                return result;
//            }
//
//            @Override
//            protected void onPostExecute(String s) {
//                dismissProgressDialog();
//
////                Log.e("s", s);
//
////                StringBuilder message = new StringBuilder(s);
//
//                if(!s.isEmpty() && s != null) {
//                    myJSONAttachments = s;
//                    insertNewAttachmentsToLocalDB();
////                    getAudioFile();
////                    featureAlertMessage("Download Complete!");
//                } else {
//                    featureAlertMessage("Download Failed.");
//                }
//            }
//        }
//
//        DownloadSelectedData downloadSelectedData = new DownloadSelectedData();
//        downloadSelectedData.execute(caseRecords);

    }

    private void insertNewAttachmentsToLocalDB() {

        String path = "";
        File fileName = null;
        ArrayList<Attachment> attachmentPaths = new ArrayList<>();
        ArrayList<Attachment> attachmentData = new ArrayList<>();

        try{
            JSONObject jsonObject = new JSONObject(myJSONAttachments);
            JSONArray caseAttachments = jsonObject.getJSONArray(TAG_CASE_ATTACHMENTS);
            Log.d("CASE ATTACHMENT LENGTH", caseAttachments.length() + "");

            for(int i = 0; i < caseAttachments.length(); i++) {
                JSONObject o = caseAttachments.getJSONObject(i);
                int caseRecordId = Integer.parseInt(o.getString(TAG_CASE_RECORD_ID));
                String description = o.getString(TAG_DESCRIPTION);
                String filePath = o.getString(TAG_FILE_PATH);
//                String encodedImage = o.getString(TAG_ENCODED_IMAGE);
                int attachmentTypeId = Integer.parseInt(o.getString(TAG_CASE_ATTACHMENT_TYPE));
                String uploadedOn = o.getString(TAG_UPLOADED_ON);
                int uploadedBy = Integer.parseInt(o.getString(TAG_UPLOADED_BY));

//                Log.d("encoded image", encodedImage);

//                writeImageToDirectory(encodedImage, fileName);
//                if(attachmentTypeId == 1) {
//
//                    fileName = createImageFile(uploadedOn);
//                    path = Uri.fromFile(fileName).getPath();
//
//                } else if (attachmentTypeId == 3) {
//
//                    fileName = createAudioFile(uploadedOn);
//                    path = Uri.fromFile(fileName).getPath();
//                }

                Log.d("DOWNLOADCONTENTACTIVITY", filePath);

                fileName = createAttachmentFile(description, uploadedOn, attachmentTypeId);
                path = Uri.fromFile(fileName).getPath();

                Attachment caseAttachment = new Attachment(caseRecordId, path, description,
                        attachmentTypeId, uploadedOn, uploadedBy);

                attachmentData.add(caseAttachment);

//                insertCaseAttachment(caseAttachment);

                Attachment fileAttachment = new Attachment(filePath, fileName);
                attachmentPaths.add(fileAttachment);

            }

        }catch (JSONException e) {
            e.printStackTrace();
            featureAlertMessage("Download Failed!");
            Log.d("DownloadContentActivity", e.getMessage());
        }

        writeFileToDirectory(attachmentPaths, attachmentData);

    }




    private String getCaseRecordStatusString(int caseRecordStatusId) {

        String result = null;
        Resources res = getResources();
        String[] caseRecordStatuses = res.getStringArray(R.array.record_status);

        switch (caseRecordStatusId) {

            case 1: result = caseRecordStatuses[0];
                break;

            case 2: result = caseRecordStatuses[1];
                break;

            case 3: result = caseRecordStatuses[2];
                break;

            case 4: result = caseRecordStatuses[3];
                break;

            case 5: result = caseRecordStatuses[4];
                break;

            case 6: result = caseRecordStatuses[5];
                break;

            case 7: result = caseRecordStatuses[6];
                break;

        }

        return result;
    }

    private String getUserName(int userId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String result;

        try {
            result = getBetterDb.getPatientName(userId);
        } catch (NullPointerException e) {
            e.printStackTrace();
            result = "New Patient";
        }


        getBetterDb.closeDatabase();

        return result;
    }

    public String getDoctorName (int updatedBy) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String result;

        result = getBetterDb.getUserName(updatedBy);

        getBetterDb.closeDatabase();

        return result;
    }

    private void updateCaseRecordAdditionalNotes(ArrayList<CaseRecord> selectedCaseRecords) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < selectedCaseRecords.size(); i++)
            getBetterDb.updateCaseRecordAdditionalNotes(selectedCaseRecords.get(i).getCaseRecordId(),
                    selectedCaseRecords.get(i).getCaseRecordAdditionalNotes());

        getBetterDb.closeDatabase();
    }

    private void updateLocalCaseRecordHistory(ArrayList<CaseRecord> caseRecords) {

        class UpdateLocalCaseRecordHistory extends AsyncTask<ArrayList<CaseRecord>, Void, String> {

            @SafeVarargs
            @Override
            protected final String doInBackground(ArrayList<CaseRecord>... params) {

                ArrayList<CaseRecord> data = params[0];
                try {
                    getBetterDb.openDatabase();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                for(int i = 0; i < data.size(); i++) {

                    getBetterDb.updateLocalCaseRecordHistory(data.get(i));
                }

                getBetterDb.closeDatabase();

                return null;
            }

            @Override
            protected void onPostExecute(String s) {

            }
        }

        UpdateLocalCaseRecordHistory updateLocalCaseRecordHistory = new UpdateLocalCaseRecordHistory();
        updateLocalCaseRecordHistory.execute(caseRecords);

    }

    private void insertCaseAttachment(Attachment attachment) {

        try{
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getBetterDb.insertCaseRecordAttachments(attachment);

        getBetterDb.closeDatabase();
    }

//    private File createAudioFile(String uploaded_on) {
//
//        File mediaStorageDir = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
//                DirectoryConstants.CASE_RECORD_ATTACHMENT_DIRECTORY_NAME);
//
//        if (!mediaStorageDir.exists()) {
//            if (!mediaStorageDir.mkdirs()) {
//                Log.d("Debug", "Oops! Failed create "
//                        + DirectoryConstants.CASE_RECORD_ATTACHMENT_DIRECTORY_NAME + " directory");
//                return null;
//            }
//        }
//
//        File audioFile = new File (mediaStorageDir.getPath() + File.pathSeparator + "AUDIO_" + uploaded_on + ".3gp");
//
//        return audioFile;
//    }

    private File createAttachmentFile(String description, String uploaded_on, int attachmentType) {

        File attachmentFile = null;

        File mediaStorageDir = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                DirectoryConstants.CASE_RECORD_ATTACHMENT_DIRECTORY_NAME);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("Debug", "Oops! Failed create "
                        + DirectoryConstants.CASE_RECORD_ATTACHMENT_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        if(attachmentType == 1) {

            attachmentFile = new File(mediaStorageDir.getPath() + File.pathSeparator +
                    description + "_" + uploaded_on + ".jpg");

        } else if (attachmentType == 3 || attachmentType == 5) {

            attachmentFile = new File(mediaStorageDir.getPath() + File.pathSeparator +
                    description + "_" + uploaded_on + ".3gp");

        } else if (attachmentType == 2) {

            attachmentFile = new File(mediaStorageDir.getPath() + File.pathSeparator +
                    description + "_" + uploaded_on + ".mp4");
        }

        return attachmentFile;
    }

//    private File createImageFile(String uploaded_on) {
//
//        File mediaStorageDir = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
//                DirectoryConstants.CASE_RECORD_ATTACHMENT_DIRECTORY_NAME);
//
//        if (!mediaStorageDir.exists()) {
//            if (!mediaStorageDir.mkdirs()) {
//                Log.d("Debug", "Oops! Failed create "
//                        + DirectoryConstants.CASE_RECORD_ATTACHMENT_DIRECTORY_NAME + " directory");
//                return null;
//            }
//        }
//
//        File profileImageFile = new File (mediaStorageDir.getPath() + File.pathSeparator + "IMG_" + uploaded_on + ".jpg");
//
//
//        return profileImageFile;
//    }

    private void writeFileToDirectory (ArrayList<Attachment> attachmentFile, ArrayList<Attachment> attachmentData) {


        class TransferFiletoLocal extends AsyncTask<String, Integer, Integer> {

            int count, bytesAvailable;
            int maxBufferSize = 1024 * 1024;

            public TransferFiletoLocal(int count) {
                this.count = count;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgressDialog("Downloading Attachments...");

            }

            @Override
            protected void onPostExecute(Integer s) {
                super.onPostExecute(s);

                dismissProgressDialog();

                if(s == -1) {
                    featureAlertMessage("Download Failed");
                } else if (s == 0) {
//                    insertCaseAttachment(attachmentData.get(count));
                    featureAlertMessage("Successfully Downloaded Attachments!");
                }
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                dDialog.setProgress(values[0]);
            }

            @Override
            protected Integer doInBackground(String... params) {

                InputStream in = null;
                OutputStream out = null;
                HttpURLConnection conn = null;

                try {

                    URL url = new URL(params[0]);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.connect();

                    if(conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        Log.d("Connection Status", "Server returned HTTP " + conn.getResponseCode()
                                + " " + conn.getResponseMessage());
                        return -1;
                    }

                    in = conn.getInputStream();

                    out = new FileOutputStream(new File(params[1]));

//                    bytesAvailable = in.available();

                    bytesAvailable = conn.getContentLength();

                    int bufferSize = Math.min(bytesAvailable, maxBufferSize);

                    byte data[] = new byte[bufferSize];
//                    byte data[] = new byte[2048 * 2 * 2];
                    long total = 0;
                    int count;

                    while ((count = in.read(data)) != -1) {

                        total += count;

                        if(bytesAvailable > 0) {
                            publishProgress((int) total * 100 / bytesAvailable);
                        }

                        out.write(data, 0, count);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return -1;
                } finally {

                    try {
                        if(out != null) {
                            out.close();
                        }

                        if(in != null) {
                            in.close();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if(conn != null) {
                        conn.disconnect();
                    }
                }

                return 0;
            }
        }

        for(int i = 0; i < attachmentFile.size(); i++) {

            String filePath = Uri.fromFile(attachmentFile.get(i).getFileName()).getPath();
            TransferFiletoLocal transferFiletoLocal = new TransferFiletoLocal(i);
            transferFiletoLocal.execute(attachmentFile.get(i).getAttachmentPath(), filePath);
            insertCaseAttachment(attachmentData.get(i));
        }
    }

    private void writeImageToDirectory(String encodedImage, File file) {

        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(file);
            byte[] decodedImage = Base64.decode(encodedImage, Base64.DEFAULT);
            fos.write(decodedImage);

            fos.flush();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void featureAlertMessage(String result) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Download Status");
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

    private void showDownloadingDialog(String message) {
        if(dDialog == null) {

            dDialog = new ProgressDialog(DownloadContentActivity.this);
            dDialog.setMessage(message);
            dDialog.setIndeterminate(true);
            dDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        }
        dDialog.show();
    }

    private void showProgressDialog(String message) {

        if(dDialog == null) {

            dDialog = new ProgressDialog(DownloadContentActivity.this);
            dDialog.setTitle("Download Status");
            dDialog.setMessage(message);
            dDialog.setIndeterminate(false);
            dDialog.setMax(100);
            dDialog.setProgress(0);
            dDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        }
        dDialog.show();
    }

    private void dismissProgressDialog() {

        if(dDialog != null && dDialog.isShowing()) {
            dDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }


}
