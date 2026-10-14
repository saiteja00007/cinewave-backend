-- Role enum to varchar to support THEATER_OWNER
ALTER TABLE users MODIFY COLUMN role VARCHAR(30) NOT NULL DEFAULT 'USER';

-- Safe procedure to add column if missing
DROP PROCEDURE IF EXISTS AddCol;
DELIMITER //
CREATE PROCEDURE AddCol(IN tableName VARCHAR(64), IN colName VARCHAR(64), IN colDef VARCHAR(255))
BEGIN
  IF NOT EXISTS (
    SELECT * FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tableName AND COLUMN_NAME = colName
  ) THEN
    SET @sql = CONCAT('ALTER TABLE ', tableName, ' ADD COLUMN ', colName, ' ', colDef);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END //
DELIMITER ;

CALL AddCol('users', 'phone', 'VARCHAR(20) NULL');
CALL AddCol('theaters', 'state', 'VARCHAR(100) DEFAULT \'Andhra Pradesh\'');
CALL AddCol('theaters', 'district', 'VARCHAR(100) DEFAULT \'\'');
CALL AddCol('theaters', 'owner_id', 'BIGINT NULL');

-- Foreign key for theater owner
DROP PROCEDURE IF EXISTS AddFk;
DELIMITER //
CREATE PROCEDURE AddFk()
BEGIN
  IF NOT EXISTS (
    SELECT * FROM information_schema.TABLE_CONSTRAINTS 
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'theaters' AND CONSTRAINT_NAME = 'fk_theater_owner'
  ) THEN
    ALTER TABLE theaters ADD CONSTRAINT fk_theater_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL;
  END IF;
END //
DELIMITER ;
CALL AddFk();

-- Widen movie columns to prevent truncation
ALTER TABLE movies MODIFY COLUMN poster_url MEDIUMTEXT;
ALTER TABLE movies MODIFY COLUMN backdrop_url TEXT;
ALTER TABLE movies MODIFY COLUMN cast_info TEXT;
ALTER TABLE movies MODIFY COLUMN genre VARCHAR(255) NOT NULL;
ALTER TABLE movies MODIFY COLUMN language VARCHAR(255) NOT NULL;
ALTER TABLE movies MODIFY COLUMN rating VARCHAR(50);

-- Booking payment and status fields
ALTER TABLE bookings MODIFY COLUMN status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED';
CALL AddCol('bookings', 'payment_method', 'VARCHAR(50) DEFAULT \'UPI\'');
CALL AddCol('bookings', 'payment_status', 'VARCHAR(50) DEFAULT \'SUCCESSFUL\'');
CALL AddCol('bookings', 'payment_reference', 'VARCHAR(50) NULL');

-- Clean up helper procedures
DROP PROCEDURE IF EXISTS AddCol;
DROP PROCEDURE IF EXISTS AddFk;
