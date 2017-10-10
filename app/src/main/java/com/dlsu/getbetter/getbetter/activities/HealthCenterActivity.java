package com.dlsu.getbetter.getbetter.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.activities.HomeActivity;
import com.dlsu.getbetter.getbetter.adapters.HealthCenterListAdapter;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.objects.DividerItemDecoration;
import com.dlsu.getbetter.getbetter.objects.HealthCenter;
import com.dlsu.getbetter.getbetter.sessionmanagers.SystemSessionManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class HealthCenterActivity extends AppCompatActivity {

    private DataAdapter getBetterDb;
    private SystemSessionManager systemSessionManager;
    private ArrayList<HealthCenter> healthCenters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_center);

        systemSessionManager = new SystemSessionManager(this);
        if(systemSessionManager.checkLogin())
            finish();

        HashMap<String, String> user = systemSessionManager.getUserDetails();
        TextView userLabel = (TextView)findViewById(R.id.hc_welcome_text);
        RecyclerView healthCenterRecycler = (RecyclerView)findViewById(R.id.hc_recycler_view);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(this);


        String email = user.get(SystemSessionManager.LOGIN_USER_NAME);
        healthCenters = new ArrayList<>();
        initializeDatabase();
        getHealthCenterList();

        userLabel.append(" " + getUserName(email) + "!");
        HealthCenterListAdapter healthCenterListAdapter = new HealthCenterListAdapter(healthCenters);

        if (healthCenterRecycler != null) {
            healthCenterRecycler.setHasFixedSize(true);
            healthCenterRecycler.setLayoutManager(new LinearLayoutManager(this));
            healthCenterRecycler.setAdapter(healthCenterListAdapter);
            healthCenterRecycler.addItemDecoration(dividerItemDecoration);
        }

        healthCenterListAdapter.SetOnItemClickListener(new HealthCenterListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                String healthCenterName = healthCenters.get(position).getHealthCenterName();
                String healthCenterId = String.valueOf(healthCenters.get(position).getHealthCenterId());

                systemSessionManager.setHealthCenter(healthCenterName, healthCenterId);

                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initializeDatabase () {

        getBetterDb = new DataAdapter(this);

        try {
            getBetterDb.createDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void getHealthCenterList() {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        healthCenters.addAll(getBetterDb.getHealthCenters());

        getBetterDb.closeDatabase();

    }

    private String getUserName(String userEmail) {


        try {
            getBetterDb.openDatabase();
        }catch (SQLException e) {
            e.printStackTrace();
        }

        String username = getBetterDb.getUserName(userEmail);

        getBetterDb.closeDatabase();

        return username;
    }
}
