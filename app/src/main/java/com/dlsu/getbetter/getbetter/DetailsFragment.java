package com.dlsu.getbetter.getbetter;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.activities.ViewCaseRecordActivity;
import com.dlsu.getbetter.getbetter.cryptoGB.aes;
import com.dlsu.getbetter.getbetter.cryptoGB.file_aes;
import com.dlsu.getbetter.getbetter.database.DataAdapter;
import com.dlsu.getbetter.getbetter.objects.Attachment;
import com.dlsu.getbetter.getbetter.objects.CaseRecord;
import com.dlsu.getbetter.getbetter.objects.Patient;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDate;
import org.joda.time.Years;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringTokenizer;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsFragment extends Fragment implements MediaController.MediaPlayerControl {

    private DataAdapter getBetterDb;
    private MediaPlayer nMediaPlayer;
    private MediaController nMediaController;
    private Handler nHandler = new Handler();

    private CaseRecord caseRecord;
    private ImageView profilePic;
    private Patient patientInfo;
    private ArrayList<Attachment> caseAttachments;


    public DetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeDatabase();
        int caseRecordId;

        if(getArguments().containsKey("case record id")) {
            caseRecordId = getArguments().getInt("case record id");
            getCaseDetails(caseRecordId);
            getCaseAttachments(caseRecordId);
            prepFilesDisplay();
            prepareMediaPlayer();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_view_updated_case_record, container, false);
        TextView patientName = (TextView)rootView.findViewById(R.id.detail_patient_name);
        TextView ageGender = (TextView)rootView.findViewById(R.id.detail_age_gender);
        TextView chiefComplaint = (TextView)rootView.findViewById(R.id.detail_chief_complaint);
        TextView controlNumber = (TextView)rootView.findViewById(R.id.detail_control_number);
        TextView additionalNotes = (TextView)rootView.findViewById(R.id.detail_instruction);
        Button viewUpdatedCaseBtn = (Button)rootView.findViewById(R.id.detail_view_case_btn);
        profilePic = (ImageView)rootView.findViewById(R.id.detail_picture_display);

        nMediaController.setMediaPlayer(DetailsFragment.this);
        nMediaController.setAnchorView(rootView.findViewById(R.id.detail_hpi_media_player));

        int[] birthdateTemp = new int[3];
        String patientAgeGender = "";
        String gender = patientInfo.getGender();

        if(patientInfo.getBirthdate() != null) {

            StringTokenizer tok = new StringTokenizer(patientInfo.getBirthdate(), "-");
            int i = 0;
            while(tok.hasMoreTokens()) {

                birthdateTemp[i] = Integer.parseInt(tok.nextToken());
                i++;
            }

            LocalDate birthdate = new LocalDate(birthdateTemp[0], birthdateTemp[1], birthdateTemp[2]);
            LocalDate now = new LocalDate();

            Years age = Years.yearsBetween(birthdate, now);

            String patientAge = age.getYears() + "";
            patientAgeGender = patientAge + " yrs. old, " + gender;
        }

        if (ageGender != null) {
            ageGender.setText(patientAgeGender);
        }

        patientName.setText(caseRecord.getPatientName());
        chiefComplaint.setText(caseRecord.getCaseRecordComplaint());
        controlNumber.setText(caseRecord.getCaseRecordControlNumber());
        additionalNotes.setText(caseRecord.getCaseRecordAdditionalNotes());

        viewUpdatedCaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ViewCaseRecordActivity.class);
                intent.putExtra("caseRecordId", caseRecord.getCaseRecordId());
                intent.putExtra("patientId", (long)caseRecord.getUserId());
                startActivity(intent);
            }
        });

        startMediaPlayer();


        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        prepFilesDisplay();
        setPic(profilePic, caseRecord.getProfilePic());
    }

    private void initializeDatabase () {

        getBetterDb = new DataAdapter(this.getContext());

        try {
            getBetterDb.createDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getCaseDetails(int caseRecordId) {

        try {
            getBetterDb.openDatabase();
        }catch (SQLException e) {
            e.printStackTrace();
        }

        caseRecord = getBetterDb.getCaseRecord(caseRecordId);

        patientInfo = getBetterDb.getPatient((long) caseRecord.getUserId());
        String patientName = patientInfo.getFirstName() + " " + patientInfo.getLastName();
        caseRecord.setPatientName(patientName);
        caseRecord.setProfilePic(patientInfo.getProfileImageBytes());

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

    private String getHpiOutputFile() {

        String result = "";

        if(caseAttachments.isEmpty()) {
            Log.e("attachments is empty", "true");
        } else {
            for(int i = 0; i < caseAttachments.size(); i++) {

                if(caseAttachments.get(i).getAttachmentType() == 5) {
                    result = caseAttachments.get(i).getAttachmentPath();
                }
            }
        }
        return result;
    }

    private void setPic(ImageView mImageView, String mCurrentPhotoPath) {
        // Get the dimensions of the View
        int targetW = 100;
        int targetH = 100;

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

    private void startMediaPlayer() {

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

    private void prepareMediaPlayer() {

        killMediaPlayer();
        String recordedHPIOutputFile = getHpiOutputFile();
        nMediaPlayer = new MediaPlayer();
        nMediaController = new MediaController(this.getContext()) {
            @Override
            public void hide() {

            }
        };
        //Uri hpiRecordingUri = Uri.parse(recordedHpiOutputFile);

        nMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            nMediaPlayer.setDataSource(recordedHPIOutputFile);
            nMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void killMediaPlayer() {
        nMediaController.hide();
        if(nMediaPlayer != null) {
            try{
                nMediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        killMediaPlayer();
        prepFilesStore();
    }

    @Override
    public void onStop() {
        super.onStop();
        killMediaPlayer();
        prepFilesStore();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        killMediaPlayer();
        prepFilesStore();
    }

    @Override
    public void start() {
        nMediaPlayer.start();
    }

    @Override
    public void pause() {
        if(nMediaPlayer.isPlaying())
            nMediaPlayer.pause();
    }

    @Override
    public int getDuration() {
        return nMediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return nMediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        nMediaPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return nMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return (nMediaPlayer.getCurrentPosition() * 100) / nMediaPlayer.getDuration();
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

    //decrypt files for display
    private void prepFilesDisplay(){
        doSomethingCryptFile("dec", new File(caseRecord.getProfilePic()));
        if (caseAttachments.size()>0){
            for(int i=0; i<caseAttachments.size(); i++){
                doSomethingCryptFile("dec", new File(caseAttachments.get(i).getAttachmentPath()));
            }
        }
    }

    //encrypt files for storage
    private void prepFilesStore(){
        doSomethingCryptFile("enc", new File(caseRecord.getProfilePic()));
        if (caseAttachments.size()>0){
            for(int i=0; i<caseAttachments.size(); i++){
                doSomethingCryptFile("enc", new File(caseAttachments.get(i).getAttachmentPath()));
            }
        }
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
            FileOutputStream fos = new FileOutputStream(input);
            fos.write(read(input));
            fos.flush();
            fos.close();
        } catch(Exception e){
            Log.w("error", e.toString());
        }
        switch(dec){
            case "enc":{
                mastercry.encryptFile(input);
                Log.d("Action", "enc");
            }; break;
            case "dec":{
                mastercry.decryptFile(input);
                Log.d("Action", "dec");
            }; break;
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
        checkPermissions(this.getContext());
//        File set = null;
//        OutputStream in = null;
//        DataOutputStream dos = null;
        set = createFile(this.getContext(), "crypto.dat");
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
        checkPermissions(con);
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
        checkPermissions(this.getContext());
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
