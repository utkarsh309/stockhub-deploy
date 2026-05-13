-- This file runs automatically when MySQL container starts for the first time
-- Creates all databases needed by each microservice

CREATE DATABASE IF NOT EXISTS stockhub_auth;
CREATE DATABASE IF NOT EXISTS stockhub_product;
CREATE DATABASE IF NOT EXISTS stockhub_warehouse;
CREATE DATABASE IF NOT EXISTS stockhub_purchase;
CREATE DATABASE IF NOT EXISTS stockhub_supplier;
CREATE DATABASE IF NOT EXISTS stockhub_movement;
CREATE DATABASE IF NOT EXISTS stockhub_alert;
CREATE DATABASE IF NOT EXISTS stockhub_report;