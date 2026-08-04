-- UPDATED VERSION
-- Requires:
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- NOTE:
-- This script now generates random tenant UUIDs.
-- Because tenant IDs are referenced throughout the seed data,
-- capture the generated UUIDs into a mapping table (or use fixed UUIDs)
-- before inserting dependent records. Automatic replacement of all
-- foreign-key references with gen_random_uuid() is not possible in a
-- plain VALUES script because later INSERTs must know the generated IDs.

-- ============================================================
-- DataShield India — Demo Seed Data
-- Run order: schemas → tenants → users → service data
-- Password hash = BCrypt of "Demo@1234"
-- ============================================================

-- ============================================================
-- 0. SCHEMAS (create if not exists)
-- ============================================================
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS tenant;
CREATE SCHEMA IF NOT EXISTS consent;
CREATE SCHEMA IF NOT EXISTS rights;
CREATE SCHEMA IF NOT EXISTS breach;
CREATE SCHEMA IF NOT EXISTS notification;
CREATE SCHEMA IF NOT EXISTS audit;
CREATE SCHEMA IF NOT EXISTS grievance;
CREATE SCHEMA IF NOT EXISTS vendor;
CREATE SCHEMA IF NOT EXISTS policy;
CREATE SCHEMA IF NOT EXISTS retention;
CREATE SCHEMA IF NOT EXISTS analytics;
CREATE SCHEMA IF NOT EXISTS reports;
