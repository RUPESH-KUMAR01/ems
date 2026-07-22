package com.rupesh.ems.configs;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public class TwilioConfiguration {
    @NotBlank @JsonProperty("accountSid") public String ACCOUNT_SID;
    
    @NotBlank @JsonProperty("authToken") public String AUTH_TOKEN;

    @NotBlank @JsonProperty("fromPhoneNumber") public String FROM_PHONE_NUMBER;

    public TwilioConfiguration(){}

    public String getACCOUNT_SID() {
        return ACCOUNT_SID;
    }

    public void setACCOUNT_SID(String ACCOUNT_SID) {
        this.ACCOUNT_SID = ACCOUNT_SID;
    }

    public String getAUTH_TOKEN() {
        return AUTH_TOKEN;
    }

    public void setAUTH_TOKEN(String AUTH_TOKEN) {
        this.AUTH_TOKEN = AUTH_TOKEN;
    }

    public String getFROM_PHONE_NUMBER() {
        return FROM_PHONE_NUMBER;
    }

    public void setFROM_PHONE_NUMBER(String FROM_PHONE_NUMBER) {
        this.FROM_PHONE_NUMBER = FROM_PHONE_NUMBER;
    }
}
