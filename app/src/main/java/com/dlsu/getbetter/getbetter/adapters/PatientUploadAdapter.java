package com.dlsu.getbetter.getbetter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.R;
import com.dlsu.getbetter.getbetter.objects.Patient;

import java.util.ArrayList;

/**
 * Created by mikedayupay on 25/02/2016.
 * GetBetter 2016
 */
public class PatientUploadAdapter extends ArrayAdapter<Patient> {

    private ArrayList<Patient> patients;
    private LayoutInflater inflater;
//    Context context;



    public PatientUploadAdapter(Context context, int textViewResourceId, ArrayList<Patient> patients) {
        super(context, textViewResourceId, patients);

//        this.patients = new ArrayList<>();
//        this.patients.addAll(patients);
//        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    private class ViewHolder {
        TextView patientName;
        TextView patientId;
        CheckBox checkBox;

        public ViewHolder() {

        }

        public ViewHolder(TextView patientName, TextView patientId, CheckBox checkBox) {
            this.patientName = patientName;
            this.patientId = patientId;
            this.checkBox = checkBox;
        }

        public TextView getPatientName() {
            return patientName;
        }

        public void setPatientName(TextView patientName) {
            this.patientName = patientName;
        }

        public TextView getPatientId() {
            return patientId;
        }

        public void setPatientId(TextView patientId) {
            this.patientId = patientId;
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }

        public void setCheckBox(CheckBox checkBox) {
            this.checkBox = checkBox;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        Patient patient = this.getItem(position);

        if(convertView == null) {

            convertView = inflater.inflate(R.layout.patient_list_item_checkbox, parent, false);

            holder = new ViewHolder();
            holder.patientName = (TextView)convertView.findViewById(R.id.upload_patient_item_name);
            holder.patientId = (TextView)convertView.findViewById(R.id.upload_patient_id);
            holder.checkBox = (CheckBox)convertView.findViewById(R.id.upload_patient_checkbox);
            convertView.setTag(holder);

            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    Patient patient = (Patient)cb.getTag();
                    patient.setChecked(cb.isChecked());
                }
            });

        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        String id = patient.getId() + "";
        String name = patient.getLastName() + ", " + patient.getFirstName();
        holder.patientId.setText(id);
        holder.patientName.setText(name);
        holder.checkBox.setChecked(patient.isChecked());
        holder.checkBox.setTag(patient);

        return convertView;
    }
}
