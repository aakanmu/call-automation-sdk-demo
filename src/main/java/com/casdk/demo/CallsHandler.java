package com.casdk.demo;

import java.util.Optional;

import org.springframework.cloud.function.adapter.azure.FunctionInvoker;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.casdk.demo.model.CallEventDto;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

public class CallsHandler extends FunctionInvoker<CallEventDto, Object> {

    @FunctionName("calls")
    public HttpResponseMessage execute(
            @HttpTrigger(name = "callEvent", methods = {
                    HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS, route = "calls/{contextId}") HttpRequestMessage<Optional<String>> request,
            @BindingName("contextId") String contextId,
            ExecutionContext context) {

        CALogger logger = CALogger.getInstance();
        logger.setContext(context);

        Optional<String> requestBodyOptional = request.getBody();
        if (requestBodyOptional.isEmpty()) {
            context.getLogger().info("Looks like an empty call event, no action is to be taken");
            return null;
        }

        String requestBody = requestBodyOptional.get();
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(request.getUri().toString()).build();
        MultiValueMap<String, String> queryParams = uriComponents.getQueryParams();
        String callerId = queryParams.getFirst("callerId");

        CallEventDto callEventDto = new CallEventDto(requestBody, contextId, callerId);

        logger.info("Calls Event Caller ID: " + callerId);
        logger.info("Calls Event Request Body: " + callEventDto.getBody());

        return request
                .createResponseBuilder(HttpStatus.OK)
                .body(handleRequest(callEventDto, context))
                .header("Content-Type", "application/json")
                .build();
    }
}
