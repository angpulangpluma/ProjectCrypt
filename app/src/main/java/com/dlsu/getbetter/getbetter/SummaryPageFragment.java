package com.dlsu.getbetter.getbetter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.MediaController;

import com.dlsu.getbetter.getbetter.activities.ViewImageActivity;
import com.dlsu.getbetter.getbetter.adapters.FileAttachmentsAdapter;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.objects.Attachment;
import com.dlsu.getbetter.getbetter.objects.DividerItemDecoration;
import com.dlsu.getbetter.getbetter.objects.Patient;
import com.dlsu.getbetter.getbetter.sessionmanagers.NewPatientSessionManager;
import com.dlsu.getbetter.getbetter.sessionmanagers.SystemSessionManager;


import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class SummaryPageFragment extends Fragment implements View.OnClickListener, MediaController.MediaPlayerControl {

    private String patientName;
    private String patientAgeGender;
    private String patientProfileImage;
    private String patientFirstName;
    private String patientMiddleName;
    private String patientLastName;
    private String patientBirthdate;
    private String patientGender;
    private String patientCivilStatus;
    private String patientBloodType;
    private String chiefComplaint;
    private String controlNumber;
    private String uploadedDate;
    private String attachmentName;

    private static final int REQUEST_IMAGE_ATTACHMENT = 100;
    private static final int REQUEST_VIDEO_ATTACHMENT = 200;
    private static final int REQUEST_AUDIO_ATTACHMENT = 300;

    private static final int MEDIA_TYPE_IMAGE = 1;
    private static final int MEDIA_TYPE_VIDEO = 2;
    private static final int MEDIA_TYPE_AUDIO = 3;

    private long patientId;
    private int caseRecordId;
    private int healthCenterId;
    private int userId;

    private ArrayList<Attachment> attachments;

    private FileAttachmentsAdapter fileAdapter;
    private RecyclerView.LayoutManager fileListLayoutManager;
    private ImageView summaryProfileImage;
    private CardView recordSoundContainer;
    private Button recordSoundbBtn;
    private Button stopRecordBtn;
    private Button playSoundBtn;
    private Button doneRecordBtn;
    private Button cancelRecordBtn;

    private MediaRecorder hpiRecorder;
    private MediaPlayer nMediaPlayer;
    private MediaController nMediaController;
    private Handler nHandler = new Handler();

    private Uri fileUri;
    private String audioOutputFile;

    private DataAdapter getBetterDb;
    private NewPatientSessionManager newPatientDetails;

    public SummaryPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SystemSessionManager systemSessionManager = new SystemSessionManager(getActivity());

        if(systemSessionManager.checkLogin())
            getActivity().finish();

        HashMap<String, String> user = systemSessionManager.getUserDetails();
        HashMap<String, String> hc = systemSessionManager.getHealthCenter();
        HashMap<String, String> patientDetails = newPatientDetails.getNewPatientDetails();
        healthCenterId = Integer.parseInt(hc.get(SystemSessionManager.HEALTH_CENTER_ID));

        initializeDatabase();
        getUserId(user.get(SystemSessionManager.LOGIN_USER_NAME));
        newPatientDetails = new NewPatientSessionManager(getActivity());

        patientProfileImage = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_PROFILE_IMAGE);
        patientFirstName = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_FIRST_NAME);
        patientMiddleName = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_MIDDLE_NAME);
        patientLastName = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_LAST_NAME);
        patientBirthdate = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_BIRTHDATE);
        patientGender = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_GENDER);
        patientCivilStatus = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_CIVIL_STATUS);
        patientBloodType = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_BLOOD_TYPE);
        chiefComplaint = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_CHIEF_COMPLAINT);
        String recordedHpiOutputFile = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_DOC_HPI_RECORD);
        Patient patient = new Patient(patientFirstName, patientMiddleName, patientLastName, patientBirthdate,
                patientGender, patientCivilStatus, patientProfileImage);

        String patientInfoFormImage = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_DOC_IMAGE1);
        String familySocialHistoryFormImage = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_DOC_IMAGE2);
        String chiefComplaintFormImage = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_DOC_IMAGE3);
        String patientInfoFormImageTitle = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_DOC_IMAGE1_TITLE);
        String familySocialHistoryFormImageTitle = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_DOC_IMAGE2_TITLE);
        String chiefComplaintFormImageTitle = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_DOC_IMAGE3_TITLE);

        if(!newPatientDetails.isActivityNewPatient()) {
            HashMap<String, String> pId = newPatientDetails.getPatientInfo();
            patientId = Long.valueOf(pId.get(NewPatientSessionManager.PATIENT_ID));
            patientFirstName = pId.get(NewPatientSessionManager.NEW_PATIENT_FIRST_NAME);
            patientLastName = pId.get(NewPatientSessionManager.NEW_PATIENT_LAST_NAME);
            caseRecordId = generateCaseRecordId(patientId);
            controlNumber = generateControlNumber(patientId);
        }

        attachments = new ArrayList<>();
        uploadedDate = getTimeStamp();

        fileAdapter = new FileAttachmentsAdapter(attachments); //encrypt here
        addPhotoAttachment(patientInfoFormImage, patientInfoFormImageTitle, uploadedDate);
        addPhotoAttachment(familySocialHistoryFormImage, familySocialHistoryFormImageTitle, uploadedDate);
        addPhotoAttachment(chiefComplaintFormImage, chiefComplaintFormImageTitle, uploadedDate);
        addHPIAttachment(recordedHpiOutputFile, chiefComplaint, uploadedDate);

        fileListLayoutManager = new LinearLayoutManager(this.getContext());

        patientName = patientFirstName + " " + patientMiddleName + " " + patientLastName;
        patientAgeGender = patient.getAge() + " yrs. old, " + patientGender;

        nMediaPlayer = new MediaPlayer();
        nMediaController = new MediaController(this.getContext()) {
            @Override
            public void hide() {

            }
        };
        //Uri hpiRecordingUri = Uri.parse(recordedHpiOutputFile);

        nMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try { //encrypt here
            nMediaPlayer.setDataSource(recordedHpiOutputFile);
            nMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeDatabase () {

        getBetterDb = new DataAdapter(this.getContext());

        try {
            getBetterDb.createDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void getUserId(String username) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        userId = getBetterDb.getUserId(username);
        getBetterDb.closeDatabase();

    }

    private void addPhotoAttachment (String path, String title, String uploadedOn) {

        Attachment attachment = new Attachment(path, title, 1, uploadedOn);
            attachments.add(fileAdapter.getItemCount(), attachment);
            fileAdapter.notifyItemInserted(fileAdapter.getItemCount() - 1);
    }

    private void addVideoAttachment (String path, String title, String uploadedOn) {

        Attachment attachment = new Attachment(path, title, 2, uploadedOn);
        attachments.add(fileAdapter.getItemCount(), attachment);
        fileAdapter.notifyItemInserted(fileAdapter.getItemCount() - 1);
    }

    private void addAudioAttachment (String path, String title, String uploadedOn) {

        Attachment attachment = new Attachment(path, title, 3, uploadedOn);
        attachments.add(attachment);
    }

    private void addHPIAttachment(String path, String title, String uploadedOn) {

        Attachment attachment = new Attachment(path, title, 5, uploadedOn);
        attachments.add(attachment);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_summary_page, container, false);

        TextView summaryChiefComplaint = (TextView)rootView.findViewById(R.id.summary_page_chief_complaint);

        TextView summaryPatientName = (TextView) rootView.findViewById(R.id.summary_page_patient_name);
        TextView summaryAgeGender = (TextView) rootView.findViewById(R.id.summary_page_age_gender);
        Button summarySubmitBtn = (Button) rootView.findViewById(R.id.summary_page_submit_btn);
        Button summaryTakePicBtn = (Button)rootView.findViewById(R.id.summary_page_take_pic_btn);
        Button summaryRecordVideo = (Button)rootView.findViewById(R.id.summary_page_rec_video_btn);
        Button summaryRecordAudio = (Button)rootView.findViewById(R.id.summary_page_rec_sound_btn);
        Button summaryTakePicDocBtn = (Button)rootView.findViewById(R.id.summary_page_take_pic_doc_btn);
        Button summaryUpdatePatientRecBtn = (Button)rootView.findViewById(R.id.summary_update_patient_rec_btn);
        Button summaryEstethoscopeBtn = (Button)rootView.findViewById(R.id.summary_page_estethoscope_btn);
        Button summarySelectFileBtn = (Button)rootView.findViewById(R.id.summary_page_select_file_btn);
        RecyclerView attachmentFileList = (RecyclerView) rootView.findViewById(R.id.summary_page_files_list);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext());

        recordSoundContainer = (CardView)rootView.findViewById(R.id.summary_page_record_sound_container);
        recordSoundbBtn = (Button)rootView.findViewById(R.id.summary_page_audio_record_btn);
        stopRecordBtn = (Button)rootView.findViewById(R.id.summary_page_audio_stop_record_btn);
        playSoundBtn = (Button)rootView.findViewById(R.id.summary_page_audio_play_recorded_btn);
        doneRecordBtn = (Button)rootView.findViewById(R.id.summary_page_record_audio_done_btn);
        cancelRecordBtn = (Button)rootView.findViewById(R.id.summary_page_record_audio_cancel_btn);

        summaryProfileImage = (ImageView) rootView.findViewById(R.id.profile_picture_display);

        nMediaController.setMediaPlayer(SummaryPageFragment.this);
        nMediaController.setAnchorView(rootView.findViewById(R.id.hpi_media_player));

        summaryPatientName.setText(patientName);
        summaryAgeGender.setText(patientAgeGender);
        summaryChiefComplaint.setText(chiefComplaint);

        recordSoundContainer.setVisibility(View.GONE);
        stopRecordBtn.setEnabled(false);
        playSoundBtn.setEnabled(false);

        summarySubmitBtn.setOnClickListener(this);
        summaryTakePicBtn.setOnClickListener(this);
        summaryTakePicDocBtn.setOnClickListener(this);
        summaryRecordVideo.setOnClickListener(this);
        summaryRecordAudio.setOnClickListener(this);
        summaryUpdatePatientRecBtn.setOnClickListener(this);
        summaryEstethoscopeBtn.setOnClickListener(this);
        summarySelectFileBtn.setOnClickListener(this);

        recordSoundbBtn.setOnClickListener(this);
        stopRecordBtn.setOnClickListener(this);
        playSoundBtn.setOnClickListener(this);
        doneRecordBtn.setOnClickListener(this);
        cancelRecordBtn.setOnClickListener(this);

        attachmentFileList.setHasFixedSize(true);
        attachmentFileList.setLayoutManager(fileListLayoutManager);
        attachmentFileList.setAdapter(fileAdapter);
        attachmentFileList.addItemDecoration(dividerItemDecoration);
        fileAdapter.SetOnItemClickListener(new FileAttachmentsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getActivity(), ViewImageActivity.class);
                intent.putExtra("imageUrl", attachments.get(position).getAttachmentPath());
                intent.putExtra("imageTitle", attachments.get(position).getAttachmentDescription());
                startActivity(intent);
            }
        });

        nMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                nHandler.post(new Runnable() {
                    public void run() {
                        nMediaController.show(0);
                        nMediaPlayer.start();
                    }
                });
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        setPic(summaryProfileImage, patientProfileImage);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if(id == R.id.summary_page_submit_btn) {

            if(newPatientDetails.isActivityNewPatient()) {

                new InsertPatientTask().execute(patientFirstName, patientMiddleName, patientLastName,
                        patientBirthdate, patientGender, patientCivilStatus, patientBloodType, patientProfileImage);
            } else {

                new InsertCaseRecordTask().execute();
            }

            if(nMediaPlayer.isPlaying()) {
                nMediaPlayer.stop();
                nMediaPlayer.release();
            }

            if(nMediaController.isShowing()) {
                nMediaController.hide();
            }

            newPatientDetails.endSession();
            getActivity().finish();


        } else if (id == R.id.summary_page_take_pic_btn) {

            takePicture();

        } else if (id == R.id.summary_page_take_pic_doc_btn) {

            takePicture();

        } else if (id == R.id.summary_page_rec_video_btn) {

            recordVideo();

        } else if (id == R.id.summary_page_estethoscope_btn) {

            featureAlertMessage();

        } else if (id == R.id.summary_page_select_file_btn) {


            featureAlertMessage();

        } else if (id == R.id.summary_page_rec_sound_btn) {

            audioOutputFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/" +
                    "hpi_recording_" + getTimeStamp().substring(0,9) + ".3gp";
            hpiRecorder = new MediaRecorder();
            hpiRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            hpiRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            hpiRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            hpiRecorder.setOutputFile(audioOutputFile);
            recordSoundContainer.setVisibility(View.VISIBLE);

        } else if (id == R.id.summary_page_audio_record_btn) {

            try {
                hpiRecorder.prepare();
                hpiRecorder.start();

            } catch (IllegalStateException | IOException e) {

                e.printStackTrace();

            }

            stopRecordBtn.setEnabled(true);


        } else if (id == R.id.summary_page_audio_stop_record_btn) {

            hpiRecorder.stop();
            hpiRecorder.release();
            hpiRecorder = null;

            stopRecordBtn.setEnabled(false);
            playSoundBtn.setEnabled(true);


        } else if (id == R.id.summary_page_audio_play_recorded_btn) {

            MediaPlayer mp = new MediaPlayer();

            try {

                mp.setDataSource(audioOutputFile);
            } catch (IOException e ) {

                e.printStackTrace();

            }

            try {
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mp.start();


        } else if (id == R.id.summary_page_record_audio_done_btn) {


        } else if (id == R.id.summary_page_record_audio_cancel_btn) {


        }
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public int getBufferPercentage() {

        return (nMediaPlayer.getCurrentPosition() * 100) / nMediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return nMediaPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return nMediaPlayer.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return nMediaPlayer.isPlaying();
    }

    @Override
    public void pause() {
        if(nMediaPlayer.isPlaying())
            nMediaPlayer.pause();
    }

    @Override
    public void seekTo(int pos) {
        nMediaPlayer.seekTo(pos);
    }

    @Override
    public void start() {
        nMediaPlayer.start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_IMAGE_ATTACHMENT) {
            if(resultCode == Activity.RESULT_OK) {

                editAttachmentName(MEDIA_TYPE_IMAGE);

            } else if(resultCode == Activity.RESULT_CANCELED) {

            } else {

            }
        } else if(requestCode == REQUEST_VIDEO_ATTACHMENT) {
            if(resultCode == Activity.RESULT_OK) {

                editAttachmentName(MEDIA_TYPE_VIDEO);
//                addVideoAttachment(videoAttachmentPath, "video", uploadedDate);

            } else if (resultCode == Activity.RESULT_CANCELED) {

            } else {

            }
        } else if(requestCode == REQUEST_AUDIO_ATTACHMENT) {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(nMediaPlayer.isPlaying()) {
            nMediaPlayer.stop();
            nMediaPlayer.release();
        }

        if(nMediaController.isShowing()) {
            nMediaController.hide();
        }
    }

    private void editAttachmentName (final int type) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Image Filename");

        // Set up the input
        final EditText input = new EditText(getContext());

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                attachmentName = input.getText().toString();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                if(type == MEDIA_TYPE_IMAGE) {
                    addPhotoAttachment(fileUri.getPath(), attachmentName, uploadedDate);
                } else if(type == MEDIA_TYPE_VIDEO) {
                    addVideoAttachment(fileUri.getPath(), attachmentName, uploadedDate);
                }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private class InsertPatientTask extends AsyncTask<String, Void, Long> {

        private final ProgressDialog progressDialog = new ProgressDialog(SummaryPageFragment.this.getContext());


        @Override
        protected void onPreExecute() {
            this.progressDialog.setMessage("Inserting Patient Info...");
            this.progressDialog.show();

        }

        @Override
        protected Long doInBackground(String... params) {

            long rowId;

            try {
                getBetterDb.openDatabase();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            rowId = getBetterDb.insertPatientInfo(params[0], params[1], params[2], params[3], params[4],
                    params[5], params[6], params[7], healthCenterId);

            getBetterDb.closeDatabase();

            return rowId;
        }

        @Override
        protected void onPostExecute(Long result) {

            if (this.progressDialog.isShowing()) {
                this.progressDialog.dismiss();
            }

            Toast.makeText(SummaryPageFragment.this.getContext(), "Patient ID: " + result + " inserted.",
                    Toast.LENGTH_LONG).show();

            patientId = result;
            caseRecordId = generateCaseRecordId(result);
            Log.e("case record id", caseRecordId+"");
            controlNumber = generateControlNumber(result);
            new InsertCaseRecordTask().execute();
        }
    }

//    private class InsertNewAttachment extends AsyncTask<Void, Void, String> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//
//        }
//
//        @Override
//        protected String doInBackground(Void... params) {
//
//            editAttachmentName();
//            return attachmentName;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//
//            addPhotoAttachment(imageAttachmentPath, result, uploadedDate);
//
//        }
//    }

    private void insertCaseRecord() {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Log.e("id", caseRecordId + "");
        Log.e("patientId", patientId + "");
        Log.e("chiefComplaint", chiefComplaint);

        getBetterDb.insertCaseRecord(caseRecordId, patientId, healthCenterId, chiefComplaint,
                    controlNumber);


        getBetterDb.closeDatabase();
    }

    private void insertCaseRecordHistory() {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Log.e("history id", caseRecordId + "");
        getBetterDb.insertCaseRecordHistory(caseRecordId, userId, uploadedDate);

        getBetterDb.closeDatabase();
    }

    private void insertCaseRecordAttachments() {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < attachments.size(); i++) {

            Log.e("attachment id", caseRecordId + "");
            attachments.get(i).setCaseRecordId(caseRecordId);
            getBetterDb.insertCaseRecordAttachments(attachments.get(i));
        }

        getBetterDb.closeDatabase();
    }

    private String generateControlNumber(long pId) {

        String result;
        String firstChar = patientFirstName.substring(0, 1).toUpperCase();
        String secondChar = patientLastName.substring(0, 1).toUpperCase();
        String patientIdChar = String.valueOf(pId);

        result = firstChar + secondChar + patientIdChar + "-" + caseRecordId;
        Log.e("control number", result);

        return result;
    }

    private int generateCaseRecordId(long patientId) {

        ArrayList<Integer> storedIds;
        int caseRecordId;
        int a = 251;
        int c = 134;
        int m = 312;
        int generatedRandomId = m / 2;

        generatedRandomId = (a * generatedRandomId + c) % m;
        caseRecordId = Integer.parseInt(patientId + Integer.toString(generatedRandomId));

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        storedIds = getBetterDb.getCaseRecordIds();
        getBetterDb.closeDatabase();


        if(storedIds.isEmpty()) {
            return caseRecordId;
        } else {
            while(storedIds.contains(caseRecordId)) {
                generatedRandomId = (a * generatedRandomId + c) % m;
                caseRecordId = Integer.parseInt(Long.toString(patientId) + Integer.toString(generatedRandomId));
            }

            return caseRecordId;
        }
    }

    private class InsertCaseRecordTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog progressDialog = new ProgressDialog(SummaryPageFragment.this.getContext());

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Inserting case record...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            insertCaseRecord();
            insertCaseRecordAttachments();
            insertCaseRecordHistory();

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            if(progressDialog.isShowing()) {
                progressDialog.hide();
                progressDialog.dismiss();
            }
        }
    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null) {
            fileUri = savedInstanceState.getParcelable("file_uri");
        }
    }

    private File createMediaFile(int type) {

        String timeStamp = getTimeStamp().substring(0, 9);
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), DirectoryConstants.CASE_RECORD_ATTACHMENT_DIRECTORY_NAME);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("Debug", "Oops! Failed create "
                        + DirectoryConstants.CASE_RECORD_ATTACHMENT_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        File mediaFile;

        if(type == MEDIA_TYPE_IMAGE) {

            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        } else if (type == MEDIA_TYPE_VIDEO) {

            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");

        } else if (type == MEDIA_TYPE_AUDIO) {

            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "AUD_" + timeStamp + ".3gp");

        } else {
            return null;
        }

        return mediaFile;
    }

    private Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(createMediaFile(type));
    }

    private void takePicture() { //encrypt here

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, REQUEST_IMAGE_ATTACHMENT);
    }

    private void recordVideo() { //encrypt here

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
//        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 5491520L);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, REQUEST_VIDEO_ATTACHMENT);
    }

    private void recordSound() {


    }

    private void setPic(ImageView mImageView, String mCurrentPhotoPath) { //decrypt here
        // Get the dimensions of the View
        int targetW = 255;//mImageView.getWidth();
        int targetH = 200;// mImageView.getHeight();
        Log.e("width and height", targetW + targetH + "");

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the patientProfileImage
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the patientProfileImage file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }

    private String getTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private void featureAlertMessage() {

        AlertDialog.Builder builder = new AlertDialog.Builder(SummaryPageFragment.this.getActivity());
        builder.setTitle("Feature Not Available!");
        builder.setMessage("Sorry! This feature is still under construction. :)");

        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

}
