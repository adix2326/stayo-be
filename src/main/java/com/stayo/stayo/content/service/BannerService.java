package com.stayo.stayo.content.service;

import com.stayo.stayo.content.entity.Banner;
import com.stayo.stayo.content.repository.BannerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannerService {
    private final BannerRepository bannerRepository;

    public List<Banner> getActiveBanners() {
        log.info("Fetching active dashboard banners");
        return bannerRepository.findAllByOrderByDisplayOrderAsc();
    }
}
