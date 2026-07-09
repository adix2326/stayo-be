package com.stayo.stayo.content.service;

import com.stayo.stayo.content.entity.PopularSearch;
import com.stayo.stayo.content.repository.PopularSearchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopularSearchService {
    private final PopularSearchRepository popularSearchRepository;

    public List<PopularSearch> getPopularSearches() {
        log.info("Fetching popular searches");
        return popularSearchRepository.findAll();
    }
}
