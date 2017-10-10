package com.dlsu.getbetter.getbetter.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.dlsu.getbetter.getbetter.DirectoryConstants;
import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.sessionmanagers.NewPatientSessionManager;
import com.dlsu.getbetter.getbetter.sessionmanagers.SystemSessionManager;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewPatientInfoActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener,
        View.OnClickListener, AdapterView.OnItemSelectedListener {

    private CircleImageView profileImage;
    private TextInputEditText firstNameInput;
    private TextInputEditText middleNameInput;
    private TextInputEditText lastNameInput;
    private TextInputEditText birthdateInput;
    private Button backBtn;
    private Button nextBtn;
    private Spinner genderChoice;
    private Spinner civilStatusChoice;
    private Spinner bloodTypeChoice;

    private NewPatientSessionManager newPatientSessionManager;
    private String genderSelected;
    private String civilStatusSelected;
    private String bloodTypeSelected;
    private String birthDate;
    private int healthCenterId;
    private Uri fileUri;
    private DataAdapter getBetterDb;

    private static final int REQUEST_IMAGE1 = 100;

    private static final String TAG = "debug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_patient_info);

//        newPatientSessionManager = new NewPatientSessionManager(this);
        SystemSessionManager systemSessionManager = new SystemSessionManager(this);
        if(systemSessionManager.checkLogin())
            finish();

        HashMap<String, String> hc = systemSessionManager.getHealthCenter();
        healthCenterId = Integer.parseInt(hc.get(SystemSessionManager.HEALTH_CENTER_ID));
        initializeDatabase();
        bindViews(this);
        showDatePlaceholder();
        initGenderAdapter(this);
        initCivilStatusAdapter(this);
        initBloodTypeAdapter(this);
        bindListeners(this);

    }

    private void bindViews(NewPatientInfoActivity activity) {

        activity.profileImage = (CircleImageView)activity.findViewById(R.id.profile_picture_select);
        activity.firstNameInput = (TextInputEditText)activity.findViewById(R.id.first_name_input);
        activity.middleNameInput = (TextInputEditText)activity.findViewById(R.id.middle_name_input);
        activity.lastNameInput = (TextInputEditText)activity.findViewById(R.id.last_name_input);
        activity.birthdateInput = (TextInputEditText)activity.findViewById(R.id.birthdate_input);
        activity.genderChoice = (Spinner)activity.findViewById(R.id.gender_spinner);
        activity.civilStatusChoice = (Spinner)activity.findViewById(R.id.civil_status_spinner);
        activity.bloodTypeChoice = (Spinner)activity.findViewById(R.id.blood_type_spinner);
        activity.backBtn = (Button)activity.findViewById(R.id.new_patient_back_btn);
        activity.nextBtn = (Button)activity.findViewById(R.id.new_patient_next_btn);

        activity.firstNameInput.setError(null);
        activity.lastNameInput.setError(null);
    }

    private void bindListeners(NewPatientInfoActivity activity) {

        activity.profileImage.setOnClickListener(this);
        activity.birthdateInput.setOnClickListener(this);
        activity.genderChoice.setOnItemSelectedListener(this);
        activity.civilStatusChoice.setOnItemSelectedListener(this);
        activity.bloodTypeChoice.setOnItemSelectedListener(this);
        activity.backBtn.setOnClickListener(this);
        activity.nextBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        if(id == R.id.birthdate_input) {

            datePickerDialog();

        } else if (id == R.id.new_patient_back_btn) {

            finish();


        } else if (id == R.id.new_patient_next_btn) {

            if(checkForMissingFields()) {
                //show snackbar alert
            } else {

                new InsertPatientTask().execute();

            }


        } else if (id == R.id.profile_picture_select) {
            takePicture();
        }
    }

    private void initializeDatabase() {

        getBetterDb = new DataAdapter(this);

        try {
            getBetterDb.createDatabase();
        } catch(SQLException e ){
            e.printStackTrace();
        }
    }


    private long savePatientInfo() {

        String firstName = firstNameInput.getText().toString();
        String middleName = middleNameInput.getText().toString();
        String lastName = lastNameInput.getText().toString();

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "savePatientInfo: " + genderSelected);
        Log.d(TAG, "savePatientInfo: " + civilStatusSelected);

        long patientId = getBetterDb.insertPatientInfo(firstName, middleName, lastName, birthDate,
                genderSelected, civilStatusSelected, bloodTypeSelected, fileUri.getPath(), healthCenterId);

        getBetterDb.closeDatabase();

        return patientId;
    }

    private class InsertPatientTask extends AsyncTask<String, Void, Long> {

        ProgressDialog progressDialog = new ProgressDialog(NewPatientInfoActivity.this);

        @Override
        protected void onPreExecute() {
            this.progressDialog.setMessage("Inserting Patient Info...");
            this.progressDialog.show();

        }

        @Override
        protected Long doInBackground(String... params) {

            long rowId;

            rowId = savePatientInfo();

            return rowId;
        }

        @Override
        protected void onPostExecute(Long result) {

            if (this.progressDialog.isShowing()) {
                this.progressDialog.dismiss();
            }

            long patientId = result;
            Intent intent = new Intent(NewPatientInfoActivity.this, ViewPatientActivity.class);
            intent.putExtra("patientId", patientId);
            Log.d(TAG, "onPostExecute: " + patientId);
            startActivity(intent);
            finish();

//            Toast.makeText(NewPatientInfoActivity.this, "Patient ID: " + result + " inserted.",
//                    Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkForMissingFields () {

        String firstName = firstNameInput.getText().toString();
        String lastName = lastNameInput.getText().toString();

        if(firstName.trim().length() <= 0) {
            firstNameInput.setError("First name is required");
            firstNameInput.requestFocus();
        }

        if(lastName.trim().length() <= 0) {
            lastNameInput.setError("Last name is required");
            lastNameInput.requestFocus();
        }

        return firstNameInput.getText().toString().trim().length() <= 0 || lastNameInput.getText().toString().trim().length() <= 0
                || birthDate.trim().length() <= 0 || fileUri.getPath().trim().length() <= 0;
    }



    private void datePickerDialog() {

        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(NewPatientInfoActivity.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));

        dpd.setThemeDark(true);
        dpd.setTitle("Birthdate");
        dpd.showYearPickerFirst(true);
        dpd.setMaxDate(Calendar.getInstance());
        dpd.show(getFragmentManager(), "DatePickerDialog");

    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        monthOfYear += 1;
        String month = monthOfYear + "";
        String day = dayOfMonth + "";

        if(monthOfYear < 10) {
            month = "0" + monthOfYear;
        }

        if(dayOfMonth < 10) {
            day = "0" + dayOfMonth;
        }

        birthDate = year + "-" + month + "-" + day;
        String date = day + "/" + month + "/" + year;
        birthdateInput.setText(date);
    }

    private void showDatePlaceholder() {

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        month += 1;
        String monthString = month + "";
        String dayString = day + "";

        if(month < 10) {
            monthString = "0" + month;
        }

        if(day < 10) {
            dayString = "0" + day;
        }

        birthDate = year + "-" + month + "-" + day;
        String date = dayString + "/" + monthString + "/" + year;
        birthdateInput.setText(date);
    }

    private File createImageFile() {

        File mediaStorageDir = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                DirectoryConstants.PROFILE_IMAGE_DIRECTORY_NAME);


        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("Debug", "Oops! Failed create "
                        + DirectoryConstants.PROFILE_IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        return new File (mediaStorageDir.getPath() + File.separator + "profile_image_" + getTimeStamp() + ".jpg");
    }

    private void takePicture() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = Uri.fromFile(createImageFile());

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, REQUEST_IMAGE1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_IMAGE1 && resultCode == Activity.RESULT_OK) {
            setPic(profileImage, fileUri.getPath());
        }
    }

    private void setPic(CircleImageView mImageView, String mCurrentPhotoPath) {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {

        switch(parent.getId()) {
            case R.id.gender_spinner:
                genderSelected = (parent.getItemAtPosition(position)).toString();
                break;

            case R.id.civil_status_spinner:
                civilStatusSelected = (parent.getItemAtPosition(position)).toString();
                break;

            case R.id.blood_type_spinner:
                bloodTypeSelected = (parent.getItemAtPosition(position)).toString();
                break;
        }
    }

    private void initGenderAdapter(NewPatientInfoActivity activity) {

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(activity,
                R.array.genders, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activity.genderChoice.setAdapter(genderAdapter);
    }

    private void initCivilStatusAdapter(NewPatientInfoActivity activity) {

        ArrayAdapter<CharSequence> civilStatusAdapter = ArrayAdapter.createFromResource(activity,
                R.array.civil_statuses, android.R.layout.simple_spinner_item);
        civilStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activity.civilStatusChoice.setAdapter(civilStatusAdapter);
    }

    private void initBloodTypeAdapter(NewPatientInfoActivity activity) {

        ArrayAdapter<CharSequence> bloodTypeAdapter = ArrayAdapter.createFromResource(activity,
                R.array.blood_type, android.R.layout.simple_spinner_item);
        bloodTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activity.bloodTypeChoice.setAdapter(bloodTypeAdapter);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

        switch(parent.getId()) {
            case R.id.gender_spinner:
                genderSelected = (parent.getSelectedItem()).toString();
//                genderSelected = genderChoice.getText().toString();
                break;

            case R.id.civil_status_spinner:
                civilStatusSelected = (parent.getSelectedItem()).toString();
//                civilStatusSelected = civilStatusChoice.getText().toString();
                break;

            case R.id.blood_type_spinner:
                bloodTypeSelected = (parent.getSelectedItem()).toString();
                break;

        }
    }

    private String getTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}
