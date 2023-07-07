package com.casdk.demo;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.azure.communication.callautomation.CallAutomationEventParser;
import com.azure.communication.callautomation.CallConnectionAsync;
import com.azure.communication.callautomation.models.AddParticipantOptions;
import com.azure.communication.callautomation.models.AddParticipantResult;
import com.azure.communication.callautomation.models.CallInvite;
import com.azure.communication.callautomation.models.CallMediaRecognizeDtmfOptions;
import com.azure.communication.callautomation.models.DtmfResult;
import com.azure.communication.callautomation.models.DtmfTone;
import com.azure.communication.callautomation.models.FileSource;
import com.azure.communication.callautomation.models.PlaySource;
import com.azure.communication.callautomation.models.PlayToAllOptions;
import com.azure.communication.callautomation.models.RecordingStateResult;
import com.azure.communication.callautomation.models.ServerCallLocator;
import com.azure.communication.callautomation.models.StartRecordingOptions;
import com.azure.communication.callautomation.models.TransferCallResult;
import com.azure.communication.callautomation.models.TransferCallToParticipantOptions;
import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import com.azure.communication.callautomation.models.events.CallConnected;
import com.azure.communication.callautomation.models.events.PlayCompleted;
import com.azure.communication.callautomation.models.events.RecognizeCompleted;
import com.azure.communication.callautomation.models.events.RecognizeFailed;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.http.rest.Response;
import com.casdk.demo.model.CallEventDto;

@Component
public class Calls implements Function<CallEventDto, Object> {

    private String applicationPhoneNumber = Constants.applicationPhoneNumber;
    private String phoneNumberToAddToCall = Constants.phoneNumberToAddToCall; // in format of +1...

    private void hangupAsync(String callConnectionId) {
        CallAutomationAsyncClientSingleton.getInstance().getCallConnectionAsync(callConnectionId)
                .hangUp(true).block();
    }

