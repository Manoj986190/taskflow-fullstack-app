package com.taskflow.taskflow_backend.repository;

import com.taskflow.taskflow_backend.entity.TokenBlocklist;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface TokenBlocklistRepository
        extends JpaRepository<TokenBlocklist, String> {

    boolean existsByJti(String jti);

    @Transactional
    @Modifying
    void deleteByUser_IdAndJtiNot(Long userId, String currentJti);

    List<TokenBlocklist> findAllByUser_Id(Long userId);
}