package com.taskflow.taskflow_backend.repository;

import com.taskflow.taskflow_backend.entity.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPreferencesRepository
        extends JpaRepository<UserPreferences, Long> {

    Optional<UserPreferences> findByUser_Id(Long userId);
}