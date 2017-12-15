package com.dlsu.getbetter.getbetter.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.adapters.FileAttachmentsAdapter;
import com.dlsu.getbetter.getbetter.cryptoGB.aes;
import com.dlsu.getbetter.getbetter.cryptoGB.file_aes;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.objects.Attachment;
import com.dlsu.getbetter.getbetter.objects.CaseRecord;
import com.dlsu.getbetter.getbetter.objects.DividerItemDecoration;
import com.dlsu.getbetter.getbetter.objects.Patient;
import com.dlsu.getbetter.getbetter.sessionmanagers.SystemSessionManager;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewCaseRecordActivity extends AppCompatActivity implements MediaController.MediaPlayerControl, View.OnClickListener {

    private static final String TAG = "ViewCaseRecordActivity";

    private TextView patientName;
    private TextView healthCenterName;
    private TextView ageGender;
    private TextView chiefComplaint;
    private TextView controlNumber;
    private CircleImageView profilePic;
    private Button backBtn;
    private Button updateCaseBtn;
    private RecyclerView attachmentList;

    private int healthCenterId;
    private CaseRecord caseRecord;
    private Patient patientInfo;
    private ArrayList<Attachment> caseAttachments;

    private DataAdapter getBetterDb;
    private MediaPlayer nMediaPlayer;
    private MediaController nMediaController;
    private Handler nHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_case_record);

        cryptoInit(new File("crypto.dat"));

        SystemSessionManager systemSessionManager = new SystemSessionManager(this);
        Bundle extras = getIntent().getExtras();
        int caseRecordId = extras.getInt("caseRecordId");
        long patientId = extras.getLong("patientId");

        HashMap<String, String> user = systemSessionManager.getUserDetails();
        HashMap<String, String> hc = systemSessionManager.getHealthCenter();
        healthCenterId = Integer.parseInt(hc.get(SystemSessionManager.HEALTH_CENTER_ID));

        killMediaPlayer();
        nMediaPlayer = new MediaPlayer();
        nMediaController = new MediaController(ViewCaseRecordActivity.this) {
            @Override
            public void hide() {

            }
        };

        nMediaController.setMediaPlayer(ViewCaseRecordActivity.this);
        nMediaController.setAnchorView(findViewById(R.id.hpi_media_player));

        initializeDatabase();
        getCaseRecord(caseRecordId);
        getCaseAttachments(caseRecordId);
        getPatientInfo(patientId);
        prepFilesDisplay();
        bindViews(this);
        bindListeners(this);
        initFileList(this);

        String recordedHpiOutputFile = getHpiOutputFile(caseRecordId);
        Log.d(TAG, "onCreate: " + recordedHpiOutputFile);
        nMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

