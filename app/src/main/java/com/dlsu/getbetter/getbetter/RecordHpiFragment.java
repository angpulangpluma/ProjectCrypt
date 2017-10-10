package com.dlsu.getbetter.getbetter;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dlsu.getbetter.getbetter.cryptoGB.file_aes;
import com.dlsu.getbetter.getbetter.sessionmanagers.NewPatientSessionManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.os.Environment.DIRECTORY_DOCUMENTS;


/**
 * A simple {@link Fragment} subclass.
 */


public class RecordHpiFragment extends Fragment implements View.OnClickListener {

    private Button stopRecBtn;
    private Button playRecBtn;
    private MediaRecorder hpiRecorder;
    private String outputFile;
    private String chiefComplaintName = "";

    private NewPatientSessionManager newPatientSessionManager;


    public RecordHpiFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        newPatientSessionManager = new NewPatientSessionManager(getActivity());

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());

        outputFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/" +
                "hpi_recording_" + timeStamp + ".3gp";

        hpiRecorder = new MediaRecorder();
        hpiRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        hpiRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        hpiRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        hpiRecorder.setOutputFile(outputFile);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_record_hpi, container, false);

        Button nextBtn = (Button) rootView.findViewById(R.id.hpi_next_btn);
        Button recordBtn = (Button) rootView.findViewById(R.id.hpi_record_btn);
        stopRecBtn = (Button)rootView.findViewById(R.id.hpi_stop_record_btn);
        playRecBtn = (Button)rootView.findViewById(R.id.hpi_play_recorded_btn);

        stopRecBtn.setEnabled(false);
        playRecBtn.setEnabled(false);


        recordBtn.setOnClickListener(this);
        stopRecBtn.setOnClickListener(this);
        playRecBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if(id == R.id.hpi_next_btn) {

            if(chiefComplaintName.isEmpty()) {
                Toast.makeText(this.getContext(), "Please record the HPI", Toast.LENGTH_LONG).show();
            } else {

                newPatientSessionManager.setHPIRecord(outputFile, chiefComplaintName);
                SummaryPageFragment summaryPageFragment = new SummaryPageFragment();
                getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).commit();
                getActivity().getSupportFragmentManager().beginTransaction().
                        replace(R.id.fragment_container, summaryPageFragment).commit();

            }

        } else if(id == R.id.hpi_record_btn) {

            try {
                hpiRecorder.prepare();
                hpiRecorder.start();

            } catch (IllegalStateException | IOException e) {

                e.printStackTrace();

            }

            stopRecBtn.setEnabled(true);
            Toast.makeText(this.getContext(), "Now Recording....", Toast.LENGTH_LONG).show();

        } else if (id == R.id.hpi_stop_record_btn) {


            hpiRecorder.stop();
            hpiRecorder.release();
            hpiRecorder = null;

            stopRecBtn.setEnabled(false);
            playRecBtn.setEnabled(true);

            editImageTitle();

        } else if (id == R.id.hpi_play_recorded_btn) {

            MediaPlayer mp = new MediaPlayer();

            try {

                mp.setDataSource(outputFile);
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
    }

    private void editImageTitle () {

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Chief Complaint");

        // Set up the input
        final EditText input = new EditText(this.getContext());

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                chiefComplaintName = input.getText().toString();
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

}
