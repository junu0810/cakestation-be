package com.cakestation.backend.cakestore.controller;

import com.cakestation.backend.cakestore.service.CakeStoreService;
import com.cakestation.backend.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static com.cakestation.backend.cakestore.fixture.StoreFixture.getCreateCakeStoreDto;
import static com.cakestation.backend.user.fixture.UserFixture.getKakaoUserDto;

@AutoConfigureMockMvc
@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {"spring.config.location=classpath:application-test.yml"})
public class CakeStoreControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CakeStoreService cakeStoreService;

    @Autowired
    private UserService userService;

    @Test
    public void 가게_조회() throws Exception {

        // given
        // 회원 등록
        userService.join(getKakaoUserDto());

        // 가게 등록
        cakeStoreService.saveStore(getCreateCakeStoreDto());

        String uri = "/api/stores/1";
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(uri)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").exists())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }
}
