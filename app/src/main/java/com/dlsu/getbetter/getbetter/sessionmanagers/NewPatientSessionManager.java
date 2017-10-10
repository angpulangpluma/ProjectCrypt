package com.dlsu.getbetter.getbetter.sessionmanagers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.dlsu.getbetter.getbetter.activities.ExistingPatientActivity;

import java.util.HashMap;

/**
 * Created by mikedayupay on 02/02/2016.
 * GetBetter 2016
 */
public class NewPatientSessionManager {

    private SharedPreferences pref;
    private Editor editor;
    private Context _context;

    private static final String PREFER_NAME = "NewPatientPref";

    public static final String PATIENT_ID = "patientId";
    public static final String NEW_PATIENT_FIRST_NAME = "newPatientFirstName";
    public static final String NEW_PATIENT_MIDDLE_NAME = "newPatientMiddleName";
    public static final String NEW_PATIENT_LAST_NAME = "newPatientLastName";
    public static final String NEW_PATIENT_BIRTHDATE = "newPatientBirthdate";
    public static final String NEW_PATIENT_AGE = "newPatientAge;";
    public static final String NEW_PATIENT_GENDER = "newPatientGender";
    public static final String NEW_PATIENT_CIVIL_STATUS = "newPatientCivilStatus";
    public static final String NEW_PATIENT_BLOOD_TYPE = "newPatientBloodType";
    public static final String NEW_PATIENT_PROFILE_IMAGE = "newPatientProfileImage";
    public static final String NEW_PATIENT_DOC_IMAGE1 = "newPatientDocImage1";
    public static final String NEW_PATIENT_DOC_IMAGE2 = "newPatientDocImage2";
    public static final String NEW_PATIENT_DOC_IMAGE3 = "newPatientDocImage3";
    public static final String NEW_PATIENT_DOC_IMAGE1_TITLE = "newPatientDocImage1Title";
    public static final String NEW_PATIENT_DOC_IMAGE2_TITLE = "newPatientDocImage2Title";
    public static final String NEW_PATIENT_DOC_IMAGE3_TITLE = "newPatientDocImage3Title";
    public static final String NEW_PATIENT_DOC_HPI_RECORD = "newPatientHpiRecord";
    public static final String NEW_PATIENT_CHIEF_COMPLAINT = "newPatientChiefComplaint";
    private static final String IS_ACTIVITY_NEW_PATIENT = "isActivityNewPatient";
    private static final String IS_DOCUMENTS_EMPTY = "isDocumentsEmpty";
    private static final String IS_HPI_EMPTY = "isHpiEmpty";


    public NewPatientSessionManager (Context context) {

        this._context = context;
        int PRIVATE_MODE = 0;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);

