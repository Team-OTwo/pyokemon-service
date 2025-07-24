package com.pyokemon.tenant.api.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.pyokemon.tenant.api.dto.request.CreateTenantRequestDto;
import com.pyokemon.tenant.api.dto.request.LoginRequestDto;
import com.pyokemon.tenant.api.dto.request.UpdatePasswordRequestDto;
import com.pyokemon.tenant.api.dto.request.UpdateProfileRequestDto;
import com.pyokemon.tenant.api.dto.response.TenantDetailResponseDto;
import com.pyokemon.tenant.api.dto.response.TenantListResponseDto;
import com.pyokemon.tenant.api.entity.Tenant;
import com.pyokemon.tenant.api.repository.TenantRepository;
import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.tenant.mapper.TenantConverter;
import com.pyokemon.tenant.secret.jwt.TokenGenerator;
import com.pyokemon.tenant.secret.jwt.dto.TokenDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantService 단위 테스트")
class TenantServiceTest {

  @Mock
  private TenantRepository tenantRepository;

  @Mock
  private TenantConverter tenantConverter;

  @Mock
  private TokenGenerator tokenGenerator;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private TenantService tenantService;

  private Tenant testTenant;
  private TenantDetailResponseDto testResponseDto;
  private CreateTenantRequestDto testCreateRequest;
  private LoginRequestDto testLoginRequest;

  @BeforeEach
  void setUp() {
    // 테스트용 Tenant 엔티티
    testTenant = Tenant.builder().id(1L).loginId("test-tenant").password("encoded-password")
        .corpName("테스트 회사").corpId("123-45-67890").city("서울시").street("강남구 테헤란로").zipcode("12345")
        .ceoName("홍길동").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

    // 테스트용 응답 DTO
    testResponseDto =
        TenantDetailResponseDto.builder().id(1L).loginId("test-tenant").corpName("테스트 회사")
            .corpId("123-45-67890").city("서울시").street("강남구 테헤란로").zipcode("12345").ceoName("홍길동")
            .createdAt(testTenant.getCreatedAt()).updatedAt(testTenant.getUpdatedAt()).build();

    // 테스트용 생성 요청 DTO
    testCreateRequest = new CreateTenantRequestDto();
    testCreateRequest.setLoginId("test-tenant");
    testCreateRequest.setPassword("password123");
    testCreateRequest.setCorpName("테스트 회사");
    testCreateRequest.setBusinessNumber("123-45-67890");
    testCreateRequest.setCity("서울시");
    testCreateRequest.setStreet("강남구 테헤란로");
    testCreateRequest.setZipcode("12345");
    testCreateRequest.setCeoName("홍길동");

    // 테스트용 로그인 요청 DTO
    testLoginRequest = new LoginRequestDto();
    testLoginRequest.setLoginId("test-tenant");
    testLoginRequest.setPassword("password123");
  }

  @Nested
  @DisplayName("getTenantById 테스트")
  class GetTenantByIdTest {

    @Test
    @DisplayName("정상적인 테넌트 조회 성공")
    void getTenantById_Success() {
      // given
      Long tenantId = 1L;
      given(tenantRepository.findById(tenantId)).willReturn(Optional.of(testTenant));
      given(tenantConverter.toResponseDto(testTenant)).willReturn(testResponseDto);

      // when
      TenantDetailResponseDto result = tenantService.getTenantById(tenantId);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(tenantId);
      assertThat(result.getLoginId()).isEqualTo("test-tenant");
      assertThat(result.getCorpName()).isEqualTo("테스트 회사");

      verify(tenantRepository).findById(tenantId);
      verify(tenantConverter).toResponseDto(testTenant);
    }

    @Test
    @DisplayName("존재하지 않는 테넌트 조회 시 예외 발생")
    void getTenantById_NotFound() {
      // given
      Long tenantId = 999L;
      given(tenantRepository.findById(tenantId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> tenantService.getTenantById(tenantId))
          .isInstanceOf(BusinessException.class);

      verify(tenantRepository).findById(tenantId);
      verify(tenantConverter, never()).toResponseDto(any());
    }

    @Test
    @DisplayName("null ID로 조회 시 예외 발생")
    void getTenantById_NullId() {
      // when & then
      assertThatThrownBy(() -> tenantService.getTenantById(null))
          .isInstanceOf(BusinessException.class);

      verify(tenantRepository, never()).findById(any());
    }
  }

