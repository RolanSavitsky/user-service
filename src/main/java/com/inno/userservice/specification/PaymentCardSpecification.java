package com.inno.userservice.specification;

import com.inno.userservice.entity.PaymentCard;
import com.inno.userservice.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecification {

    public static Specification<PaymentCard> filter(String name, String surname) {
        return (root, query, criteriaBuilder) -> {
            Join<PaymentCard, User> user = root.join("user");
            Predicate predicate = criteriaBuilder.conjunction();
            if (name != null && !name.isBlank()) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(user.get("name"), name));
            }
            if (surname != null && !surname.isBlank()) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(user.get("surname"), surname));
            }

            return predicate;
        };
    }
}