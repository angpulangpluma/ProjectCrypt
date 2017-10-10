package com.dlsu.getbetter.getbetter.activities;

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
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.DirectoryConstants;
import com.dlsu.getbetter.getbetter.R;
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

import de.hdodenhof.circleimageview.CircleImageView;

// TODO: 06/12/2016 record audio function

public class SummaryActivity extends AppCompatActivity implements View.OnClickListener, MediaController.MediaPlayerControl {

    private static final String TAG = "SummaryActivity";

    private TextView patientNameText;
    private TextView healthCenter;
    private TextView ageGender;
    private TextView chiefComplaintText;
    private CircleImageView profilePicture;
    private RecyclerView attachmentLists;
    private Button submitButton;
    private Button backButton;
    private Button takePicture;
    private Button takeVideo;
    private Button openRecordAudio;
    private Button takePictureDocument;
    private Button recordAudio;
    private Button stopRecord;
    private Button playRecordedAudio;
    private Button cancelRecording;
    private Button saveRecording;
    private CardView recordAudioContainer;
    private TextView secondsView;
    private TextView minutesView;

    private NewPatientSessionManager newPatientDetails;
    private MediaRecorder hpiRecorder;
    private MediaPlayer nMediaPlayer;
    private MediaPlayer mp;
    private MediaController nMediaController;
    private Handler nHandler = new Handler();
    private Uri fileUri;
    private String audioOutputFile;

    private DataAdapter getBetterDb;
    private FileAttachmentsAdapter fileAdapter;
    private long patientId;
    private int caseRecordId;
    private int healthCenterId;
    private int userId;
    private String patientProfileImage;
    private String patientFirstName;
    private String patientLastName;
    private String patientAge;
    private String patientGender;
    private String chiefComplaint;
    private String controlNumber;
    private String healthCenterName;
    private ArrayList<Attachment> attachments;
    private String attachmentName;
    private String recordedHpiOutputFile;
    private String patientInfoFormImage;
    private String familySocialHistoryFormImage;
    private String chiefComplaintFormImage;
    private String patientInfoFormImageTitle;
    private String familySocialHistoryFormImageTitle;
    private String chiefComplaintFormImageTitle;

    private static final int REQUEST_IMAGE_ATTACHMENT = 100;
    private static final int REQUEST_VIDEO_ATTACHMENT = 200;

    private static final int MEDIA_TYPE_IMAGE = 1;
    private static final int MEDIA_TYPE_VIDEO = 2;
    private static final int MEDIA_TYPE_AUDIO = 3;

    private Handler handler;
    private boolean isRecording;
    private int seconds, minutes, recordTime;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        SystemSessionManager systemSessionManager = new SystemSessionManager(this);
        if(systemSessionManager.checkLogin())
            finish();


        newPatientDetails = new NewPatientSessionManager(this);
        HashMap<String, String> user = systemSessionManager.getUserDetails();
        HashMap<String, String> hc = systemSessionManager.getHealthCenter();
        healthCenterId = Integer.parseInt(hc.get(SystemSessionManager.HEALTH_CENTER_ID));

        initializeDatabase();
        getUserId(user.get(SystemSessionManager.LOGIN_USER_NAME));
        getHealthCenterName(healthCenterId);
        initializePatientInfo();
        bindViews(this);
        bindListeners(this);

        Log.d(TAG, "onCreate: " + recordedHpiOutputFile);

        attachments = new ArrayList<>();
        fileAdapter = new FileAttachmentsAdapter(attachments);
        addPhotoAttachment(patientInfoFormImage, patientInfoFormImageTitle, getDateStamp());
        addPhotoAttachment(familySocialHistoryFormImage, familySocialHistoryFormImageTitle, getDateStamp());
        addPhotoAttachment(chiefComplaintFormImage, chiefComplaintFormImageTitle, getDateStamp());
        addHPIAttachment(recordedHpiOutputFile, chiefComplaint, getDateStamp());
        initializeAttachmentList(this);
        initializeMediaPlayer(this);

