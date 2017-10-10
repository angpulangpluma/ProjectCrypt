package com.dlsu.getbetter.getbetter.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.adapters.FileAttachmentsAdapter;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.objects.Attachment;
import com.dlsu.getbetter.getbetter.objects.CaseRecord;
import com.dlsu.getbetter.getbetter.objects.DividerItemDecoration;
import com.dlsu.getbetter.getbetter.objects.Patient;
import com.dlsu.getbetter.getbetter.sessionmanagers.SystemSessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewCaseRecordActivity extends AppCompatActivity implements MediaController.MediaPlayerControl, View.OnClickListener {

    private static final String TAG = "ViewCaseRecordActivity";

    private TextView patientName;
    private TextView healthCenterName;
    private TextView ageGender;
    private TextView chiefComplaint;
    private TextView controlNumber;
    private CircleImageView profilePic;
    private Button backBtn;
    private Button updateCaseBtn;
    private RecyclerView attachmentList;

    private int healthCenterId;
    private CaseRecord caseRecord;
    private Patient patientInfo;
    private ArrayList<Attachment> caseAttachments;

    private DataAdapter getBetterDb;
    private MediaPlayer nMediaPlayer;
    private MediaController nMediaController;
    private Handler nHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_case_record);

        SystemSessionManager systemSessionManager = new SystemSessionManager(this);
        Bundle extras = getIntent().getExtras();
        int caseRecordId = extras.getInt("caseRecordId");
        long patientId = extras.getLong("patientId");

        HashMap<String, String> user = systemSessionManager.getUserDetails();
        HashMap<String, String> hc = systemSessionManager.getHealthCenter();
        healthCenterId = Integer.parseInt(hc.get(SystemSessionManager.HEALTH_CENTER_ID));

        killMediaPlayer();
        nMediaPlayer = new MediaPlayer();
        nMediaController = new MediaController(ViewCaseRecordActivity.this) {
            @Override
            public void hide() {

            }
        };

        nMediaController.setMediaPlayer(ViewCaseRecordActivity.this);
        nMediaController.setAnchorView(findViewById(R.id.hpi_media_player));

        initializeDatabase();
        getCaseRecord(caseRecordId);
        getCaseAttachments(caseRecordId);
        getPatientInfo(patientId);
        bindViews(this);
        bindListeners(this);
        initFileList(this);

        String recordedHpiOutputFile = getHpiOutputFile(caseRecordId);
        Log.d(TAG, "onCreate: " + recordedHpiOutputFile);
        nMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            nMediaPlayer.setDataSource(recordedHpiOutputFile);
            nMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "onCreate: " + e.toString());
        }

        nMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                nHandler.post(new Runnable() {
                    public void run() {
                        nMediaController.show(0);
                        nMediaController.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                nMediaPlayer.start();
                            }
                        });

                    }
                });
            }
        });
    }

    private void bindViews(ViewCaseRecordActivity activity) {

        activity.patientName = (TextView)activity.findViewById(R.id.view_case_patient_name);
        activity.healthCenterName = (TextView)activity.findViewById(R.id.view_case_health_center);
        activity.ageGender = (TextView)activity.findViewById(R.id.view_case_age_gender);
        activity.chiefComplaint = (TextView)activity.findViewById(R.id.view_case_chief_complaint);
        activity.controlNumber = (TextView)activity.findViewById(R.id.view_case_control_number);
        activity.attachmentList = (RecyclerView)activity.findViewById(R.id.view_case_files_list);
        activity.profilePic = (CircleImageView) activity.findViewById(R.id.profile_picture_display);
        activity.backBtn = (Button)activity.findViewById(R.id.view_case_back_btn);
        activity.updateCaseBtn = (Button)activity.findViewById(R.id.update_case_record_btn);

        String fullName = patientInfo.getFirstName() + " " + patientInfo.getMiddleName() + " " + patientInfo.getLastName();
        String gender = patientInfo.getGender();
        String patientAgeGender = patientInfo.getAge() + " yrs. old, " + gender;
        setPic(profilePic, patientInfo.getProfileImageBytes());

        ageGender.setText(patientAgeGender);
        chiefComplaint.setText(caseRecord.getCaseRecordComplaint());
        controlNumber.setText(caseRecord.getCaseRecordControlNumber());
        patientName.setText(fullName);
        healthCenterName.setText(getHealthCenterString(healthCenterId));
        activity.updateCaseBtn.setVisibility(View.INVISIBLE);

    }

    private void bindListeners(ViewCaseRecordActivity activity) {

        activity.backBtn.setOnClickListener(activity);
        activity.updateCaseBtn.setOnClickListener(activity);
    }

    private void initFileList(ViewCaseRecordActivity activity) {

        FileAttachmentsAdapter fileAdapter = new FileAttachmentsAdapter(caseAttachments);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(activity);

        activity.attachmentList.setHasFixedSize(true);
        activity.attachmentList.setLayoutManager(layoutManager);
        activity.attachmentList.setAdapter(fileAdapter);
        activity.attachmentList.addItemDecoration(dividerItemDecoration);
        fileAdapter.SetOnItemClickListener(new FileAttachmentsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                if(caseAttachments.get(position).getAttachmentType() == 1) {
                    Intent intent = new Intent(ViewCaseRecordActivity.this, ViewImageActivity.class);
                    intent.putExtra("imageUrl", caseAttachments.get(position).getAttachmentPath());
                    intent.putExtra("imageTitle", caseAttachments.get(position).getAttachmentDescription());
                    startActivity(intent);
                } else {
                    //do nothing
                }
            }
        });
    }

    private void initializeDatabase() {

        getBetterDb = new DataAdapter(this);

        try {
            getBetterDb.createDatabase();
        } catch(SQLException e ){
            e.printStackTrace();
        }

    }

    private void getCaseRecord(int caseRecordId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        caseRecord = getBetterDb.getCaseRecord(caseRecordId);

        getBetterDb.closeDatabase();

    }

    private void getCaseAttachments(int caseRecordId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        caseAttachments = new ArrayList<>();
        caseAttachments.addAll(getBetterDb.getCaseRecordAttachments(caseRecordId));

        getBetterDb.closeDatabase();

    }

    private void getPatientInfo(long patientId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        patientInfo = getBetterDb.getPatient(patientId);

        getBetterDb.closeDatabase();
    }

    private String getHealthCenterString(int healthCenterId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String healthCenterName = getBetterDb.getHealthCenterString(healthCenterId);

        getBetterDb.closeDatabase();

        return healthCenterName;

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if(id == R.id.view_case_back_btn) {

            finish();

        } else if (id == R.id.update_case_record_btn) {

        }
    }

    private void setPic(ImageView mImageView, String mCurrentPhotoPath) {
        // Get the dimensions of the View
        int targetW = 255;//mImageView.getWidth();
        int targetH = 200;// mImageView.getHeight();

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

    private String getHpiOutputFile(int caseRecordId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String result;

        result = getBetterDb.getHPI(caseRecordId);

        getBetterDb.closeDatabase();

        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        nMediaController.hide();
        killMediaPlayer();
    }

    private void killMediaPlayer() {
        if(nMediaPlayer != null) {
            try{
                nMediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
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

}
