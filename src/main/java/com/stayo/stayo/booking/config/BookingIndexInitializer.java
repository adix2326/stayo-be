package com.stayo.stayo.booking.config;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Creates a partial unique index on the bookings collection at application startup.
 *
 * Index: { userId: 1, pgId: 1 } — unique only when status is NOT in [CANCELLED, OWNER_REJECTED].
 *
 * This prevents two concurrent "check-then-insert" requests from both succeeding.
 * MongoDB's @CompoundIndex annotation does not support partialFilterExpression,
 * so we create the index programmatically.
 *
 * The createIndex call is idempotent — if the index already exists with the same
 * definition it is a no-op.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingIndexInitializer implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) {
        try {
            MongoDatabase db = mongoTemplate.getDb();
            MongoCollection<Document> collection = db.getCollection("bookings");

            Bson keys = Indexes.compoundIndex(
                    Indexes.ascending("userId"),
                    Indexes.ascending("pgId")
            );

            // Only enforce uniqueness for active bookings —
            // CANCELLED and OWNER_REJECTED bookings are excluded from the constraint.
            Document partialFilter = new Document("status",
                    new Document("$nin", List.of("CANCELLED", "OWNER_REJECTED"))
            );

            IndexOptions options = new IndexOptions()
                    .name("unique_active_booking_per_user_pg")
                    .unique(true)
                    .partialFilterExpression(partialFilter);

            String indexName = collection.createIndex(keys, options);
            log.info("Booking concurrency index ensured: {}", indexName);
        } catch (Exception e) {
            // Log but don't crash — the app-level check-then-insert still provides
            // a safety net; this index is a hardening layer.
            log.warn("Failed to create booking uniqueness index (non-fatal): {}", e.getMessage());
        }
    }
}
