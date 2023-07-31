package com.example.naejango.domain.follow.repository;

import com.example.naejango.domain.follow.domain.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    List<Follow> findByUserId(Long userId);

    Follow findByUserIdAndStorageId(Long userId, Long storageId);

    boolean existsByUserIdAndStorageId(Long userId, Long storageId);
}