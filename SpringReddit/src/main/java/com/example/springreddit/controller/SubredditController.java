package com.example.springreddit.controller;

import com.example.springreddit.dto.SubredditDto;
import com.example.springreddit.model.Subreddit;
import com.example.springreddit.service.SubredditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subreddits")
public class SubredditController {

    @Autowired
    private SubredditService subredditService;

    @PostMapping
    public ResponseEntity<?> createNewSubreddit(@RequestBody SubredditDto.CreateSubredditRequest request){
        try{
            Subreddit savedSub = subredditService.createSubreddit(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(savedSub));
        }
        catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<SubredditDto.SubredditResponse>> getAllSubreddits(){
        List<SubredditDto.SubredditResponse> subreddits = subredditService.getAllSubreddits()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(subreddits);
    }

    private SubredditDto.SubredditResponse mapToResponse(Subreddit subreddit){
        SubredditDto.SubredditResponse response = new SubredditDto.SubredditResponse();

        response.setId(subreddit.getId());
        response.setName(subreddit.getName());
        response.setDescription(subreddit.getDescription());
        response.setCreatedAt(subreddit.getCreatedAt());

        return response;
    }

    @GetMapping("/{name}")
    public ResponseEntity<?> getSubredditByName(@PathVariable String name){
        try{
            Subreddit subreddit = subredditService.getSubredditByName(name);
            return ResponseEntity.ok(mapToResponse(subreddit));
        }
        catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editSubreddit(@PathVariable Long id, @RequestBody SubredditDto.EditSubredditRequest request){
        try{
            Subreddit editedSubreddit = subredditService.editSubreddit(id, request);
            return ResponseEntity.ok(mapToResponse(editedSubreddit));
        }
        catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubreddit(@PathVariable Long id){
        try{
            subredditService.deleteSubreddit(id);
            return ResponseEntity.ok("Subreddit deleted successfully");
        }
        catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
