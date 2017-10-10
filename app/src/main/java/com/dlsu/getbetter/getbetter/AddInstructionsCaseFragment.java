package com.dlsu.getbetter.getbetter;


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
public class AddInstructionsCaseFragment extends Fragment {

    private DataAdapter getBetterDb;

    private ArrayList<CaseRecord> addInstructionCases;
    private UpdatedCaseRecordAdapter updatedCaseRecordAdapter;
    private int selectedCaseRecordId = 0;


    public AddInstructionsCaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SystemSessionManager systemSessionManager = new SystemSessionManager(getActivity());

        HashMap<String, String> hc = systemSessionManager.getHealthCenter();
        int healthCenterId = Integer.parseInt(hc.get(SystemSessionManager.HEALTH_CENTER_ID));

        addInstructionCases = new ArrayList<>();

        initializeDatabase();
        getAddInstructionsCases(healthCenterId);

        updatedCaseRecordAdapter = new UpdatedCaseRecordAdapter(addInstructionCases);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_updated_case, container, false);
        RecyclerView addInstructionsRecycler = (RecyclerView)rootView.findViewById(R.id.updated_case_recycler);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext());
        addInstructionsRecycler.setHasFixedSize(true);
        addInstructionsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        addInstructionsRecycler.setAdapter(updatedCaseRecordAdapter);
        addInstructionsRecycler.addItemDecoration(dividerItemDecoration);
        updatedCaseRecordAdapter.SetOnItemClickListener(new UpdatedCaseRecordAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                selectedCaseRecordId = addInstructionCases.get(position).getCaseRecordId();
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

    private void getAddInstructionsCases(int healthCenterId) {

        try{
            getBetterDb.openDatabase();
        }catch (SQLException e) {
            e.printStackTrace();
        }

        addInstructionCases.addAll(getBetterDb.getAddInstructionCaseRecords(healthCenterId));

        for(int i = 0; i < addInstructionCases.size(); i++) {

            Patient patientInfo = getBetterDb.getPatient((long) addInstructionCases.get(i).getUserId());
            String patientName = patientInfo.getFirstName() + " " + patientInfo.getLastName();
            addInstructionCases.get(i).setPatientName(patientName);
            addInstructionCases.get(i).setProfilePic(patientInfo.getProfileImageBytes());

        }

        getBetterDb.closeDatabase();
    }

}