  @Nested
  @DisplayName("createTenant 테스트")
  class CreateTenantTest {

    @Test
    @DisplayName("정상적인 테넌트 생성 성공")
    void createTenant_Success() {
      // given
      given(tenantRepository.existsByLoginId(testCreateRequest.getLoginId())).willReturn(false);
      given(tenantRepository.existsByCorpId(testCreateRequest.getBusinessNumber()))
          .willReturn(false);
      given(passwordEncoder.encode(testCreateRequest.getPassword())).willReturn("encoded-password");
      given(tenantConverter.toEntity(testCreateRequest, "encoded-password")).willReturn(testTenant);
      given(tenantConverter.toResponseDto(testTenant)).willReturn(testResponseDto);

      // when
      TenantDetailResponseDto result = tenantService.createTenant(testCreateRequest);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getLoginId()).isEqualTo(testCreateRequest.getLoginId());
      assertThat(result.getCorpName()).isEqualTo(testCreateRequest.getCorpName());

      verify(tenantRepository).existsByLoginId(testCreateRequest.getLoginId());
      verify(tenantRepository).existsByCorpId(testCreateRequest.getBusinessNumber());
      verify(passwordEncoder).encode(testCreateRequest.getPassword());
      verify(tenantRepository).insert(testTenant);
      verify(tenantConverter).toEntity(testCreateRequest, "encoded-password");
      verify(tenantConverter).toResponseDto(testTenant);
    }

    @Test
    @DisplayName("중복된 로그인 ID로 생성 시 예외 발생")
    void createTenant_DuplicateLoginId() {
      // given
      given(tenantRepository.existsByLoginId(testCreateRequest.getLoginId())).willReturn(true);

      // when & then
      assertThatThrownBy(() -> tenantService.createTenant(testCreateRequest))
          .isInstanceOf(BusinessException.class);

      verify(tenantRepository).existsByLoginId(testCreateRequest.getLoginId());
      verify(tenantRepository, never()).existsByCorpId(any());
      verify(tenantRepository, never()).insert(any());
    }

    @Test
    @DisplayName("중복된 사업자번호로 생성 시 예외 발생")
    void createTenant_DuplicateBusinessNumber() {
      // given
      given(tenantRepository.existsByLoginId(testCreateRequest.getLoginId())).willReturn(false);
      given(tenantRepository.existsByCorpId(testCreateRequest.getBusinessNumber()))
          .willReturn(true);

      // when & then
      assertThatThrownBy(() -> tenantService.createTenant(testCreateRequest))
          .isInstanceOf(BusinessException.class);

      verify(tenantRepository).existsByLoginId(testCreateRequest.getLoginId());
      verify(tenantRepository).existsByCorpId(testCreateRequest.getBusinessNumber());
      verify(tenantRepository, never()).insert(any());
    }
  }

  @Nested
  @DisplayName("login 테스트")
  class LoginTest {

