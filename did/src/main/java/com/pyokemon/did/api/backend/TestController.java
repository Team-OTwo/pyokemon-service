package com.pyokemon.did.api.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.did.event.consumer.message.booking.BookingEvent;
import com.pyokemon.did.service.AcaPyConnectionService;
import com.pyokemon.did.service.IssuedVcService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {
  private final AcaPyConnectionService acaPyConnectionService;
  private final IssuedVcService issuedVcService;

  @PostMapping
  public void hello() {
    // acaPyConnectionService.createAcaPyConnection(2L, 37L);
    BookingEvent bookingEvent = new BookingEvent();
    bookingEvent.setBookingId(1L);
    bookingEvent.setAccountId(37L);
    bookingEvent.setTenantId(2L);
    bookingEvent.setEventScheduleId(4L);
    bookingEvent.setSeatId(5L);
    bookingEvent.setStatus("CONFIRM");

    issuedVcService.issueCredential(bookingEvent);
  }

  @PostMapping("/issue-vc")
  public String issueVC(@RequestBody TestIssueVCRequest request) {
    log.info("VC 발급 테스트 요청: {}", request);

    try {
      // issuedVcService.issueCredential(request);
      return "VC 발급 성공";
    } catch (Exception e) {
      log.error("VC 발급 실패: {}", e.getMessage(), e);
      return "VC 발급 실패: " + e.getMessage();
    }
  }

  @GetMapping("/check-vc/{bookingId}")
  public String checkVC(@PathVariable Long bookingId) {
    log.info("VC 상태 확인 요청: bookingId={}", bookingId);

    try {
      // Boolean isIssued = issuedVcService.isIssuedVC(bookingId);
      // return "VC 발급 상태: " + (isIssued ? "발급됨" : "미발급");
      return "hello";
    } catch (Exception e) {
      log.error("VC 상태 확인 실패: {}", e.getMessage(), e);
      return "VC 상태 확인 실패: " + e.getMessage();
    }
  }

  public static class TestIssueVCRequest {
    private Long userId;
    private Long tenantId;
    private Long bookingId;

    // Getters and Setters
    public Long getUserId() {
      return userId;
    }

    public void setUserId(Long userId) {
      this.userId = userId;
    }

    public Long getTenantId() {
      return tenantId;
    }

    public void setTenantId(Long tenantId) {
      this.tenantId = tenantId;
    }

    public Long getBookingId() {
      return bookingId;
    }

    public void setBookingId(Long bookingId) {
      this.bookingId = bookingId;
    }

    @Override
    public String toString() {
      return "TestIssueVCRequest{" + "userId=" + userId + ", tenantId=" + tenantId + ", bookingId="
          + bookingId + '}';
    }
  }
}
