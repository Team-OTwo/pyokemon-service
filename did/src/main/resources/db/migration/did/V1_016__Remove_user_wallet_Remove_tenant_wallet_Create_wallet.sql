DROP TABLE IF EXISTS tb_user_wallet;
DROP TABLE IF EXISTS tb_tenant_wallet;

CREATE TABLE tb_wallet (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL UNIQUE,
  account_role VARCHAR(500) NOT NULL,
  token VARCHAR(500) NOT NULL,
  public_did VARCHAR(255),
  public_verkey VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  INDEX idx_account_id (account_id),
  INDEX idx_token (token),
  INDEX idx_public_did (public_did),
  INDEX idx_public_verkey (public_verkey),
  INDEX idx_created_at (created_at)
);