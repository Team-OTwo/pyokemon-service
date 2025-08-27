DROP TABLE IF EXISTS tb_issued_vc;

CREATE TABLE tb_issued_vc
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    cred_ex_id      VARCHAR(255) NOT NULL UNIQUE,
    cred_id         VARCHAR(255),
    pres_ex_id      VARCHAR(255),
    verify_invi_url VARCHAR(255),
    booking_id      BIGINT       NOT NULL,
    user_id         BIGINT       NOT NULL,
    tenant_id       BIGINT       NOT NULL,
    status          VARCHAR(50)  NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX           idx_cred_ex_id (cred_ex_id),
    INDEX           idx_created_at (created_at),
    INDEX           idx_user_booking_status (user_id, booking_id, status)
);