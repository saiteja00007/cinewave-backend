CREATE TABLE IF NOT EXISTS payments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  payment_reference VARCHAR(50) NOT NULL UNIQUE,
  transaction_id VARCHAR(100) NULL,
  booking_id BIGINT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  payment_method VARCHAR(50) NOT NULL DEFAULT 'UPI',
  status VARCHAR(30) NOT NULL DEFAULT 'SUCCESSFUL',
  gateway VARCHAR(50) NOT NULL DEFAULT 'SANDBOX',
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NULL,
  INDEX idx_payment_booking (booking_id),
  INDEX idx_payment_status (status),
  CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS otp_verifications (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  destination VARCHAR(150) NOT NULL,
  otp_hash VARCHAR(255) NOT NULL,
  type VARCHAR(20) NOT NULL DEFAULT 'EMAIL',
  attempts INT NOT NULL DEFAULT 0,
  verified BIT(1) NOT NULL DEFAULT 0,
  expires_at DATETIME(6) NOT NULL,
  resend_after DATETIME(6) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  INDEX idx_otp_destination (destination)
);
