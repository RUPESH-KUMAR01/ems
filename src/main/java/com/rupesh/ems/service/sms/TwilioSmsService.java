package com.rupesh.ems.service.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rupesh.ems.configs.TwilioConfiguration;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class TwilioSmsService implements SmsService{
    private final String ACCOUNT_SID;
    private final String AUTH_TOKEN;
    private final String FROM_PHONE_NUMBER;
    private static final Logger LOGGER = LoggerFactory.getLogger(TwilioSmsService.class);
    public TwilioSmsService(TwilioConfiguration twilioConfiguration){
        this.ACCOUNT_SID=twilioConfiguration.getACCOUNT_SID();
        this.AUTH_TOKEN=twilioConfiguration.getAUTH_TOKEN();
        this.FROM_PHONE_NUMBER=twilioConfiguration.getFROM_PHONE_NUMBER();
    }
    
    @Override
    public void sendOtp(String phoneNumber, String message) {
        LOGGER.info("Sending OTP to phone number: " + phoneNumber);
        Twilio.init(ACCOUNT_SID,AUTH_TOKEN);
        Message smsMessage=Message.creator(new PhoneNumber(phoneNumber),new PhoneNumber(FROM_PHONE_NUMBER),message).create();
        LOGGER.info("OTP sent successfully to phone number: " + phoneNumber);
    }
}
