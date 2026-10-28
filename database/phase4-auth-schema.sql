-- Back up daily_expense_tracker before applying this migration.
-- Run only after reviewing the schema and confirming the expenses engine.

CREATE TABLE users (
    user_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- Existing expense rows remain unassigned (user_id = NULL).
-- Per-user application queries will not show unassigned rows.
ALTER TABLE expenses
    ENGINE=InnoDB,
    ADD COLUMN user_id BIGINT UNSIGNED NULL,
    ADD INDEX idx_expenses_user_id (user_id),
    ADD CONSTRAINT fk_expenses_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE SET NULL;
