package com.dlsu.getbetter.getbetter.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.objects.Attachment;

import java.util.ArrayList;

/**
 * Created by mikedayupay on 13/02/2016.
 * GetBetter 2016
 */
public class FileAttachmentsAdapter extends RecyclerView.Adapter<FileAttachmentsAdapter.ViewHolder> {

    private ArrayList<Attachment> filesDataset;
    private OnItemClickListener mItemClickListener;
    private int selectedItem = 0;

    public FileAttachmentsAdapter(ArrayList<Attachment> filesDataset) {

        this.filesDataset = filesDataset;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView fileTitle;
        ImageView typeIcon;


        public ViewHolder(View v) {
            super(v);
            itemView.setOnClickListener(this);
            fileTitle = (TextView)v.findViewById(R.id.summary_page_file_list_item);
            typeIcon = (ImageView)v.findViewById(R.id.file_type_icon);

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

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener =  mItemClickListener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.summary_page_item,
                parent, false);

        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.fileTitle.setText(filesDataset.get(position).getAttachmentDescription());

        if(filesDataset.get(position).getAttachmentType() == 1) {
            holder.typeIcon.setImageResource(R.drawable.ic_action_picture);
        } else if (filesDataset.get(position).getAttachmentType() == 2) {
            holder.typeIcon.setImageResource(R.drawable.ic_action_video);
        } else if (filesDataset.get(position).getAttachmentType() == 3 || filesDataset.get(position).getAttachmentType() == 5) {
            holder.typeIcon.setImageResource(R.drawable.ic_audiotrack);
        }

    }

    @Override
    public int getItemCount() {
        return filesDataset.size();
    }


}
