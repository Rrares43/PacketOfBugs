package com.example.springreddit.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "filters")
public class Filter {
    @Id
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String label;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer usageCount = 0;

    public Filter(Long id, String name, String label) {
        this.id = id;
        this.name = name;
        this.label = label;
    }
}