        caseRecordId = generateCaseRecordId();
        controlNumber = generateControlNumber(patientId);

    }

    private void bindViews(SummaryActivity activity) {

        activity.patientNameText = (TextView)activity.findViewById(R.id.summary_page_patient_name);
        activity.healthCenter = (TextView)activity.findViewById(R.id.summary_page_health_center);
        activity.ageGender = (TextView)activity.findViewById(R.id.summary_page_age_gender);
        activity.chiefComplaintText = (TextView)activity.findViewById(R.id.summary_page_chief_complaint);
        activity.profilePicture = (CircleImageView)activity.findViewById(R.id.profile_picture_display);
        activity.submitButton = (Button)activity.findViewById(R.id.summary_page_submit_btn);
        activity.backButton = (Button)activity.findViewById(R.id.summary_page_back_btn);
        activity.takePicture = (Button)activity.findViewById(R.id.summary_page_take_pic_btn);
        activity.takeVideo = (Button)activity.findViewById(R.id.summary_page_rec_video_btn);
        activity.openRecordAudio = (Button)activity.findViewById(R.id.summary_page_rec_sound_btn);
        activity.takePictureDocument = (Button)activity.findViewById(R.id.summary_page_take_pic_doc_btn);
        activity.recordAudio = (Button)activity.findViewById(R.id.summary_page_audio_record_btn);
        activity.stopRecord = (Button)activity.findViewById(R.id.summary_page_audio_stop_record_btn);
        activity.playRecordedAudio = (Button)activity.findViewById(R.id.summary_page_audio_play_recorded_btn);
        activity.recordAudioContainer = (CardView)activity.findViewById(R.id.summary_page_record_sound_container);
        activity.cancelRecording = (Button)activity.findViewById(R.id.summary_page_record_audio_cancel_btn);
        activity.saveRecording = (Button)activity.findViewById(R.id.summary_page_record_audio_done_btn);

        activity.healthCenter.setText(healthCenterName);
        setPic(profilePicture, patientProfileImage);
        activity.patientNameText.setText(patientFullName(patientFirstName, patientLastName));
        String patientAgeGender = patientAge + ", " + patientGender;
        activity.ageGender.setText(patientAgeGender);
        activity.chiefComplaintText.setText(chiefComplaint);
        activity.stopRecord.setEnabled(false);
        activity.playRecordedAudio.setEnabled(false);
        activity.saveRecording.setEnabled(false);

    }

    private void bindListeners(SummaryActivity activity) {

        submitButton.setOnClickListener(activity);
        backButton.setOnClickListener(activity);
        takePicture.setOnClickListener(activity);
        takeVideo.setOnClickListener(activity);
        openRecordAudio.setOnClickListener(activity);
        takePictureDocument.setOnClickListener(activity);
        recordAudio.setOnClickListener(activity);
        stopRecord.setOnClickListener(activity);
        playRecordedAudio.setOnClickListener(activity);
        cancelRecording.setOnClickListener(activity);
        saveRecording.setOnClickListener(activity);

    }

    private void initializeAttachmentList(SummaryActivity activity) {

        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(activity);
        RecyclerView.LayoutManager fileListLayoutManager = new LinearLayoutManager(activity);
        activity.attachmentLists = (RecyclerView)activity.findViewById(R.id.summary_page_files_list);
        activity.attachmentLists.setHasFixedSize(true);
        activity.attachmentLists.setAdapter(fileAdapter);
        activity.attachmentLists.setLayoutManager(fileListLayoutManager);
        activity.attachmentLists.addItemDecoration(dividerItemDecoration);
        fileAdapter.SetOnItemClickListener(new FileAttachmentsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                if(attachments.get(position).getAttachmentType() == 1) {
                    Intent intent = new Intent(SummaryActivity.this, ViewImageActivity.class);
                    intent.putExtra("imageUrl", attachments.get(position).getAttachmentPath());
                    intent.putExtra("imageTitle", attachments.get(position).getAttachmentDescription());
                    startActivity(intent);
                }

            }
        });

    }

    private void initializePatientInfo() {

        HashMap<String, String> patientDetails = newPatientDetails.getPatientInfo();
        patientId = Long.parseLong(patientDetails.get(NewPatientSessionManager.PATIENT_ID));
        patientProfileImage = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_PROFILE_IMAGE);
        patientFirstName = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_FIRST_NAME);
        patientLastName = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_LAST_NAME);
        patientAge = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_AGE);
        patientGender = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_GENDER);
        chiefComplaint = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_CHIEF_COMPLAINT);
        recordedHpiOutputFile = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_DOC_HPI_RECORD);
        patientInfoFormImage = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_DOC_IMAGE1);
        familySocialHistoryFormImage = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_DOC_IMAGE2);
        chiefComplaintFormImage = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_DOC_IMAGE3);
        patientInfoFormImageTitle = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_DOC_IMAGE1_TITLE);
        familySocialHistoryFormImageTitle = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_DOC_IMAGE2_TITLE);
        chiefComplaintFormImageTitle = patientDetails.get(NewPatientSessionManager.NEW_PATIENT_DOC_IMAGE3_TITLE);

    }

    private void initializeDatabase () {

        getBetterDb = new DataAdapter(this);

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

    private void getHealthCenterName(int healthCenterId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        healthCenterName = getBetterDb.getHealthCenterString(healthCenterId);
        getBetterDb.closeDatabase();
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        if(id == R.id.summary_page_submit_btn) {

            new InsertCaseRecordTask().execute();
            if(nMediaPlayer.isPlaying()) {
                nMediaPlayer.stop();
                nMediaPlayer.release();
            }

            if(nMediaController.isShowing()) {
                nMediaController.hide();
            }

            newPatientDetails.endSession();

            CaptureDocumentsActivity.getInstance().finish();
            RecordHpiActivity.getInstance().finish();
            finish();

        } else if(id == R.id.summary_page_back_btn) {

            finish();

        } else if(id == R.id.summary_page_take_pic_btn) {

            takePicture();

        } else if(id == R.id.summary_page_take_pic_doc_btn) {

            takePicture();

        } else if(id == R.id.summary_page_rec_video_btn) {

            recordVideo();

        } else if(id == R.id.summary_page_rec_sound_btn) {

//            initializeMediaRecorder();
            featureAlertMessage();

        } else if(id == R.id.summary_page_audio_record_btn) {

            recordAudio();

        } else if(id == R.id.summary_page_audio_stop_record_btn) {

            stopRecording();

        } else if(id == R.id.summary_page_audio_play_recorded_btn) {

            playRecording();

        } else if(id == R.id.summary_page_record_audio_cancel_btn) {

            if(!audioOutputFile.isEmpty()){
                File file = new File(audioOutputFile);
                boolean deleted = file.delete();
                Log.d("file deleted?", deleted + "");
                playRecordedAudio.setEnabled(false);
            }

            recordAudioContainer.setVisibility(View.INVISIBLE);
            recordAudio.setEnabled(false);

        } else if(id == R.id.summary_page_record_audio_done_btn) {

            editAttachmentName(MEDIA_TYPE_AUDIO);
            recordAudioContainer.setVisibility(View.INVISIBLE);
            recordAudio.setEnabled(false);
            playRecordedAudio.setEnabled(false);

        }

    }

    private String generateControlNumber(long pId) {

        String result;
        int a = 251;
        int c = 134;
        int m = 312;
        int generatedRandomId = m / 2;

        generatedRandomId = (a * generatedRandomId + c) % m;

        String firstChar = patientFirstName.substring(0, 1).toUpperCase();
        String secondChar = patientLastName.substring(0, 1).toUpperCase();
        String lastChar = patientLastName.substring(patientLastName.length() - 1, patientLastName.length()).toUpperCase();
        String patientIdChar = String.valueOf(pId);

        result = firstChar + secondChar + patientIdChar + "-" + generatedRandomId + lastChar;
        Log.e("control number", result);

        return result;
    }

    private int generateCaseRecordId() {

        ArrayList<Integer> storedIds;
        int caseRecordId = 1;
//        int a = 251;
//        int c = 134;
//        int m = 312;
//        int generatedRandomId = m / 2;
//
//        generatedRandomId = (a * generatedRandomId + c) % m;
//        caseRecordId = Integer.parseInt(patientId + Integer.toString(generatedRandomId));

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
            while (storedIds.contains(caseRecordId)){
                caseRecordId += 1;
            }

            return caseRecordId;
        }
    }

    private void initializeMediaRecorder() {

        audioOutputFile = getOutputMediaFileUri(MEDIA_TYPE_AUDIO).getPath();
        hpiRecorder = new MediaRecorder();
        hpiRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        hpiRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        hpiRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        hpiRecorder.setOutputFile(audioOutputFile);
        recordAudioContainer.setVisibility(View.VISIBLE);
    }

    private void recordAudio() {

        try {
            hpiRecorder.prepare();
            hpiRecorder.start();

        } catch (IllegalStateException | IOException e) {

            e.printStackTrace();

        }

        stopRecord.setEnabled(true);
    }

    private void stopRecording() {

        hpiRecorder.stop();
        hpiRecorder.release();
        hpiRecorder = null;

        stopRecord.setEnabled(false);
        playRecordedAudio.setEnabled(true);
        saveRecording.setEnabled(true);
    }

    private void playRecording() {

        mp = new MediaPlayer();

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
    }


    private void initializeMediaPlayer(SummaryActivity activity) {

        nMediaPlayer = new MediaPlayer();
        nMediaController = new MediaController(activity) {
            @Override
            public void hide() {

            }
        };
        //Uri hpiRecordingUri = Uri.parse(recordedHpiOutputFile);

        nMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            nMediaPlayer.setDataSource(recordedHpiOutputFile);
            nMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        nMediaController.setMediaPlayer(activity);
        nMediaController.setAnchorView(activity.findViewById(R.id.hpi_media_player));

        nMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                nHandler.post(new Runnable() {
                    public void run() {
                        nMediaController.show(0);
                        nMediaPlayer.start();
                    }
                });
            }
        });

    }

    private File createMediaFile(int type) {

        String timeStamp = getTimeStamp();
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                DirectoryConstants.CASE_RECORD_ATTACHMENT_DIRECTORY_NAME);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("Debug", "Oops! Failed create "
                        + DirectoryConstants.CASE_RECORD_ATTACHMENT_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        File mediaFile;

        if(type == MEDIA_TYPE_IMAGE) {

            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "image_attachment_" + timeStamp + ".jpg");

        } else if (type == MEDIA_TYPE_VIDEO) {

            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "video_attachment_" + timeStamp + ".mp4");

        } else if (type == MEDIA_TYPE_AUDIO) {

            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "audio_attachment_" + timeStamp + ".3gp");

        } else {
            return null;
        }

        return mediaFile;
    }

    private Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(createMediaFile(type));
    }

    private void takePicture() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, REQUEST_IMAGE_ATTACHMENT);
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
            if (resultCode == Activity.RESULT_OK) {

                editAttachmentName(MEDIA_TYPE_VIDEO);
//                addVideoAttachment(videoAttachmentPath, "video", uploadedDate);

            } else if (resultCode == Activity.RESULT_CANCELED) {

            } else {

            }
        }
    }

    private void recordVideo() {

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
//        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 5491520L);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, REQUEST_VIDEO_ATTACHMENT);
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

    private void editAttachmentName (final int type) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Image Filename");

        // Set up the input
        final EditText input = new EditText(this);

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                attachmentName = input.getText().toString();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                if(type == MEDIA_TYPE_IMAGE) {
                    addPhotoAttachment(fileUri.getPath(), attachmentName, getDateStamp());
                } else if(type == MEDIA_TYPE_VIDEO) {
                    addVideoAttachment(fileUri.getPath(), attachmentName, getDateStamp());
                } else if(type == MEDIA_TYPE_AUDIO) {
                    addAudioAttachment(audioOutputFile, attachmentName, getDateStamp());
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

    private void setPic(CircleImageView mImageView, String mCurrentPhotoPath) {
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
        return new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
    }

    private String getDateStamp() {
        return  new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private String patientFullName (String firstName, String lastName) {

        return firstName +
                " " +
                lastName;
    }

    private void featureAlertMessage() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Feature Not Available!");
        builder.setMessage("Sorry! This feature is still under development.");

        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void insertCaseRecord() {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }


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

        getBetterDb.insertCaseRecordHistory(caseRecordId, userId, getDateStamp());

        getBetterDb.closeDatabase();
    }

    private void insertCaseRecordAttachments() {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < attachments.size(); i++) {

            Log.d(TAG, "insertCaseRecordAttachments: " + attachments.get(i).getAttachmentPath());
            attachments.get(i).setCaseRecordId(caseRecordId);
            attachments.get(i).setUploadedBy(userId);
            getBetterDb.insertCaseRecordAttachments(attachments.get(i));
        }

        getBetterDb.closeDatabase();
    }


    private class InsertCaseRecordTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog progressDialog = new ProgressDialog(SummaryActivity.this);

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

    Runnable UpdateRecordTime = new Runnable() {
        @Override
        public void run() {
            if(isRecording) {
                if(seconds < 10) {
                    secondsView.setText("0" + seconds);
                }
                else {
                    secondsView.setText(String.valueOf(seconds));
                }

                recordTime += 1;
                seconds += 1;

                if(seconds > 60) {
                    seconds = 0;
                    minutes += 1;
                    minutesView.setText("0" + minutes);
                }
                handler.postDelayed(this, 1000);
            }
        }
    };

    Runnable UpdatePlayTime = new Runnable() {
        @Override
        public void run() {
            if(mp.isPlaying()) {

                if(seconds < 10) {
                    secondsView.setText("0" + seconds);
                }
                else {
                    secondsView.setText(String.valueOf(seconds));
                }

                seconds += 1;

                if(seconds > 60) {
                    seconds = 0;
                    minutes += 1;
                    minutesView.setText("0" + minutes);
                }
                handler.postDelayed(this, 1000);

            }
        }
    };


}