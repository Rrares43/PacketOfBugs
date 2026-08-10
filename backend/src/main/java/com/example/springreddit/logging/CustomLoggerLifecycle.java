package com.example.springreddit.logging;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
class CustomLoggerLifecycle {

    @PreDestroy
    void onDestroy() {
        CustomLogger.getInstance().shutdownGracefully();
    }
}
