package com.dlsu.getbetter.getbetter.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.dlsu.getbetter.getbetter.objects.Attachment;
import com.dlsu.getbetter.getbetter.objects.CaseRecord;
import com.dlsu.getbetter.getbetter.objects.HealthCenter;
import com.dlsu.getbetter.getbetter.objects.Patient;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by mikedayupay on 10/01/2016.
 * GetBetter 2016
 * ADD: get encrypt key function, final value for encryption key
 */
public class DataAdapter {

    private static final String TAG = "DataAdapter";

    private int gOpenCounter;

    private SQLiteDatabase getBetterDb;
    private DatabaseHelper getBetterDatabaseHelper;

    private static final String USER_TABLE = "tbl_users";
    private static final String USER_TABLE_UPLOAD = "tbl_users_upload";
    private static final String CASE_RECORD_TABLE = "tbl_case_records";
    private static final String CASE_RECORD_TABLE_UPLOAD = "tbl_case_records_upload";
    private static final String CASE_RECORD_HISTORY_TABLE = "tbl_case_record_history";
    private static final String CASE_RECORD_ATTACHMENTS_TABLE = "tbl_case_record_attachments";
    private static final String HEALTH_CENTER_TABLE = "tbl_health_centers";


    public DataAdapter(Context context) {
        Context myContext = context;
        getBetterDatabaseHelper = DatabaseHelper.getInstance(myContext);
    }

    public DataAdapter createDatabase() throws SQLException {

        try {
            getBetterDatabaseHelper.createDatabase();
        }catch (IOException ioe) {
            Log.e(TAG, ioe.toString() + "UnableToCreateDatabase");
            throw new Error("UnableToCreateDatabase");
        }
        return this;
    }

    public synchronized DataAdapter openDatabase() throws SQLException {

        gOpenCounter++;

        if(gOpenCounter == 1) {

            try {
                getBetterDatabaseHelper.openDatabase();
                getBetterDatabaseHelper.close();
                getBetterDb = getBetterDatabaseHelper.getWritableDatabase();
            }catch (SQLException sqle) {
                Log.e(TAG, "open >>" +sqle.toString());
                throw sqle;
            }

        }

        return this;
    }

    public synchronized void closeDatabase() {

        gOpenCounter--;

        if(gOpenCounter == 0) {

            getBetterDb.close();
        }

        Log.d("database open", gOpenCounter + "");
    }

    public boolean checkLogin (String username, String password) {

        String sql = "SELECT * FROM tbl_users WHERE email = '" + username + "' AND pass = '" + password + "'";

        Cursor c = getBetterDb.rawQuery(sql, null);

        if(c.getCount() > 0) {
            c.close();
            return true;
        } else {
            c.close();
            return false;
        }
    }

    public int getUserId(String username) {

        int result;
        String sql = "SELECT _id FROM tbl_users WHERE email = '" + username + "'";
        Cursor c = getBetterDb.rawQuery(sql, null);

        c.moveToFirst();
        result = c.getInt(c.getColumnIndexOrThrow("_id"));

        c.close();
        return result;
    }

    public String getUserName (int userId) {

        String result;
        String sql = "SELECT first_name, last_name FROM " + USER_TABLE + " WHERE _id = " + userId;
        Cursor c = getBetterDb.rawQuery(sql, null);

        c.moveToFirst();

        result = c.getString(c.getColumnIndexOrThrow("first_name")) + " " + c.getString(c.getColumnIndexOrThrow("last_name"));

        c.close();

        return result;
    }

    public String getUserName (String email) {

        String result;
        String sql = "SELECT first_name, last_name FROM " + USER_TABLE + " WHERE email = '" + email + "'";
        Cursor c = getBetterDb.rawQuery(sql, null);

        c.moveToFirst();
        result = c.getString(c.getColumnIndexOrThrow("first_name")) + " " + c.getString(c.getColumnIndexOrThrow("last_name"));

        c.close();

        return result;
    }

