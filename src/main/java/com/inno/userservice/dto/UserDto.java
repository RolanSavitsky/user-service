package com.inno.userservice.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {

    private long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 100)
    private String surname;

    @NotNull
    @Past
    private LocalDate birthDate;

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    private boolean active;
}