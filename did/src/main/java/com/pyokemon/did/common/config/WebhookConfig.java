
// WebhookConfig: ACA-Py로부터 받은 이벤트를 처리하는 방법 정의 - RestTemplate으로 ACA-Py와 HTTP 통신

@Configuration
public class WebhookConfig {

    /**
     * ACA-Py와의 HTTP 통신을 위한 RestTemplate Bean
     * Booking ACA-Py, Mediator ACA-Py와 API 호출 시 사용
     */
    @Bean
    public RestTemplate acapyRestTemplate(){
        return new RestTemplate();
    }
    
    /**
     * Webhook 이벤트 처리를 위한 핸들러 Bean
     * ACA-Py로부터 받은 이벤트를 처리하는 로직 정의
     */
    @Bean
    public WebhookEventHandler webhookEventHandler() {
        return new WebhookEventHandler();
    }

    /**
     * Webhook 이벤트 핸들러
     * - ACA-Py로부터 받은 다양한 이벤트 타입별 처리 로직
     */
    public static class WebhookEventHandler {
        
        //Connection 상태 변경 이벤트 처리 - ACA-Py에서 연결 상태가 변경될 때 호출  
        public void handleConnectionEvent(String eventData){

        }

        //oob 이벤트 처리 - mediator 초대 URL 생성 시 호출 
        public void handleOutOfBandEvent(String eventData){

        }

        //credential 발급 이벤트 처리 - VC 발급 완료 시 호출 
        public void handleIssueCredentialEvent(String eventData){
            
        }

    }
}