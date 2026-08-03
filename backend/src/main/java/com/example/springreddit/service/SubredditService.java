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
        if (request.getCreatorId() == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Create subreddit failed: creator ID is null");
            throw new IllegalArgumentException("Creator ID cannot be null");
        }
        String normalizedName = normalizeName(request.getSubredditName());
        if(subredditRepository.existsByName(normalizedName)){
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Create subreddit failed: subreddit already exists with name: {}", normalizedName);
            throw new IllegalArgumentException("Subreddit already exists");
        }

        Account creator = accountRepository.findById(request.getCreatorId())
                .orElseThrow(() -> {
                    com.example.springreddit.logging.CustomLogger.getInstance().warn("Create subreddit failed: creator not found with ID: {}", request.getCreatorId());
                    return new IllegalArgumentException("Creator not found");
                });

        Subreddit subreddit = new Subreddit();
        subreddit.setName(normalizedName);
        subreddit.setDescription(request.getDescription());
        subreddit.setCreator(creator);

        Subreddit savedSubreddit = subredditRepository.save(subreddit);
        com.example.springreddit.logging.CustomLogger.getInstance().info("Subreddit created successfully with ID: {} and name: {} by creator ID: {}", savedSubreddit.getId(), normalizedName, request.getCreatorId());
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

    public Subreddit editSubreddit(Long subredditId, SubredditDto.EditSubredditRequest request){
        if (subredditId == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit subreddit failed: subreddit ID is null");
            throw new IllegalArgumentException("Subreddit ID cannot be null");
        }
        if (request == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit subreddit failed: request is null");
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getAccountId() == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit subreddit failed: account ID is null");
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() -> {
                    com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit subreddit failed: subreddit not found with ID: {}", subredditId);
                    return new IllegalArgumentException("Subreddit not found");
                });

        if(subreddit.getCreator() == null || !subreddit.getCreator().getId().equals(request.getAccountId())){
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit subreddit failed: unauthorized access to subreddit ID: {} by account ID: {}", subredditId, request.getAccountId());
            throw new SecurityException("Only the subreddit creator can edit it");
        }

        subreddit.setName(request.getSubredditName());
        subreddit.setDescription(request.getDescription());

        subredditRepository.save(subreddit);
        com.example.springreddit.logging.CustomLogger.getInstance().info("Subreddit edited successfully with ID: {} by account ID: {}", subredditId, request.getAccountId());
        return subreddit;
    }

    public void deleteSubreddit(Long subredditId, Long accountId){
        if (subredditId == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete subreddit failed: subreddit ID is null");
            throw new IllegalArgumentException("Subreddit ID cannot be null");
        }
        if (accountId == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete subreddit failed: account ID is null");
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() -> {
                    com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete subreddit failed: subreddit not found with ID: {}", subredditId);
                    return new IllegalArgumentException("Subreddit not found");
                });

        if(subreddit.getCreator() == null || !subreddit.getCreator().getId().equals(accountId)){
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete subreddit failed: unauthorized access to subreddit ID: {} by account ID: {}", subredditId, accountId);
            throw new SecurityException("Only the subreddit creator can delete it");
        }

        subredditRepository.deleteById(subredditId);
        com.example.springreddit.logging.CustomLogger.getInstance().info("Subreddit deleted successfully with ID: {} by account ID: {}", subredditId, accountId);
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Subreddit name cannot be blank");
        }
        String trimmed = name.trim();
        return trimmed.startsWith("r/") ? trimmed : "r/" + trimmed;
    }
}
