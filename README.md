# Pyokemon Service

MSA(Microservice Architecture) 기반의 Pyokemon 서비스입니다.

## 기술 스택

- **Java**: 21
- **Spring Boot**: 3.2.0
- **Gradle**: 8.5
- **Database**: MySQL 8.0
- **Message Broker**: Kafka
- **Cache**: Redis
- **API 문서**: OpenAPI 3.0 (Swagger)
- **마이그레이션**: Flyway

## 프로젝트 구조

```
pyokemon-service/
├── settings.gradle           # 모노레포 설정
├── build.gradle             # 루트 빌드 설정
├── gradlew                  # Gradle Wrapper (Unix/Linux/Mac)
├── gradlew.bat             # Gradle Wrapper (Windows)
├── gradle/                 # Gradle Wrapper 파일들
├── common/                 # 공통 모듈
│   ├── build.gradle
│   └── src/
│       ├── main/java/com/pyokemon/common/
│       │   ├── config/     # 공통 설정
│       │   ├── dto/        # 공통 DTO
│       │   ├── entity/     # 공통 엔티티
│       │   ├── exception/  # 공통 예외
│       │   └── util/       # 공통 유틸리티
│       └── main/resources/
│           └── application-common.yml
├── user/                   # 사용자 서비스 모듈
│   ├── build.gradle
│   └── src/
│       ├── main/java/com/pyokemon/user/
│       │   ├── controller/
│       │   ├── service/
│       │   ├── repository/
│       │   └── entity/
│       └── main/resources/
│           ├── application.yml
│           └── db/migration/user/  # Flyway 마이그레이션 파일
└── docker/                 # Docker 관련 파일
    └── docker-compose.yml
```

## 1. Flyway 사용법

### 설정 방법
Flyway는 데이터베이스 마이그레이션 도구로, 프로젝트에서 다음과 같이 설정되어 있습니다.

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 0
    sql-migration-prefix: V
    sql-migration-separator: __
    sql-migration-suffixes: .sql
    placeholder-replacement: true
    placeholders:
      schema: pyokemon
    validate-on-migrate: true
    out-of-order: false
    table: flyway_schema_history
    encoding: UTF-8
    create-schemas: true
```

### 마이그레이션 파일 위치
각 모듈별로 마이그레이션 파일의 위치가 다릅니다:
- user 모듈: `classpath:db/migration/user`

### 마이그레이션 파일 작성 규칙
- 파일명 형식: `V{1:Create, 2: Insert}_{순서}__{설명}.sql`
  - 예: `V1_001__Create_user_table.sql`, `V2_001__Insert_user_table.sql`
- 각 마이그레이션 파일은 멱등성을 보장해야 합니다.

### 마이그레이션 실행 방법
애플리케이션 실행 시 자동으로 마이그레이션이 적용됩니다. 별도의 명령어 실행이 필요하지 않습니다.

## 2. Docker-Compose 사용법

### 프로젝트에 포함된 서비스
`docker-compose.yml` 파일에는 다음과 같은 서비스들이 정의되어 있습니다:

- **MySQL**: 사용자 데이터 저장
  - 포트: 3306:3306
  - 데이터베이스명: pyokemon_user
  - 사용자명: pyokemon
  - 비밀번호: pyokemon123

- **Redis**: 캐싱 및 세션 관리
  - 포트: 6379:6379
  - 영구 데이터 저장 설정: --appendonly yes

- **Kafka & Zookeeper**: 비동기 메시징 시스템
  - Zookeeper 포트: 2181:2181
  - Kafka 포트: 9092:9092

### 실행 방법
```bash
# 서비스 시작
cd pyokemon-service/docker
docker-compose up -d

# 서비스 종료
docker-compose down

# 로그 확인
docker-compose logs -f [서비스명]

# 특정 서비스만 시작
docker-compose up -d [서비스명]
```

### 볼륨 관리
모든 데이터는 Docker 볼륨으로 관리되어 컨테이너를 재시작해도 데이터가 유지됩니다:
- user_mysql_data
- redis_data
- zookeeper_data
- zookeeper_logs
- kafka_data

## 3. Swagger 설정

### 설정 방법
Swagger(SpringDoc OpenAPI)는 다음과 같이 설정되어 있습니다:

```yaml
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    tags-sorter: alpha
    operations-sorter: alpha
  default-consumes-media-type: application/json
  default-produces-media-type: application/json
```

### 접근 방법
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

### API 문서화 방법
Controller 클래스와 메서드에 적절한 어노테이션을 추가하여 API를 문서화할 수 있습니다:

```java
@Operation(summary = "사용자 조회", description = "ID로 사용자 정보를 조회합니다.")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "조회 성공"),
    @ApiResponse(responseCode = "404", description = "사용자 없음")
})
@GetMapping("/users/{id}")
public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
    // 구현 코드
}
```

## 빌드 및 실행 방법

### 1. 빌드

```bash
# Windows / Unix / Linux / Mac
./gradlew build
```

### 2. 실행

```bash
# 전체 서비스 실행
./gradlew bootRun

# 특정 모듈만 실행
./gradlew :user:bootRun

# 프로필 지정 실행
./gradlew :user:bootRun --args='--spring.profiles.active=dev'
```

### 3. 테스트

```bash
# 전체 테스트
./gradlew test

