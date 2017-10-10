package com.dlsu.getbetter.getbetter.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.objects.CaseRecord;

import java.util.ArrayList;

/**
 * Created by mikedayupay on 30/05/2016.
 * GetBetter 2016
 */
public class UpdatedCaseRecordAdapter extends RecyclerView.Adapter<UpdatedCaseRecordAdapter.UpdatedCaseRecordViewHolder> {

    private ArrayList<CaseRecord> caseRecordData;
    private OnItemClickListener mItemClickListener;
    private int selectedItem = 0;

    class UpdatedCaseRecordViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView patientName;
        TextView complaint;
        TextView updatedOn;
        ImageView profilePic;


        UpdatedCaseRecordViewHolder(View itemView) {
            super(itemView);

            patientName = (TextView)itemView.findViewById(R.id.home_case_record_patient_name);
            complaint = (TextView)itemView.findViewById(R.id.home_case_record_chief_complaint);
            updatedOn = (TextView)itemView.findViewById(R.id.home_case_record_updated_on);
            profilePic = (ImageView)itemView.findViewById(R.id.home_case_record_profile_pic);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {

            if(mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getAdapterPosition());
                notifyItemChanged(selectedItem);
                selectedItem = getAdapterPosition();
                notifyItemChanged(selectedItem);
            }

        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener (final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public UpdatedCaseRecordAdapter(ArrayList<CaseRecord> caseRecords) {
        this.caseRecordData = caseRecords;
    }

    @Override
    public UpdatedCaseRecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_case_record_list_item, parent, false);
        UpdatedCaseRecordViewHolder homeCaseRecordViewHolder = new UpdatedCaseRecordViewHolder(v);

        return homeCaseRecordViewHolder;
    }

    @Override
    public void onBindViewHolder(final UpdatedCaseRecordViewHolder holder, final int position) {


        holder.patientName.setText(caseRecordData.get(position).getPatientName());
        holder.complaint.setText(caseRecordData.get(position).getCaseRecordComplaint());
        holder.updatedOn.setText(caseRecordData.get(position).getCaseRecordUpdatedOn());

        holder.itemView.post(new Runnable() {
            @Override
            public void run() {
                setPic(holder.profilePic, caseRecordData.get(position).getProfilePic());
            }
        });

        holder.itemView.setSelected(selectedItem == position);

    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return caseRecordData.size();
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
}
