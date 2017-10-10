package com.dlsu.getbetter.getbetter.activities;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.dlsu.getbetter.getbetter.NewPatientFragment;
import com.dlsu.getbetter.getbetter.R;

public class NewPatientRecordActivity extends AppCompatActivity {

    private String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_patient_record);

//        if(savedInstanceState != null) {
//
//        }

        NewPatientFragment newPatientFragment = new NewPatientFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, newPatientFragment);
        fragmentTransaction.commit();
    }

    public String getData() {
        return data;
    }

    //    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//
//        getSupportFragmentManager().putFragment(outState, "mContent", mContent);
//    }
}
