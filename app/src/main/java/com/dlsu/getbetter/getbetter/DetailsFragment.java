package com.dlsu.getbetter.getbetter;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.activities.ViewCaseRecordActivity;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.objects.Attachment;
import com.dlsu.getbetter.getbetter.objects.CaseRecord;
import com.dlsu.getbetter.getbetter.objects.Patient;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringTokenizer;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsFragment extends Fragment implements MediaController.MediaPlayerControl {

    private DataAdapter getBetterDb;
    private MediaPlayer nMediaPlayer;
    private MediaController nMediaController;
    private Handler nHandler = new Handler();

    private CaseRecord caseRecord;
    private ImageView profilePic;
    private Patient patientInfo;
    private ArrayList<Attachment> caseAttachments;


    public DetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeDatabase();
        int caseRecordId;

        if(getArguments().containsKey("case record id")) {
            caseRecordId = getArguments().getInt("case record id");
            getCaseDetails(caseRecordId);
            getCaseAttachments(caseRecordId);
            prepareMediaPlayer();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_view_updated_case_record, container, false);
        TextView patientName = (TextView)rootView.findViewById(R.id.detail_patient_name);
        TextView ageGender = (TextView)rootView.findViewById(R.id.detail_age_gender);
        TextView chiefComplaint = (TextView)rootView.findViewById(R.id.detail_chief_complaint);
        TextView controlNumber = (TextView)rootView.findViewById(R.id.detail_control_number);
        TextView additionalNotes = (TextView)rootView.findViewById(R.id.detail_instruction);
        Button viewUpdatedCaseBtn = (Button)rootView.findViewById(R.id.detail_view_case_btn);
        profilePic = (ImageView)rootView.findViewById(R.id.detail_picture_display);

        nMediaController.setMediaPlayer(DetailsFragment.this);
        nMediaController.setAnchorView(rootView.findViewById(R.id.detail_hpi_media_player));

        int[] birthdateTemp = new int[3];
        String patientAgeGender = "";
        String gender = patientInfo.getGender();

        if(patientInfo.getBirthdate() != null) {

            StringTokenizer tok = new StringTokenizer(patientInfo.getBirthdate(), "-");
            int i = 0;
            while(tok.hasMoreTokens()) {

                birthdateTemp[i] = Integer.parseInt(tok.nextToken());
                i++;
            }

            LocalDate birthdate = new LocalDate(birthdateTemp[0], birthdateTemp[1], birthdateTemp[2]);
            LocalDate now = new LocalDate();

            Years age = Years.yearsBetween(birthdate, now);

            String patientAge = age.getYears() + "";
            patientAgeGender = patientAge + " yrs. old, " + gender;
        }

        if (ageGender != null) {
            ageGender.setText(patientAgeGender);
        }

        patientName.setText(caseRecord.getPatientName());
        chiefComplaint.setText(caseRecord.getCaseRecordComplaint());
        controlNumber.setText(caseRecord.getCaseRecordControlNumber());
        additionalNotes.setText(caseRecord.getCaseRecordAdditionalNotes());

        viewUpdatedCaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ViewCaseRecordActivity.class);
                intent.putExtra("caseRecordId", caseRecord.getCaseRecordId());
                intent.putExtra("patientId", (long)caseRecord.getUserId());
                startActivity(intent);
            }
        });

        startMediaPlayer();


        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        setPic(profilePic, caseRecord.getProfilePic());
    }

    private void initializeDatabase () {

        getBetterDb = new DataAdapter(this.getContext());

        try {
            getBetterDb.createDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getCaseDetails(int caseRecordId) {

        try {
            getBetterDb.openDatabase();
        }catch (SQLException e) {
            e.printStackTrace();
        }

        caseRecord = getBetterDb.getCaseRecord(caseRecordId);

        patientInfo = getBetterDb.getPatient((long) caseRecord.getUserId());
        String patientName = patientInfo.getFirstName() + " " + patientInfo.getLastName();
        caseRecord.setPatientName(patientName);
        caseRecord.setProfilePic(patientInfo.getProfileImageBytes());

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

    private String getHpiOutputFile() {

        String result = "";

        if(caseAttachments.isEmpty()) {
            Log.e("attachments is empty", "true");
        } else {
            for(int i = 0; i < caseAttachments.size(); i++) {

                if(caseAttachments.get(i).getAttachmentType() == 5) {
                    result = caseAttachments.get(i).getAttachmentPath();
                }
            }
        }
        return result;
    }

    private void setPic(ImageView mImageView, String mCurrentPhotoPath) {
        // Get the dimensions of the View
        int targetW = 100;
        int targetH = 100;

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

    private void startMediaPlayer() {

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

    private void prepareMediaPlayer() {

        killMediaPlayer();
        String recordedHPIOutputFile = getHpiOutputFile();
        nMediaPlayer = new MediaPlayer();
        nMediaController = new MediaController(this.getContext()) {
            @Override
            public void hide() {

            }
        };
        //Uri hpiRecordingUri = Uri.parse(recordedHpiOutputFile);

        nMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            nMediaPlayer.setDataSource(recordedHPIOutputFile);
            nMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void killMediaPlayer() {
        nMediaController.hide();
        if(nMediaPlayer != null) {
            try{
                nMediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        killMediaPlayer();
    }

    @Override
    public void onStop() {
        super.onStop();
        killMediaPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        killMediaPlayer();
    }

    @Override
    public void start() {
        nMediaPlayer.start();
    }

    @Override
    public void pause() {
        if(nMediaPlayer.isPlaying())
            nMediaPlayer.pause();
    }

    @Override
    public int getDuration() {
        return nMediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return nMediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        nMediaPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return nMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return (nMediaPlayer.getCurrentPosition() * 100) / nMediaPlayer.getDuration();
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
}
