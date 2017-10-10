package com.dlsu.getbetter.getbetter.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.objects.HealthCenter;

import java.util.ArrayList;

import com.balysv.materialripple.MaterialRippleLayout;

/**
 * Created by mikedayupay on 26/06/2016.
 * GetBetter 2016
 */
public class HealthCenterListAdapter extends RecyclerView.Adapter<HealthCenterListAdapter.HealthCenterViewHolder> {

    private ArrayList<HealthCenter> healthCenters;
    private OnItemClickListener mItemClickListener;
    private int selectedItem = 0;

    public class HealthCenterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView healthCenterName;

        public HealthCenterViewHolder(View itemView) {
            super(itemView);

            healthCenterName = (TextView)itemView.findViewById(R.id.health_center_name);
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

    public HealthCenterListAdapter(ArrayList<HealthCenter> healthCenters) {
        this.healthCenters = healthCenters;
    }

    @Override
    public HealthCenterListAdapter.HealthCenterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

//        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

//        MaterialRippleLayout materialRippleLayout = new MaterialRippleLayout(parent.getContext());
//        materialRippleLayout.measure(MaterialRippleLayout.LayoutParams.MATCH_PARENT, MaterialRippleLayout.LayoutParams.WRAP_CONTENT);
//        MaterialRippleLayout.LayoutParams mrl = new MaterialRippleLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

//        View v = MaterialRippleLayout.on(inflater.inflate(R.layout.health_center_list_item, parent, false))
//                .rippleOverlay(true)
//                .rippleAlpha(0.2f)
//                .rippleColor(0xFF585858)
//                .rippleHover(true)
//                .create();

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.health_center_list_item, parent, false);

        return new HealthCenterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(HealthCenterListAdapter.HealthCenterViewHolder holder, int position) {

        holder.healthCenterName.setText(healthCenters.get(position).getHealthCenterName());
    }

    @Override
    public int getItemCount() {
        return healthCenters.size();
    }


}
