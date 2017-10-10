package com.dlsu.getbetter.getbetter.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.objects.CaseRecord;

import java.util.ArrayList;

/**
 * Created by mikedayupay on 09/03/2016.
 * GetBetter 2016
 */
public class CaseRecordAdapter extends RecyclerView.Adapter<CaseRecordAdapter.CaseRecordViewHolder> {

    private ArrayList<CaseRecord> caseRecordData;
    private OnItemClickListener mItemClickListener;
    private int selectedItem = 0;


    class CaseRecordViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CardView cardView;
        TextView controlNumber;
        TextView chiefComplaint;
        TextView caseRecordStatus;
        TextView date;

        CaseRecordViewHolder (View itemView) {
            super(itemView);

            controlNumber = (TextView)itemView.findViewById(R.id.case_record_control_number);
            chiefComplaint = (TextView)itemView.findViewById(R.id.case_record_chief_complaint);
            caseRecordStatus = (TextView)itemView.findViewById(R.id.case_record_status);
            date = (TextView)itemView.findViewById(R.id.case_record_date);

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

    public CaseRecordAdapter (ArrayList<CaseRecord> caseRecords) {
        this.caseRecordData = caseRecords;
    }

    @Override
    public CaseRecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.case_record_list_item, parent, false);

        return new CaseRecordViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CaseRecordViewHolder holder, int position) {

        holder.controlNumber.setText(caseRecordData.get(position).getCaseRecordControlNumber());
        holder.chiefComplaint.setText(caseRecordData.get(position).getCaseRecordComplaint());
        holder.caseRecordStatus.setText(caseRecordData.get(position).getCaseRecordStatus());
        holder.date.setText(caseRecordData.get(position).getCaseRecordUpdatedOn());

        holder.itemView.setSelected(selectedItem == position);
    }

    @Override
    public int getItemCount() {

        return caseRecordData.size();
    }




}
