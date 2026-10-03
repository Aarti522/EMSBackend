package com.example.ems.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ems.model.User;

import jakarta.transaction.Transactional;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    boolean existsByEmail(String email);

    @Transactional
    void deleteByEmployee_Id(Long employeeId);
}