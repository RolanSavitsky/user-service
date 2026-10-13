package com.inno.userservice.repository;

import com.inno.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @Modifying
    @Query(value = """
            UPDATE users
            SET name = :name, surname = :surname, birth_date = :birthDate,
                email = :email, updated_at = CURRENT_TIMESTAMP
            WHERE id = :id
            """, nativeQuery = true)
    int updateById(@Param("id") Long id,
                   @Param("name") String name,
                   @Param("surname") String surname,
                   @Param("birthDate") LocalDate birthDate,
                   @Param("email") String email);

    @Modifying
    @Query("UPDATE User user SET user.active = :active, user.updatedAt = CURRENT_TIMESTAMP WHERE user.id = :id")
    int updateActiveById(@Param("id") Long id, @Param("active") boolean active);
}