# 특정 모듈 테스트
./gradlew :user:test
```

## 주요 기능

### Common 모듈
- **ResponseDto**: 공통 응답 DTO
- **BusinessException**: 비즈니스 로직 예외
- **GlobalExceptionHandler**: 전역 예외 처리
- **BaseEntity**: 공통 엔티티 (생성일, 수정일, 삭제일)
- **WebConfig**: CORS 및 웹 설정
- **JacksonConfig**: JSON 직렬화 설정
- **StringUtils**: 문자열 유틸리티

### User 모듈
- **User Entity**: 사용자 정보 관리
- **Authentication**: 사용자 인증
- **Authorization**: 사용자 권한

## 4. 프로젝트 구조 및 모듈 설명

### 모듈 구성
프로젝트는 다음과 같은 모듈로 구성되어 있습니다:
- **common**: 공통 모듈 (BaseEntity, 예외 처리, 유틸리티 등)
  - 모든 모듈에서 공통으로 사용하는 코드를 포함
  - BaseEntity, 예외 처리, 유틸리티, 공통 설정 등
  
- **user**: 사용자 관련 서비스
  - 사용자 등록, 인증, 권한 관리 등의 기능 제공
  - MySQL 데이터베이스를 사용하여 사용자 정보 관리

### BaseEntity
모든 엔티티는 BaseEntity를 상속받아 다음 공통 필드를 사용합니다:
```java
@Getter
@Setter
public abstract class BaseEntity {
    
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    public boolean isDeleted() {
        return deletedAt != null;
    }
    
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
```

### 데이터베이스 접속 정보
- URL: jdbc:mysql://localhost:3306/pyokemon_user
- 드라이버: com.mysql.cj.jdbc.Driver
- 사용자명: pyokemon
- 비밀번호: pyokemon123


## 5. 개발 환경 설정

### 필수 소프트웨어
- JDK 21
- Docker Desktop
- Git
- IDE (IntelliJ IDEA 권장)

### 로컬 환경 설정 순서
1. 프로젝트 클론
   ```bash
   git clone https://github.com/your-org/pyokemon.git
   ```

2. Docker 컨테이너 실행
   ```bash
   cd pyokemon-service/docker
   docker-compose up -d
   ```

3. 프로젝트 빌드
   ```bash
   ./gradlew clean build
   ```

4. 애플리케이션 실행
   ```bash
   ./gradlew :user:bootRun
   ```

### IDE 설정
- 롬복(Lombok) 플러그인 설치
- 인코딩: UTF-8 설정
- JDK 21 설정

## 6. 개발 가이드 및 GitHub 작업 플로우

### 브랜치 관리
- `main`: 안정적인 프로덕션 코드
- `develop`: 개발 중인 코드의 통합 브랜치
- `feature/기능명`: 새로운 기능 개발
- `fix/버그명`: 버그 수정
- `release/버전`: 릴리스 준비

### PR(Pull Request) 규칙
1. PR 제목은 작업 내용을 명확히 표현
2. PR 설명에는 작업한 내용과 테스트 방법 기재
3. 코드 리뷰 후 승인 받은 PR만 병합 가능
4. PR은 기능 단위로 작게 유지

### 코딩 스타일
- 자바 코드는 [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) 준수
- 코드 포맷팅은 IDE 자동화 권장
- 클래스명, 인터페이스명은 파스칼 케이스(PascalCase)
- 변수명, 메소드명은 카멜 케이스(camelCase)
- 상수는 대문자 스네이크 케이스(UPPER_SNAKE_CASE)

### 네이밍 컨벤션

#### Java 코드
| 유형 | 규칙 | 예시 |
|------|------|------|
| 클래스 | 명사, PascalCase | `UserService`, `OrderController` |
| 인터페이스 | 형용사/명사, PascalCase | `Searchable`, `UserRepository` |
| 추상 클래스 | Abstract 접두사, PascalCase | `AbstractEntity`, `AbstractService` |
| 메소드 | 동사로 시작, camelCase | `findById()`, `processOrder()` |
| 변수 | 명사, camelCase | `userId`, `orderCount` |
| 상수 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT`, `API_KEY` |
| Enum | PascalCase, 값은 UPPER_SNAKE_CASE | `enum Status { ACTIVE, INACTIVE }` |
| 패키지 | 소문자, 점으로 구분 | `com.pyokemon.user.service` |

#### 데이터베이스
| 유형 | 규칙 | 예시 |
|------|------|------|
| 테이블 | snake_case, 'tb_' 접두사 | `tb_user`, `tb_order` |
| 컬럼 | snake_case | `user_id`, `created_at` |
| 기본 키 | 'id' 또는 테이블명_id | `id`, `user_id` |
| 인덱스 | idx_테이블명_컬럼명 | `idx_user_email`, `idx_order_status` |
| 유니크 제약 | uk_테이블명_컬럼명 | `uk_user_email`, `uk_product_code` |

#### REST API
| 유형 | 규칙 | 예시 |
|------|------|------|
| URL 경로 | 복수형 명사, 소문자, 하이픈(-) 사용 | `/users`, `/order-items` |
| 쿼리 파라미터 | camelCase | `?userId=1&orderDate=2023-01-01` |
| 응답 필드 | camelCase | `{ "userId": 1, "orderCount": 5 }` |

#### 리소스 파일
| 유형 | 규칙 | 예시 |
|------|------|------|
| 이미지 | 소문자, 하이픈(-) | `user-profile.png`, `logo-small.jpg` |
| CSS/JS | 소문자, 하이픈(-) | `main-style.css`, `user-service.js` |
| 설정 파일 | 소문자, 하이픈(-) | `application-dev.yml`, `log4j2-prod.xml` |

### API 개발 시 주의사항
- 모든 API 응답은 `ResponseDto` 사용
- 비즈니스 로직 예외는 `BusinessException` 사용
- 엔티티는 `BaseEntity` 상속
- API 문서는 Swagger 어노테이션 사용

### 새로운 서비스 모듈 추가
1. `settings.gradle`에 모듈 추가
2. 모듈 디렉토리 생성
3. 모듈별 `build.gradle` 생성
4. `common` 모듈 dependency 추가 