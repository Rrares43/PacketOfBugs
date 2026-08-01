package com.example.springreddit.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import org.springframework.stereotype.Component;

@Component
public class LogBuffer {
    private static final int MAX_LOGS = 50;
    private static final DateTimeFormatter TIMESTAMP = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<String> logs = Collections.synchronizedList(new ArrayList<>());

    public synchronized void addLog(String level, String message) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP);
        String formattedLog = "[" + timestamp + "] [" + level + "] " + message;
        
        logs.add(formattedLog);
        if (logs.size() > MAX_LOGS) {
            logs.remove(0);
        }
        
    }

    public List<String> getLogs() {
        return new ArrayList<>(logs);
    }

    public void clear() {
        logs.clear();
    }
    
    public int getLogCount() {
        return logs.size();
    }
}

