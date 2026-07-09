package com.stayo.stayo.content.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "dashboard_categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardCategory {
    @Id
    private String id;
    private String title;
    private String subtitle;
    private String icon;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
