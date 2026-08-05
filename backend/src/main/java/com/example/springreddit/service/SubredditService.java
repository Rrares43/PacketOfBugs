package com.example.springreddit.service;

import com.example.springreddit.dto.SubredditDto;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.Subreddit;
import com.example.springreddit.repository.AccountRepository;
import com.example.springreddit.repository.SubredditRepository;
import com.example.springreddit.repository.SubredditSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SubredditService {
    @Autowired
    private SubredditRepository subredditRepository;

    @Autowired
    private AccountRepository accountRepository;

    public Subreddit createSubreddit(SubredditDto.CreateSubredditRequest request){
        if (request == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Create subreddit failed: request is null");
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.creatorId() == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Create subreddit failed: creator ID is null");
            throw new IllegalArgumentException("Creator ID cannot be null");
        }
        String normalizedName = normalizeName(request.name());
        if(subredditRepository.existsByName(normalizedName)){
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Create subreddit failed: subreddit already exists with name: {}", normalizedName);
            throw new IllegalArgumentException("Subreddit already exists");
        }

        Account creator = accountRepository.findById(request.creatorId())
                .orElseThrow(() -> {
                    com.example.springreddit.logging.CustomLogger.getInstance().warn("Create subreddit failed: creator not found with ID: {}", request.creatorId());
                    return new IllegalArgumentException("Creator not found");
                });

        Subreddit subreddit = new Subreddit();
        subreddit.setName(normalizedName);
        subreddit.setDescription(request.description());
        subreddit.setCreator(creator);

        Subreddit savedSubreddit = subredditRepository.save(subreddit);
        com.example.springreddit.logging.CustomLogger.getInstance().info("Subreddit created successfully with ID: {} and name: {} by creator ID: {}", savedSubreddit.getId(), normalizedName, request.creatorId());
        return savedSubreddit;
    }

    public List<SubredditSummary> getAllSubredditSummaries() {
        return subredditRepository.findAllSummaries();
    }

    public List<Subreddit> getSubredditsByCreatorUsername(String username) {
        return subredditRepository.findByCreator_Username(username);
    }

    public Subreddit getSubredditByName(String subredditName){
        if (subredditName == null || subredditName.isBlank()) {
            throw new IllegalArgumentException("Subreddit name cannot be blank");
        }
        String searchName = normalizeName(subredditName);
        return subredditRepository.findByName(searchName)
                .orElseThrow(() -> new IllegalArgumentException("Subreddit not found"));
    }

    public Subreddit editSubreddit(String name, SubredditDto.EditSubredditRequest request){
        if (name == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit subreddit failed: subreddit name is null");
            throw new IllegalArgumentException("Subreddit name cannot be null");
        }
        if (request == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit subreddit failed: request is null");
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.creatorId() == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit subreddit failed: account ID is null");
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        Subreddit subreddit = subredditRepository.findByName(name)
                .orElseThrow(() -> {
                    com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit subreddit failed: subreddit not found with name: {}", name);
                    return new IllegalArgumentException("Subreddit not found");
                });

        if(subreddit.getCreator() == null || !subreddit.getCreator().getId().equals(request.creatorId())){
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit subreddit failed: unauthorized access to subreddit name: {} by account ID: {}", name, request.creatorId());
            throw new SecurityException("Only the subreddit creator can edit it");
        }

        subreddit.setName(request.displayName());
        subreddit.setDescription(request.description());

        subredditRepository.save(subreddit);
        com.example.springreddit.logging.CustomLogger.getInstance().info("Subreddit edited successfully with ID: {} by account ID: {}", name, request.creatorId());
        return subreddit;
    }

    public void deleteSubreddit(String name, Long accountId){
        if (name == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete subreddit failed: subreddit ID is null");
            throw new IllegalArgumentException("Subreddit ID cannot be null");
        }
        if (accountId == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete subreddit failed: account ID is null");
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        Subreddit subreddit = subredditRepository.findByName(name)
                .orElseThrow(() -> {
                    com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete subreddit failed: subreddit not found with name: {}", name);
                    return new IllegalArgumentException("Subreddit not found");
                });

        if(subreddit.getCreator() == null || !subreddit.getCreator().getId().equals(accountId)){
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete subreddit failed: unauthorized access to subreddit name: {} by account ID: {}", name, accountId);
            throw new SecurityException("Only the subreddit creator can delete it");
        }

        subredditRepository.deleteByName(name);
        com.example.springreddit.logging.CustomLogger.getInstance().info("Subreddit deleted successfully with name: {} by account ID: {}", name, accountId);
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Subreddit name cannot be blank");
        }
        String trimmed = name.trim();
        return trimmed.startsWith("r/") ? trimmed : "r/" + trimmed;
    }
}
