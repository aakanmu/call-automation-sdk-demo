package com.casdk.demo;

public class Constants {

    
    public static final String connectionString = "endpoint=https://acs-pstn-test-comm-resource.communication.azure.com/;accesskey=dzqWxHnrGtrgnm4os8EGbHB72a7fa/hGhzv8+oldAl/7FvGY6735QGRa2xSiISXBurn/177KPPHYrOSR21sXTQ==";// "<acs_resoure_connection_string>"; 
    public static final String callbackBaseUri = "https://ca-sdk-data-test.azurewebsites.net/api"; // "https://<functionAppName>.azurewebsites.net/api"; 
    
    public static final String agentAudio = callbackBaseUri + "/audio/agent.wav";
    public static final String customercareAudio = callbackBaseUri + "/audio/customercare.wav";
    public static final String invalidAudio = callbackBaseUri + "/audio/invalid.wav";
    public static final String mainmenuAudio = callbackBaseUri + "/audio/mainmenu.wav";
    public static final String marketingAudio = callbackBaseUri + "/audio/marketing.wav";
    public static final String salesAudio = callbackBaseUri + "/audio/sales.wav";
    public static final String applicationPhoneNumber = "+19078856191"; //"<application_phone_number>";
    public static final String phoneNumberToAddToCall = "+420776121792";
}
