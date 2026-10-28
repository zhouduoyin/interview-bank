CREATE DATABASE IF NOT EXISTS interview_bank
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'interview'@'localhost' IDENTIFIED BY 'interview_pass';
CREATE USER IF NOT EXISTS 'interview'@'%' IDENTIFIED BY 'interview_pass';

GRANT ALL PRIVILEGES ON interview_bank.* TO 'interview'@'localhost';
GRANT ALL PRIVILEGES ON interview_bank.* TO 'interview'@'%';

FLUSH PRIVILEGES;
