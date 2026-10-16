package com.inno.userservice.integration;

import com.inno.userservice.dto.UserDto;
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
public class UserIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
    }

    @Test
    void createUser_and_GetWithCacheCheck() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("Bob");
        userDto.setSurname("Brown");
        userDto.setEmail("bob@example.com");
        userDto.setBirthDate(LocalDate.of(2000, 2, 10));

        String responseString = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Bob"))
                .andReturn().getResponse().getContentAsString();

        UserDto createdUser = objectMapper.readValue(responseString, UserDto.class);
        Long userId = createdUser.getId();

        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("bob@example.com"));

        assertThat(cacheManager.getCache("users").get(userId)).isNotNull();
    }

    @Test
    void createUser_validationErrors() throws Exception {
        UserDto invalidDto = new UserDto();
        invalidDto.setName("");
        invalidDto.setSurname("Brown");
        invalidDto.setEmail("not-an-email");
        invalidDto.setBirthDate(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    void updateUser_and_CacheEvictionCheck() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("Bob");
        userDto.setSurname("Brown");
        userDto.setEmail("bob@example.com");
        userDto.setBirthDate(LocalDate.of(2000, 2, 10));

        String responseString = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UserDto createdUser = objectMapper.readValue(responseString, UserDto.class);
        Long userId = createdUser.getId();

        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isOk());

        Cache usersCache = cacheManager.getCache("users");
        assertThat(usersCache).isNotNull();
        assertThat(usersCache.get(userId)).isNotNull();

        createdUser.setName("updatedBob");
        mockMvc.perform(put("/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdUser)))
                .andExpect(status().isOk());

        UserDto cachedUser = (UserDto) usersCache.get(userId).get();
        assertThat(cachedUser.getName()).isEqualTo("updatedBob");
    }

    @Test
    void deleteUser_and_NotFoundCheck() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("Bob");
        userDto.setSurname("Brown");
        userDto.setEmail("bob@example.com");
        userDto.setBirthDate(LocalDate.of(2000, 2, 10));

        String responseString = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UserDto createdUser = objectMapper.readValue(responseString, UserDto.class);
        Long userId = createdUser.getId();

        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isOk());

        Cache usersCache = cacheManager.getCache("users");
        assertThat(usersCache).isNotNull();
        assertThat(usersCache.get(userId)).isNotNull();

        mockMvc.perform(delete("/users/" + userId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isNotFound());

        assertThat(usersCache.get(userId)).isNull();
    }

    @Test
    void deactivateUser_success() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("Bob");
        userDto.setSurname("Brown");
        userDto.setEmail("bob@example.com");
        userDto.setBirthDate(LocalDate.of(2000, 2, 10));

        String responseString = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UserDto createdUser = objectMapper.readValue(responseString, UserDto.class);
        Long userId = createdUser.getId();

        mockMvc.perform(patch("/users/" + userId + "/deactivate"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void getUsers_withPaginationAndFilter() throws Exception {
        UserDto user1 = new UserDto();
        user1.setName("Alice");
        user1.setSurname("Brown");
        user1.setEmail("alice@example.com");
        user1.setBirthDate(LocalDate.of(1995, 1, 1));

        UserDto user2 = new UserDto();
        user2.setName("Bob");
        user2.setSurname("Brown");
        user2.setEmail("bob@example.com");
        user2.setBirthDate(LocalDate.of(1992, 2, 2));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2)));

        mockMvc.perform(get("/users")
                        .param("surname", "Brown")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].surname").value("Brown"))
                .andExpect(jsonPath("$.content[1].surname").value("Brown"));
    }
}