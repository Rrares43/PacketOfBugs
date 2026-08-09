package com.example.springreddit.mapper;

import com.example.springreddit.dto.SubredditDto;
import com.example.springreddit.model.Subreddit;
import com.example.springreddit.repository.SubredditSummary;
import org.springframework.stereotype.Component;

@Component
public class SubredditMapper {

    public SubredditDto.SubredditResponse mapToResponse(Subreddit subreddit) {
        return new SubredditDto.SubredditResponse(
                subreddit.getId(),
                subreddit.getName(),
                subreddit.getDisplayName(),
                subreddit.getDescription(),
                subreddit.getMemberCount(),
                subreddit.getPostCount(),
                subreddit.getIconURL(),
                subreddit.getCreatedAt()
        );
    }

    public SubredditDto.SubredditResponse mapSummaryToResponse(SubredditSummary summary) {
        return new SubredditDto.SubredditResponse(
                summary.getId(),
                summary.getName(),
                summary.getDisplayName(),
                summary.getDescription(),
                summary.getMemberCount(),
                summary.getPostCount(),
                summary.getIconURL(),
                summary.getCreatedAt()
        );
    }
}
