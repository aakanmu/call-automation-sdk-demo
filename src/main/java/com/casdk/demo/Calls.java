package com.casdk.demo;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.azure.communication.callautomation.CallConnectionAsync;
import com.azure.communication.callautomation.EventHandler;
import com.azure.communication.callautomation.models.AddParticipantsOptions;
import com.azure.communication.callautomation.models.AddParticipantsResult;
import com.azure.communication.callautomation.models.CallMediaRecognizeDtmfOptions;
import com.azure.communication.callautomation.models.FileSource;
import com.azure.communication.callautomation.models.PlayOptions;
import com.azure.communication.callautomation.models.PlaySource;
import com.azure.communication.callautomation.models.RecordingStateResult;
import com.azure.communication.callautomation.models.ServerCallLocator;
import com.azure.communication.callautomation.models.StartRecordingOptions;
import com.azure.communication.callautomation.models.TransferCallResult;
import com.azure.communication.callautomation.models.TransferToParticipantCallOptions;
import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import com.azure.communication.callautomation.models.events.CallConnected;
import com.azure.communication.callautomation.models.events.PlayCompleted;
import com.azure.communication.callautomation.models.events.RecognizeCompleted;
import com.azure.communication.callautomation.models.events.RecognizeFailed;
import com.azure.communication.callautomation.models.DtmfTone;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.http.rest.Response;
import com.casdk.demo.model.CallEventDto;

@Component
public class Calls implements Function<CallEventDto, Object> {

    private String applicationPhoneNumber = Constants.applicationPhoneNumber;
    private String phoneNumberToAddToCall = Constants.phoneNumberToAddToCall; // in format of +1...

    private void hangupAsync(String callConnectionId) {
        System.out.println("Performing Hangup operation");
        CallAutomationAsyncClientSingleton.getInstance().getCallConnectionAsync(callConnectionId)
                .hangUp(true).block();
    }

