-- Mini Patient and Appointment System — database schema
-- To apply it:
-- docker exec -i openemr-mysql mysql -uroot -proot < db_schema.sql

CREATE DATABASE IF NOT EXISTS openemr
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_turkish_ci;

USE openemr;

-- Polyclinics
CREATE TABLE IF NOT EXISTS app_polyclinics (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100),
  description VARCHAR(255),
  PRIMARY KEY (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_turkish_ci;

-- Doctors
CREATE TABLE IF NOT EXISTS app_doctors (
  id BIGINT NOT NULL AUTO_INCREMENT,
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  title VARCHAR(50),
  polyclinic_id BIGINT,
  PRIMARY KEY (id),
  CONSTRAINT fk_app_doctors_polyclinic
    FOREIGN KEY (polyclinic_id)
    REFERENCES app_polyclinics(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_turkish_ci;

-- Patients
CREATE TABLE IF NOT EXISTS app_patients (
  id BIGINT NOT NULL AUTO_INCREMENT,
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  date_of_birth DATE,
  phone_number VARCHAR(50),
  address VARCHAR(255),
  PRIMARY KEY (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_turkish_ci;

-- Appointments
CREATE TABLE IF NOT EXISTS app_appointments (
  id BIGINT NOT NULL AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT,
  appointment_date DATETIME,
  reason VARCHAR(255),
  PRIMARY KEY (id),
  CONSTRAINT fk_app_appointments_patient
    FOREIGN KEY (patient_id)
    REFERENCES app_patients(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT fk_app_appointments_doctor
    FOREIGN KEY (doctor_id)
    REFERENCES app_doctors(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_turkish_ci;
