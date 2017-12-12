package com.dlsu.getbetter.getbetter.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.DirectoryConstants;
import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.cryptoGB.Serializator;
import com.dlsu.getbetter.getbetter.cryptoGB.aes;
import com.dlsu.getbetter.getbetter.cryptoGB.file_aes;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.objects.Patient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import de.hdodenhof.circleimageview.CircleImageView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import static android.os.Environment.DIRECTORY_DOCUMENTS;
//import static com.dlsu.getbetter.getbetter.cryptoGB.BackProcessResponseReciever.ACTION_RESP;

public class UpdatePatientRecordActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener, DatePickerDialog.OnDateSetListener {

    private Button submitBtn;
    private Button backBtn;
    private CircleImageView profileImage;
    private Spinner genderSpinner;
    private Spinner civilStatusSpinner;
    private Spinner bloodTypeSpinner;
    private TextInputEditText firstNameInput;
    private TextInputEditText middleNameInput;
    private TextInputEditText lastNameInput;
    private TextInputEditText birthdateInput;

    private DataAdapter getBetterDb;
    private Patient patient;
    private long patientId;
    private String birthDate;
    private String genderSelected;
    private String civilStatusSelected;
    private String bloodTypeSelected;
    private String profilePicPath;

    private static final int REQUEST_IMAGE1 = 100;

    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_patient_info);

        Bundle extras = getIntent().getExtras();
        patientId = 0;

        if(extras != null) {
            patientId = extras.getLong("selectedPatient");
        }

        Log.e("patient id", patientId + "");

        initializeDatabase();
        patient = getPatientInfo(patientId);
        bindViews(this);
        initGenderAdapter(this);
        initCivilStatusAdapter(this);
        initBloodTypeAdapter(this);
        showDatePlaceholder();
        bindListeners(this);

        cryptoInit(new File("crypto.dat"));
    }

    private void bindViews(UpdatePatientRecordActivity activity) {

        activity.profileImage = (CircleImageView)activity.findViewById(R.id.profile_picture_select);
        activity.firstNameInput = (TextInputEditText)activity.findViewById(R.id.first_name_input);
        activity.middleNameInput = (TextInputEditText)activity.findViewById(R.id.middle_name_input);
        activity.lastNameInput = (TextInputEditText)activity.findViewById(R.id.last_name_input);
        activity.birthdateInput = (TextInputEditText)activity.findViewById(R.id.birthdate_input);
        activity.genderSpinner = (Spinner)activity.findViewById(R.id.gender_spinner);
        activity.civilStatusSpinner = (Spinner)activity.findViewById(R.id.civil_status_spinner);
        activity.bloodTypeSpinner = (Spinner)activity.findViewById(R.id.blood_type_spinner);
        activity.backBtn = (Button)activity.findViewById(R.id.new_patient_back_btn);
        activity.submitBtn = (Button)activity.findViewById(R.id.new_patient_next_btn);

        activity.firstNameInput.setError(null);
        activity.lastNameInput.setError(null);
        firstNameInput.setText(patient.getFirstName());
        middleNameInput.setText(patient.getMiddleName());
        lastNameInput.setText(patient.getLastName());
        profilePicPath = patient.getProfileImageBytes();
        activity.submitBtn.setText(R.string.save);
        doSomethingCryptFile("dec", new File(profilePicPath));
        setPic(profileImage, patient.getProfileImageBytes());

    }

    private void bindListeners(UpdatePatientRecordActivity activity) {

        activity.profileImage.setOnClickListener(this);
        activity.birthdateInput.setOnClickListener(this);
        activity.genderSpinner.setOnItemSelectedListener(this);
        activity.civilStatusSpinner.setOnItemSelectedListener(this);
        activity.bloodTypeSpinner.setOnItemSelectedListener(this);
        activity.backBtn.setOnClickListener(this);
        activity.submitBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if(id == R.id.new_patient_next_btn) {

            if(checkForMissingFields()) {

            } else {
                new UpdatePatientInfoTask().execute();
            }


        } else if (id == R.id.profile_picture_select) {

            takePicture();

        } else if (id == R.id.new_patient_back_btn) {

            finish();
            Intent i = new Intent(this, HomeActivity.class);
            startActivity(i);

        } else if (id == R.id.birthdate_input) {

            datePickerDialog();

        }
    }

    private void initializeDatabase () {

        getBetterDb = new DataAdapter(this);

        try {
            getBetterDb.createDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private Patient getPatientInfo(long patientId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Patient patient = getBetterDb.getPatient(patientId);

        getBetterDb.closeDatabase();

        return patient;

    }

    private int savePatientInfo() {

        String firstName = firstNameInput.getText().toString();
        String middleName = middleNameInput.getText().toString();
        String lastName = lastNameInput.getText().toString();
        int result;

        doSomethingCryptFile("enc", new File(profilePicPath));
        Patient newPatient = new Patient(firstName, middleName, lastName,
                birthDate, genderSelected, civilStatusSelected, profilePicPath);

//        Log.d("First Name", newPatient.getFirstName());
//        Log.d("Middle Name", newPatient.getMiddleName());
//        Log.d("Last Name", newPatient.getLastName());
//        Log.d("Gender", newPatient.getGender());
//        Log.d("Civil Status", newPatient.getCivilStatus());

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        result = getBetterDb.updatePatientInfo(newPatient, patientId);
        getBetterDb.closeDatabase();

        return result;

    }

    private class UpdatePatientInfoTask extends AsyncTask<String, Void, Integer> {

        ProgressDialog progressDialog = new ProgressDialog(UpdatePatientRecordActivity.this);

        @Override
        protected void onPreExecute() {
            this.progressDialog.setMessage("Updating Patient Info...");
            this.progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... strings) {

            return savePatientInfo();

        }

        @Override
        protected void onPostExecute(Integer aInt) {
            super.onPostExecute(aInt);
            if (this.progressDialog.isShowing()) {
                this.progressDialog.dismiss();
            }

            Intent intent = new Intent(UpdatePatientRecordActivity.this, ViewPatientActivity.class);
            intent.putExtra("patientId", patientId);
            startActivity(intent);
            finish();


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
                || birthDate.trim().length() <= 0 || profilePicPath.trim().length() <= 0;
    }



    private void datePickerDialog() {

        Calendar now = Calendar.getInstance();
        com.wdullaer.materialdatetimepicker.date.DatePickerDialog dpd = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(UpdatePatientRecordActivity.this,
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
    public void onDateSet(com.wdullaer.materialdatetimepicker.date.DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

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

        StringTokenizer token = new StringTokenizer(patient.getBirthdate(), "-");
        int year = Integer.parseInt(token.nextElement().toString());
        int month = Integer.parseInt(token.nextElement().toString());
        int day = Integer.parseInt(token.nextElement().toString());

//        month += 1;
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

    private void doSomethingCryptFile(String dec, File input){

        Log.w("service in", "yes");

        file_aes mastercry = new file_aes(cryptoInit(new File("crypto.dat")));
//        File path = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS),
//                DirectoryConstants.CRYPTO_FOLDER);
//        path.mkdirs();
//        File output = new File(path.getPath() +"/" + input.getName());
//        File output = new File(path.getPath() +"/" + input.getName());
//        Log.w("output", output.getAbsolutePath());
        try {
            switch (dec) {
                case "enc": {
                    mastercry.encryptFile(input);
                    Log.d("Action", "enc");
                }
                ;
                break;
                case "dec": {
                    File f = new File(getFilesDir(), input.getName());
                    if (f.createNewFile() || f.exists()) {
                        Log.w("file?", "yep");
                        if(FilenameUtils.getExtension(f.getName()).equals("jpg") ||
                                FilenameUtils.getExtension(f.getName()).equals("JPG")) {
                            Log.w("decrypt?", "image!");
                            byte[] file = mastercry.decryptFileImage(input, getContentResolver());
                            FileOutputStream fos = this.openFileOutput(f.getName(), Context.MODE_PRIVATE);
                            fos.write(file);
                            fos.close();
                        } else {
                            Log.w("decrypt?", "other!");
                            mastercry.decryptFileOther(input);
                        }
                        Log.d("Action", "dec");
                    } else Log.w("file?", "nope");
                }
                ;
                break;
            }
        } catch(IOException e){
            Log.w("error", e.toString());
        }
//
    }

    private byte[] read(File file) throws IOException{
        byte[] buffer = new byte[(int) file.length()];
        InputStream ios = null;
        try{
            ios = new FileInputStream(file);
            if(ios.read(buffer)==-1){
                throw new IOException(
                        "EOF reached while trying to read the whole file.");
            }
        } finally{
            try {
                if (ios != null) ios.close();
            } catch (IOException e){
                Log.w("error", e.getMessage());
            }
        }
        return buffer;
    }

    private aes cryptoInit(File set) {
        checkPermissions(this);
//        File set = null;
//        OutputStream in = null;
//        DataOutputStream dos = null;
        set = createFile(this, "crypto.dat");
        aes master = null;
        if(set!=null){
            try{
                master = new aes();
                master.loadKey(set);
                master.setCipher();
//                master.saveKey(master.getKey(), set);
//                in = new FileOutputStream(set);
//                dos = new DataOutputStream(in);
//                dos.write(master.getKey().getEncoded());
            } catch(Exception e){
                Log.w("error", e.getMessage());
            }
        }
        return master;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_IMAGE1 && resultCode == Activity.RESULT_OK) {
//            File path = ;
//            if(path.mkdirs()){
//                File set = createFileDuplicate(
//                        new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS),
//                                DirectoryConstants.CRYPTO_FOLDER).getPath(),
//                    FilenameUtils.getBaseName(profilePicPath),
//                        fileUri.getPath()
//                );
//                if (set!=null)
            profilePicPath = fileUri.getPath();
            setPic(profileImage, fileUri.getPath());
//                else Log.w("enc file?", "no");
//            }

        }
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

//        ActivityCompat.requestPermissions(this, new String[]{
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File img = createImageFile();
        fileUri = Uri.fromFile(img);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, REQUEST_IMAGE1);


    }

    private void setPic(CircleImageView mImageView, String mCurrentPhotoPath) {
        // Get the dimensions of the View
        int targetW = 255;//mImageView.getWidth();
        int targetH = 150;//mImageView.getHeight();

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

    private String getTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private void initGenderAdapter(UpdatePatientRecordActivity activity) {

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(activity,
                R.array.genders, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activity.genderSpinner.setAdapter(genderAdapter);
        int position = genderAdapter.getPosition(patient.getGender());
        activity.genderSpinner.setSelection(position);
    }

    private void initCivilStatusAdapter(UpdatePatientRecordActivity activity) {

        ArrayAdapter<CharSequence> civilStatusAdapter = ArrayAdapter.createFromResource(activity,
                R.array.civil_statuses, android.R.layout.simple_spinner_item);
        civilStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activity.civilStatusSpinner.setAdapter(civilStatusAdapter);
        int position = civilStatusAdapter.getPosition(patient.getCivilStatus());
        activity.civilStatusSpinner.setSelection(position);
    }

    private void initBloodTypeAdapter(UpdatePatientRecordActivity activity) {

        ArrayAdapter<CharSequence> bloodTypeAdapter = ArrayAdapter.createFromResource(activity,
                R.array.blood_type, android.R.layout.simple_spinner_item);
        bloodTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activity.bloodTypeSpinner.setAdapter(bloodTypeAdapter);
        int position = bloodTypeAdapter.getPosition(patient.getBloodType());
        activity.bloodTypeSpinner.setSelection(position);
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

    private File createFile(Context con, String newname){
        checkPermissions(this);
        File f = null;
        InputStream in;
        OutputStream out;
        boolean isFileUnlocked = false;
        try {
            f = con.getFileStreamPath(newname);
            if (!f.exists()) {
                if (f.createNewFile()) {
                    Log.w("file?", "new");
                    in = new FileInputStream(f);
                    out = new FileOutputStream(f);
                    if (IOUtils.copy(in, out)>0) {
                        Log.w("copy?", "yes");
                        out.close();
                        in.close();
                        if (f.canRead()) {
                            Log.w("read?", "yes");
                            try {
                                long lastmod = f.lastModified();
                                Log.w("last modified", Long.toString(lastmod));
                                org.apache.commons.io.FileUtils.touch(f);
                                isFileUnlocked = true;
                            } catch (IOException e) {
                                //                            isFileUnlocked = false;
                                Log.w("error", e.getMessage());
                            }
                        } else Log.w("read?", "no");
                    } else Log.w("copy?", "no");
                } else Log.w("file?", "no");
            } else Log.w("exists?", "yes");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    private File createFileDuplicate(String path, String newname, String oldfile){
        checkPermissions(this);
        File f = null;
        InputStream in;
        OutputStream out;
        boolean isFileUnlocked = false;
        try {
            f = new File(path, newname + "." +
                    FilenameUtils.getExtension(oldfile));
            if(f.createNewFile()) {
                Log.w("file?", "yes");
                in = new FileInputStream(new File(oldfile));
                out = new FileOutputStream(f);
                if (IOUtils.copy(in, out)>-1) {
                    Log.w("copy?", "yes");
                    out.close();
                    in.close();
                    if (f.canRead()) {
                        Log.w("exists?", "yes");
                        try {
                            long lastmod = f.lastModified();
                            Log.w("last modified", Long.toString(lastmod));
                            org.apache.commons.io.FileUtils.touch(f);
                            isFileUnlocked = true;
                        } catch (IOException e) {
                            //                            isFileUnlocked = false;
                            Log.w("error", e.getMessage());
                        }
                    } else Log.w("exists?", "no");
                } else Log.w("copy?", "no");
            } else {
                f = null;
                Log.w("file?", "no");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    private void checkPermissions(Context context){
        int readStuff = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeStuff = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        Log.w("read?", Integer.toString(readStuff));
//        Log.w("write?", Integer.toString(writeStuff));
        //for read stuff
        if(readStuff == PackageManager.PERMISSION_GRANTED)
            Log.w("read?", "yes");
        else if(readStuff == PackageManager.PERMISSION_DENIED)
            Log.w("read?", "no");

        //for write stuff
        if(writeStuff == PackageManager.PERMISSION_GRANTED)
            Log.w("write?", "yes");
        else if(writeStuff == PackageManager.PERMISSION_DENIED)
            Log.w("write?", "no");
    }
}
