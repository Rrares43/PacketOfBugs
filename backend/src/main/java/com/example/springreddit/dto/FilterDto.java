package com.example.springreddit.dto;

public class FilterDto {

    private Long id;
    private String name;
    private String label;

    public FilterDto(Long id, String name, String label) {
        this.id = id;
        this.name = name;
        this.label = label;
    }

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getLabel() {
        return label;
    }

}
