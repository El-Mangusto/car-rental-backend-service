package com.elmangusto.carrental.repository;

import com.elmangusto.carrental.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phone);

    Optional<User> findByLogin(String login);
}
