package com.casdk.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class CallEventDto {
    
    private String body;
    private String contextId;
    private String callerId;
    
}
