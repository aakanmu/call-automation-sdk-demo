package com.casdk.demo;

import com.azure.communication.callautomation.CallAutomationAsyncClient;
import com.azure.communication.callautomation.CallAutomationClientBuilder;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.rest.Response;

public class CallAutomationAsyncClientSingleton {

    private static CallAutomationAsyncClient instance = null;

    public static CallAutomationAsyncClient getInstance() {
        if (instance == null) {
            instance = new CallAutomationClientBuilder()
                    .connectionString(Constants.connectionString)
                    .buildAsyncClient();
        }

        return instance;
    }

    public static String getResponse(Response<?> response) {
        StringBuilder responseString;
        responseString = new StringBuilder("StatusCode: " + response.getStatusCode() + ", Headers: { ");

        for (HttpHeader header : response.getHeaders()) {
            responseString.append(header.getName()).append(":").append(header.getValue()).append(", ");
        }
        responseString.append("} ");
        return responseString.toString();
    }
}
