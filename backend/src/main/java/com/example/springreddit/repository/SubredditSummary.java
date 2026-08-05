package com.example.springreddit.repository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface SubredditSummary {
    UUID getId();
    String getName();
    String getDescription();
    String getDisplayName();
    String getIconURL();
    long getMemberCount();
    Long getCreatorId();
    String getCreatorUsername();
    LocalDateTime getCreatedAt();
    long getPostCount();
}
