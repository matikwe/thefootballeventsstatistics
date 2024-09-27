package com.example.thefootballeventsstatistics.repository;

import com.example.thefootballeventsstatistics.entity.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
}