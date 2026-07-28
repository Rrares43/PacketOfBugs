package com.example.springreddit.service;

import com.example.springreddit.dto.SubredditDto;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.Subreddit;
import com.example.springreddit.repository.AccountRepository;
import com.example.springreddit.repository.SubredditRepository;
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
        if(subredditRepository.existsByName(request.getSubredditName())){
            throw new IllegalArgumentException("Subreddit already exists");
        }

        Optional<Account> creator = Optional.of(accountRepository.findById(request.getCreatorId())
                .orElseThrow(() -> new IllegalArgumentException("Creator not found")));


        Subreddit subreddit = new Subreddit();
        subreddit.setName(request.getSubredditName());
        subreddit.setDescription(request.getDescription());
        subreddit.setCreator(creator.get());

        return subredditRepository.save(subreddit);
    }

    public List<Subreddit> getAllSubreddits(){
        return subredditRepository.findAll();
    }

    public List<Subreddit> getSubredditsByCreatorUsername(String username) {
        return subredditRepository.findByCreator_Username(username);
    }

    public Subreddit getSubredditByName(String subredditName){
        String searchName = subredditName.startsWith("r/") ? subredditName : "r/" + subredditName;
        return subredditRepository.findByName(searchName)
                .orElseThrow(() -> new IllegalArgumentException("Subreddit not found"));
    }

    public Subreddit editSubreddit(Long subredditId, SubredditDto.EditSubredditRequest request){
        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() -> new IllegalArgumentException("Subreddit not found"));

        subreddit.setName(request.getSubredditName());
        subreddit.setDescription(request.getDescription());

        return subredditRepository.save(subreddit);
    }

    public void deleteSubreddit(Long subredditId){
        if (!subredditRepository.existsById(subredditId)){
            throw new IllegalArgumentException("Subreddit not found");
        }
        subredditRepository.deleteById(subredditId);
    }
}
