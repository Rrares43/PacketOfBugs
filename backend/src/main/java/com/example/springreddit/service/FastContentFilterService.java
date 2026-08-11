package com.example.springreddit.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FastContentFilterService {

    private static final String[] BLACKLIST = {

            "prost", "anal", "hack", "spam", "retard",
            "proasta", "pula", "pule", "pulii", "pizda", "pizde", "coaie", "coi",
            "muie", "muist", "futut", "futa", "futere", "sloboz", "cacat", "rahat",
            "jeg", "jegos", "curva", "curve", "bulangiu", "handicapat",

            "fuck", "fucking", "fucked", "fucker", "motherfucker", "bitch", "bitches",
            "ass", "asshole", "dick", "dicks", "pussy", "penis", "pennis", "cunt",
            "cock", "cocksucker", "bastard", "slut", "whore", "wanker"
    };

    private Pattern forbiddenWordPattern;

    @PostConstruct
    void init() {
        String joined = String.join("|", BLACKLIST);
        forbiddenWordPattern = Pattern.compile("\\b(?i)(" + joined + ")\\b");
    }

    public String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        Matcher matcher = forbiddenWordPattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "*".repeat(matcher.group().length()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
