package com.qg.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MethodInvocation {
    private String methodName;
    private String projectId;
    private final AtomicInteger event = new AtomicInteger(0);

    public void incrementEvent(int delta) {
        event.addAndGet(delta);
    }

    public int getEventCount() {
        return event.get();
    }

}