package com.stayo.stayo.property.service;

import com.stayo.stayo.property.entity.DashboardCategory;
import com.stayo.stayo.property.repository.DashboardCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {
    private final DashboardCategoryRepository dashboardCategoryRepository;

    public List<DashboardCategory> getCategories() {
        log.info("Fetching dashboard categories");
        return dashboardCategoryRepository.findAllByOrderByDisplayOrderAsc();
    }
}
