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
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getCreatorId() == null) {
            throw new IllegalArgumentException("Creator ID cannot be null");
        }
        String normalizedName = normalizeName(request.getSubredditName());
        if(subredditRepository.existsByName(normalizedName)){
            throw new IllegalArgumentException("Subreddit already exists");
        }

        Account creator = accountRepository.findById(request.getCreatorId())
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));

        Subreddit subreddit = new Subreddit();
        subreddit.setName(normalizedName);
        subreddit.setDescription(request.getDescription());
        subreddit.setCreator(creator);

        return subredditRepository.save(subreddit);
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
            throw new IllegalArgumentException("Subreddit ID cannot be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getAccountId() == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() -> new IllegalArgumentException("Subreddit not found"));

        if(subreddit.getCreator() == null || !subreddit.getCreator().getId().equals(request.getAccountId())){
            throw new SecurityException("Only the subreddit creator can edit it");
        }

        subreddit.setName(request.getSubredditName());
        subreddit.setDescription(request.getDescription());

        return subredditRepository.save(subreddit);
    }

    public void deleteSubreddit(Long subredditId, Long accountId){
        if (subredditId == null) {
            throw new IllegalArgumentException("Subreddit ID cannot be null");
        }
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() -> new IllegalArgumentException("Subreddit not found"));

        if(subreddit.getCreator() == null || !subreddit.getCreator().getId().equals(accountId)){
            throw new SecurityException("Only the subreddit creator can delete it");
        }

        subredditRepository.deleteById(subredditId);
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Subreddit name cannot be blank");
        }
        String trimmed = name.trim();
        return trimmed.startsWith("r/") ? trimmed : "r/" + trimmed;
    }
}