//        doSomethingCryptFile("dec", new File(recordedHpiOutputFile));
        try {
            File f = new File(this.getFilesDir(), FilenameUtils.getName(recordedHpiOutputFile));
            nMediaPlayer.setDataSource(f.getPath());
            nMediaPlayer.prepare();
        } catch (IOException e) {
            Log.w("error", e.toString());
            Log.d(TAG, "onCreate: " + e.toString());
        }

        nMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                nHandler.post(new Runnable() {
                    public void run() {
                        nMediaController.show(0);
                        nMediaController.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                nMediaPlayer.start();
                            }
                        });

                    }
                });
            }
        });
    }

    private void bindViews(ViewCaseRecordActivity activity) {

        activity.patientName = (TextView)activity.findViewById(R.id.view_case_patient_name);
        activity.healthCenterName = (TextView)activity.findViewById(R.id.view_case_health_center);
        activity.ageGender = (TextView)activity.findViewById(R.id.view_case_age_gender);
        activity.chiefComplaint = (TextView)activity.findViewById(R.id.view_case_chief_complaint);
        activity.controlNumber = (TextView)activity.findViewById(R.id.view_case_control_number);
        activity.attachmentList = (RecyclerView)activity.findViewById(R.id.view_case_files_list);
        activity.profilePic = (CircleImageView) activity.findViewById(R.id.profile_picture_display);
        activity.backBtn = (Button)activity.findViewById(R.id.view_case_back_btn);
        activity.updateCaseBtn = (Button)activity.findViewById(R.id.update_case_record_btn);

        String fullName = patientInfo.getFirstName() + " " + patientInfo.getMiddleName() + " " + patientInfo.getLastName();
        String gender = patientInfo.getGender();
        String patientAgeGender = patientInfo.getAge() + " yrs. old, " + gender;
//        doSomethingCryptFile("dec", new File(patientInfo.getProfileImageBytes()));
        File f = new File(getFilesDir(), new File(patientInfo.getProfileImageBytes()).getName());
        setPic(profilePic, f.getPath());

        ageGender.setText(patientAgeGender);
        chiefComplaint.setText(caseRecord.getCaseRecordComplaint());
        controlNumber.setText(caseRecord.getCaseRecordControlNumber());
        patientName.setText(fullName);
        healthCenterName.setText(getHealthCenterString(healthCenterId));
        activity.updateCaseBtn.setVisibility(View.INVISIBLE);

    }

    private void bindListeners(ViewCaseRecordActivity activity) {

        activity.backBtn.setOnClickListener(activity);
        activity.updateCaseBtn.setOnClickListener(activity);
    }

    private void initFileList(ViewCaseRecordActivity activity) {

        FileAttachmentsAdapter fileAdapter = new FileAttachmentsAdapter(caseAttachments);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(activity);

        activity.attachmentList.setHasFixedSize(true);
        activity.attachmentList.setLayoutManager(layoutManager);
        activity.attachmentList.setAdapter(fileAdapter);
        activity.attachmentList.addItemDecoration(dividerItemDecoration);
        fileAdapter.SetOnItemClickListener(new FileAttachmentsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                if(caseAttachments.get(position).getAttachmentType() == 1) {
                    Intent intent = new Intent(ViewCaseRecordActivity.this, ViewImageActivity.class);
//                    doSomethingCryptFile("dec", new File(caseAttachments.get(position).getAttachmentPath()));
                    File f = new File(getFilesDir(), FilenameUtils.getName(caseAttachments.get(position).getAttachmentPath()));
                    intent.putExtra("imageUrl", f.getPath());
                    intent.putExtra("imageTitle", caseAttachments.get(position).getAttachmentDescription());
                    startActivity(intent);
                } else {
                    //do nothing
                }
            }
        });
    }

    private void initializeDatabase() {

        getBetterDb = new DataAdapter(this);

        try {
            getBetterDb.createDatabase();
        } catch(SQLException e ){
            e.printStackTrace();
        }

    }

    private void getCaseRecord(int caseRecordId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        caseRecord = getBetterDb.getCaseRecord(caseRecordId);

        getBetterDb.closeDatabase();

    }

    private void getCaseAttachments(int caseRecordId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        caseAttachments = new ArrayList<>();
        caseAttachments.addAll(getBetterDb.getCaseRecordAttachments(caseRecordId));

        getBetterDb.closeDatabase();

    }

    private void getPatientInfo(long patientId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        patientInfo = getBetterDb.getPatient(patientId);

        getBetterDb.closeDatabase();
    }

    private String getHealthCenterString(int healthCenterId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String healthCenterName = getBetterDb.getHealthCenterString(healthCenterId);

        getBetterDb.closeDatabase();

        return healthCenterName;

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if(id == R.id.view_case_back_btn) {
            //prepFilesStore();
            finish();

        } else if (id == R.id.update_case_record_btn) {

        }
    }

    private void setPic(ImageView mImageView, String mCurrentPhotoPath) {
        // Get the dimensions of the View
        int targetW = 255;//mImageView.getWidth();
        int targetH = 200;// mImageView.getHeight();

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

        try {
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(mCurrentPhotoPath));
            mImageView.setImageBitmap(bitmap);
        } catch(FileNotFoundException ex){
            Log.w("error", ex.toString());
        }
    }

    private String getHpiOutputFile(int caseRecordId) {

        try {
            getBetterDb.openDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String result;

        result = getBetterDb.getHPI(caseRecordId);

        getBetterDb.closeDatabase();

        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        nMediaController.hide();
        killMediaPlayer();
    }

    private void killMediaPlayer() {
        if(nMediaPlayer != null) {
            try{
                nMediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public int getBufferPercentage() {

        return (nMediaPlayer.getCurrentPosition() * 100) / nMediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return nMediaPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return nMediaPlayer.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return nMediaPlayer.isPlaying();
    }

    @Override
    public void pause() {
        if(nMediaPlayer.isPlaying())
            nMediaPlayer.pause();
    }

    @Override
    public void seekTo(int pos) {
        nMediaPlayer.seekTo(pos);
    }

    @Override
    public void start() {
        nMediaPlayer.start();
    }

    //decrypt files for display
    private void prepFilesDisplay(){
//        doSomethingCryptFile("dec", new File(patientInfo.getProfileImageBytes()));
        doSomethingCryptFile("dec", new File(getHpiOutputFile(getIntent().getExtras().getInt("caseRecordId"))));
        for (int i=0; i<caseAttachments.size(); i++){
            doSomethingCryptFile("dec", new File(caseAttachments.get(i).getAttachmentPath()));
        }
//        doSomethingCryptFile("dec", new File(patientProfileImage));
//        if(attachments.size()>0){
//            for(int i=0; i<attachments.size(); i++)
//                doSomethingCryptFile("dec", new File(attachments.get(i).getAttachmentPath()));
//        }
    }

    //encrypt files for storage
    private void prepFilesStore(){
//        doSomethingCryptFile("enc", new File(patientInfo.getProfileImageBytes()));
        doSomethingCryptFile("enc", new File(getHpiOutputFile(getIntent().getExtras().getInt("caseRecordId"))));
        for (int i=0; i<caseAttachments.size(); i++){
            doSomethingCryptFile("enc", new File(caseAttachments.get(i).getAttachmentPath()));
        }
//        doSomethingCryptFile("enc", new File(patientProfileImage));
//        if(attachments.size()>0){
//            for(int i=0; i<attachments.size(); i++)
//                doSomethingCryptFile("enc", new File(attachments.get(i).getAttachmentPath()));
//        }
    }

    private void doSomethingCryptFile(String dec, File input) {

        Log.w("service in", "yes");

        file_aes mastercry = new file_aes(cryptoInit(new File("crypto.dat")));
        File f = new File(getFilesDir(), input.getName());
        try {
//        File path = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS),
//                DirectoryConstants.CRYPTO_FOLDER);
//        path.mkdirs();
//        File output = new File(path.getPath() +"/" + input.getName());
//        File output = new File(path.getPath() +"/" + input.getName());
//        Log.w("output", output.getAbsolutePath());
//        try {
//            FileOutputStream fos = new FileOutputStream(input);
//            fos.write(read(input));
//            fos.flush();
//            fos.close();
//        } catch(Exception e){
//            Log.w("error", e.toString());
//        }
            switch (dec) {
                case "enc": {
                    mastercry.encryptFile(input);
                    Log.d("Action", "enc");
                }
                ;
                break;
                case "dec": {
                    if (f.createNewFile() && !f.exists()) {
                        Log.w("file?", "yep");
                        byte[] file = mastercry.decryptFile(input);
                        FileOutputStream fos = this.openFileOutput(f.getName(), Context.MODE_PRIVATE);
                        fos.write(file);
                        fos.close();
                        Log.d("Action", "dec");
                    } else Log.w("file?", "nope");
                }
                ;
                break;
            }
//        } else Log.w("error", "no file");
        } catch (Exception e) {
            Log.w("error", e.toString());
        }
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
