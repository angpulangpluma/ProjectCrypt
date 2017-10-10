package com.dlsu.getbetter.getbetter.objects;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import java.util.StringTokenizer;

/**
 * Created by mikedayupay on 27/01/2016.
 * GetBetter 2016
 */
public class Patient {

    private long id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String birthdate;
    private String gender;
    private String civilStatus;
    private String bloodType;
    private String profileImageBytes;
    private String dateLastConsultation;
    private boolean checked = false;


    public Patient(long id, String firstName, String middleName, String lastName,
                   String birthdate, String gender, String civilStatus, String bloodType,
                   String profileImageBytes) {
        this.id = id;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.birthdate = birthdate;
        this.gender = gender;
        this.civilStatus = civilStatus;
        this.bloodType = bloodType;
        this.profileImageBytes = profileImageBytes;
    }

    public Patient(String firstName, String middleName, String lastName, String birthdate,
                   String gender, String civilStatus, String profileImageBytes) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.birthdate = birthdate;
        this.gender = gender;
        this.civilStatus = civilStatus;
        this.profileImageBytes = profileImageBytes;
    }

    public long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public String getAge() {

        try {

            int[] birthdateTemp = new int[3];
            StringTokenizer tok = new StringTokenizer(getBirthdate(), "-");
            int i = 0;
            while(tok.hasMoreTokens()) {

                birthdateTemp[i] = Integer.parseInt(tok.nextToken());
                i++;
            }

            LocalDate birthdate = new LocalDate(birthdateTemp[0], birthdateTemp[1], birthdateTemp[2]);
            LocalDate now = new LocalDate();

            Years years = Years.yearsBetween(birthdate, now);
            return years.getYears() + "";

        }catch (NullPointerException e) {

            e.printStackTrace();
            return String.valueOf(0);
        }
    }

    public String getDateLastConsultation() {
        return dateLastConsultation;
    }

    public void setDateLastConsultation(String dateLastConsultation) {
        this.dateLastConsultation = dateLastConsultation;
    }

    public String getGender() {
        return gender;
    }

    public String getCivilStatus() {
        return civilStatus;
    }

    public String getBloodType() {
        return bloodType;
    }

    public String getProfileImageBytes() {
        return profileImageBytes;
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
