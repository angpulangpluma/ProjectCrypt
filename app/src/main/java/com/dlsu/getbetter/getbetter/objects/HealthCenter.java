package com.dlsu.getbetter.getbetter.objects;

/**
 * Created by mikedayupay on 17/02/2016.
 * GetBetter 2016
 */
public class HealthCenter {

    private int healthCenterId;
    private String healthCenterName;

    public HealthCenter(int healthCenterId, String healthCenterName) {
        this.healthCenterId = healthCenterId;
        this.healthCenterName = healthCenterName;
    }

    public int getHealthCenterId() {
        return healthCenterId;
    }

    public void setHealthCenterId(int healthCenterId) {
        this.healthCenterId = healthCenterId;
    }

    public String getHealthCenterName() {
        return healthCenterName;
    }

    public void setHealthCenterName(String healthCenterName) {
        this.healthCenterName = healthCenterName;
    }
}
