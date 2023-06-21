package com.casdk.demo;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.SystemEventNames;
import com.azure.messaging.eventgrid.systemevents.AcsRecordingChunkInfoProperties;
import com.azure.messaging.eventgrid.systemevents.AcsRecordingFileStatusUpdatedEventData;
import com.azure.messaging.eventgrid.systemevents.SubscriptionValidationEventData;
import com.azure.messaging.eventgrid.systemevents.SubscriptionValidationResponse;

@Component
public class Recording implements Function<String, Object> {

    Logger logger = Logger.getLogger(Recording.class.getName());

    @Override
    public Object apply(String requestBody) {
        List<EventGridEvent> eventGridEvents = EventGridEvent.fromString(requestBody);

        for (EventGridEvent eventGridEvent : eventGridEvents) {
            if (eventGridEvent.getEventType().equals(SystemEventNames.EVENT_GRID_SUBSCRIPTION_VALIDATION)) {
                try {

                    SubscriptionValidationEventData subscriptionValidationEvent = eventGridEvent.getData()
                            .toObject(SubscriptionValidationEventData.class);
                    SubscriptionValidationResponse responseData = new SubscriptionValidationResponse();
                    responseData.setValidationResponse(subscriptionValidationEvent.getValidationCode());

                    return responseData;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            if (eventGridEvent.getEventType().equals(SystemEventNames.COMMUNICATION_RECORDING_FILE_STATUS_UPDATED)) {
                try {
                    AcsRecordingFileStatusUpdatedEventData event = eventGridEvent
                            .getData()
                            .toObject(AcsRecordingFileStatusUpdatedEventData.class);

                    AcsRecordingChunkInfoProperties recordingChunk = event
                            .getRecordingStorageInfo()
                            .getRecordingChunks().get(0);

                    String fileName = String.format("%s.mp4", recordingChunk.getDocumentId());
                    Response<BinaryData> downloadResponse = CallAutomationAsyncClientSingleton.getInstance()
                            .getCallRecordingAsync()
                            .downloadContentWithResponse(recordingChunk.getContentLocation(), null).block();

                    FileOutputStream fos = new FileOutputStream(new File(fileName));
                    fos.write(downloadResponse.getValue().toBytes());

                    logger.log(Level.INFO,
                            String.format("Download media response --> %s",
                                    CallAutomationAsyncClientSingleton.getResponse(downloadResponse)));
                    logger.log(Level.INFO,
                            String.format("successfully downloaded recording file here: %s ", fileName));

                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.log(Level.SEVERE, e.getMessage());
                    return null;
                }
            } else {
                return eventGridEvent.getEventType() + " is not handled.";
            }
        }

        return "Event count is not available.";
    }

}
