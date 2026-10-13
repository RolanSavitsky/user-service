package com.inno.userservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class PaymentCardDto implements Serializable {

    private Long id;

    @NotNull
    private Long userId;

    @NotBlank
    @Size(max = 100)
    private String number;

    @NotBlank
    @Size(max = 100)
    private String holder;

    @NotNull
    @Future
    private LocalDate expirationDate;

    private boolean active;
}