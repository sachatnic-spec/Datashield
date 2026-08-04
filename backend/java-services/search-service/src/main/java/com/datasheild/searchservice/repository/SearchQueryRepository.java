package com.datasheild.searchservice.repository;

import com.datasheild.searchservice.entity.SearchQuery;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchQueryRepository extends JpaRepository<SearchQuery, UUID> {
}
