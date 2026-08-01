package com.example.springreddit.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class LogBufferInitializer {

    private final LogBuffer logBuffer;

    public LogBufferInitializer(LogBuffer logBuffer) {
        this.logBuffer = logBuffer;
    }

    @PostConstruct
    public void initializeLogBuffer() {
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
            
            LogBufferAppender appender = new LogBufferAppender(logBuffer);
            appender.setContext(loggerContext);
            appender.setName("LogBuffer");
            
            PatternLayoutEncoder encoder = new PatternLayoutEncoder();
            encoder.setContext(loggerContext);
            encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n");
            encoder.start();
            
            appender.start();
            rootLogger.addAppender(appender);
            
            System.out.println("[LogBuffer] Appender initialized and attached to root logger");
        } catch (Exception e) {
            System.err.println("[LogBuffer] ERROR: Failed to initialize appender: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

