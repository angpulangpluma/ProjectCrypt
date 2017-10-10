package com.dlsu.getbetter.getbetter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.dlsu.getbetter.getbetter.sessionmanagers.NewPatientSessionManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class CaptureDocumentsFragment extends Fragment implements View.OnClickListener {

    private ImageView captureBasicInfoBtn;
    private ImageView captureFamilyHistBtn;
    private ImageView captureChiefComplaintBtn;

    private static final int REQUEST_IMAGE1 = 100;
    private static final int REQUEST_IMAGE2 = 200;
    private static final int REQUEST_IMAGE3 = 300;

    private static final String PATIENT_INFO_FORM_TITLE = "Patient Information Form";
    private static final String FAMILY_SOCIAL_HISTORY_FORM_TITLE = "Family and Social History Form";
    private static final String CHIEF_COMPLAINT_FORM_TITLE = "Chief Complaint Form";

    private static final String PATIENT_INFO_FORM_FILENAME = "patientinfoform";
    private static final String FAMILY_SOCIAL_HISTORY_FORM_FILENAME = "familysocialhistoryform";
    private static final String CHIEF_COMPLAINT_FORM_FILENAME = "chiefcomplaintform";

    private String patientInfoImagePath;
    private String familySocialHistoryImagePath;
    private String chiefComplaintImagePath;

    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private Animator mCurrentAnimator;

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private int mShortAnimationDuration;

    private NewPatientSessionManager newPatientSessionManager;

    public CaptureDocumentsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        newPatientSessionManager = new NewPatientSessionManager(getActivity());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_capture_documents, container, false);

        captureBasicInfoBtn = (ImageView)rootView.findViewById(R.id.capture_docu_patient_info_image);
        captureFamilyHistBtn = (ImageView)rootView.findViewById(R.id.capture_docu_family_social_history_image);
        captureChiefComplaintBtn = (ImageView)rootView.findViewById(R.id.capture_docu_chief_complaint_image);
        final Button viewBasicInfoImage = (Button) rootView.findViewById(R.id.capture_docu_view_patient_info_image);
        final Button viewFamilySocialImage = (Button) rootView.findViewById(R.id.capture_docu_view_family_social_image);
        final Button viewChiefCompImage = (Button) rootView.findViewById(R.id.capture_docu_view_chief_complaint_image);

        captureBasicInfoBtn.setOnClickListener(this);
        captureFamilyHistBtn.setOnClickListener(this);
        captureChiefComplaintBtn.setOnClickListener(this);
        viewBasicInfoImage.setOnClickListener(this);
        viewFamilySocialImage.setOnClickListener(this);
        viewChiefCompImage.setOnClickListener(this);

        Button nextBtn = (Button)rootView.findViewById(R.id.capture_docu_fragment_next_btn);
        Button backBtn = (Button)rootView.findViewById(R.id.capture_document_fragment_back_btn);

        nextBtn.setOnClickListener(this);
        viewBasicInfoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(patientInfoImagePath == null) {
                    //do nothing
                    Toast.makeText(getContext(), "No image to view.", Toast.LENGTH_LONG).show();
                }else {
                    zoomImageFromThumb(viewBasicInfoImage, patientInfoImagePath, rootView);
                }

            }
        });
        viewFamilySocialImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(familySocialHistoryImagePath == null) {
                    Toast.makeText(getContext(), "No image to view.", Toast.LENGTH_LONG).show();
                }else {
                    zoomImageFromThumb(viewFamilySocialImage, familySocialHistoryImagePath, rootView);
                }

            }
        });
        viewChiefCompImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(chiefComplaintImagePath == null) {
                    Toast.makeText(getContext(), "No image to view.", Toast.LENGTH_LONG).show();
                } else {
                    zoomImageFromThumb(viewChiefCompImage, chiefComplaintImagePath, rootView);
                }

            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == Activity.RESULT_OK) {

            switch (requestCode) {

                case REQUEST_IMAGE1:

                    setPic(captureBasicInfoBtn, patientInfoImagePath);
//                    Bitmap photo = BitmapFactory.decodeFile(data.getExtras().get("output").toString());
//                    captureBasicInfoBtn.setImageBitmap(photo);
//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                    if (photo != null) {
//                        photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                    }
//                    byte[] b = baos.toByteArray();
//
//                    encodedImage1 = Base64.encodeToString(b, Base64.DEFAULT);
//                    Log.d("image byte", encodedImage1 + "");


                    break;

                case REQUEST_IMAGE2:

                    setPic(captureFamilyHistBtn, familySocialHistoryImagePath);
//                    Bitmap photo2 = (Bitmap)data.getExtras().get("data");
//                    captureFamilyHistBtn.setImageBitmap(photo2);
//                    ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
//                    if (photo2 != null) {
//                        photo2.compress(Bitmap.CompressFormat.JPEG, 100, baos2);
//                    }
//                    byte[] b2 = baos2.toByteArray();
//
//                    encodedImage2 = Base64.encodeToString(b2, Base64.DEFAULT);
//                    Log.d("image byte", encodedImage2 + "");


                    break;

                case REQUEST_IMAGE3:

                    setPic(captureChiefComplaintBtn, chiefComplaintImagePath);
//                    Bitmap photo3 = (Bitmap)data.getExtras().get("data");
//                    captureChiefComplaintBtn.setImageBitmap(photo3);
//                    ByteArrayOutputStream baos3 = new ByteArrayOutputStream();
//                    if (photo3 != null) {
//                        photo3.compress(Bitmap.CompressFormat.JPEG, 100, baos3);
//                    }
//                    byte[] b3 = baos3.toByteArray();
//
//                    encodedImage3 = Base64.encodeToString(b3, Base64.DEFAULT);
//                    Log.d("image byte", encodedImage3 + "");


                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        File imageFile = null;

        switch(id) {
            case R.id.capture_docu_fragment_next_btn:

                if(chiefComplaintImagePath.isEmpty()) {

                    Toast.makeText(this.getContext(), "Please Take a Photo of the three forms", Toast.LENGTH_LONG).show();

                } else {

                    newPatientSessionManager.setDocImages(patientInfoImagePath, familySocialHistoryImagePath, chiefComplaintImagePath,
                            PATIENT_INFO_FORM_TITLE, FAMILY_SOCIAL_HISTORY_FORM_TITLE, CHIEF_COMPLAINT_FORM_TITLE);
                    RecordHpiFragment recordHpiFragment = new RecordHpiFragment();
                    getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).commit();
                    getActivity().getSupportFragmentManager().beginTransaction().
                            replace(R.id.fragment_container, recordHpiFragment).commit();
                }

                break;

            case R.id.capture_docu_patient_info_image:
                try {

                    try {
                        imageFile = createImageFile(PATIENT_INFO_FORM_FILENAME);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (imageFile != null) {
                        patientInfoImagePath = imageFile.getAbsolutePath();
                    }
                    Log.e("patient info image path", patientInfoImagePath);
                    takePicture(REQUEST_IMAGE1, imageFile);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.capture_docu_family_social_history_image:
                try {

                    try {
                        imageFile = createImageFile(FAMILY_SOCIAL_HISTORY_FORM_FILENAME);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if(imageFile != null) {
                        familySocialHistoryImagePath = imageFile.getAbsolutePath();
                    }

                    takePicture(REQUEST_IMAGE2, imageFile);
                } catch (ActivityNotFoundException e) {
                  e.printStackTrace();
                }
                break;

            case R.id.capture_docu_chief_complaint_image:
                try {

                    try {
                        imageFile = createImageFile(CHIEF_COMPLAINT_FORM_FILENAME);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if(imageFile != null) {
                        chiefComplaintImagePath = imageFile.getAbsolutePath();
                    }

                    takePicture(REQUEST_IMAGE3, imageFile);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                break;

        }

    }

    private File createImageFile(String imageTitle) throws IOException { //encrypt here


        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
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

    private void setPic(ImageView mImageView, String mCurrentPhotoPath) {
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

    private void zoomImageFromThumb(final View thumbView, String photoPath, View rootView) {

        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView)rootView.findViewById(
                R.id.expanded_image);

//        int targetW = expandedImageView.getWidth();
//        int targetH = expandedImageView.getHeight();
//
//        // Get the dimensions of the bitmap
//        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//        bmOptions.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(photoPath, bmOptions);
//        int photoW = bmOptions.outWidth;
//        int photoH = bmOptions.outHeight;
//
//        // Determine how much to scale down the image
//        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
//
//        // Decode the image file into a Bitmap sized to fill the View
//        bmOptions.inJustDecodeBounds = false;
//        bmOptions.inSampleSize = scaleFactor;
//        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(photoPath);

        expandedImageView.setImageBitmap(bitmap);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        rootView.findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }

}
