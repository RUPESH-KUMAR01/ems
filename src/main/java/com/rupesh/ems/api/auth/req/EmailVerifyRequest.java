package com.rupesh.ems.api.auth.req;

public class EmailVerifyRequest {
    private String otp;
    
    public EmailVerifyRequest() {
        
    }
    public EmailVerifyRequest(String otp) {
        this.otp = otp;
    }


    public String getOtp() {
        return otp;
    }
}
