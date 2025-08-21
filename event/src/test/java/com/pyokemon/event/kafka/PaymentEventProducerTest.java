package com.pyokemon.event.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.pyokemon.common.dto.kafka.PaymentEvent;

@SpringBootApplication
public class PaymentEventProducerTest {

  public static void main(String[] args) {
    SpringApplication.run(PaymentEventProducerTest.class, args);
  }

  @Component
  public static class PaymentEventProducer implements CommandLineRunner {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void run(String... args) throws Exception {
      // event_schedule_id = 13인 공연의 좌석 결제 테스트

      // 1. 13번 좌석 결제 완료 이벤트 발행
      PaymentEvent doneEvent = new PaymentEvent(1L, 13013L, "DONE"); // 13013 = scheduleId(13) *
                                                                     // 1000 + seatId(13)
      kafkaTemplate.send("payment-events", doneEvent);
      System.out.println("Payment DONE event sent for scheduleId=13, seatId=13, bookingId: "
          + doneEvent.getBookingId());

      // 잠시 대기
      Thread.sleep(2000);

      // 2. 25번 좌석 결제 완료 이벤트 발행
      PaymentEvent doneEvent2 = new PaymentEvent(2L, 13025L, "DONE"); // 13025 = scheduleId(13) *
                                                                      // 1000 + seatId(25)
      kafkaTemplate.send("payment-events", doneEvent2);
      System.out.println("Payment DONE event sent for scheduleId=13, seatId=25, bookingId: "
          + doneEvent2.getBookingId());

      // 잠시 대기
      Thread.sleep(2000);

      // 3. 50번 좌석 결제 취소 이벤트 발행
      PaymentEvent canceledEvent = new PaymentEvent(3L, 13050L, "CANCELED"); // 13050 =
                                                                             // scheduleId(13) *
                                                                             // 1000 + seatId(50)
      kafkaTemplate.send("payment-events", canceledEvent);
      System.out.println("Payment CANCELED event sent for scheduleId=13, seatId=50, bookingId: "
          + canceledEvent.getBookingId());

      // 잠시 대기
      Thread.sleep(2000);

      // 4. 75번 좌석 결제 실패 이벤트 발행
      PaymentEvent failedEvent = new PaymentEvent(4L, 13075L, "FAILED"); // 13075 = scheduleId(13) *
                                                                         // 1000 + seatId(75)
      kafkaTemplate.send("payment-events", failedEvent);
      System.out.println("Payment FAILED event sent for scheduleId=13, seatId=75, bookingId: "
          + failedEvent.getBookingId());

      // 애플리케이션 종료
      Thread.sleep(5000);
      System.exit(0);
    }
  }
}