    @Override
    public Object apply(CallEventDto callEventDto) {
        System.out.println("in here we are");

        String requestBody = callEventDto.getBody();

        List<CallAutomationEventBase> acsEvents = EventHandler.parseEventList(requestBody);

        PlaySource playSource = null;
        String callerId = callEventDto.getCallerId().replaceAll("\\s", "");
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>requestBody");
        System.out.println(requestBody);
        System.out.println(callerId);

        for (CallAutomationEventBase acsEvent : acsEvents) {
            if (acsEvent instanceof CallConnected) {
                System.out.println("CallConnected");
                CallConnected event = (CallConnected) acsEvent;

                // Call was answered and is now established
                String callConnectionId = event.getCallConnectionId();

                // Start recording
                ServerCallLocator serverCallLocator = new ServerCallLocator(
                        CallAutomationAsyncClientSingleton.getInstance().getCallConnectionAsync(callConnectionId)
                                .getCallProperties().block().getServerCallId());
                // StartRecordingOptions recordingOptions = new
                // StartRecordingOptions(serverCallLocator);
                // Response<RecordingStateResult> response =
                // CallAutomationAsyncClientSingleton.getInstance()
                // .getCallRecordingAsync().startRecordingWithResponse(recordingOptions).block();
                // System.out.println("Start Recording with recording ID: " +
                // response.getValue().getRecordingId());

                CommunicationIdentifier target = CommunicationIdentifier.fromRawId(callerId);

                // Play audio then recognize 1-digit DTMF input with pound (#) stop tone
                playSource = new FileSource().setUri(Constants.mainmenuAudio);
                CallMediaRecognizeDtmfOptions recognizeOptions = new CallMediaRecognizeDtmfOptions(target, 1);
                recognizeOptions.setInterToneTimeout(Duration.ofSeconds(10))
                        .setStopTones(new ArrayList<>(Arrays.asList(DtmfTone.POUND)))
                        .setInitialSilenceTimeout(Duration.ofSeconds(5))
                        .setInterruptPrompt(true)
                        .setPlayPrompt(playSource)
                        .setOperationContext("MainMenu");

                CallAutomationAsyncClientSingleton.getInstance().getCallConnectionAsync(callConnectionId)
                        .getCallMediaAsync()
                        .startRecognizing(recognizeOptions)
                        .block();
            } else if (acsEvent instanceof RecognizeCompleted) {
                System.out.println("RecognizeCompleted");
                RecognizeCompleted event = (RecognizeCompleted) acsEvent;
                // This RecognizeCompleted correlates to the previous action as per the
                // OperationContext value
                if (event.getOperationContext().equals("MainMenu")) {

                    DtmfTone tone = event.getCollectTonesResult().getTones().get(0);
                    if (tone == DtmfTone.ONE) {
                        playSource = new FileSource().setUri(Constants.salesAudio);
                    } else if (tone == DtmfTone.TWO) {
                        playSource = new FileSource().setUri(Constants.marketingAudio);
                    } else if (tone == DtmfTone.THREE) {
                        playSource = new FileSource().setUri(Constants.customercareAudio);

                        CallConnectionAsync callConnectionAsync = CallAutomationAsyncClientSingleton.getInstance()
                                .getCallConnectionAsync(event.getCallConnectionId());

                        // Transfer call to another participant
                        CommunicationIdentifier target = new PhoneNumberIdentifier(phoneNumberToAddToCall);
                        TransferToParticipantCallOptions transferToParticipantCallOptions = new TransferToParticipantCallOptions(target)
                                .setSourceCallerId(new PhoneNumberIdentifier(applicationPhoneNumber));
                        Response<TransferCallResult> addParticipantsResultResponse = callConnectionAsync
                                .transferToParticipantCallWithResponse(transferToParticipantCallOptions).block();

                        System.out.println(String.format("transferToParticipantCallOptions Response %s",
                                CallAutomationAsyncClientSingleton.getResponse(addParticipantsResultResponse)));
                    } else if (tone == DtmfTone.FOUR) {
                        playSource = new FileSource().setUri(Constants.agentAudio);

                        CallConnectionAsync callConnectionAsync = CallAutomationAsyncClientSingleton.getInstance()
                                .getCallConnectionAsync(event.getCallConnectionId());

                        // Invite other participants to the call
                        CommunicationIdentifier target = new PhoneNumberIdentifier(phoneNumberToAddToCall);
                        List<CommunicationIdentifier> targets = new ArrayList<>(Arrays.asList(target));
                        AddParticipantsOptions addParticipantsOptions = new AddParticipantsOptions(targets)
                                .setSourceCallerId(new PhoneNumberIdentifier(applicationPhoneNumber));
                        Response<AddParticipantsResult> addParticipantsResultResponse = callConnectionAsync
                                .addParticipantsWithResponse(addParticipantsOptions).block();

                        System.out.println(String.format("addParticipants Response %s",
                                CallAutomationAsyncClientSingleton.getResponse(addParticipantsResultResponse)));

                    } else if (tone == DtmfTone.FIVE) {
                        hangupAsync(event.getCallConnectionId());
                        break;
                    } else {
                        playSource = new FileSource().setUri(Constants.invalidAudio);
                    }
                    String callConnectionId = event.getCallConnectionId();
                    CallAutomationAsyncClientSingleton.getInstance().getCallConnectionAsync(callConnectionId)
                            .getCallMediaAsync().playToAllWithResponse(playSource, new PlayOptions()).block();
                }
            } else if (acsEvent instanceof RecognizeFailed) {
                System.out.println("RecognizeFailed");

                RecognizeFailed event = (RecognizeFailed) acsEvent;
                String callConnectionId = event.getCallConnectionId();
                playSource = new FileSource().setUri(Constants.invalidAudio);
                CallAutomationAsyncClientSingleton.getInstance().getCallConnectionAsync(callConnectionId)
                        .getCallMediaAsync().playToAllWithResponse(playSource, new PlayOptions()).block();
            } else if (acsEvent instanceof PlayCompleted) {
                System.out.println("PlayComleted");

                hangupAsync(acsEvent.getCallConnectionId());
            }

        }

        return "";
    }

}
