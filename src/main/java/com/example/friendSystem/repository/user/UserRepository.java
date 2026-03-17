package com.example.friendSystem.repository.user;

import com.example.friendSystem.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 사용자(User) 조회/저장을 담당하는 리포지토리.
 */
public interface UserRepository extends JpaRepository<User, Long> { }