        editor = pref.edit();
    }

    public void createNewPatientSession (String firstName, String middleName, String lastName,
                                         String birthdate, String gender, String civilStatus,
                                         String bloodType, String profileImage) {

        editor.putString(NEW_PATIENT_FIRST_NAME, firstName);
        editor.putString(NEW_PATIENT_MIDDLE_NAME, middleName);
        editor.putString(NEW_PATIENT_LAST_NAME, lastName);
        editor.putString(NEW_PATIENT_BIRTHDATE, birthdate);
        editor.putString(NEW_PATIENT_GENDER, gender);
        editor.putString(NEW_PATIENT_CIVIL_STATUS, civilStatus);
        editor.putString(NEW_PATIENT_BLOOD_TYPE, bloodType);
        editor.putString(NEW_PATIENT_PROFILE_IMAGE, profileImage);
        editor.putBoolean(IS_ACTIVITY_NEW_PATIENT, true);
        editor.commit();
    }

    public HashMap<String, String> getNewPatientDetails () {

        HashMap<String, String> newPatient = new HashMap<>();

        newPatient.put(NEW_PATIENT_FIRST_NAME, pref.getString(NEW_PATIENT_FIRST_NAME, null));
        newPatient.put(NEW_PATIENT_MIDDLE_NAME, pref.getString(NEW_PATIENT_MIDDLE_NAME, null));
        newPatient.put(NEW_PATIENT_LAST_NAME, pref.getString(NEW_PATIENT_LAST_NAME, null));
        newPatient.put(NEW_PATIENT_BIRTHDATE, pref.getString(NEW_PATIENT_BIRTHDATE, null));
        newPatient.put(NEW_PATIENT_GENDER, pref.getString(NEW_PATIENT_GENDER, null));
        newPatient.put(NEW_PATIENT_CIVIL_STATUS, pref.getString(NEW_PATIENT_CIVIL_STATUS, null));
        newPatient.put(NEW_PATIENT_BLOOD_TYPE, pref.getString(NEW_PATIENT_BLOOD_TYPE, null));
        newPatient.put(NEW_PATIENT_PROFILE_IMAGE, pref.getString(NEW_PATIENT_PROFILE_IMAGE, null));
        newPatient.put(NEW_PATIENT_DOC_IMAGE1, pref.getString(NEW_PATIENT_DOC_IMAGE1, null));
        newPatient.put(NEW_PATIENT_DOC_IMAGE2, pref.getString(NEW_PATIENT_DOC_IMAGE2, null));
        newPatient.put(NEW_PATIENT_DOC_IMAGE3, pref.getString(NEW_PATIENT_DOC_IMAGE3, null));
        newPatient.put(NEW_PATIENT_DOC_IMAGE1_TITLE, pref.getString(NEW_PATIENT_DOC_IMAGE1_TITLE, null));
        newPatient.put(NEW_PATIENT_DOC_IMAGE2_TITLE, pref.getString(NEW_PATIENT_DOC_IMAGE2_TITLE, null));
        newPatient.put(NEW_PATIENT_DOC_IMAGE3_TITLE, pref.getString(NEW_PATIENT_DOC_IMAGE3_TITLE, null));
        newPatient.put(NEW_PATIENT_DOC_HPI_RECORD, pref.getString(NEW_PATIENT_DOC_HPI_RECORD, null));
        newPatient.put(NEW_PATIENT_CHIEF_COMPLAINT, pref.getString(NEW_PATIENT_CHIEF_COMPLAINT, null));

        return newPatient;
    }

    public HashMap<String, String> getPatientInfo () {
        HashMap<String, String> patientInfo = new HashMap<>();

        patientInfo.put(PATIENT_ID, pref.getString(PATIENT_ID, null));
        patientInfo.put(NEW_PATIENT_FIRST_NAME, pref.getString(NEW_PATIENT_FIRST_NAME, null));
        patientInfo.put(NEW_PATIENT_LAST_NAME, pref.getString(NEW_PATIENT_LAST_NAME, null));
        patientInfo.put(NEW_PATIENT_AGE, pref.getString(NEW_PATIENT_AGE, null));
        patientInfo.put(NEW_PATIENT_GENDER, pref.getString(NEW_PATIENT_GENDER, null));
        patientInfo.put(NEW_PATIENT_BLOOD_TYPE, pref.getString(NEW_PATIENT_BLOOD_TYPE, null));
        patientInfo.put(NEW_PATIENT_PROFILE_IMAGE, pref.getString(NEW_PATIENT_PROFILE_IMAGE, null));
        patientInfo.put(NEW_PATIENT_DOC_IMAGE1, pref.getString(NEW_PATIENT_DOC_IMAGE1, null));
        patientInfo.put(NEW_PATIENT_DOC_IMAGE2, pref.getString(NEW_PATIENT_DOC_IMAGE2, null));
        patientInfo.put(NEW_PATIENT_DOC_IMAGE3, pref.getString(NEW_PATIENT_DOC_IMAGE3, null));
        patientInfo.put(NEW_PATIENT_DOC_IMAGE1_TITLE, pref.getString(NEW_PATIENT_DOC_IMAGE1_TITLE, null));
        patientInfo.put(NEW_PATIENT_DOC_IMAGE2_TITLE, pref.getString(NEW_PATIENT_DOC_IMAGE2_TITLE, null));
        patientInfo.put(NEW_PATIENT_DOC_IMAGE3_TITLE, pref.getString(NEW_PATIENT_DOC_IMAGE3_TITLE, null));
        patientInfo.put(NEW_PATIENT_DOC_HPI_RECORD, pref.getString(NEW_PATIENT_DOC_HPI_RECORD, null));
        patientInfo.put(NEW_PATIENT_CHIEF_COMPLAINT, pref.getString(NEW_PATIENT_CHIEF_COMPLAINT, null));

        return patientInfo;
    }

    public void setDocImages(String docImage1, String docImage2, String docImage3,
                             String docImage1Title, String docImage2Title, String docImage3Title) {

        editor.putString(NEW_PATIENT_DOC_IMAGE1, docImage1);
        editor.putString(NEW_PATIENT_DOC_IMAGE2, docImage2);
        editor.putString(NEW_PATIENT_DOC_IMAGE3, docImage3);
        editor.putString(NEW_PATIENT_DOC_IMAGE1_TITLE, docImage1Title);
        editor.putString(NEW_PATIENT_DOC_IMAGE2_TITLE, docImage2Title);
        editor.putString(NEW_PATIENT_DOC_IMAGE3_TITLE, docImage3Title);
        editor.putBoolean(IS_DOCUMENTS_EMPTY, false);
        editor.commit();
    }

    public void setHPIRecord(String hpiRecord, String chiefComplaint) {

        editor.putString(NEW_PATIENT_DOC_HPI_RECORD, hpiRecord);
        editor.putString(NEW_PATIENT_CHIEF_COMPLAINT, chiefComplaint);
        editor.putBoolean(IS_HPI_EMPTY, false);
        editor.commit();
    }

    public void setPatientInfo(String patientId, String firstName, String lastName, String age,
                               String gender, String civilStatus, String bloodType, String profileImagePath) {
        editor.putString(PATIENT_ID, patientId);
        editor.putString(NEW_PATIENT_FIRST_NAME, firstName);
        editor.putString(NEW_PATIENT_LAST_NAME, lastName);
        editor.putString(NEW_PATIENT_AGE, age);
        editor.putString(NEW_PATIENT_GENDER, gender);
        editor.putString(NEW_PATIENT_CIVIL_STATUS, civilStatus);
        editor.putString(NEW_PATIENT_BLOOD_TYPE, bloodType);
        editor.putString(NEW_PATIENT_PROFILE_IMAGE, profileImagePath);
        editor.putBoolean(IS_ACTIVITY_NEW_PATIENT, false);
        editor.commit();
    }

    public void endSession () {

        this.editor.clear();
        this.editor.commit();

        Intent intent = new Intent(_context, ExistingPatientActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        _context.startActivity(intent);
    }

    public boolean isActivityNewPatient () {
        return pref.getBoolean(IS_ACTIVITY_NEW_PATIENT, false);
    }

    public boolean isDocumentsEmpty() {
        return pref.getBoolean(IS_DOCUMENTS_EMPTY, true);
    }

    public boolean isHpiEmpty() {
        return pref.getBoolean(IS_HPI_EMPTY, true);
    }


}
