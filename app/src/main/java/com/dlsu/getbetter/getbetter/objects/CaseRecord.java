package com.dlsu.getbetter.getbetter.objects;

import java.util.ArrayList;

/**
 * Created by mikedayupay on 25/02/2016.
 * GetBetter 2016
 */
public class CaseRecord {

    private int caseRecordId;
    private int caseRecordStatusId;
    private int caseId;
    private int userId;
    private String patientName;
    private String profilePic;
    private String healthCenter;
    private String caseRecordComplaint;
    private String caseRecordAdditionalNotes;
    private String caseRecordControlNumber;
    private String caseRecordStatus;
    private String caseRecordUpdatedOn;
    private int caseRecordUpdatedBy;
    private ArrayList<Attachment> attachments;
    private boolean checked = false;

    public CaseRecord() {

    }

    //constructor for getCaseRecordHistory db query function
    public CaseRecord(int caseRecordId, int caseRecordStatusId, int caseRecordUpdatedBy, String caseRecordUpdatedOn) {
        this.caseRecordId = caseRecordId;
        this.caseRecordStatusId = caseRecordStatusId;
        this.caseRecordUpdatedBy = caseRecordUpdatedBy;
        this.caseRecordUpdatedOn = caseRecordUpdatedOn;
    }

    public CaseRecord(int caseRecordId, String caseRecordComplaint, String caseRecordControlNumber) {
        this.caseRecordId = caseRecordId;
        this.caseRecordComplaint = caseRecordComplaint;
        this.caseRecordControlNumber = caseRecordControlNumber;
    }

    public CaseRecord(int caseRecordId, String patientName, String caseRecordComplaint, String caseRecordAdditionalNotes,
                      String healthCenter, String caseRecordStatus, String caseRecordUpdatedOn) {
        this.caseRecordId = caseRecordId;
        this.patientName = patientName;
        this.caseRecordComplaint = caseRecordComplaint;
        this.caseRecordAdditionalNotes = caseRecordAdditionalNotes;
        this.healthCenter = healthCenter;
        this.caseRecordStatus = caseRecordStatus;
        this.caseRecordUpdatedOn = caseRecordUpdatedOn;
    }

    public CaseRecord(int caseRecordId, int userId, String caseRecordComplaint, String caseRecordUpdatedOn) {
        this.caseRecordId = caseRecordId;
        this.userId = userId;
        this.caseRecordComplaint = caseRecordComplaint;
        this.caseRecordUpdatedOn = caseRecordUpdatedOn;
    }

    public CaseRecord(int caseRecordId, String complaint, int userId, String caseRecordControlNumber, String caseRecordAdditionalNotes) {
        this.caseRecordId = caseRecordId;
        this.caseRecordComplaint = complaint;
        this.userId = userId;
        this.caseRecordControlNumber = caseRecordControlNumber;
        this.caseRecordAdditionalNotes = caseRecordAdditionalNotes;
    }

    public CaseRecord(int caseRecordId, int caseId, int userId, int caseRecordStatusId, String caseRecordComplaint,
                      String caseRecordAdditionalNotes, String caseRecordControlNumber, String caseRecordUpdatedOn) {

        this.caseRecordId = caseRecordId;
        this.caseId = caseId;
        this.userId = userId;
        this.caseRecordStatusId = caseRecordStatusId;
        this.caseRecordComplaint = caseRecordComplaint;
        this.caseRecordAdditionalNotes = caseRecordAdditionalNotes;
        this.caseRecordControlNumber = caseRecordControlNumber;
        this.caseRecordUpdatedOn = caseRecordUpdatedOn;
    }


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setCaseRecordStatus(String caseRecordStatus) {
        this.caseRecordStatus = caseRecordStatus;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public void setCaseRecordId(int caseRecordId) {
        this.caseRecordId = caseRecordId;
    }

    public int getCaseRecordStatusId() {
        return caseRecordStatusId;
    }

    public int getCaseId() {
        return caseId;
    }

    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    public void setCaseRecordStatusId(int caseRecordStatusId) {
        this.caseRecordStatusId = caseRecordStatusId;
    }

    public void setCaseRecordComplaint(String caseRecordComplaint) {
        this.caseRecordComplaint = caseRecordComplaint;
    }

    public void setCaseRecordControlNumber(String caseRecordControlNumber) {
        this.caseRecordControlNumber = caseRecordControlNumber;
    }

    public String getCaseRecordUpdatedOn() {
        return caseRecordUpdatedOn;
    }

    public void setCaseRecordUpdatedOn(String caseRecordUpdatedOn) {
        this.caseRecordUpdatedOn = caseRecordUpdatedOn;
    }

    public int getCaseRecordUpdatedBy() {
        return caseRecordUpdatedBy;
    }

    public void setCaseRecordUpdatedBy(int caseRecordUpdatedBy) {
        this.caseRecordUpdatedBy = caseRecordUpdatedBy;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getHealthCenter() {
        return healthCenter;
    }

    public void setHealthCenter(String healthCenter) {
        this.healthCenter = healthCenter;
    }

    public int getCaseRecordId() {
        return caseRecordId;
    }

    public String getCaseRecordComplaint() {
        return caseRecordComplaint;
    }

    public String getCaseRecordControlNumber() {
        return caseRecordControlNumber;
    }

    public String getCaseRecordStatus() {
        return caseRecordStatus;
    }

    public String getCaseRecordAdditionalNotes() {
        return caseRecordAdditionalNotes;
    }

    public void setCaseRecordAdditionalNotes(String caseRecordAdditionalNotes) {
        this.caseRecordAdditionalNotes = caseRecordAdditionalNotes;
    }

    public ArrayList<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(ArrayList<Attachment> attachments) {
        this.attachments = attachments;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void toggleChecked() {
        checked = !checked;
    }
}
