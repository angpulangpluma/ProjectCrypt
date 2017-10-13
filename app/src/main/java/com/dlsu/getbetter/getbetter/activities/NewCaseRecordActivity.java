package com.dlsu.getbetter.getbetter.activities;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.dlsu.getbetter.getbetter.CaptureDocumentsFragment;
import com.dlsu.getbetter.getbetter.R;

import java.io.Serializable;

public class NewCaseRecordActivity extends AppCompatActivity
    implements Serializable{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_patient_record);

        CaptureDocumentsFragment captureDocumentsFragment = new CaptureDocumentsFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, captureDocumentsFragment);
        fragmentTransaction.commit();
    }
}
