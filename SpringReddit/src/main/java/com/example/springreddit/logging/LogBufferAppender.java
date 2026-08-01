package com.example.springreddit.logging;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class LogBufferAppender extends AppenderBase<ILoggingEvent> {

    private final LogBuffer logBuffer;

    public LogBufferAppender(LogBuffer logBuffer) {
        this.logBuffer = logBuffer;
    }

    @Override
    protected void append(ILoggingEvent event) {
        try {
            String levelName = event.getLevel().toString();
            String message = event.getFormattedMessage();
            logBuffer.addLog(levelName, message);
        } catch (Exception e) {
            System.err.println("Failed to append to LogBuffer: " + e.getMessage());
        }
    }
}
