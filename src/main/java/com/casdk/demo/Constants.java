package com.casdk.demo;

public class Constants {

    public static final String connectionString = "<acs_resoure_connection_string>"; 
    public static final String callbackBaseUri = "https://<functionAppName>.azurewebsites.net/api"; 
    
    public static final String agentAudio = callbackBaseUri + "/audio/agent.wav";
    public static final String customercareAudio = callbackBaseUri + "/audio/customercare.wav";
    public static final String invalidAudio = callbackBaseUri + "/audio/invalid.wav";
    public static final String mainmenuAudio = callbackBaseUri + "/audio/mainmenu.wav";
    public static final String marketingAudio = callbackBaseUri + "/audio/marketing.wav";
    public static final String salesAudio = callbackBaseUri + "/audio/sales.wav";
    public static final String applicationPhoneNumber = "<application_phone_number>";
    public static final String phoneNumberToAddToCall = "<phone_number_for_add_participant_or_transfer>";

}