    @Test
    @DisplayName("정상적인 로그인 성공")
    void login_Success() {
      // given
      TokenDto.JwtToken accessToken = new TokenDto.JwtToken("access-token", 3600);
      TokenDto.JwtToken refreshToken = new TokenDto.JwtToken("refresh-token", 86400);
      TokenDto.AccessRefreshToken expectedTokens =
          new TokenDto.AccessRefreshToken(accessToken, refreshToken);

      given(tenantRepository.findByLoginId(testLoginRequest.getLoginId()))
          .willReturn(Optional.of(testTenant));
      given(passwordEncoder.matches(testLoginRequest.getPassword(), testTenant.getPassword()))
          .willReturn(true);
      given(tokenGenerator.generateAccessRefreshToken(testTenant.getLoginId(), "WEB"))
          .willReturn(expectedTokens);

      // when
      TokenDto.AccessRefreshToken result = tenantService.login(testLoginRequest);

      // then
      assertThat(result).isNotNull();
      assertThat(result.access().token()).isEqualTo("access-token");
      assertThat(result.refresh().token()).isEqualTo("refresh-token");

      verify(tenantRepository).findByLoginId(testLoginRequest.getLoginId());
      verify(passwordEncoder).matches(testLoginRequest.getPassword(), testTenant.getPassword());
      verify(tokenGenerator).generateAccessRefreshToken(testTenant.getLoginId(), "WEB");
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 로그인 시 예외 발생")
    void login_UserNotFound() {
      // given
      given(tenantRepository.findByLoginId(testLoginRequest.getLoginId()))
          .willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> tenantService.login(testLoginRequest))
          .isInstanceOf(BusinessException.class);

      verify(tenantRepository).findByLoginId(testLoginRequest.getLoginId());
      verify(passwordEncoder, never()).matches(any(), any());
      verify(tokenGenerator, never()).generateAccessRefreshToken(any(), any());
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 예외 발생")
    void login_WrongPassword() {
      // given
      given(tenantRepository.findByLoginId(testLoginRequest.getLoginId()))
          .willReturn(Optional.of(testTenant));
      given(passwordEncoder.matches(testLoginRequest.getPassword(), testTenant.getPassword()))
          .willReturn(false);

      // when & then
      assertThatThrownBy(() -> tenantService.login(testLoginRequest))
          .isInstanceOf(BusinessException.class);

      verify(tenantRepository).findByLoginId(testLoginRequest.getLoginId());
      verify(passwordEncoder).matches(testLoginRequest.getPassword(), testTenant.getPassword());
      verify(tokenGenerator, never()).generateAccessRefreshToken(any(), any());
    }

    @Test
    @DisplayName("빈 로그인 ID로 로그인 시 예외 발생")
    void login_EmptyLoginId() {
      // given
      LoginRequestDto emptyLoginIdRequest = new LoginRequestDto();
      emptyLoginIdRequest.setLoginId("");
      emptyLoginIdRequest.setPassword("password123");

      // when & then
      assertThatThrownBy(() -> tenantService.login(emptyLoginIdRequest))
          .isInstanceOf(BusinessException.class);

      verify(tenantRepository, never()).findByLoginId(any());
    }

    @Test
    @DisplayName("빈 비밀번호로 로그인 시 예외 발생")
    void login_EmptyPassword() {
      // given
      LoginRequestDto emptyPasswordRequest = new LoginRequestDto();
      emptyPasswordRequest.setLoginId("test-tenant");
      emptyPasswordRequest.setPassword("");

      // when & then
      assertThatThrownBy(() -> tenantService.login(emptyPasswordRequest))
          .isInstanceOf(BusinessException.class);

      verify(tenantRepository, never()).findByLoginId(any());
    }
  }

  @Nested
  @DisplayName("getAllTenants 테스트")
  class GetAllTenantsTest {

    @Test
    @DisplayName("전체 테넌트 리스트 조회 성공")
    void getAllTenants_Success() {
      // given
      List<Tenant> tenants = Arrays.asList(testTenant);
      TenantListResponseDto expectedResponse =
          TenantListResponseDto.of(Arrays.asList(testResponseDto));

      given(tenantRepository.findAll()).willReturn(tenants);
      given(tenantConverter.toListResponseDto(tenants)).willReturn(expectedResponse);

      // when
      TenantListResponseDto result = tenantService.getAllTenants();

      // then
      assertThat(result).isNotNull();
      assertThat(result.getTotalCount()).isEqualTo(1);
      assertThat(result.getTenants()).hasSize(1);

      verify(tenantRepository).findAll();
      verify(tenantConverter).toListResponseDto(tenants);
    }

    @Test
    @DisplayName("빈 테넌트 리스트 조회")
    void getAllTenants_EmptyList() {
      // given
      List<Tenant> emptyTenants = Arrays.asList();
      TenantListResponseDto expectedResponse = TenantListResponseDto.of(Arrays.asList());

      given(tenantRepository.findAll()).willReturn(emptyTenants);
      given(tenantConverter.toListResponseDto(emptyTenants)).willReturn(expectedResponse);

      // when
      TenantListResponseDto result = tenantService.getAllTenants();

      // then
      assertThat(result).isNotNull();
      assertThat(result.getTotalCount()).isEqualTo(0);
      assertThat(result.getTenants()).isEmpty();

      verify(tenantRepository).findAll();
      verify(tenantConverter).toListResponseDto(emptyTenants);
    }
  }

