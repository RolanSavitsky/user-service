package com.inno.userservice.service;

import com.inno.userservice.dto.UserDto;
import com.inno.userservice.entity.PaymentCard;
import com.inno.userservice.entity.User;
import com.inno.userservice.exception.NotFoundException;
import com.inno.userservice.mapper.UserMapper;
import com.inno.userservice.repository.PaymentCardRepository;
import com.inno.userservice.repository.UserRepository;
import com.inno.userservice.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PaymentCardRepository paymentCardRepository;
    private final CacheManager cacheManager;

    public UserDto create(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Cacheable(value = "users", key = "#id")
    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        return userMapper.toDto(user);
    }

    public Page<UserDto> getAll(String name, String surname, Pageable pageable) {
        return userRepository
                .findAll(UserSpecification.filter(name, surname), pageable)
                .map(userMapper::toDto);
    }

    @CachePut(value = "users", key = "#id")
    @Transactional
    public UserDto update(Long id, UserDto userDto) {
        int updated = userRepository.updateById(id, userDto.getName(),
                userDto.getSurname(), userDto.getBirthDate(), userDto.getEmail());

        if (updated == 0) {
            throw new NotFoundException("User not found: " + id);
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        return userMapper.toDto(user);
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found: " + id);
        }

        List<Long> cardIds = paymentCardRepository.findAllByUserId(id)
                .stream()
                .map(PaymentCard::getId)
                .toList();

        userRepository.deleteById(id);

        Cache cardCache = cacheManager.getCache("cards");
        if (cardCache != null) {
            for (Long cardId : cardIds) {
                cardCache.evict(cardId);
            }
        }
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public void activate(Long id) {
        if (userRepository.updateActiveById(id, true) == 0) {
            throw new NotFoundException("User not found: " + id);
        }
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public void deactivate(Long id) {
        if (userRepository.updateActiveById(id, false) == 0) {
            throw new NotFoundException("User not found: " + id);
        }
    }
}