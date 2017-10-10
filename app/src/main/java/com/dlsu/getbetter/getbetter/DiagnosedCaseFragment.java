package com.dlsu.getbetter.getbetter;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dlsu.getbetter.getbetter.adapters.UpdatedCaseRecordAdapter;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.objects.CaseRecord;
import com.dlsu.getbetter.getbetter.objects.DividerItemDecoration;
import com.dlsu.getbetter.getbetter.objects.Patient;
import com.dlsu.getbetter.getbetter.sessionmanagers.SystemSessionManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.1
 */
public class DiagnosedCaseFragment extends Fragment {


    private DataAdapter getBetterDb;
    private OnCaseRecordSelected mCallback;

    private ArrayList<CaseRecord> diagnosedCases;
    private UpdatedCaseRecordAdapter updatedCaseRecordAdapter;
    private int selectedCaseRecordId = 0;

    public DiagnosedCaseFragment() {
        // Required empty public constructor
    }

    public interface OnCaseRecordSelected {
        void onCaseRecordSelected(int caseRecordId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SystemSessionManager systemSessionManager = new SystemSessionManager(getActivity());
        HashMap<String, String> hc = systemSessionManager.getHealthCenter();

        diagnosedCases = new ArrayList<>();

        int healthCenterId = Integer.parseInt(hc.get(SystemSessionManager.HEALTH_CENTER_ID));

        initializeDatabase();
        getDiagnosedCaseRecords(healthCenterId);

        updatedCaseRecordAdapter = new UpdatedCaseRecordAdapter(diagnosedCases);
        Log.d("Diagnosed", "" + diagnosedCases.size());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_updated_case, container, false);
        RecyclerView diagnosedCaseRecycler = (RecyclerView)rootView.findViewById(R.id.updated_case_recycler);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext());
        diagnosedCaseRecycler.setHasFixedSize(true);
        diagnosedCaseRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        diagnosedCaseRecycler.setAdapter(updatedCaseRecordAdapter);
        diagnosedCaseRecycler.addItemDecoration(dividerItemDecoration);
        updatedCaseRecordAdapter.SetOnItemClickListener(new UpdatedCaseRecordAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                selectedCaseRecordId = diagnosedCases.get(position).getCaseRecordId();
                mCallback.onCaseRecordSelected(selectedCaseRecordId);
            }
        });


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

    private void getDiagnosedCaseRecords(int healthCenterId) {

        try {
            getBetterDb.openDatabase();
        }catch (SQLException e) {
            e.printStackTrace();
        }

        diagnosedCases.addAll(getBetterDb.getDiagnosedCaseRecords(healthCenterId));

        for(int i = 0; i < diagnosedCases.size(); i++) {

            Patient patientInfo = getBetterDb.getPatient((long) diagnosedCases.get(i).getUserId());
            String patientName = patientInfo.getFirstName() + " " + patientInfo.getLastName();
            diagnosedCases.get(i).setPatientName(patientName);
            diagnosedCases.get(i).setProfilePic(patientInfo.getProfileImageBytes());

        }

        getBetterDb.closeDatabase();


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            mCallback = (OnCaseRecordSelected) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

}