    public long insertPatientInfo(String firstName, String middleName, String lastName,
                                  String birthDate, String gender, String civilStatus, String bloodType,
                                  String profileImage, int healthCenterId) {

        int genderId;
        int civilId;
        long rowId;

        String genderSql = "SELECT _id FROM tbl_genders WHERE gender_name = '" + gender + "'";
        Cursor cGender = getBetterDb.rawQuery(genderSql, null);
        cGender.moveToFirst();
        genderId = cGender.getInt(cGender.getColumnIndex("_id"));
        cGender.close();

        String civilSql = "SELECT _id FROM tbl_civil_statuses WHERE civil_status_name = '" + civilStatus + "'";
        Cursor cCivil = getBetterDb.rawQuery(civilSql, null);
        cCivil.moveToFirst();
        civilId = cCivil.getInt(cCivil.getColumnIndex("_id"));
        cCivil.close();

        ContentValues cv = new ContentValues();
        cv.put("first_name", firstName);
        cv.put("middle_name", middleName);
        cv.put("last_name", lastName);
        cv.put("birthdate", birthDate);
        cv.put("gender_id", genderId);
        cv.put("civil_status_id", civilId);
        cv.put("blood_type", bloodType);
        cv.put("profile_url", profileImage);
        cv.put("role_id", 6);
        cv.put("default_health_center", healthCenterId);

        rowId = getBetterDb.insert(USER_TABLE, null, cv);

        cv.put("_id", rowId);

        getBetterDb.insert(USER_TABLE_UPLOAD, null, cv);

        return rowId;
    }

    public long insertCaseRecord (int caseRecordId, long userId, int healthCenterId, String complaint,
                                  String controlNumber) {

        long rowId;

        ContentValues cv = new ContentValues();
        cv.put("_id", caseRecordId);
        cv.put("case_id", 6);
        cv.put("user_id", userId);
        cv.put("health_center_id", healthCenterId);
        cv.put("complaint", complaint);
        cv.put("control_number", controlNumber);

        rowId = getBetterDb.insert(CASE_RECORD_TABLE, null, cv);
        getBetterDb.insert(CASE_RECORD_TABLE_UPLOAD, null, cv);

        return rowId;
    }

    public long insertCaseRecordHistory(int caseRecordId, int userId,
                                        String dateUpdated) {

        long rowId;

        ContentValues cv = new ContentValues();
        cv.put("_id", caseRecordId);
        cv.put("record_status_id", 1);
        cv.put("updated_by", userId);
        cv.put("updated_on", dateUpdated);

        rowId = getBetterDb.insert(CASE_RECORD_HISTORY_TABLE, null, cv);

        return rowId;
    }

    public long insertCaseRecordAttachments(Attachment attachment) {

        long rowId;

        ContentValues cv = new ContentValues();
        cv.put("case_record_id", attachment.getCaseRecordId());
        cv.put("description", attachment.getAttachmentDescription());
        cv.put("case_file_url", attachment.getAttachmentPath());
        cv.put("case_record_attachment_type_id", attachment.getAttachmentType());
        cv.put("uploaded_on", attachment.getUploadedDate());
        cv.put("uploaded_by", attachment.getUploadedBy());

        rowId = getBetterDb.insert(CASE_RECORD_ATTACHMENTS_TABLE, null, cv);

        return rowId;
    }

    public ArrayList<Patient> getPatients (int healthCenterId) {

        ArrayList<Patient> results = new ArrayList<>();

        String sql = "SELECT u._id AS id, u.first_name AS first_name, u.middle_name AS middle_name, " +
                "u.last_name AS last_name, u.birthdate AS birthdate, g.gender_name AS gender, " +
                "c.civil_status_name AS civil_status, u.blood_type AS blood_type, u.profile_url AS image " +
                "FROM tbl_users AS u, tbl_genders AS g, tbl_civil_statuses AS c " +
                "WHERE u.gender_id = g._id AND u.civil_status_id = c._id AND u.role_id = 6 AND " +
                "default_health_center = " + healthCenterId;

        Cursor c = getBetterDb.rawQuery(sql, null);

        Log.e("cursor", c.getCount() + "");
        while (c.moveToNext()) {
            Patient patient = new Patient(c.getLong(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("first_name")),
                    c.getString(c.getColumnIndexOrThrow("middle_name")),
                    c.getString(c.getColumnIndexOrThrow("last_name")),
                    c.getString(c.getColumnIndexOrThrow("birthdate")),
                    c.getString(c.getColumnIndexOrThrow("gender")),
                    c.getString(c.getColumnIndexOrThrow("civil_status")),
                    c.getString(c.getColumnIndexOrThrow("blood_type")),
                    c.getString(c.getColumnIndexOrThrow("image")));

            results.add(patient);
        }

        Log.e("data patient", results.size() + "");
        c.close();
        return results;
    }

