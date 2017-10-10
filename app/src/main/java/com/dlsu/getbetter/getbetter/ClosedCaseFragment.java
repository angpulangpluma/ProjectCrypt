package com.dlsu.getbetter.getbetter;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.objects.CaseRecord;
import com.dlsu.getbetter.getbetter.objects.Patient;
import com.dlsu.getbetter.getbetter.sessionmanagers.SystemSessionManager;

import java.sql.SQLException;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ClosedCaseFragment extends Fragment {

    SystemSessionManager systemSessionManager;
    private DataAdapter getBetterDb;

    private ArrayList<CaseRecord> closedCases;


    public ClosedCaseFragment() {
        // Required empty public constructor
    }

    public interface OnCaseRecordSelected {
        void onCaseRecordSelected(int caseRecordId);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_updated_case, container, false);
        RecyclerView closedCaseRecycler = (RecyclerView)rootView.findViewById(R.id.updated_case_recycler);

        closedCaseRecycler.setHasFixedSize(true);
        closedCaseRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        return rootView;
    }

    private void initializeDatabase () {

        getBetterDb = new DataAdapter(this.getContext());

        try {
            getBetterDb.createDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getClosedCases(int healthCenterId) {

        try {
            getBetterDb.openDatabase();
        }catch (SQLException e) {
            e.printStackTrace();
        }

        closedCases.addAll(getBetterDb.getClosedCaseRecords(healthCenterId));

        for(int i = 0; i < closedCases.size(); i++) {

            Patient patientInfo = getBetterDb.getPatient((long) closedCases.get(i).getUserId());
            String patientName = patientInfo.getFirstName() + " " + patientInfo.getLastName();
            closedCases.get(i).setPatientName(patientName);
            closedCases.get(i).setProfilePic(patientInfo.getProfileImageBytes());

        }

        getBetterDb.closeDatabase();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            OnCaseRecordSelected mCallback = (OnCaseRecordSelected) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

}
