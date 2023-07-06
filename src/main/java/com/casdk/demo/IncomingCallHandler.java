package com.casdk.demo;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import org.springframework.cloud.function.adapter.azure.FunctionInvoker;

import java.util.Optional;

public class IncomingCallHandler extends FunctionInvoker<String, Object> {

        @FunctionName("incomingCall")
        public HttpResponseMessage execute(
                        @HttpTrigger(name = "incomingCall", methods = {
                                        HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
                        ExecutionContext context) {
                CALogger logger = CALogger.getInstance();
                logger.setContext(context);

                Optional<String> requestBodyOptional = request.getBody();
                if (requestBodyOptional.isEmpty()) {
                        logger.info("Looks like an empty request, no action is to be taken");
                        return null;
                }

                String requestBody = requestBodyOptional.get();

                logger.info("Incoming Call Request Body: " + requestBody);

                return request
                                .createResponseBuilder(HttpStatus.OK)
                                .body(handleRequest(requestBody, context))
                                .header("Content-Type", "application/json")
                                .build();
        }
}
