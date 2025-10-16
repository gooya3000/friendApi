package com.example.aprbackendassignment.repository.user;

import com.example.aprbackendassignment.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 사용자(User) 조회/저장을 담당하는 리포지토리.
 */
public interface UserRepository extends JpaRepository<User, Long> { }
