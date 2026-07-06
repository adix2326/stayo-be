package com.stayo.stayo.config;

import com.stayo.stayo.property.entity.City;
import com.stayo.stayo.property.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class CityDataSeeder implements CommandLineRunner {

    private final CityRepository cityRepository;

    @Override
    public void run(String... args) throws Exception {
        if(cityRepository.count() == 0){
            log.info("Cities collection is empty. Seeding default popular cities...");

            List<City> defaultCities = Arrays.asList(
                    createCity("Bangalore", "https://images.unsplash.com/photo-1596176530529-78163a4f7af2?auto=format&fit=crop&w=600&q=80"),
                    createCity("Mumbai", "https://images.unsplash.com/photo-1566552881560-0be862a7c445?auto=format&fit=crop&w=600&q=80"),
                    createCity("Delhi", "https://images.unsplash.com/photo-1587474260584-136574528ed5?auto=format&fit=crop&w=600&q=80"),
                    createCity("Pune", "https://images.unsplash.com/photo-1601999109332-542b18dbec57?auto=format&fit=crop&w=600&q=80"),
                    createCity("Hyderabad", "https://images.unsplash.com/photo-1605007493699-af65834f8a00?auto=format&fit=crop&w=600&q=80"),
                    createCity("Chennai", "https://images.unsplash.com/photo-1582510003544-4d00b7f74220?auto=format&fit=crop&w=600&q=80")
            );

            cityRepository.saveAll(defaultCities);
            log.info("Successfully seeded {} popular cities.", defaultCities.size());
        } else {
            log.info("Cities database already initialized. Skipping seeding.");
        }
    }

    private City createCity(String name, String imageUrl){
        City city = new City();
        city.setName(name);
        city.setImageUrl(imageUrl);
        city.setIsPopular(true);
        city.setIsActive(true);
        city.setCreatedAt(LocalDateTime.now());
        city.setUpdatedAt(LocalDateTime.now());
        return city;
    }
}
