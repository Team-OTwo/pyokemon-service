DROP TABLE IF EXISTS tb_issued_vc;

CREATE TABLE tb_issued_vc
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    cred_ex_id      VARCHAR(255) UNIQUE,
    pres_ex_id      VARCHAR(255) NOT NULL,
    verify_invi_url TEXT         NOT NULL,
    booking_id      BIGINT       NOT NULL,
    user_id         BIGINT       NOT NULL,
    tenant_id       BIGINT       NOT NULL,
    status          VARCHAR(50)  NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX           idx_pres_ex_id (pres_ex_id),
    INDEX           idx_booking (booking_id),
    INDEX           idx_created_at (created_at)
);