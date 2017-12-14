package com.dlsu.getbetter.getbetter.activities;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.cryptoGB.aes;
import com.dlsu.getbetter.getbetter.cryptoGB.file_aes;
import com.dlsu.getbetter.getbetter.sessionmanagers.NewPatientSessionManager;
import com.dlsu.getbetter.getbetter.sessionmanagers.SystemSessionManager;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class CaptureDocumentsActivity extends AppCompatActivity implements View.OnClickListener {

    private static CaptureDocumentsActivity captureDocumentsActivity;
    private static final String TAG = "Capture";

    private ImageView patientInfoImage;
    private ImageView chiefComplaintImage;
    private ImageView familySocialImage;
    private Button viewPatientInfoImage;
    private Button removePatientInfoImage;
    private Button viewChiefComplaintImage;
    private Button removeChiefComplaintImage;
    private Button viewSocialFamilyImage;
    private Button removeSocialFamilyImage;
    private Button capturePatientInfo;
    private Button captureChiefComplaint;
    private Button captureFamilySocial;
    private Button backButton;
    private Button nextButton;
    private LinearLayout patientInfoActionButtons;
    private LinearLayout chiefComplaintActionButtons;
    private LinearLayout socialFamilyActionButtons;
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;
    private String patientInfoImagePath = "";
    private String familySocialHistoryImagePath = "";
    private String chiefComplaintImagePath = "";
    private NewPatientSessionManager newPatientSessionManager;

    private static final String PATIENT_INFO_FORM_TITLE = "Patient Information Form";
    private static final String FAMILY_SOCIAL_HISTORY_FORM_TITLE = "Family and Social History Form";
    private static final String CHIEF_COMPLAINT_FORM_TITLE = "Chief Complaint Form";

    private static final String PATIENT_INFO_FORM_FILENAME = "patientinfo";
    private static final String FAMILY_SOCIAL_HISTORY_FORM_FILENAME = "familysocialhistory";
    private static final String CHIEF_COMPLAINT_FORM_FILENAME = "chiefcomplaint";

    private static final int REQUEST_PATIENT_INFO_IMAGE = 100;
    private static final int REQUEST_CHIEF_COMPLAINT_IMAGE = 200;
    private static final int REQUEST_FAMILY_SOCIAL_IMAGE = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_documents);

        SystemSessionManager systemSessionManager = new SystemSessionManager(this);
        if(systemSessionManager.checkLogin())
            finish();

        captureDocumentsActivity = this;

        newPatientSessionManager = new NewPatientSessionManager(this);

        bindViews(this);
        bindListeners(this);

        if(!newPatientSessionManager.isDocumentsEmpty()) {
            Log.d(TAG, "if fired");
            HashMap<String, String> documents = newPatientSessionManager.getPatientInfo();
            this.patientInfoImagePath = documents.get(NewPatientSessionManager.NEW_PATIENT_DOC_IMAGE1);
            this.familySocialHistoryImagePath = documents.get(NewPatientSessionManager.NEW_PATIENT_DOC_IMAGE2);
            this.chiefComplaintImagePath = documents.get(NewPatientSessionManager.NEW_PATIENT_DOC_IMAGE3);
            prepFilesDisplay();
            File f = new File(getFilesDir(), new File(this.patientInfoImagePath).getName());
            setPic(this.patientInfoImage, f.getPath());
            f = new File(getFilesDir(), new File(this.familySocialHistoryImagePath).getName());
            setPic(this.familySocialImage, f.getPath());
            f = new File(getFilesDir(), new File(this.chiefComplaintImagePath).getName());
            setPic(this.chiefComplaintImage, f.getPath());
            this.captureChiefComplaint.setVisibility(View.GONE);
            this.capturePatientInfo.setVisibility(View.GONE);
            this.captureFamilySocial.setVisibility(View.GONE);
            this.patientInfoActionButtons.setVisibility(View.VISIBLE);
            this.chiefComplaintActionButtons.setVisibility(View.VISIBLE);
            this.socialFamilyActionButtons.setVisibility(View.VISIBLE);
        }

        cryptoInit(new File("crypto.dat"));

    }

    private void bindViews(CaptureDocumentsActivity activity) {
        activity.patientInfoImage = (ImageView)activity.findViewById(R.id.patient_info_image);
        activity.chiefComplaintImage = (ImageView)activity.findViewById(R.id.chief_complaint_image);
        activity.familySocialImage = (ImageView)activity.findViewById(R.id.family_social_history_image);
        activity.capturePatientInfo = (Button)activity.findViewById(R.id.capture_docu_patient_info_image);
        activity.captureChiefComplaint = (Button)activity.findViewById(R.id.capture_docu_chief_complaint_image);
        activity.captureFamilySocial = (Button)activity.findViewById(R.id.capture_docu_family_social_history_image);
        activity.viewPatientInfoImage = (Button)activity.findViewById(R.id.capture_docu_view_patient_info_image);
        activity.viewChiefComplaintImage = (Button)activity.findViewById(R.id.capture_docu_view_chief_complaint_image);
        activity.viewSocialFamilyImage = (Button)activity.findViewById(R.id.capture_docu_view_family_social_image);
        activity.removePatientInfoImage = (Button)activity.findViewById(R.id.capture_docu_remove_patient_info_image);
        activity.removeChiefComplaintImage = (Button)activity.findViewById(R.id.capture_docu_remove_chief_complaint_image);
        activity.removeSocialFamilyImage = (Button)activity.findViewById(R.id.capture_docu_remove_family_social_image);
        activity.backButton = (Button)activity.findViewById(R.id.capture_document_back_btn);
        activity.nextButton = (Button)activity.findViewById(R.id.capture_document_next_btn);
        activity.patientInfoActionButtons = (LinearLayout)findViewById(R.id.patient_info_image_action_buttons);
        activity.chiefComplaintActionButtons = (LinearLayout)findViewById(R.id.chief_complaint_image_action_buttons);
        activity.socialFamilyActionButtons = (LinearLayout)findViewById(R.id.family_social_image_action_buttons);
        activity.patientInfoActionButtons.setVisibility(View.GONE);
        activity.chiefComplaintActionButtons.setVisibility(View.GONE);
        activity.socialFamilyActionButtons.setVisibility(View.GONE);
    }

    private void bindListeners(CaptureDocumentsActivity activity) {
        activity.capturePatientInfo.setOnClickListener(activity);
        activity.captureChiefComplaint.setOnClickListener(activity);
        activity.captureFamilySocial.setOnClickListener(activity);
        activity.viewPatientInfoImage.setOnClickListener(activity);
        activity.viewChiefComplaintImage.setOnClickListener(activity);
        activity.viewSocialFamilyImage.setOnClickListener(activity);
        activity.removePatientInfoImage.setOnClickListener(activity);
        activity.removeChiefComplaintImage.setOnClickListener(activity);
        activity.removeSocialFamilyImage.setOnClickListener(activity);
        activity.backButton.setOnClickListener(activity);
        activity.nextButton.setOnClickListener(activity);
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        if(id == R.id.capture_docu_patient_info_image) {

            this.patientInfoImagePath = captureDocument(PATIENT_INFO_FORM_FILENAME,
                    REQUEST_PATIENT_INFO_IMAGE);

        } else if(id == R.id.capture_docu_chief_complaint_image) {

            this.chiefComplaintImagePath = captureDocument(CHIEF_COMPLAINT_FORM_FILENAME,
                    REQUEST_CHIEF_COMPLAINT_IMAGE);

        } else if(id == R.id.capture_docu_family_social_history_image) {

           this.familySocialHistoryImagePath = captureDocument(FAMILY_SOCIAL_HISTORY_FORM_FILENAME,
                   REQUEST_FAMILY_SOCIAL_IMAGE);

        } else if(id == R.id.capture_docu_view_patient_info_image) {

//            zoomImageFromThumb(viewPatientInfoImage, patientInfoImagePath);
            Intent intent = new Intent(this, ViewImageActivity.class);
            intent.putExtra("imageUrl", this.patientInfoImagePath);
            intent.putExtra("imageTitle", PATIENT_INFO_FORM_TITLE);
            startActivity(intent);


        } else if(id == R.id.capture_docu_view_chief_complaint_image) {

//            zoomImageFromThumb(viewChiefComplaintImage, chiefComplaintImagePath);
            Intent intent = new Intent(this, ViewImageActivity.class);
            intent.putExtra("imageUrl", this.chiefComplaintImagePath);
            intent.putExtra("imageTitle", CHIEF_COMPLAINT_FORM_TITLE);
            startActivity(intent);

        } else if(id == R.id.capture_docu_view_family_social_image) {

//            zoomImageFromThumb(viewSocialFamilyImage, familySocialHistoryImagePath);
            Intent intent = new Intent(this, ViewImageActivity.class);
            intent.putExtra("imageUrl", this.familySocialHistoryImagePath);
            intent.putExtra("imageTitle", FAMILY_SOCIAL_HISTORY_FORM_TITLE);
            startActivity(intent);

        } else if(id == R.id.capture_docu_remove_patient_info_image) {

            removeImageDialog(REQUEST_PATIENT_INFO_IMAGE);

        } else if(id == R.id.capture_docu_remove_chief_complaint_image) {

            removeImageDialog(REQUEST_CHIEF_COMPLAINT_IMAGE);

        } else if(id == R.id.capture_docu_remove_family_social_image) {

            removeImageDialog(REQUEST_FAMILY_SOCIAL_IMAGE);

        } else if(id == R.id.capture_document_back_btn) {

            Intent intent = new Intent(this, ExistingPatientActivity.class);
//            Intent intent = new Intent(this, ViewPatientActivity.class);
//            Log.d(TAG, "" + newPatientSessionManager.getPatientInfo().get(NewPatientSessionManager.PATIENT_ID));
//            intent.putExtra("patientId", newPatientSessionManager.getPatientInfo().get(NewPatientSessionManager.PATIENT_ID));
            newPatientSessionManager.endSession();
            startActivity(intent);

            finish();

        } else if(id == R.id.capture_document_next_btn) {

            if(this.patientInfoImagePath.isEmpty() || this.chiefComplaintImagePath.isEmpty() ||
                    this.familySocialHistoryImagePath.isEmpty()) {

                //  todo show snackbar alert missing documents
                Toast.makeText(captureDocumentsActivity, "Please take a photo of all the required documents.", Toast.LENGTH_SHORT).show();

            } else {

                newPatientSessionManager.setDocImages(this.patientInfoImagePath, this.familySocialHistoryImagePath, this.chiefComplaintImagePath,
                        PATIENT_INFO_FORM_TITLE, FAMILY_SOCIAL_HISTORY_FORM_TITLE, CHIEF_COMPLAINT_FORM_TITLE);
                prepFilesStore();
                Intent intent = new Intent(this, RecordHpiActivity.class);
                startActivity(intent);

            }
        }

    }

    private String captureDocument(String fileName, int requestId) { //encrypt here

        File imageFile = null;
        String imagePath = "null";

        try {
            try {
                imageFile = createImageFile(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(imageFile != null) {
                takePicture(requestId, imageFile);
                imagePath = imageFile.getAbsolutePath();
            }

        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }

        return imagePath;
    }

    private File createImageFile(String imageTitle) throws IOException {


        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date());
        String imageFileName = imageTitle + "_" + timeStamp;

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);


        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void takePicture(int requestImage, File imageFile) {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (imageFile != null) {

            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
            startActivityForResult(intent, requestImage);
            Log.e("image path", imageFile.getAbsolutePath());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == Activity.RESULT_OK) {

            switch (requestCode) {

                case REQUEST_PATIENT_INFO_IMAGE: {

                    setPic(this.patientInfoImage, this.patientInfoImagePath);
                    this.patientInfoActionButtons.setVisibility(View.VISIBLE);
                    this.capturePatientInfo.setVisibility(View.GONE);
//                    doSomethingCryptFile("enc", new File(this.patientInfoImagePath));
                    Log.d("patientinfoimgenc", "yes");
                    break;
                }

                case REQUEST_CHIEF_COMPLAINT_IMAGE: {

                    setPic(this.chiefComplaintImage, this.chiefComplaintImagePath);
                    this.chiefComplaintActionButtons.setVisibility(View.VISIBLE);
                    this.captureChiefComplaint.setVisibility(View.GONE);
//                    doSomethingCryptFile("enc", new File(this.chiefComplaintImagePath));
                    Log.d("chiefcompimgenc", "yes");
                    break;
                }

                case REQUEST_FAMILY_SOCIAL_IMAGE: {

                    setPic(this.familySocialImage, this.familySocialHistoryImagePath);
                    this.socialFamilyActionButtons.setVisibility(View.VISIBLE);
                    this.captureFamilySocial.setVisibility(View.GONE);
//                    doSomethingCryptFile("enc", new File(this.familySocialHistoryImagePath));
                    Log.d("famhistimgenc", "yes");
                    break;
                }
            }
        }
    }

    private void setPic(ImageView mImageView, String mCurrentPhotoPath) {
        // Get the dimensions of the View
        int targetW = 300;//mImageView.getWidth();
        int targetH = 220;//mImageView.getHeight();

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

    private void removeImageDialog (final int type) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("REMOVE IMAGE");
        builder.setMessage("Are you sure you want to remove the image?");

        // Set up the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(type == REQUEST_PATIENT_INFO_IMAGE) {

                    patientInfoImage.setImageResource(R.drawable.ic_insert_photo);
                    patientInfoImagePath = "";
                    patientInfoActionButtons.setVisibility(View.GONE);
                    capturePatientInfo.setVisibility(View.VISIBLE);

                } else if (type == REQUEST_CHIEF_COMPLAINT_IMAGE) {

                    chiefComplaintImage.setImageResource(R.drawable.ic_insert_photo);
                    chiefComplaintImagePath = "";
                    chiefComplaintActionButtons.setVisibility(View.GONE);
                    captureChiefComplaint.setVisibility(View.VISIBLE);

                } else if (type == REQUEST_FAMILY_SOCIAL_IMAGE) {

                    familySocialImage.setImageResource(R.drawable.ic_insert_photo);
                    familySocialHistoryImagePath = "";
                    socialFamilyActionButtons.setVisibility(View.GONE);
                    captureFamilySocial.setVisibility(View.VISIBLE);

                }
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public static CaptureDocumentsActivity getInstance() {
        return captureDocumentsActivity;
    }

    //decrypt files for display
    private void prepFilesDisplay(){
        HashMap<String, String> documents = newPatientSessionManager.getPatientInfo();
        if (!newPatientSessionManager.isDocumentsEmpty()){
            doSomethingCryptFile("dec", new File(documents.get(NewPatientSessionManager.NEW_PATIENT_DOC_IMAGE1)));
            doSomethingCryptFile("dec", new File(documents.get(NewPatientSessionManager.NEW_PATIENT_DOC_IMAGE2)));
            doSomethingCryptFile("dec", new File(documents.get(NewPatientSessionManager.NEW_PATIENT_DOC_IMAGE3)));
        }
//        doSomethingCryptFile("dec", new File(patientProfileImage));
//        if(attachments.size()>0){
//            for(int i=0; i<attachments.size(); i++)
//                doSomethingCryptFile("dec", new File(attachments.get(i).getAttachmentPath()));
//        }
    }

    //encrypt files for storage
    private void prepFilesStore(){
        HashMap<String, String> documents = newPatientSessionManager.getPatientInfo();
        if (!newPatientSessionManager.isDocumentsEmpty()){
            doSomethingCryptFile("enc", new File(documents.get(NewPatientSessionManager.NEW_PATIENT_DOC_IMAGE1)));
            doSomethingCryptFile("enc", new File(documents.get(NewPatientSessionManager.NEW_PATIENT_DOC_IMAGE2)));
            doSomethingCryptFile("enc", new File(documents.get(NewPatientSessionManager.NEW_PATIENT_DOC_IMAGE3)));
        }
//        doSomethingCryptFile("enc", new File(patientProfileImage));
//        if(attachments.size()>0){
//            for(int i=0; i<attachments.size(); i++)
//                doSomethingCryptFile("enc", new File(attachments.get(i).getAttachmentPath()));
//        }
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

    private void doSomethingCryptFile(String dec, File input){

        Log.w("service in", "yes");

        file_aes mastercry = new file_aes(cryptoInit(new File("crypto.dat")));
        File f = new File(getFilesDir(), input.getName());
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
        try{
            switch (dec) {
                case "enc": {
                    //mastercry.encryptFile(input);
                    Log.d("Action", "enc");
                }
                ;
                break;
                case "dec": {
                    if (f.createNewFile() || f.exists()) {
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
        } catch(Exception e){
            Log.w("error", e.toString());
        }
//
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

//    private aes cryptoInit(){
////        Serializator str = new Serializator();
//        checkPermissions(this);
//        File set = null;
//        aes mstr = null;
//        set = createFile(this, "datadb.dat");
//        if(set!=null){
//            mstr = Serializator.deserialize(set.getPath(), aes.class);
//            Log.w("crypto", Boolean.toString(mstr!=null));
//            Log.w("key", String.valueOf(mstr.getKey().getEncoded()));
//            Log.w("cipher", Boolean.toString(mstr.getCipher()!=null));
//        }
//        return mstr;
//    }

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
