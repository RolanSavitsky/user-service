package com.inno.userservice.service;

import com.inno.userservice.dto.PaymentCardDto;
import com.inno.userservice.entity.PaymentCard;
import com.inno.userservice.entity.User;
import com.inno.userservice.exception.NotFoundException;
import com.inno.userservice.mapper.PaymentCardMapper;
import com.inno.userservice.repository.PaymentCardRepository;
import com.inno.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
public class PaymentCardServiceTest {

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private PaymentCardMapper paymentCardMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentCardService paymentCardService;

    @Test
    void create_whenUserExists_returnCreatedDto() {
        Long userId = 1L;
        PaymentCardDto inputDto = new PaymentCardDto();
        inputDto.setUserId(userId);
        inputDto.setNumber("1234567");

        User owner = new User();
        owner.setId(userId);

        PaymentCard card = new PaymentCard();
        PaymentCard savedCard = new PaymentCard();
        savedCard.setId(10L);

        PaymentCardDto expectedDto = new PaymentCardDto();
        expectedDto.setId(10L);
        expectedDto.setNumber("1234567");

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(paymentCardMapper.toEntity(inputDto)).thenReturn(card);
        when(paymentCardRepository.saveWithLimit(card)).thenReturn(savedCard);
        when(paymentCardMapper.toDto(savedCard)).thenReturn(expectedDto);

        PaymentCardDto result = paymentCardService.create(inputDto);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("1234567", result.getNumber());

        verify(userRepository, times(1)).findById(userId);
        verify(paymentCardMapper, times(1)).toEntity(inputDto);
        verify(paymentCardRepository, times(1)).saveWithLimit(card);
        verify(paymentCardMapper, times(1)).toDto(savedCard);
    }

    @Test
    void getById_whenCardNotFound_throwsNotFoundException() {
        Long cardId = 500L;

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> paymentCardService.getById(cardId));

        assertTrue(exception.getMessage().contains("Payment card not found"));
        verify(paymentCardRepository, times(1)).findById(cardId);
        verifyNoInteractions(paymentCardMapper);
    }

    @Test
    void getAll_returnsPagedDtos() {
        Pageable pageable = PageRequest.of(0, 10);
        PaymentCard card = new PaymentCard();
        Page<PaymentCard> cardPage = new PageImpl<>(List.of(card), pageable, 1);
        PaymentCardDto expectedDto = new PaymentCardDto();

        when(paymentCardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(cardPage);
        when(paymentCardMapper.toDto(card)).thenReturn(expectedDto);

        Page<PaymentCardDto> result = paymentCardService.getAll("Steve", "Jobs", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(paymentCardRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        verify(paymentCardMapper, times(1)).toDto(card);
    }

    @Test
    void getAllByUserId_returnsList() {
        Long userId = 1L;
        PaymentCard card = new PaymentCard();
        PaymentCardDto expectedDto = new PaymentCardDto();

        when(paymentCardRepository.findAllByUserId(userId)).thenReturn(List.of(card));
        when(paymentCardMapper.toDto(card)).thenReturn(expectedDto);

        List<PaymentCardDto> result = paymentCardService.getAllByUserId(userId);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(paymentCardRepository, times(1)).findAllByUserId(userId);
        verify(paymentCardMapper, times(1)).toDto(card);
    }

    @Test
    void update_whenCardExists_returnsUpdatedDto() {
        Long cardId = 1L;
        PaymentCardDto updateDto = new PaymentCardDto();
        updateDto.setNumber("1234567");
        updateDto.setHolder("Steve Jobs");
        updateDto.setExpirationDate(LocalDate.of(2028, 12, 31));

        PaymentCard updatedCard = new PaymentCard();
        updatedCard.setId(cardId);

        PaymentCardDto expectedDto = new PaymentCardDto();
        expectedDto.setId(1L);

        when(paymentCardRepository.updateById(eq(cardId), any(), any(), any())).thenReturn(1);
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(updatedCard));
        when(paymentCardMapper.toDto(updatedCard)).thenReturn(expectedDto);

        PaymentCardDto result = paymentCardService.update(cardId, updateDto);

        assertNotNull(result);
        assertEquals(cardId, result.getId());

        verify(paymentCardRepository, times(1)).updateById(eq(cardId), any(), any(), any());
        verify(paymentCardRepository, times(1)).findById(cardId);
        verify(paymentCardMapper, times(1)).toDto(updatedCard);
    }

    @Test
    void delete_whenCardExists_deletesCards() {
        Long cardId = 1L;

        when(paymentCardRepository.existsById(cardId)).thenReturn(true);

        assertDoesNotThrow(() -> paymentCardService.delete(cardId));

        verify(paymentCardRepository, times(1)).existsById(cardId);
        verify(paymentCardRepository, times(1)).deleteById(cardId);
    }

    @Test
    void activate_whenCardExists_updatesStatus() {
        Long userId = 1L;

        when(paymentCardRepository.updateActiveById(userId, true)).thenReturn(1);
        assertDoesNotThrow(() -> paymentCardService.activate(userId));
        verify(paymentCardRepository, times(1)).updateActiveById(userId, true);
    }

    @Test
    void deactivate_whenCardExists_updatesStatus() {
        Long userId = 1L;

        when(paymentCardRepository.updateActiveById(userId, false)).thenReturn(1);
        assertDoesNotThrow(() -> paymentCardService.deactivate(userId));
        verify(paymentCardRepository, times(1)).updateActiveById(userId, false);
    }
}