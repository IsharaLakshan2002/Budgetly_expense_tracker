-- 1. Users Table
CREATE TABLE users (
    id VARCHAR2(36) PRIMARY KEY, -- To store UUID from client
    name VARCHAR2(255) NOT NULL,
    email VARCHAR2(255) NOT NULL UNIQUE,
    password VARCHAR2(255) NOT NULL, -- In a real app, this should be a hash
    sync_status NUMBER(1,0) DEFAULT 0 NOT NULL,
    is_deleted NUMBER(1,0) DEFAULT 0 NOT NULL,
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    last_modified TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,

    -- Advanced Constraints
    CONSTRAINT chk_user_email CHECK (email LIKE '%_@_%._%')
);

-- Trigger to update last_modified timestamp on user update
CREATE OR REPLACE TRIGGER trg_users_last_modified
BEFORE UPDATE ON users
FOR EACH ROW
BEGIN
    :new.last_modified := SYSTIMESTAMP;
END;
/


-- 2. Expenses Table
CREATE TABLE expenses (
    id VARCHAR2(36) PRIMARY KEY, -- To store UUID from client
    user_id VARCHAR2(36) NOT NULL,
    expense_date DATE NOT NULL,
    category VARCHAR2(100) NOT NULL,
    amount NUMBER(18, 2) NOT NULL,
    notes VARCHAR2(1000),
    sync_status NUMBER(1,0) DEFAULT 0 NOT NULL,
    is_deleted NUMBER(1,0) DEFAULT 0 NOT NULL,
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    last_modified TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,

    -- Advanced Constraints
    CONSTRAINT chk_expense_amount CHECK (amount > 0),

    -- Foreign Key
    CONSTRAINT fk_expense_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- Trigger to update last_modified timestamp on expense update
CREATE OR REPLACE TRIGGER trg_expenses_last_modified
BEFORE UPDATE ON expenses
FOR EACH ROW
BEGIN
    :new.last_modified := SYSTIMESTAMP;
END;
/


-- 3. Budgets Table
CREATE TABLE budgets (
    id VARCHAR2(36) PRIMARY KEY, -- To store UUID from client
    user_id VARCHAR2(36) NOT NULL,
    month VARCHAR2(7) NOT NULL, -- For 'YYYY-MM' format
    limit_amount NUMBER(18, 2) NOT NULL,
    sync_status NUMBER(1,0) DEFAULT 0 NOT NULL,
    is_deleted NUMBER(1,0) DEFAULT 0 NOT NULL,
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    last_modified TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,

    -- Advanced Constraints
    CONSTRAINT chk_budget_limit CHECK (limit_amount >= 0),

    -- Unique Constraint
    CONSTRAINT uq_user_month UNIQUE (user_id, month),

    -- Foreign Key
    CONSTRAINT fk_budget_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- Trigger to update last_modified timestamp on budget update
CREATE OR REPLACE TRIGGER trg_budgets_last_modified
BEFORE UPDATE ON budgets
FOR EACH ROW
BEGIN
    :new.last_modified := SYSTIMESTAMP;
END;
/


-- 4. Savings Table
CREATE TABLE savings (
    id VARCHAR2(36) PRIMARY KEY, -- To store UUID from client
    user_id VARCHAR2(36) NOT NULL,
    goal_name VARCHAR2(255) NOT NULL,
    target_amount NUMBER(18, 2) NOT NULL,
    current_amount NUMBER(18, 2) NOT NULL,
    sync_status NUMBER(1,0) DEFAULT 0 NOT NULL,
    is_deleted NUMBER(1,0) DEFAULT 0 NOT NULL,
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    last_modified TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,

    -- Advanced Constraints
    CONSTRAINT chk_saving_target CHECK (target_amount > 0),
    CONSTRAINT chk_saving_current CHECK (current_amount >= 0 AND current_amount <= target_amount),

    -- Unique Constraint
    CONSTRAINT uq_user_goal UNIQUE (user_id, goal_name),

    -- Foreign Key
    CONSTRAINT fk_saving_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- Trigger to update last_modified timestamp on saving update
CREATE OR REPLACE TRIGGER trg_savings_last_modified
BEFORE UPDATE ON savings
FOR EACH ROW
BEGIN
    :new.last_modified := SYSTIMESTAMP;
END;
/