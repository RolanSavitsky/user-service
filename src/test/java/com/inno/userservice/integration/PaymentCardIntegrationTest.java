package com.inno.userservice.integration;

import com.inno.userservice.dto.PaymentCardDto;
import com.inno.userservice.dto.UserDto;
import com.inno.userservice.repository.PaymentCardRepository;
import com.inno.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class PaymentCardIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        paymentCardRepository.deleteAll();
        userRepository.deleteAll();

        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
    }

    @Test
    void createCard_and_GetWithCacheCheck() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("Bob");
        userDto.setSurname("Brown");
        userDto.setEmail("bob@example.com");
        userDto.setBirthDate(LocalDate.of(2000, 2, 10));

        String userResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Bob"))
                .andReturn().getResponse().getContentAsString();

        UserDto createdUser = objectMapper.readValue(userResponse, UserDto.class);
        Long userId = createdUser.getId();

        PaymentCardDto cardDto = new PaymentCardDto();
        cardDto.setUserId(userId);
        cardDto.setNumber("1234567");
        cardDto.setHolder("BOB BROWN");
        cardDto.setExpirationDate(LocalDate.now().plusYears(3));

        String cardResponse = mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.number").value("1234567"))
                .andReturn().getResponse().getContentAsString();

        PaymentCardDto createdCard = objectMapper.readValue(cardResponse, PaymentCardDto.class);
        Long cardId = createdCard.getId();

        mockMvc.perform(get("/cards/" + cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("1234567"));

        Cache cardsCache = cacheManager.getCache("cards");
        assertThat(cardsCache).isNotNull();
        assertThat(cardsCache.get(cardId)).isNotNull();
    }

    @Test
    void createCard_exceedLimit_shouldReturnConflict() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("Bob");
        userDto.setSurname("Brown");
        userDto.setEmail("bob@example.com");
        userDto.setBirthDate(LocalDate.of(2000, 2, 10));

        String userResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Bob"))
                .andReturn().getResponse().getContentAsString();

        UserDto createdUser = objectMapper.readValue(userResponse, UserDto.class);
        Long userId = createdUser.getId();

        for (int i = 1; i <= 5; i++) {
            PaymentCardDto cardDto = new PaymentCardDto();
            cardDto.setUserId(userId);
            cardDto.setNumber("123456" + i);
            cardDto.setHolder("BOB BROWN");
            cardDto.setExpirationDate(LocalDate.now().plusYears(2));

            mockMvc.perform(post("/cards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cardDto)))
                    .andExpect(status().isCreated());
        }

        assertThat(paymentCardRepository.count()).isEqualTo(5);

        PaymentCardDto sixthCard = new PaymentCardDto();
        sixthCard.setUserId(userId);
        sixthCard.setNumber("9999999");
        sixthCard.setHolder("BOB BROWN");
        sixthCard.setExpirationDate(LocalDate.now().plusYears(2));

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sixthCard)))
                .andExpect(status().isConflict());
    }

    @Test
    void createCard_duplicateNumber_shouldReturnConflict() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("Bob");
        userDto.setSurname("Brown");
        userDto.setEmail("bob@example.com");
        userDto.setBirthDate(LocalDate.of(2000, 2, 10));

        String userResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Bob"))
                .andReturn().getResponse().getContentAsString();

        UserDto createdUser = objectMapper.readValue(userResponse, UserDto.class);
        Long userId = createdUser.getId();

        PaymentCardDto cardDto1 = new PaymentCardDto();
        cardDto1.setUserId(userId);
        cardDto1.setNumber("1111111");
        cardDto1.setHolder("BOB BROWN");
        cardDto1.setExpirationDate(LocalDate.now().plusYears(3));

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.number").value("1111111"))
                .andReturn().getResponse().getContentAsString();

        PaymentCardDto cardDto2 = new PaymentCardDto();
        cardDto2.setUserId(userId);
        cardDto2.setNumber("1111111");
        cardDto2.setHolder("BOB BROWN");
        cardDto2.setExpirationDate(LocalDate.now().plusYears(3));

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto2)))
                .andExpect(status().isConflict());
    }

    @Test
    void getCardById_notFound() throws Exception {
        long nonExistentId = 999L;

        mockMvc.perform(get("/cards/" + nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_shouldCascadeDeleteCards() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("Bob");
        userDto.setSurname("Brown");
        userDto.setEmail("bob@example.com");
        userDto.setBirthDate(LocalDate.of(2000, 2, 10));

        String userResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Bob"))
                .andReturn().getResponse().getContentAsString();

        UserDto createdUser = objectMapper.readValue(userResponse, UserDto.class);
        Long userId = createdUser.getId();

        PaymentCardDto cardDto = new PaymentCardDto();
        cardDto.setUserId(userId);
        cardDto.setNumber("1234567");
        cardDto.setHolder("BOB BROWN");
        cardDto.setExpirationDate(LocalDate.now().plusYears(3));

        String cardResponse = mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.number").value("1234567"))
                .andReturn().getResponse().getContentAsString();

        assertThat(paymentCardRepository.count()).isEqualTo(1);

        mockMvc.perform(delete("/users/" + userId))
                .andExpect(status().isNoContent());

        assertThat(paymentCardRepository.count()).isEqualTo(0);
    }
}