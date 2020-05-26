package wooteco.subway.web.member;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static wooteco.subway.service.member.MemberServiceTest.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import wooteco.subway.doc.LoginMemberDocumentation;
import wooteco.subway.domain.favorite.FavoriteDetail;
import wooteco.subway.domain.member.Member;
import wooteco.subway.infra.JwtTokenProvider;
import wooteco.subway.service.member.MemberService;
import wooteco.subway.service.member.dto.FavoriteRequest;
import wooteco.subway.service.member.dto.LoginRequest;
import wooteco.subway.service.member.dto.UpdateMemberRequest;
import wooteco.subway.service.member.exception.NotFoundMemberException;

@ExtendWith(RestDocumentationExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class LoginMemberControllerTest {
    @MockBean
    MemberService memberService;

    @MockBean
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext,
        RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .addFilter(new ShallowEtagHeaderFilter())
            .apply(documentationConfiguration(restDocumentation))
            .build();
    }

    @Test
    @DisplayName("로그인 요청")
    void login() throws Exception {
        given(memberService.createToken(any())).willReturn("token");

        LoginRequest loginRequest = new LoginRequest(TEST_USER_EMAIL, TEST_USER_PASSWORD);
        String inputJson = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/oauth/token")
            .content(inputJson)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("token"))
            .andExpect(jsonPath("$.tokenType").value("bearer"))
            .andDo(print())
            .andDo(LoginMemberDocumentation.login());
    }

    @Test
    @DisplayName("내 정보 조회")
    void getMemberOfMine() throws Exception {
        Member member = new Member(1L, TEST_USER_EMAIL, TEST_USER_NAME, TEST_USER_PASSWORD);
        given(jwtTokenProvider.validateToken(any())).willReturn(true);
        given(memberService.findMemberByEmail(any())).willReturn(member);

        mockMvc.perform(get("/me")
            .header("Authorization", "Bearer access_token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.email").value(TEST_USER_EMAIL))
            .andExpect(jsonPath("$.name").value(TEST_USER_NAME))
            .andDo(LoginMemberDocumentation.getMemberOfMine());
    }

    @Test
    @DisplayName("내 정보 조회 - 토큰이 유효하지 않는 경우")
    void getMemberOfMine_invalid_token() throws Exception {
        given(jwtTokenProvider.validateToken(any())).willReturn(false);

        mockMvc.perform(get("/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("내 정보 수정")
    void updateMemberOfMine() throws Exception {
        Member member = new Member(1L, TEST_USER_EMAIL, TEST_USER_NAME, TEST_USER_PASSWORD);
        given(jwtTokenProvider.validateToken(any())).willReturn(true);
        given(memberService.findMemberByEmail(any())).willReturn(member);

        UpdateMemberRequest updateMemberRequest = new UpdateMemberRequest("updateName",
            "updatePassword");
        String inputJson = objectMapper.writeValueAsString(updateMemberRequest);

        mockMvc.perform(
            patch("/me")
                .header("Authorization", "Bearer access_token")
                .content(inputJson)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(LoginMemberDocumentation.updateMemberOfMine());
    }

    @Test
    @DisplayName("내 정보 수정 - 토큰이 유효하지 않는 경우")
    void updateMemberOfMine_invalid_token() throws Exception {
        given(jwtTokenProvider.validateToken(any())).willReturn(false);

        UpdateMemberRequest updateMemberRequest = new UpdateMemberRequest("updateName",
            "updatePassword");
        String inputJson = objectMapper.writeValueAsString(updateMemberRequest);

        mockMvc.perform(patch("/me")
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("회원 탈퇴")
    void deleteMemberOfMine() throws Exception {
        Member member = new Member(1L, TEST_USER_EMAIL, TEST_USER_NAME, TEST_USER_PASSWORD);
        given(jwtTokenProvider.validateToken(any())).willReturn(true);
        given(memberService.findMemberByEmail(any())).willReturn(member);

        mockMvc.perform(delete("/me")
            .header("Authorization", "Bearer access_token"))
            .andExpect(status().isNoContent())
            .andDo(LoginMemberDocumentation.deleteMemberOfMine());
    }

    @Test
    @DisplayName("회원 탈퇴 - 토큰이 유효하지 않는 경우")
    void deleteMemberOfMine_invalid_token() throws Exception {
        given(jwtTokenProvider.validateToken(any())).willReturn(false);

        mockMvc.perform(delete("/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("회원 탈퇴 - 토큰이 유효하지만 이미 회원 탈퇴한 경우")
    void deleteMemberOfMine_with_valid_token_after_delete_account() throws Exception {
        given(jwtTokenProvider.validateToken(any())).willReturn(true);
        given(memberService.findMemberByEmail(any())).willThrow(NotFoundMemberException.class);

        mockMvc.perform(delete("/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("즐겨찾기 추가")
    void createFavorite() throws Exception {
        given(jwtTokenProvider.validateToken(any())).willReturn(true);
        String inputJson = objectMapper.writeValueAsString(new FavoriteRequest(1L, 2L));

        mockMvc.perform(post("/me/favorites")
            .header("Authorization", "Bearer access_token")
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andDo(LoginMemberDocumentation.createFavorite());
    }

    @Test
    @DisplayName("즐겨찾기 조회")
    void getFavorites() throws Exception {
        List<FavoriteDetail> favoriteDetails = Lists.newArrayList(
            new FavoriteDetail(1L, 2L, "잠실역", "삼성역"));

        given(jwtTokenProvider.validateToken(any())).willReturn(true);
        given(memberService.getFavorites(any())).willReturn(favoriteDetails);

        mockMvc.perform(get("/me/favorites")
            .header("Authorization", "Bearer access_token")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("[0].sourceId").value(favoriteDetails.get(0).getSourceId()))
            .andExpect(jsonPath("[0].targetId").value(favoriteDetails.get(0).getTargetId()))
            .andExpect(jsonPath("[0].sourceName").value(favoriteDetails.get(0).getSourceName()))
            .andExpect(jsonPath("[0].targetName").value(favoriteDetails.get(0).getTargetName()))
            .andDo(print())
            .andDo(LoginMemberDocumentation.getFavorites());
    }

    @Test
    @DisplayName("즐겨찾기 여부 확인")
    void hasFavorite() throws Exception {
        given(jwtTokenProvider.validateToken(any())).willReturn(true);
        given(memberService.hasFavorite(any(), anyLong(), anyLong())).willReturn(true);

        mockMvc.perform(get("/me/favorites/from/{sourceId}/to/{targetId}", 1L, 2L)
            .header("Authorization", "Bearer access_token")
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exist").value(true))
            .andDo(print())
            .andDo(LoginMemberDocumentation.hasFavorite());
    }

    @Test
    @DisplayName("즐겨찾기 삭제")
    void deleteFavorite() throws Exception {
        given(jwtTokenProvider.validateToken(any())).willReturn(true);

        mockMvc.perform(delete("/me/favorites/from/{sourceId}/to/{targetId}", 1L, 2L)
            .header("Authorization", "Bearer access_token"))
            .andExpect(status().isNoContent())
            .andDo(print())
            .andDo(LoginMemberDocumentation.deleteFavorite());
    }
}