    public ArrayList<Patient> getPatientsUpload (int healthCenterId) {

        ArrayList<Patient> results = new ArrayList<>();

        String sql = "SELECT u._id AS id, u.first_name AS first_name, u.middle_name AS middle_name, " +
                "u.last_name AS last_name, u.birthdate AS birthdate, g.gender_name AS gender, " +
                "c.civil_status_name AS civil_status, u.blood_type AS blood_type, u.profile_url AS image " +
                "FROM tbl_users_upload AS u, tbl_genders AS g, tbl_civil_statuses AS c " +
                "WHERE u.gender_id = g._id AND u.civil_status_id = c._id AND u.role_id = 6";

        Cursor c = getBetterDb.rawQuery(sql, null);

        Log.e("cursor", c.getCount() + "");
        while (c.moveToNext()) {
            Patient patient = new Patient(c.getLong(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("first_name")),
                    c.getString(c.getColumnIndexOrThrow("middle_name")),
                    c.getString(c.getColumnIndexOrThrow("last_name")),
                    c.getString(c.getColumnIndexOrThrow("birthdate")),
                    c.getString(c.getColumnIndexOrThrow("gender")),
                    c.getString(c.getColumnIndexOrThrow("civil_status")),
                    c.getString(c.getColumnIndexOrThrow("blood_type")),
                    c.getString(c.getColumnIndexOrThrow("image")));

            results.add(patient);
        }

        c.close();
        return results;
    }

    public Patient getPatient (long patientId) {

        String sql = "SELECT u._id AS id, u.first_name AS first_name, u.middle_name AS middle_name, " +
                "u.last_name AS last_name, u.birthdate AS birthdate, g.gender_name AS gender, " +
                "c.civil_status_name AS civil_status, u.blood_type AS blood_type, u.profile_url AS image " +
                "FROM tbl_users AS u, tbl_genders AS g, tbl_civil_statuses AS c " +
                "WHERE u.gender_id = g._id AND u.civil_status_id = c._id AND u._id = " + patientId;

        Cursor c = getBetterDb.rawQuery(sql, null);

        Log.e("cursor", c.getCount() + "");

        c.moveToFirst();
        Patient patient = new Patient(c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("first_name")),
                c.getString(c.getColumnIndexOrThrow("middle_name")),
                c.getString(c.getColumnIndexOrThrow("last_name")),
                c.getString(c.getColumnIndexOrThrow("birthdate")),
                c.getString(c.getColumnIndexOrThrow("gender")),
                c.getString(c.getColumnIndexOrThrow("civil_status")),
                c.getString(c.getColumnIndexOrThrow("blood_type")),
                c.getString(c.getColumnIndexOrThrow("image")));

