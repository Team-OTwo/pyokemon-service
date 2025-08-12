package com.pyokemon.account.tenant.service;

import static com.pyokemon.account.auth.entity.AccountStatus.DELETED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.security.crypto.password.PasswordEncoder;
import com.pyokemon.common.util.PasswordUtil;

import com.pyokemon.account.auth.entity.Account;
import com.pyokemon.account.auth.entity.AccountStatus;
import com.pyokemon.account.auth.repository.AccountRepository;
import com.pyokemon.account.tenant.dto.request.TenantRegisterRequestDto;
import com.pyokemon.account.tenant.dto.request.UpdateTenantProfileRequestDto;
import com.pyokemon.account.tenant.dto.response.TenantListResponseDto;
import com.pyokemon.account.tenant.dto.response.TenantProfileResponseDto;
import com.pyokemon.account.tenant.entity.Tenant;
import com.pyokemon.account.tenant.repository.TenantRepository;
import com.pyokemon.common.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

  @Mock
  private TenantRepository tenantRepository;

  @Mock
  private AccountRepository accountRepository;

  @Mock
  // private PasswordEncoder passwordEncoder;
  private PasswordUtil passwordUtil;

  @InjectMocks
  private TenantService tenantService;

  private TenantRegisterRequestDto registerRequest;
  private UpdateTenantProfileRequestDto updateRequest;
  private Account account;
  private Tenant tenant;

  @BeforeEach
  void setUp() {
    registerRequest = TenantRegisterRequestDto.builder().loginId("test_tenant")
        .password("password123").name("테스트 테넌트").corpId("1234567890").city("서울시")
        .street("강남구 테헤란로 123").zipcode("06123").ceo("홍길동").build();

    updateRequest = UpdateTenantProfileRequestDto.builder().city("부산시").street("해운대구 센텀로 456")
        .zipcode("48058").ceo("김철수").build();

    account = Account.builder().accountId(1L).loginId("test_tenant").password("encoded_password")
        .role("TENANT").status(AccountStatus.ACTIVE).build();

    tenant = Tenant.builder().tenantId(1L).accountId(1L).name("테스트 테넌트").corpId("1234567890")
        .city("서울시").street("강남구 테헤란로 123").zipcode("06123").ceo("홍길동").build();
  }

  @Test
  void 테넌트_등록() {
    // Given
    when(accountRepository.findByLoginId(registerRequest.getLoginId())).thenReturn(Optional.empty());
    when(tenantRepository.findByCorpId(registerRequest.getCorpId())).thenReturn(Optional.empty());
    when(passwordUtil.encode(registerRequest.getPassword())).thenReturn("encoded_password");
    when(accountRepository.insert(any(Account.class))).thenReturn(1);
    when(tenantRepository.insert(any(Tenant.class))).thenReturn(1);

    // When
    TenantProfileResponseDto result = tenantService.registerTenant(registerRequest);

    // Then
    assertNotNull(result);
    assertEquals(registerRequest.getName(), result.getName());
    assertEquals(registerRequest.getCorpId(), result.getCorpId());
    assertEquals(registerRequest.getLoginId(), result.getLoginId());
    
    verify(accountRepository).findByLoginId(registerRequest.getLoginId());
    verify(tenantRepository).findByCorpId(registerRequest.getCorpId());
    verify(passwordUtil).encode(registerRequest.getPassword());
    verify(accountRepository).insert(any(Account.class));
    verify(tenantRepository).insert(any(Tenant.class));
  }

  @Test
  void 테넌트_등록_로그인ID_중복() {
    // Given
    when(accountRepository.findByLoginId(registerRequest.getLoginId())).thenReturn(Optional.of(account));

    // When & Then
    BusinessException exception = assertThrows(BusinessException.class, 
        () -> tenantService.registerTenant(registerRequest));
    assertEquals("DUPLICATE_LOGIN_ID", exception.getErrorCode());
  }

  @Test
  void 테넌트_등록_사업자번호_중복() {
    // Given
    when(accountRepository.findByLoginId(registerRequest.getLoginId())).thenReturn(Optional.empty());
    when(tenantRepository.findByCorpId(registerRequest.getCorpId())).thenReturn(Optional.of(tenant));

    // When & Then
    BusinessException exception = assertThrows(BusinessException.class, 
        () -> tenantService.registerTenant(registerRequest));
    assertEquals("DUPLICATE_CORP_ID", exception.getErrorCode());
  }

  @Test
  void 테넌트_프로필_조회() {
    // Given
    when(tenantRepository.findByTenantId(1L)).thenReturn(Optional.of(tenant));
    when(accountRepository.findByAccountId(1L)).thenReturn(Optional.of(account));

    // When
    TenantProfileResponseDto result = tenantService.getTenantProfile(1L);

    // Then
    assertNotNull(result);
    assertEquals(tenant.getName(), result.getName());
    assertEquals(tenant.getCorpId(), result.getCorpId());
    assertEquals(account.getLoginId(), result.getLoginId());
  }

  @Test
  void 테넌트_프로필_조회_테넌트없음() {
    // Given
    when(tenantRepository.findByTenantId(1L)).thenReturn(Optional.empty());

    // When & Then
    BusinessException exception = assertThrows(BusinessException.class, 
        () -> tenantService.getTenantProfile(1L));
    assertEquals("TENANT_NOT_FOUND", exception.getErrorCode());
  }

  @Test
  void 테넌트_프로필_수정() {
    // Given
    when(tenantRepository.findByTenantId(1L)).thenReturn(Optional.of(tenant));
    when(accountRepository.findByAccountId(1L)).thenReturn(Optional.of(account));
    when(tenantRepository.update(any(Tenant.class))).thenReturn(1);

    // When
    TenantProfileResponseDto result = tenantService.updateTenantProfile(1L, updateRequest);

    // Then
    assertNotNull(result);
    assertEquals(updateRequest.getCity(), result.getCity());
    assertEquals(updateRequest.getStreet(), result.getStreet());
    assertEquals(updateRequest.getZipcode(), result.getZipcode());
    assertEquals(updateRequest.getCeo(), result.getCeo());
    
    verify(tenantRepository).update(any(Tenant.class));
  }

  @Test
  void 테넌트_삭제() {
    // Given
    when(tenantRepository.findByTenantId(1L)).thenReturn(Optional.of(tenant));
    when(accountRepository.updateStatus(1L, DELETED)).thenReturn(1);
    when(tenantRepository.delete(1L)).thenReturn(1);

    // When
    tenantService.deleteTenant(1L);

    // Then
    verify(accountRepository).updateStatus(1L, DELETED);
    verify(tenantRepository).delete(1L);
  }

  @Test
  void 내_테넌트_프로필_조회() {
    // Given
    when(tenantRepository.findByAccountId(1L)).thenReturn(Optional.of(tenant));
    when(accountRepository.findByAccountId(1L)).thenReturn(Optional.of(account));

    // When
    TenantProfileResponseDto result = tenantService.getMyTenantProfile(1L, "1");

    // Then
    assertNotNull(result);
    assertEquals(tenant.getName(), result.getName());
    assertEquals(account.getLoginId(), result.getLoginId());
  }

  @Test
  void 내_테넌트_프로필_조회_권한없음() {
    // When & Then
    BusinessException exception =
        assertThrows(BusinessException.class, () -> tenantService.getMyTenantProfile(1L, "2"));
    assertEquals("ACCESS_DENIED", exception.getErrorCode());
  }

  @Test
  void 내_테넌트_프로필_수정() {
    // Given
    when(tenantRepository.findByAccountId(1L)).thenReturn(Optional.of(tenant));
    when(accountRepository.findByAccountId(1L)).thenReturn(Optional.of(account));
    when(tenantRepository.update(any(Tenant.class))).thenReturn(1);

    // When
    TenantProfileResponseDto result = tenantService.updateMyTenantProfile(1L, updateRequest, "1");

    // Then
    assertNotNull(result);
    assertEquals(updateRequest.getCity(), result.getCity());
    verify(tenantRepository).update(any(Tenant.class));
  }

  @Test
  void 내_테넌트_계정_삭제() {
    // Given
    when(tenantRepository.findByAccountId(1L)).thenReturn(Optional.of(tenant));
    when(accountRepository.updateStatus(1L, DELETED)).thenReturn(1);
    when(tenantRepository.delete(1L)).thenReturn(1);

    // When
    tenantService.deleteMyTenantAccount(1L, "1");

    // Then
    verify(accountRepository).updateStatus(1L, DELETED);
    verify(tenantRepository).delete(1L);
  }

  @Test
  void 모든_테넌트_조회() {
    // Given
    List<Tenant> tenants = Arrays.asList(tenant);
    when(tenantRepository.findAll()).thenReturn(tenants);
    when(accountRepository.findByAccountId(1L)).thenReturn(Optional.of(account));

    // When
    TenantListResponseDto result = tenantService.getAllTenants();

    // Then
    assertNotNull(result);
    assertEquals(1, result.getTotalCount());
    assertEquals(1, result.getTenants().size());
    assertEquals(tenant.getName(), result.getTenants().get(0).getName());
    assertEquals(account.getLoginId(), result.getTenants().get(0).getLoginId());
  }
}
