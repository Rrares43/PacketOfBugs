package com.example.springreddit.repository;

import java.time.LocalDateTime;

public interface SubredditSummary {
    Long getId();
    String getName();
    String getDescription();
    Long getCreatorId();
    String getCreatorUsername();
    LocalDateTime getCreatedAt();
    long getPostCount();
}
