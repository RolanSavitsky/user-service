package com.inno.userservice.specification;

import com.inno.userservice.entity.User;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

public class UserSpecification {

    public static Specification<User> filter(String name, String surname) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (name != null && !name.isBlank()) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("name"), name));
            }
            if (surname != null && !surname.isBlank()) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("surname"), surname));
            }

            return predicate;
        };
    }
}