        c.close();
        return patient;

    }

    public void removePatientUpload (long userId) {

        getBetterDb.delete(USER_TABLE_UPLOAD, "_id = " + userId, null);

    }

    public void removeCaseRecordUpload (int caseRecordId) {

        getBetterDb.delete(CASE_RECORD_TABLE_UPLOAD, "_id = " + caseRecordId, null);
    }

    public ArrayList<HealthCenter> getHealthCenters() {

        ArrayList<HealthCenter> results = new ArrayList<>();

        String sql = "SELECT _id, health_center_name FROM " + HEALTH_CENTER_TABLE;

        Cursor c = getBetterDb.rawQuery(sql, null);

        while (c.moveToNext()) {
            HealthCenter hc = new HealthCenter(c.getInt(c.getColumnIndexOrThrow("_id")),
                    c.getString(c.getColumnIndexOrThrow("health_center_name")));

            results.add(hc);
        }
        c.close();
        return results;
    }

    public String getHealthCenterString(int healthCenterId) {

        String result;

        String sql = "SELECT * FROM " + HEALTH_CENTER_TABLE + " WHERE _id = " + healthCenterId;
        Cursor c = getBetterDb.rawQuery(sql, null);

        c.moveToFirst();
        result = c.getString(c.getColumnIndexOrThrow("health_center_name"));

        c.close();

        return result;
    }

    public int getHealthCenterId(String healthCenter) {

        int result;
        String sql = "SELECT _id FROM tbl_health_centers WHERE health_center_name = '" + healthCenter + "'";

        Cursor c = getBetterDb.rawQuery(sql, null);

        c.moveToFirst();
        result = c.getInt(c.getColumnIndex("_id"));

        c.close();
        return result;
    }

    public int getGenderId(String genderName) {

        int result;
        String sql = "SELECT _id FROM tbl_genders WHERE gender_name = '" + genderName + "'";
        Cursor c = getBetterDb.rawQuery(sql, null);

        c.moveToFirst();
        result = c.getInt(c.getColumnIndexOrThrow("_id"));

        c.close();
        return result;
    }

    public int getCivilStatusId(String civilStatusName) {

        int result;
        String sql = "SELECT _id FROM tbl_civil_statuses WHERE civil_status_name = '" + civilStatusName + "'";
        Cursor c = getBetterDb.rawQuery(sql, null);

        c.moveToFirst();
        result = c.getInt(c.getColumnIndexOrThrow("_id"));

        c.close();
        return result;
    }

    public ArrayList<Integer> getCaseRecordIds () {

        ArrayList<Integer> ids = new ArrayList<>();

        String sql = "SELECT _id FROM tbl_case_records";
        Cursor c = getBetterDb.rawQuery(sql, null);

        while(c.moveToNext()) {
            ids.add(c.getInt(c.getColumnIndexOrThrow("_id")));
        }

        c.close();
        return ids;
    }

    public int getAttachmentTypeId (String attachmentType) {

        int result = 0;
        String sql = "SELECT _id FROM tbl_case_record_attachment_types WHERE case_record_attachment_type_name = '"
                + attachmentType + "'";

        Cursor c = getBetterDb.rawQuery(sql, null);

        c.moveToFirst();
        result = c.getInt(c.getColumnIndexOrThrow("_id"));
        c.close();
        return result;
    }

    public ArrayList<CaseRecord> getCaseRecords (long patientId) {

        ArrayList<CaseRecord> results = new ArrayList<>();

        String sql = "SELECT * FROM tbl_case_records WHERE user_id = " + patientId;
        Cursor c = getBetterDb.rawQuery(sql, null);

        while(c.moveToNext()) {
            CaseRecord caseRecord = new CaseRecord(c.getInt(c.getColumnIndexOrThrow("_id")),
                    c.getString(c.getColumnIndexOrThrow("complaint")),
                    c.getString(c.getColumnIndexOrThrow("control_number")));

            results.add(caseRecord);
        }
        c.close();
        return results;
    }

    public CaseRecord getCaseRecord(int caseRecordId) {

        String sql = "SELECT * FROM tbl_case_records WHERE _id = " + caseRecordId;
        Cursor c = getBetterDb.rawQuery(sql, null);

        c.moveToFirst();
        CaseRecord result = new CaseRecord(c.getInt(c.getColumnIndexOrThrow("_id")),
                c.getString(c.getColumnIndexOrThrow("complaint")),
                c.getInt(c.getColumnIndexOrThrow("user_id")),
                c.getString(c.getColumnIndexOrThrow("control_number")),
                c.getString(c.getColumnIndexOrThrow("additional_notes")));

        c.close();

        return result;
    }

    public ArrayList<CaseRecord> getCaseRecordsUpload () {

        ArrayList<CaseRecord> results = new ArrayList<>();

        String sql = "SELECT * FROM tbl_case_records AS c " +
                "INNER JOIN (SELECT h.* FROM tbl_case_record_history AS h " +
                "INNER JOIN (SELECT _id, record_status_id, MAX(updated_on) AS maxdate " +
                "FROM tbl_case_record_history WHERE record_status_id IN (1, 2) GROUP BY _id) t " +
                "ON h._id = t._id AND h.updated_on = t.maxdate) d ON c._id = d._id";

        Cursor c = getBetterDb.rawQuery(sql, null);

        while(c.moveToNext()) {
            CaseRecord caseRecord = new CaseRecord(c.getInt(c.getColumnIndexOrThrow("_id")),
                    c.getInt(c.getColumnIndexOrThrow("case_id")),
                    c.getInt(c.getColumnIndexOrThrow("user_id")),
                    c.getInt(c.getColumnIndexOrThrow("record_status_id")),
                    c.getString(c.getColumnIndexOrThrow("complaint")),
                    c.getString(c.getColumnIndexOrThrow("additional_notes")),
                    c.getString(c.getColumnIndexOrThrow("control_number")),
                    c.getString(c.getColumnIndexOrThrow("updated_on")));

            results.add(caseRecord);
        }
        c.close();

        return results;
    }

    public ArrayList<String> getCaseRecordHistory (int caseRecordId) {
        ArrayList<String> result = new ArrayList<>();

        String sql = "SELECT u.first_name AS updated_by, h.updated_on AS updated_on, " +
                "s.case_record_status_name AS status FROM tbl_case_record_history AS h, " +
                "tbl_case_record_statuses AS s, tbl_users AS u WHERE u._id = h.updated_by AND " +
                "h.record_status_id = s._id AND h._id = " + caseRecordId + " ORDER BY updated_on LIMIT 1";

        Cursor c = getBetterDb.rawQuery(sql, null);

        c.moveToFirst();
        result.add(c.getString(c.getColumnIndexOrThrow("updated_by")));
        result.add(c.getString(c.getColumnIndexOrThrow("updated_on")));
        result.add(c.getString(c.getColumnIndexOrThrow("status")));

        c.close();

        return result;
    }

    public ArrayList<Attachment> getCaseRecordAttachments (int caseRecordId) {

        ArrayList<Attachment> attachments = new ArrayList<>();
        String sql = "SELECT * FROM tbl_case_record_attachments WHERE case_record_attachment_type_id IN (1,2,3,4) AND " +
                "case_record_id = " + caseRecordId;

        Cursor c = getBetterDb.rawQuery(sql, null);

        while (c.moveToNext()) {

            Attachment attachment = new Attachment(c.getInt(c.getColumnIndexOrThrow("case_record_id")),
                    c.getString(c.getColumnIndexOrThrow("case_file_url")),
                    c.getString(c.getColumnIndexOrThrow("description")),
                    c.getInt(c.getColumnIndexOrThrow("case_record_attachment_type_id")),
                    c.getString(c.getColumnIndexOrThrow("uploaded_on")),
                    c.getInt(c.getColumnIndexOrThrow("uploaded_by")));

            attachments.add(attachment);

        }

        c.close();
        return attachments;
    }

    public ArrayList<Attachment> getAttachments (int caseRecordId) {

        ArrayList<Attachment> attachments = new ArrayList<>();
        String sql = "SELECT * FROM " + CASE_RECORD_ATTACHMENTS_TABLE + " WHERE case_record_id = " + caseRecordId;

        Cursor c = getBetterDb.rawQuery(sql, null);

        while (c.moveToNext()) {

            Attachment attachment = new Attachment(c.getInt(c.getColumnIndexOrThrow("case_record_id")),
                    c.getString(c.getColumnIndexOrThrow("case_file_url")),
                    c.getString(c.getColumnIndexOrThrow("description")),
                    c.getInt(c.getColumnIndexOrThrow("case_record_attachment_type_id")),
                    c.getString(c.getColumnIndexOrThrow("uploaded_on")),
                    c.getInt(c.getColumnIndexOrThrow("uploaded_by")));

            attachments.add(attachment);

        }

        c.close();

        return attachments;

    }

    public String getHPI (int caseRecordId) {

        String result;

        String sql = "SELECT case_file_url FROM " + CASE_RECORD_ATTACHMENTS_TABLE + " WHERE case_record_attachment_type_id = " + 5 +
                " AND case_record_id = " + caseRecordId;

        Cursor c = getBetterDb.rawQuery(sql, null);

        c.moveToFirst();
        result = c.getString(c.getColumnIndexOrThrow("case_file_url"));

        c.close();

        return result;
    }

    public int updatePatientInfo (Patient patient, long patientId) {

        int genderId;
        int civilId;

        Log.e("gender", patient.getGender());

        String genderSql = "SELECT _id FROM tbl_genders WHERE gender_name = '" + patient.getGender() + "'";
        Cursor cGender = getBetterDb.rawQuery(genderSql, null);
        cGender.moveToFirst();
        genderId = cGender.getInt(cGender.getColumnIndex("_id"));
        cGender.close();

        String civilSql = "SELECT _id FROM tbl_civil_statuses WHERE civil_status_name = '" + patient.getCivilStatus() + "'";
        Cursor cCivil = getBetterDb.rawQuery(civilSql, null);
        cCivil.moveToFirst();
        civilId = cCivil.getInt(cCivil.getColumnIndex("_id"));
        cCivil.close();

        ContentValues cv = new ContentValues();
        cv.put("first_name", patient.getFirstName());
        cv.put("middle_name", patient.getMiddleName());
        cv.put("last_name", patient.getLastName());
        cv.put("birthdate", patient.getBirthdate());
        cv.put("gender_id", genderId);
        cv.put("civil_status_id", civilId);
        cv.put("profile_url", patient.getProfileImageBytes());

        return getBetterDb.update(USER_TABLE, cv, "_id = " + patientId, null);


    }

    public void updateCaseRecordAdditionalNotes (int caseRecordId, String additionalNotes) {

        ContentValues cv = new ContentValues();
        cv.put("additional_notes", additionalNotes);

        getBetterDb.update(CASE_RECORD_TABLE, cv, "_id = " + caseRecordId, null);
    }

    public String getPatientName (int userId) {

        String result = null;

        String sql = "SELECT first_name, last_name FROM tbl_users WHERE _id = " + userId;
        Cursor c = getBetterDb.rawQuery(sql, null);

        c.moveToFirst();
        result = c.getString(c.getColumnIndexOrThrow("last_name")) + ", " + c.getString(c.getColumnIndexOrThrow("first_name"));

        c.close();

        return result;
    }

    public ArrayList<Long> getPatientIds () {

        ArrayList<Long> result = new ArrayList<>();

        String sql = "SELECT _id FROM tbl_users";
        Cursor c = getBetterDb.rawQuery(sql, null);

        while (c.moveToNext()) {
            result.add(c.getLong(c.getColumnIndexOrThrow("_id")));
        }

        c.close();
        return result;
    }

    public void updateLocalCaseRecordHistory(CaseRecord caseRecord) {

        Log.e("update case record", caseRecord.getCaseRecordId() + "");

        ContentValues cv = new ContentValues();
        cv.put("_id", caseRecord.getCaseRecordId());
        cv.put("record_status_id", caseRecord.getCaseRecordStatusId());
        cv.put("updated_by", 441);
        cv.put("updated_on", caseRecord.getCaseRecordUpdatedOn());

        getBetterDb.insert(CASE_RECORD_HISTORY_TABLE, null, cv);
    }

    public ArrayList<CaseRecord> getUrgentCaseRecords(int healthCenterId) {
        ArrayList<CaseRecord> result = new ArrayList<>();

        String sql = "SELECT c._id AS id, c.user_id AS user, c.complaint AS complaint, h.updated_on AS updated_on" +
                " FROM tbl_case_records AS c INNER JOIN tbl_case_record_history AS h ON c._id = h._id" +
                " WHERE h.record_status_id = 6 AND c.health_center_id = " + healthCenterId;

        Cursor c = getBetterDb.rawQuery(sql, null);

        while(c.moveToNext()) {
            CaseRecord caseRecord = new CaseRecord(c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getInt(c.getColumnIndexOrThrow("user")),
                    c.getString(c.getColumnIndexOrThrow("complaint")),
                    c.getString(c.getColumnIndexOrThrow("updated_on")));

            result.add(caseRecord);
        }
        Log.e("urgent case size", result.size() + "");
        c.close();
        return result;
    }

    public ArrayList<CaseRecord> getAddInstructionCaseRecords(int healthCenterId) {
        ArrayList<CaseRecord> result = new ArrayList<>();

        String sql = "SELECT c._id AS id, c.user_id AS user, c.complaint AS complaint, h.updated_on AS updated_on" +
                " FROM tbl_case_records AS c INNER JOIN tbl_case_record_history AS h ON c._id = h._id" +
                " WHERE h.record_status_id = 3 AND c.health_center_id = ?";
        Cursor c = getBetterDb.rawQuery(sql, new String[]{String.valueOf(healthCenterId)});

        while(c.moveToNext()) {
            CaseRecord caseRecord = new CaseRecord(c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getInt(c.getColumnIndexOrThrow("user")),
                    c.getString(c.getColumnIndexOrThrow("complaint")),
                    c.getString(c.getColumnIndexOrThrow("updated_on")));

            result.add(caseRecord);
        }

        c.close();
        return result;
    }

    public ArrayList<CaseRecord> getDiagnosedCaseRecords(int healthCenterId) {
        ArrayList<CaseRecord> result = new ArrayList<>();

        String sql = "SELECT c._id AS id, c.user_id AS user, c.complaint AS complaint, h.updated_on AS updated_on" +
                " FROM tbl_case_records AS c INNER JOIN tbl_case_record_history AS h ON c._id = h._id" +
                " WHERE h.record_status_id = 7 AND c.health_center_id = ?";
        Cursor c = getBetterDb.rawQuery(sql, new String[]{String.valueOf(healthCenterId)});

        while(c.moveToNext()) {
            CaseRecord caseRecord = new CaseRecord(c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getInt(c.getColumnIndexOrThrow("user")),
                    c.getString(c.getColumnIndexOrThrow("complaint")),
                    c.getString(c.getColumnIndexOrThrow("updated_on")));

            result.add(caseRecord);
        }

        c.close();
        return result;
    }

    public ArrayList<CaseRecord> getClosedCaseRecords(int healthCenterId) {

        ArrayList<CaseRecord> result = new ArrayList<>();




        return result;
    }

    public void updateUserId (long newId, long oldId) {

        ContentValues cv = new ContentValues();
        cv.put("_id", newId);


        int row = getBetterDb.update(USER_TABLE, cv, "_id = ?", new String[]{String.valueOf(oldId)});
        Log.d(TAG, "updateUserId: " + row);

        cv.clear();
        cv.put("user_id", newId);

        getBetterDb.update(CASE_RECORD_TABLE, cv, "user_id = ?", new String[]{String.valueOf(oldId)});
        getBetterDb.update(CASE_RECORD_TABLE_UPLOAD, cv, "user_id = ?", new String[]{String.valueOf(oldId)});
    }

    public void updateCaseRecordId (int newId, int oldId) {

        ContentValues cv = new ContentValues();
        cv.put("_id", newId);

        getBetterDb.update(CASE_RECORD_TABLE, cv, "_id = " + oldId, null);
        getBetterDb.update(CASE_RECORD_HISTORY_TABLE, cv, "_id = " + oldId, null);

        cv.clear();
        cv.put("case_record_id", newId);

        getBetterDb.update(CASE_RECORD_ATTACHMENTS_TABLE, cv, "case_record_id = " + oldId, null);

    }
}
