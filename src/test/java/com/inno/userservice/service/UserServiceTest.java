package com.inno.userservice.service;

import com.inno.userservice.dto.UserDto;
import com.inno.userservice.entity.PaymentCard;
import com.inno.userservice.entity.User;
import com.inno.userservice.exception.NotFoundException;
import com.inno.userservice.mapper.UserMapper;
import com.inno.userservice.repository.PaymentCardRepository;
import com.inno.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private UserService userService;

    @Test
    void create_whenValidDto_returnsCreatedDto() {
        UserDto inputDto = new UserDto();
        inputDto.setName("Steve");

        User user = new User();
        user.setName("Steve");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Steve");

        UserDto expectedDto = new UserDto();
        expectedDto.setId(1L);
        expectedDto.setName("Steve");

        when(userMapper.toEntity(inputDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(expectedDto);

        UserDto result = userService.create(inputDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Steve", result.getName());

        verify(userMapper, times(1)).toEntity(inputDto);
        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).toDto(savedUser);
    }

    @Test
    void getById_whenUserNotFound_throwsNotFoundException() {
        Long userId = 500L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getById(userId));

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository, times(1)).findById(userId);
        verifyNoInteractions(userMapper);
    }

    @Test
    void getAll_returnsPagedYserDtos() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = new User();
        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);
        UserDto expectedDto = new UserDto();

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(userMapper.toDto(user)).thenReturn(expectedDto);

        Page<UserDto> result = userService.getAll("Steve", "Jobs", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(userRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void update_whenUserExists_returnsUpdatedDto() {
        Long userId = 1L;

        UserDto updateDto = new UserDto();
        updateDto.setName("Steve");
        updateDto.setSurname("Jobs");
        updateDto.setEmail("steve@example.com");
        updateDto.setBirthDate(LocalDate.of(1955, 2, 24));

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName("Steve");

        UserDto expectedDto = new UserDto();
        expectedDto.setId(userId);
        expectedDto.setName("Steve");

        when(userRepository.updateById(eq(userId), any(), any(), any(), any())).thenReturn(1);
        when(userRepository.findById(userId)).thenReturn(Optional.of(updatedUser));
        when(userMapper.toDto(updatedUser)).thenReturn(expectedDto);

        UserDto result = userService.update(userId, updateDto);

        assertNotNull(result);
        assertEquals("Steve", result.getName());

        verify(userRepository, times(1)).updateById(eq(userId), any(), any(), any(), any());
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).toDto(updatedUser);
    }

    @Test
    void delete_whenUserExists_deleteUserAndEvictsCardsCache() {
        Long userId = 1L;
        PaymentCard card = new PaymentCard();
        card.setId(10L);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(paymentCardRepository.findAllByUserId(userId)).thenReturn(List.of(card));
        when(cacheManager.getCache("cards")).thenReturn(cache);
        userService.delete(userId);

        verify(userRepository, times(1)).existsById(userId);
        verify(paymentCardRepository, times(1)).findAllByUserId(userId);
        verify(userRepository, times(1)).deleteById(userId);
        verify(cacheManager, times(1)).getCache("cards");
        verify(cache, times(1)).evict(10L);
    }

    @Test
    void activate_whenUserExists_updatesStatus() {
        Long userId = 1L;

        when(userRepository.updateActiveById(userId, true)).thenReturn(1);
        assertDoesNotThrow(() -> userService.activate(userId));
        verify(userRepository, times(1)).updateActiveById(userId, true);
    }

    @Test
    void deactivate_whenUserExists_updatesStatus() {
        Long userId = 1L;

        when(userRepository.updateActiveById(userId, false)).thenReturn(1);
        assertDoesNotThrow(() -> userService.deactivate(userId));
        verify(userRepository, times(1)).updateActiveById(userId, false);
    }
}