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
        if(subredditRepository.existsBySubredditName(request.getSubredditName())){
            throw new IllegalArgumentException("Subreddit already exists");
        }

        /* maybe add sub creator in the future
        Optional<Account> creator = Optional.of(accountRepository.findById(request.getCreatorId())
                .orElseThrow(() -> new IllegalArgumentException("Creator not found")));
         */

        Subreddit subreddit = new Subreddit();
        subreddit.setName(request.getSubredditName());
        subreddit.setDescription(request.getDescription());

        return subredditRepository.save(subreddit);
    }

    public List<Subreddit> getAllSubreddits(){
        return subredditRepository.findAll();
    }
}
