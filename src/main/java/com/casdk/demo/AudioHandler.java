package com.casdk.demo;

import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.springframework.cloud.function.adapter.azure.FunctionInvoker;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

public class AudioHandler extends FunctionInvoker<String, byte[]> {

    @FunctionName("audio")
    public HttpResponseMessage execute(
            @HttpTrigger(name = "audio", methods = {
                    HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS, route = "audio/{fileName}") HttpRequestMessage<Optional<String>> request,
            @BindingName("fileName") String fileName,
            ExecutionContext context) {

        context.getLogger().info("Audio filename: " + fileName);

        String filePath = "audio/" + fileName;
        byte[] audioBytes;

        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream(filePath);

            audioBytes = IOUtils.toByteArray(in);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("Unable to read audio file >>>" + e.getMessage());
            return null;
        }
        
        
        return request
                .createResponseBuilder(HttpStatus.OK)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Content-Length", String.valueOf(audioBytes.length))
                .header("Content-Type", "audio/x-wav")
                .body(audioBytes)
                .build();
    }

}
