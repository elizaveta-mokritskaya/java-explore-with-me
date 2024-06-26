package ru.practicum.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findAllByIdIn(List<Long> ids, Pageable pageable);

    boolean existsUserByEmail(String email); //true = пользователь есть

    boolean existsUserById(Long id); //true = пользователь есть
}