  @Nested
  @DisplayName("deleteTenant 테스트")
  class DeleteTenantTest {

    @Test
    @DisplayName("정상적인 테넌트 삭제 성공")
    void deleteTenant_Success() {
      // given
      Long tenantId = 1L;
      given(tenantRepository.existsById(tenantId)).willReturn(true);

      // when
      tenantService.deleteTenant(tenantId);

      // then
      verify(tenantRepository).existsById(tenantId);
      verify(tenantRepository).deleteById(tenantId);
    }

    @Test
    @DisplayName("존재하지 않는 테넌트 삭제 시 예외 발생")
    void deleteTenant_NotFound() {
      // given
      Long tenantId = 999L;
      given(tenantRepository.existsById(tenantId)).willReturn(false);

      // when & then
      assertThatThrownBy(() -> tenantService.deleteTenant(tenantId))
          .isInstanceOf(BusinessException.class);

      verify(tenantRepository).existsById(tenantId);
      verify(tenantRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("null ID로 삭제 시 예외 발생")
    void deleteTenant_NullId() {
      // when & then
      assertThatThrownBy(() -> tenantService.deleteTenant(null))
          .isInstanceOf(BusinessException.class);

      verify(tenantRepository, never()).existsById(any());
      verify(tenantRepository, never()).deleteById(any());
    }
  }

  @Nested
  @DisplayName("refresh 테스트")
  class RefreshTest {

    @Test
    @DisplayName("정상적인 토큰 갱신 성공")
    void refresh_Success() {
      // given
      TokenDto.RefreshRequest refreshRequest = new TokenDto.RefreshRequest();
      refreshRequest.setRefreshToken("valid-refresh-token");

      TokenDto.JwtToken newAccessToken = new TokenDto.JwtToken("new-access-token", 3600);
      TokenDto.AccessToken expectedResponse = new TokenDto.AccessToken(newAccessToken);

      given(tokenGenerator.validateJwtToken(refreshRequest.getRefreshToken()))
          .willReturn(testTenant.getLoginId());
      given(tenantRepository.findByLoginId(testTenant.getLoginId()))
          .willReturn(Optional.of(testTenant));
      given(tokenGenerator.generateAccessToken(testTenant.getLoginId(), "WEB"))
          .willReturn(expectedResponse);

      // when
      TokenDto.AccessToken result = tenantService.refresh(refreshRequest);

      // then
      assertThat(result).isNotNull();
      assertThat(result.access().token()).isEqualTo("new-access-token");

      verify(tokenGenerator).validateJwtToken(refreshRequest.getRefreshToken());
      verify(tenantRepository).findByLoginId(testTenant.getLoginId());
      verify(tokenGenerator).generateAccessToken(testTenant.getLoginId(), "WEB");
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰으로 갱신 시 예외 발생")
    void refresh_InvalidToken() {
      // given
      TokenDto.RefreshRequest refreshRequest = new TokenDto.RefreshRequest();
      refreshRequest.setRefreshToken("invalid-refresh-token");

      given(tokenGenerator.validateJwtToken(refreshRequest.getRefreshToken())).willReturn(null);

      // when & then
      assertThatThrownBy(() -> tenantService.refresh(refreshRequest))
          .isInstanceOf(BusinessException.class);

      verify(tokenGenerator).validateJwtToken(refreshRequest.getRefreshToken());
      verify(tenantRepository, never()).findByLoginId(any());
      verify(tokenGenerator, never()).generateAccessToken(any(), any());
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 토큰으로 갱신 시 예외 발생")
    void refresh_UserNotFound() {
      // given
      TokenDto.RefreshRequest refreshRequest = new TokenDto.RefreshRequest();
      refreshRequest.setRefreshToken("valid-refresh-token");

      given(tokenGenerator.validateJwtToken(refreshRequest.getRefreshToken()))
          .willReturn("non-existent-user");
      given(tenantRepository.findByLoginId("non-existent-user")).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> tenantService.refresh(refreshRequest))
          .isInstanceOf(BusinessException.class);

      verify(tokenGenerator).validateJwtToken(refreshRequest.getRefreshToken());
      verify(tenantRepository).findByLoginId("non-existent-user");
      verify(tokenGenerator, never()).generateAccessToken(any(), any());
    }

    @Test
    @DisplayName("빈 리프레시 토큰으로 갱신 시 예외 발생")
    void refresh_EmptyToken() {
      // given
      TokenDto.RefreshRequest refreshRequest = new TokenDto.RefreshRequest();
      refreshRequest.setRefreshToken("");

      // when & then
      assertThatThrownBy(() -> tenantService.refresh(refreshRequest))
          .isInstanceOf(BusinessException.class);

      verify(tokenGenerator, never()).validateJwtToken(any());
    }
  }

  @Nested
  @DisplayName("updateProfile 테스트")
  class UpdateProfileTest {

    private UpdateProfileRequestDto updateRequest;
    private Tenant updatedTenant;

    @BeforeEach
    void setUp() {
      updateRequest = new UpdateProfileRequestDto();
      updateRequest.setCorpName("수정된 회사명");
      updateRequest.setCity("부산시");
      updateRequest.setStreet("해운대구 센텀로");
      updateRequest.setZipcode("54321");
      updateRequest.setCeoName("김철수");

      updatedTenant = Tenant.builder().id(testTenant.getId()).loginId(testTenant.getLoginId())
          .password(testTenant.getPassword()).corpId(testTenant.getCorpId())
          .corpName(updateRequest.getCorpName()).city(updateRequest.getCity())
          .street(updateRequest.getStreet()).zipcode(updateRequest.getZipcode())
          .ceoName(updateRequest.getCeoName()).createdAt(testTenant.getCreatedAt())
          .updatedAt(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("정상적인 프로필 수정 성공")
    void updateProfile_Success() {
      // given
      Long tenantId = 1L;
      TenantDetailResponseDto updatedResponseDto =
          TenantDetailResponseDto.builder().id(tenantId).loginId(testTenant.getLoginId())
              .corpName(updateRequest.getCorpName()).corpId(testTenant.getCorpId())
              .city(updateRequest.getCity()).street(updateRequest.getStreet())
              .zipcode(updateRequest.getZipcode()).ceoName(updateRequest.getCeoName())
              .createdAt(testTenant.getCreatedAt()).updatedAt(updatedTenant.getUpdatedAt()).build();

      given(tenantRepository.findById(tenantId)).willReturn(Optional.of(testTenant)) // 첫 번째 호출: 기존
                                                                                     // 테넌트
          .willReturn(Optional.of(updatedTenant)); // 두 번째 호출: 업데이트된 테넌트
      given(tenantConverter.toResponseDto(any(Tenant.class))).willReturn(updatedResponseDto);

      // when
      TenantDetailResponseDto result = tenantService.updateProfile(tenantId, updateRequest);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getCorpName()).isEqualTo(updateRequest.getCorpName());
      assertThat(result.getCity()).isEqualTo(updateRequest.getCity());
      assertThat(result.getCeoName()).isEqualTo(updateRequest.getCeoName());

      verify(tenantRepository, times(2)).findById(tenantId); // 한번은 조회, 한번은 업데이트 후 조회
      verify(tenantRepository).update(any(Tenant.class));
      verify(tenantConverter).toResponseDto(any(Tenant.class));
    }

    @Test
    @DisplayName("존재하지 않는 테넌트 프로필 수정 시 예외 발생")
    void updateProfile_TenantNotFound() {
      // given
      Long tenantId = 999L;
      given(tenantRepository.findById(tenantId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> tenantService.updateProfile(tenantId, updateRequest))
          .isInstanceOf(BusinessException.class);

      verify(tenantRepository).findById(tenantId);
      verify(tenantRepository, never()).update(any());
    }

    @Test
    @DisplayName("null ID로 프로필 수정 시 예외 발생")
    void updateProfile_NullId() {
      // when & then
      assertThatThrownBy(() -> tenantService.updateProfile(null, updateRequest))
          .isInstanceOf(BusinessException.class);

      verify(tenantRepository, never()).findById(any());
      verify(tenantRepository, never()).update(any());
    }
  }

  @Nested
  @DisplayName("changePassword 테스트")
  class ChangePasswordTest {

    private UpdatePasswordRequestDto passwordRequest;

    @BeforeEach
    void setUp() {
      passwordRequest = new UpdatePasswordRequestDto();
      passwordRequest.setCurrentPassword("current-password");
      passwordRequest.setNewPassword("new-password123");
    }

    @Test
    @DisplayName("정상적인 비밀번호 변경 성공")
    void changePassword_Success() {
      // given
      Long tenantId = 1L;
      String newEncodedPassword = "new-encoded-password";

      given(tenantRepository.findById(tenantId)).willReturn(Optional.of(testTenant));
      given(passwordEncoder.matches(passwordRequest.getCurrentPassword(), testTenant.getPassword()))
          .willReturn(true);
      given(passwordEncoder.encode(passwordRequest.getNewPassword()))
          .willReturn(newEncodedPassword);

      // when
      tenantService.changePassword(tenantId, passwordRequest);

      // then
      verify(tenantRepository).findById(tenantId);
      verify(passwordEncoder).matches(passwordRequest.getCurrentPassword(),
          testTenant.getPassword());
      verify(passwordEncoder).encode(passwordRequest.getNewPassword());
      verify(tenantRepository).update(any(Tenant.class));
    }

    @Test
    @DisplayName("현재 비밀번호 불일치 시 예외 발생")
    void changePassword_WrongCurrentPassword() {
      // given
      Long tenantId = 1L;
      given(tenantRepository.findById(tenantId)).willReturn(Optional.of(testTenant));
      given(passwordEncoder.matches(passwordRequest.getCurrentPassword(), testTenant.getPassword()))
          .willReturn(false);

      // when & then
      assertThatThrownBy(() -> tenantService.changePassword(tenantId, passwordRequest))
          .isInstanceOf(BusinessException.class);

      verify(tenantRepository).findById(tenantId);
      verify(passwordEncoder).matches(passwordRequest.getCurrentPassword(),
          testTenant.getPassword());
      verify(passwordEncoder, never()).encode(any());
      verify(tenantRepository, never()).update(any());
    }

    @Test
    @DisplayName("존재하지 않는 테넌트 비밀번호 변경 시 예외 발생")
    void changePassword_TenantNotFound() {
      // given
      Long tenantId = 999L;
      given(tenantRepository.findById(tenantId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> tenantService.changePassword(tenantId, passwordRequest))
          .isInstanceOf(BusinessException.class);

      verify(tenantRepository).findById(tenantId);
      verify(passwordEncoder, never()).matches(any(), any());
      verify(tenantRepository, never()).update(any());
    }

    @Test
    @DisplayName("빈 현재 비밀번호로 변경 시 예외 발생")
    void changePassword_EmptyCurrentPassword() {
      // given
      Long tenantId = 1L;
      UpdatePasswordRequestDto emptyCurrentPasswordRequest = new UpdatePasswordRequestDto();
      emptyCurrentPasswordRequest.setCurrentPassword("");
      emptyCurrentPasswordRequest.setNewPassword("new-password123");

      // when & then
      assertThatThrownBy(() -> tenantService.changePassword(tenantId, emptyCurrentPasswordRequest))
          .isInstanceOf(BusinessException.class);

      verify(tenantRepository, never()).findById(any());
    }

    @Test
    @DisplayName("빈 새 비밀번호로 변경 시 예외 발생")
    void changePassword_EmptyNewPassword() {
      // given
      Long tenantId = 1L;
      UpdatePasswordRequestDto emptyNewPasswordRequest = new UpdatePasswordRequestDto();
      emptyNewPasswordRequest.setCurrentPassword("current-password");
      emptyNewPasswordRequest.setNewPassword("");

      // when & then
      assertThatThrownBy(() -> tenantService.changePassword(tenantId, emptyNewPasswordRequest))
          .isInstanceOf(BusinessException.class);

      verify(tenantRepository, never()).findById(any());
    }
  }
}
