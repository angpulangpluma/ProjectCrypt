package com.dlsu.getbetter.getbetter;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
 * A simple {@link Fragment} subclass.
 */
public class UrgentCaseFragment extends Fragment {

    private DataAdapter getBetterDb;
    private OnCaseRecordSelected mCallback;

    private ArrayList<CaseRecord> urgentCases;
    private UpdatedCaseRecordAdapter updatedCaseRecordAdapter;
    private int selectedCaseRecordId = 0;

    public UrgentCaseFragment() {
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

        urgentCases = new ArrayList<>();

        int healthCenterId = Integer.parseInt(hc.get(SystemSessionManager.HEALTH_CENTER_ID));

        initializeDatabase();
        getUrgentCases(healthCenterId);

        updatedCaseRecordAdapter = new UpdatedCaseRecordAdapter(urgentCases);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_updated_case, container, false);

        RecyclerView urgentRecycler = (RecyclerView)rootView.findViewById(R.id.updated_case_recycler);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext());
        urgentRecycler.setHasFixedSize(true);
        urgentRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        urgentRecycler.setAdapter(updatedCaseRecordAdapter);
        urgentRecycler.addItemDecoration(dividerItemDecoration);
        updatedCaseRecordAdapter.SetOnItemClickListener(new UpdatedCaseRecordAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                selectedCaseRecordId = urgentCases.get(position).getCaseRecordId();
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

    private void getUrgentCases(int healthCenterId) {

        try {
            getBetterDb.openDatabase();
        }catch (SQLException e) {
            e.printStackTrace();
        }

        urgentCases.addAll(getBetterDb.getUrgentCaseRecords(healthCenterId));

        for(int i = 0; i < urgentCases.size(); i++) {

            Patient patientInfo = getBetterDb.getPatient((long) urgentCases.get(i).getUserId());
            String patientName = patientInfo.getFirstName() + " " + patientInfo.getLastName();
            urgentCases.get(i).setPatientName(patientName);
            urgentCases.get(i).setProfilePic(patientInfo.getProfileImageBytes());

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