    @Override
    public Object apply(CallEventDto callEventDto) {
        CALogger logger = CALogger.getInstance();
        String requestBody = callEventDto.getBody();

        try {
            List<CallAutomationEventBase> acsEvents = CallAutomationEventParser.parseEvents(requestBody);

            PlaySource playSource = null;
            String callerId = callEventDto.getCallerId().replaceAll("\\s", "");
            logger.info("Caller ID: " + callerId);
            logger.info("Request Body: " + requestBody);

            for (CallAutomationEventBase acsEvent : acsEvents) {
                if (acsEvent instanceof CallConnected) {
                    logger.info("CallConnected");
                    CallConnected event = (CallConnected) acsEvent;

                    // Call was answered and is now established
                    String callConnectionId = event.getCallConnectionId();
                    logger.info("Call connected with ID: " + callConnectionId);

                    // Start recording
                    ServerCallLocator serverCallLocator = new ServerCallLocator(
                            CallAutomationAsyncClientSingleton.getInstance().getCallConnectionAsync(callConnectionId)
                                    .getCallProperties().block().getServerCallId());
                    StartRecordingOptions recordingOptions = new StartRecordingOptions(serverCallLocator);
                    Response<RecordingStateResult> response = CallAutomationAsyncClientSingleton.getInstance()
                            .getCallRecordingAsync().startWithResponse(recordingOptions).block();
                    logger.info("Start Recording with recording ID: " +
                            response.getValue().getRecordingId());

                    logger.info("Caller ID is: " + callerId);
                    CommunicationIdentifier target = PhoneNumberIdentifier.fromRawId(callerId);
                    logger.info("Target is: " + target);
                    logger.info("Target Details: " + target.getRawId());

                    // Play audio then recognize 1-digit DTMF input with pound (#) stop tone
                    playSource = new FileSource().setUrl(Constants.mainmenuAudio);
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
                    logger.info("RecognizeCompleted");
                    RecognizeCompleted event = (RecognizeCompleted) acsEvent;
                    // This RecognizeCompleted correlates to the previous action as per the
                    // OperationContext value
                    if (event.getOperationContext().equals("MainMenu")) {

                        DtmfTone tone = ((DtmfResult) event.getRecognizeResult().get()).getTones().get(0);
                        if (tone == DtmfTone.ONE) {
                            playSource = new FileSource().setUrl(Constants.salesAudio);
                        } else if (tone == DtmfTone.TWO) {
                            playSource = new FileSource().setUrl(Constants.marketingAudio);
                        } else if (tone == DtmfTone.THREE) {
                            playSource = new FileSource().setUrl(Constants.customercareAudio);

                            CallConnectionAsync callConnectionAsync = CallAutomationAsyncClientSingleton.getInstance()
                                    .getCallConnectionAsync(event.getCallConnectionId());

                            // Transfer call to another participant
                            PhoneNumberIdentifier target = new PhoneNumberIdentifier(phoneNumberToAddToCall);
                            TransferCallToParticipantOptions transferToParticipantCallOptions = new TransferCallToParticipantOptions(
                                    target);
                            // .setSourceCallerId(new PhoneNumberIdentifier(applicationPhoneNumber));
                            Response<TransferCallResult> addParticipantsResultResponse = callConnectionAsync
                                    .transferCallToParticipantWithResponse(transferToParticipantCallOptions).block();

                            logger.info(String.format("transferToParticipantCallOptions Response %s",
                                    CallAutomationAsyncClientSingleton.getResponse(addParticipantsResultResponse)));
                        } else if (tone == DtmfTone.FOUR) {
                            playSource = new FileSource().setUrl(Constants.agentAudio);

                            CallConnectionAsync callConnectionAsync = CallAutomationAsyncClientSingleton.getInstance()
                                    .getCallConnectionAsync(event.getCallConnectionId());

                            // Invite other participants to the call
                            PhoneNumberIdentifier source = new PhoneNumberIdentifier(applicationPhoneNumber);
                            PhoneNumberIdentifier target = new PhoneNumberIdentifier(phoneNumberToAddToCall);
                            List<CommunicationIdentifier> targets = new ArrayList<>(Arrays.asList(target));
                            AddParticipantOptions addParticipantsOptions = new AddParticipantOptions(
                                    new CallInvite(target, source));
                            Response<AddParticipantResult> addParticipantsResultResponse = callConnectionAsync
                                    .addParticipantWithResponse(addParticipantsOptions).block();

                            logger.info(String.format("addParticipants Response %s",
                                    CallAutomationAsyncClientSingleton.getResponse(addParticipantsResultResponse)));

                        } else if (tone == DtmfTone.FIVE) {
                            hangupAsync(event.getCallConnectionId());
                            break;
                        } else {
                            playSource = new FileSource().setUrl(Constants.invalidAudio);
                        }
                        String callConnectionId = event.getCallConnectionId();
                        PlayToAllOptions playToAllOptions = new PlayToAllOptions(playSource);
                        CallAutomationAsyncClientSingleton.getInstance().getCallConnectionAsync(callConnectionId)
                                .getCallMediaAsync().playToAllWithResponse(playToAllOptions).block();
                    }
                } else if (acsEvent instanceof RecognizeFailed) {
                    logger.info("RecognizeFailed");

                    RecognizeFailed event = (RecognizeFailed) acsEvent;
                    String callConnectionId = event.getCallConnectionId();
                    playSource = new FileSource().setUrl(Constants.invalidAudio);
                    PlayToAllOptions playToAllOptions = new PlayToAllOptions(playSource);
                    CallAutomationAsyncClientSingleton.getInstance().getCallConnectionAsync(callConnectionId)
                            .getCallMediaAsync().playToAllWithResponse(playToAllOptions).block();
                } else if (acsEvent instanceof PlayCompleted) {
                    logger.info("PlayComleted");

                    hangupAsync(acsEvent.getCallConnectionId());
                }

            }
        } catch (Exception e) {
            logger.info("Calls Exception: " + e.getMessage());
        }

        return "";
    }

}
