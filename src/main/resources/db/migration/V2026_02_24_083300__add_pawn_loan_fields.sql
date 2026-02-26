-- Migration: Add new fields to pawn_loan table for standard pawn loan features
-- Date: 2026-02-24
-- Description: Adds fields for loan duration, payment frequency, installment tracking, and redemption deadlines

-- Add new columns to pawn_loan table
ALTER TABLE pawn_loan ADD COLUMN IF NOT EXISTS redemption_deadline DATE;
ALTER TABLE pawn_loan ADD COLUMN IF NOT EXISTS loan_duration_days INTEGER DEFAULT 30;
ALTER TABLE pawn_loan ADD COLUMN IF NOT EXISTS grace_period_days INTEGER DEFAULT 7;
ALTER TABLE pawn_loan ADD COLUMN IF NOT EXISTS storage_fee DECIMAL(19,2) DEFAULT 0;
ALTER TABLE pawn_loan ADD COLUMN IF NOT EXISTS penalty_rate DECIMAL(19,2) DEFAULT 0;
ALTER TABLE pawn_loan ADD COLUMN IF NOT EXISTS payment_frequency VARCHAR(50) DEFAULT 'ONE_TIME';
ALTER TABLE pawn_loan ADD COLUMN IF NOT EXISTS number_of_installments INTEGER DEFAULT 1;
ALTER TABLE pawn_loan ADD COLUMN IF NOT EXISTS installment_amount DECIMAL(19,2);

-- Update existing records with default values
UPDATE pawn_loan SET 
    loan_duration_days = 30,
    grace_period_days = 7,
    storage_fee = 0,
    penalty_rate = 0,
    payment_frequency = 'ONE_TIME',
    number_of_installments = 1
WHERE loan_duration_days IS NULL;

-- Calculate redemption deadline for existing loans with due dates
UPDATE pawn_loan 
SET redemption_deadline = due_date + INTERVAL '7 days'
WHERE due_date IS NOT NULL AND redemption_deadline IS NULL;

-- Create index on payment_frequency for better query performance
CREATE INDEX IF NOT EXISTS idx_pawn_loan_payment_frequency ON pawn_loan(payment_frequency);

-- Create index on loan_duration_days for better query performance
CREATE INDEX IF NOT EXISTS idx_pawn_loan_duration ON pawn_loan(loan_duration_days);

-- Comment explaining the new fields
COMMENT ON COLUMN pawn_loan.redemption_deadline IS 'Date when user can collect their pawned item (due date + grace period)';
COMMENT ON COLUMN pawn_loan.loan_duration_days IS 'Duration of loan in days (default: 30)';
COMMENT ON COLUMN pawn_loan.grace_period_days IS 'Grace period after due date for redemption (default: 7)';
COMMENT ON COLUMN pawn_loan.storage_fee IS 'Fee for storing the pawned item';
COMMENT ON COLUMN pawn_loan.penalty_rate IS 'Penalty rate for late payments';
COMMENT ON COLUMN pawn_loan.payment_frequency IS 'Payment schedule: ONE_TIME, WEEKLY, BI_WEEKLY, MONTHLY, QUARTERLY, CUSTOM';
COMMENT ON COLUMN pawn_loan.number_of_installments IS 'Number of installments for installment payments';
COMMENT ON COLUMN pawn_loan.installment_amount IS 'Amount per installment (calculated if not provided)';