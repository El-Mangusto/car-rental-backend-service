package com.elmangusto.carrental.repository;

import com.elmangusto.carrental.entity.User;
import com.elmangusto.carrental.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phone);

    boolean existsByRole(Role role);

    Optional<User> findByLogin(String login);

    Optional<User> findByPhoneNumber(String phoneNumber);
}
