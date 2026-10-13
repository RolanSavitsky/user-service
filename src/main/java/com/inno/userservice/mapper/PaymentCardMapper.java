package com.inno.userservice.mapper;

import com.inno.userservice.dto.PaymentCardDto;
import com.inno.userservice.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {

    @Mapping(target = "userId", source = "user.id")
    PaymentCardDto toDto(PaymentCard card);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    PaymentCard toEntity(PaymentCardDto cardDto);
}
