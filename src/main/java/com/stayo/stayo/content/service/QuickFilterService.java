package com.stayo.stayo.content.service;

import com.stayo.stayo.content.entity.QuickFilter;
import com.stayo.stayo.content.repository.QuickFilterRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuickFilterService {
    private final QuickFilterRepository quickFilterRepository;

    public List<QuickFilter> getQuickFilters() {
        log.info("Fetching quick filters");
        return quickFilterRepository.findAllByOrderByDisplayOrderAsc();
    }
}
