package com.casdk.demo;

import com.microsoft.azure.functions.ExecutionContext;

import lombok.Data;

@Data
public class CALogger {

    private ExecutionContext context;
    private static CALogger instance = null;

    public static CALogger getInstance() {
        if (instance == null)
            instance = new CALogger();

        return instance;
    }

    public void info(String message) {
        context.getLogger().info(Thread.currentThread().getStackTrace()[2].getClassName() + ": " + message);
    }

}
