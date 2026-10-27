package com.inno.userservice.controller;

import com.inno.userservice.dto.PaymentCardDto;
import com.inno.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    @PreAuthorize("hasRole('ADMIN') or @accessChecker.isSelf(#paymentCardDto.userId)")
    @PostMapping
    public ResponseEntity<PaymentCardDto> create(@Valid @RequestBody PaymentCardDto paymentCardDto) {
        PaymentCardDto created = paymentCardService.create(paymentCardDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasRole('ADMIN') or @accessChecker.ownsCard(#id)")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentCardService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PaymentCardDto>> getAll(@RequestParam(required = false) String name,
                                                       @RequestParam(required = false) String surname,
                                                       Pageable pageable) {

        return ResponseEntity.ok(paymentCardService.getAll(name, surname, pageable));
    }

    @PreAuthorize("hasRole('ADMIN') or @accessChecker.isSelf(#userId)")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentCardDto>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentCardService.getAllByUserId(userId));
    }

    @PreAuthorize("hasRole('ADMIN') or @accessChecker.ownsCard(#id)")
    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardDto> update(@PathVariable Long id, @Valid @RequestBody PaymentCardDto paymentCardDto) {
        return ResponseEntity.ok(paymentCardService.update(id, paymentCardDto));
    }

    @PreAuthorize("hasRole('ADMIN') or @accessChecker.ownsCard(#id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        paymentCardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        paymentCardService.activate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        paymentCardService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
