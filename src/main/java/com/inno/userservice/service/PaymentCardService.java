package com.inno.userservice.service;

import com.inno.userservice.dto.PaymentCardDto;
import com.inno.userservice.entity.PaymentCard;
import com.inno.userservice.entity.User;
import com.inno.userservice.exception.NotFoundException;
import com.inno.userservice.mapper.PaymentCardMapper;
import com.inno.userservice.repository.PaymentCardRepository;
import com.inno.userservice.repository.UserRepository;
import com.inno.userservice.specification.PaymentCardSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentCardService {

    private final PaymentCardRepository paymentCardRepository;
    private final PaymentCardMapper paymentCardMapper;
    private final UserRepository userRepository;

    public PaymentCardDto create(PaymentCardDto paymentCardDto) {
        User owner = userRepository.findById(paymentCardDto.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found: " + paymentCardDto.getUserId()));

        PaymentCard paymentCard = paymentCardMapper.toEntity(paymentCardDto);
        paymentCard.setUser(owner);
        PaymentCard saved = paymentCardRepository.saveWithLimit(paymentCard);
        return paymentCardMapper.toDto(saved);
    }

    public PaymentCardDto getById(Long id) {
        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment card not found: " + id));

        return paymentCardMapper.toDto(paymentCard);
    }

    public Page<PaymentCardDto> getAll(String name, String surname, Pageable pageable) {
        return paymentCardRepository
                .findAll(PaymentCardSpecification.filter(name, surname), pageable)
                .map(paymentCardMapper::toDto);
    }

    public List<PaymentCardDto> getAllByUserId(Long userId) {
        return paymentCardRepository.findAllByUserId(userId)
                .stream()
                .map(paymentCardMapper::toDto)
                .toList();
    }

    @Transactional
    public PaymentCardDto update(Long id, PaymentCardDto paymentCardDto) {
        int updated = paymentCardRepository.updateById(id, paymentCardDto.getNumber(),
                paymentCardDto.getHolder(), paymentCardDto.getExpirationDate());

        if (updated == 0) {
            throw new NotFoundException("Payment card not found: " + id);
        }

        return getById(id);
    }

    @Transactional
    public void delete(Long id) {
        if (!paymentCardRepository.existsById(id)) {
            throw new NotFoundException("Payment card not found: " + id);
        }

        paymentCardRepository.deleteById(id);
    }

    @Transactional
    public void activate(Long id) {
        if (paymentCardRepository.updateActiveById(id, true) == 0) {
            throw new NotFoundException("Payment card not found: " + id);
        }
    }

    @Transactional
    public void deactivate(Long id) {
        if (paymentCardRepository.updateActiveById(id, false) == 0) {
            throw new NotFoundException("Payment card not found: " + id);
        }
    }
}