package com.inno.userservice.repository;

import com.inno.userservice.entity.PaymentCard;
import com.inno.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {

    long countByUserId(Long UserId);

    default PaymentCard saveWithLimit(PaymentCard card){
        if (countByUserId(card.getUser().getId()) >= User.MAX_CARDS){
            throw new IllegalStateException("User already has " + User.MAX_CARDS + " cards");
        }
        return save(card);
    }

    List<PaymentCard> findAllByUserId(Long userId);

    @Modifying
    @Query(value = """
            UPDATE payment_cards
            SET number = :number, holder = :holder,
                expiration_date = :expirationDate, updated_at = CURRENT_TIMESTAMP
            WHERE id = :id
            """, nativeQuery = true)
    int updateById(@Param("id") Long id,
                   @Param("number") String number,
                   @Param("holder") String holder,
                   @Param("expirationDate") LocalDate expirationDate);

    @Modifying
    @Query("UPDATE PaymentCard card SET card.active = :active, card.updatedAt = CURRENT_TIMESTAMP WHERE card.id = :id")
    int updateActiveById(@Param("id") Long id, @Param("active") boolean active);
}
