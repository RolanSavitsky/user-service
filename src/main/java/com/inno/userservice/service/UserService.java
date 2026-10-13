package com.inno.userservice.service;

import com.inno.userservice.dto.UserDto;
import com.inno.userservice.entity.User;
import com.inno.userservice.exception.NotFoundException;
import com.inno.userservice.mapper.UserMapper;
import com.inno.userservice.repository.UserRepository;
import com.inno.userservice.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDto create(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

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

    @Transactional
    public UserDto update(Long id, UserDto userDto) {
        int updated = userRepository.updateById(id, userDto.getName(),
                userDto.getSurname(), userDto.getBirthDate(), userDto.getEmail());

        if (updated == 0) {
            throw new NotFoundException("User not found: " + id);
        }

        return getById(id);
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found: " + id);
        }

        userRepository.deleteById(id);
    }

    @Transactional
    public void activate(Long id) {
        if (userRepository.updateActiveById(id, true) == 0) {
            throw new NotFoundException("User not found: " + id);
        }
    }

    @Transactional
    public void deactivate(Long id) {
        if (userRepository.updateActiveById(id, false) == 0) {
            throw new NotFoundException("User not found: " + id);
        }
    }
}