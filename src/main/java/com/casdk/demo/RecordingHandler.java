package com.casdk.demo;

import java.util.Optional;

import org.springframework.cloud.function.adapter.azure.FunctionInvoker;

import com.casdk.demo.model.CallEventDto;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

public class RecordingHandler extends FunctionInvoker<String, Object> {

    @FunctionName("recording")
    public HttpResponseMessage execute(
            @HttpTrigger(name = "recording", methods = {
                    HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            ExecutionContext context) {
        Optional<String> requestBodyOptional = request.getBody();
        if (requestBodyOptional.isEmpty()) {
            context.getLogger().info("Looks like an empty request, no action is to be taken");
            return null;
        }

        String requestBody = requestBodyOptional.get();

        context.getLogger().info("Recording Request Body: " + requestBody);

        return request
                .createResponseBuilder(HttpStatus.OK)
                .body(handleRequest(requestBody, context))
                .header("Content-Type", "application/json")
                .build();
    }
}
