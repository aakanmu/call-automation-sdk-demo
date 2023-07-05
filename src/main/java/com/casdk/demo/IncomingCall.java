package com.casdk.demo;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.communication.callautomation.models.AnswerCallResult;
import com.azure.messaging.eventgrid.systemevents.SubscriptionValidationEventData;
import com.azure.messaging.eventgrid.systemevents.SubscriptionValidationResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Component
public class IncomingCall implements Function<String, Object> {

    Logger logger = Logger.getLogger(IncomingCall.class.getName());

    @Override
    public Object apply(String body) {

       System.out.println("applied body is >>>> " + body);
        List<EventGridEvent> eventGridEvents = EventGridEvent.fromString(body);

        for (EventGridEvent eventGridEvent : eventGridEvents) {
            logger.log(Level.INFO, "event grid type = " + eventGridEvent.getEventType());

            // Handle the subscription validation event
            if (eventGridEvent.getEventType().equals("Microsoft.EventGrid.SubscriptionValidationEvent")) {
                SubscriptionValidationEventData subscriptionValidationEventData = eventGridEvent.getData()
                        .toObject(SubscriptionValidationEventData.class);
                SubscriptionValidationResponse subscriptionValidationResponse = new SubscriptionValidationResponse()
                        .setValidationResponse(subscriptionValidationEventData.getValidationCode());
                logger.log(Level.INFO, "validation successful -> " + subscriptionValidationResponse);
                return subscriptionValidationResponse;
            }

            logger.log(Level.INFO, "processing json data");
            // Answer the incoming call and pass the callbackUri where Call Automation
            // events will be delivered
            JsonObject data = new Gson().fromJson(eventGridEvent.getData().toString(), JsonObject.class); // Extract
                                                                                                          // body of
                                                                                                          // the
                                                                                                          // event
            String incomingCallContext = data.get("incomingCallContext").getAsString(); // Query the incoming call
                                                                                        // context info for
                                                                                        // answering
            String callerId = data.getAsJsonObject("from").get("rawId").getAsString(); // Query the id of caller for
                                                                                       // preparing the Recognize
                                                                                       // prompt.

            // Call events of this call will be sent to an url with unique id.
            String callbackUri = Constants.callbackBaseUri
                    + String.format("/calls/%s?callerId=%s", UUID.randomUUID(), callerId);

            logger.log(Level.INFO, "callbackUri is >>>> " + callbackUri);
            logger.log(Level.INFO, "incomingCallContext = " + incomingCallContext);

            AnswerCallResult answerCallResult = CallAutomationAsyncClientSingleton.getInstance()
                    .answerCall(incomingCallContext, callbackUri).block();
        }

        return "";
    }

}
