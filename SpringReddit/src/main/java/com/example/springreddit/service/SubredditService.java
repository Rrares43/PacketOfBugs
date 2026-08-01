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
import java.util.Optional;

@Service
public class SubredditService {
    @Autowired
    private SubredditRepository subredditRepository;

    @Autowired
    private AccountRepository accountRepository;

    public Subreddit createSubreddit(SubredditDto.CreateSubredditRequest request){
        String normalizedName = normalizeName(request.getSubredditName());
        if(subredditRepository.existsByName(normalizedName)){
            throw new IllegalArgumentException("Subreddit already exists");
        }

        Optional<Account> creator = Optional.of(accountRepository.findById(request.getCreatorId())
                .orElseThrow(() -> new IllegalArgumentException("Creator not found")));


        Subreddit subreddit = new Subreddit();
        subreddit.setName(normalizedName);
        subreddit.setDescription(request.getDescription());
        subreddit.setCreator(creator.get());

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
        String searchName = normalizeName(subredditName);
        return subredditRepository.findByName(searchName)
                .orElseThrow(() -> new IllegalArgumentException("Subreddit not found"));
    }

    public Subreddit editSubreddit(Long subredditId, SubredditDto.EditSubredditRequest request){
        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() -> new IllegalArgumentException("Subreddit not found"));

        if(subreddit.getCreator() == null || !subreddit.getCreator().getId().equals(request.getAccountId())){
            throw new SecurityException("Only the subreddit creator can edit it");
        }

        subreddit.setName(request.getSubredditName());
        subreddit.setDescription(request.getDescription());

        return subredditRepository.save(subreddit);
    }

    public void deleteSubreddit(Long subredditId, long accountId){
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
