package com.dlsu.getbetter.getbetter.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.R;

public class ViewImageActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        Bundle extras = getIntent().getExtras();

        String imageUrl = extras.getString("imageUrl");
        String title = extras.getString("imageTitle");

        ImageView imageView = (ImageView)findViewById(R.id.view_image);
        Button backBtn = (Button)findViewById(R.id.view_image_back);
        ImageButton deleteBtn = (ImageButton)findViewById(R.id.view_image_delete);
        ImageButton shareBtn = (ImageButton)findViewById(R.id.view_image_share);
        TextView caption = (TextView)findViewById(R.id.view_image_caption);
        caption.setText(title);

        backBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);


        setPic(imageView, imageUrl);
    }

    private void setPic(ImageView mImageView, String mCurrentPhotoPath) {
        // Get the dimensions of the View
        int targetW = 1000;
        int targetH = 550;
        Log.e("width and height", targetW + targetH + "");

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
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.view_image_back) {
            finish();
        } else if (id == R.id.view_image_delete) {

        }

    }
}
