package com.example.springreddit.service;

import com.example.springreddit.dto.SubredditDto;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.Subreddit;
import com.example.springreddit.repository.AccountRepository;
import com.example.springreddit.repository.SubredditRepository;
import com.example.springreddit.repository.SubredditSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SubredditService {
    @Autowired
    private SubredditRepository subredditRepository;

    @Autowired
    private AccountRepository accountRepository;

    public Subreddit createSubreddit(SubredditDto.CreateSubredditRequest request){
        if (request == null) {
            log.warn("Create subreddit failed: request is null");
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getCreatorId() == null) {
            log.warn("Create subreddit failed: creator ID is null");
            throw new IllegalArgumentException("Creator ID cannot be null");
        }
        String normalizedName = normalizeName(request.getSubredditName());
        if(subredditRepository.existsByName(normalizedName)){
            log.warn("Create subreddit failed: subreddit already exists with name: {}", normalizedName);
            throw new IllegalArgumentException("Subreddit already exists");
        }

        Account creator = accountRepository.findById(request.getCreatorId())
                .orElseThrow(() -> {
                    log.warn("Create subreddit failed: creator not found with ID: {}", request.getCreatorId());
                    return new IllegalArgumentException("Creator not found");
                });

        Subreddit subreddit = new Subreddit();
        subreddit.setName(normalizedName);
        subreddit.setDescription(request.getDescription());
        subreddit.setCreator(creator);

        Subreddit savedSubreddit = subredditRepository.save(subreddit);
        log.info("Subreddit created successfully with ID: {} and name: {} by creator ID: {}", savedSubreddit.getId(), normalizedName, request.getCreatorId());
        return savedSubreddit;
    }

    public List<Subreddit> getAllSubreddits(){
        return subredditRepository.findAll();
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
            log.warn("Edit subreddit failed: subreddit ID is null");
            throw new IllegalArgumentException("Subreddit ID cannot be null");
        }
        if (request == null) {
            log.warn("Edit subreddit failed: request is null");
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getAccountId() == null) {
            log.warn("Edit subreddit failed: account ID is null");
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() -> {
                    log.warn("Edit subreddit failed: subreddit not found with ID: {}", subredditId);
                    return new IllegalArgumentException("Subreddit not found");
                });

        if(subreddit.getCreator() == null || !subreddit.getCreator().getId().equals(request.getAccountId())){
            log.warn("Edit subreddit failed: unauthorized access to subreddit ID: {} by account ID: {}", subredditId, request.getAccountId());
            throw new SecurityException("Only the subreddit creator can edit it");
        }

        subreddit.setName(request.getSubredditName());
        subreddit.setDescription(request.getDescription());

        subredditRepository.save(subreddit);
        log.info("Subreddit edited successfully with ID: {} by account ID: {}", subredditId, request.getAccountId());
        return subreddit;
    }

    public void deleteSubreddit(Long subredditId, Long accountId){
        if (subredditId == null) {
            log.warn("Delete subreddit failed: subreddit ID is null");
            throw new IllegalArgumentException("Subreddit ID cannot be null");
        }
        if (accountId == null) {
            log.warn("Delete subreddit failed: account ID is null");
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() -> {
                    log.warn("Delete subreddit failed: subreddit not found with ID: {}", subredditId);
                    return new IllegalArgumentException("Subreddit not found");
                });

        if(subreddit.getCreator() == null || !subreddit.getCreator().getId().equals(accountId)){
            log.warn("Delete subreddit failed: unauthorized access to subreddit ID: {} by account ID: {}", subredditId, accountId);
            throw new SecurityException("Only the subreddit creator can delete it");
        }

        subredditRepository.deleteById(subredditId);
        log.info("Subreddit deleted successfully with ID: {} by account ID: {}", subredditId, accountId);
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Subreddit name cannot be blank");
        }
        String trimmed = name.trim();
        return trimmed.startsWith("r/") ? trimmed : "r/" + trimmed;
    }
}
