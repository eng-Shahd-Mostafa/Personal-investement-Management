-- Create database if not exists
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'IslamicFinanceDB')
BEGIN
    CREATE DATABASE IslamicFinanceDB;
    PRINT 'Database created successfully';
END
GO

USE IslamicFinanceDB;
GO

-- Users table
CREATE TABLE Users (
    user_id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(100) NOT NULL,
    email NVARCHAR(100) UNIQUE NOT NULL,
    password NVARCHAR(100) NOT NULL,
    created_at DATETIME DEFAULT GETDATE()
);

-- Assets table
CREATE TABLE Assets (
    asset_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT FOREIGN KEY REFERENCES Users(user_id),
    asset_type NVARCHAR(50) NOT NULL,
    name NVARCHAR(100) NOT NULL,
    quantity DECIMAL(18,2) NOT NULL,
    purchase_price DECIMAL(18,2) NOT NULL,
    purchase_date DATE NOT NULL,
    is_halal BIT DEFAULT 1
);

EXEC sp_rename 'Assets.asset_id', 'id', 'COLUMN';
EXEC sp_rename 'Assets.asset_type', 'type', 'COLUMN';

ALTER TABLE Assets
ALTER COLUMN quantity INT NOT NULL;

ALTER TABLE Users
ALTER COLUMN password NVARCHAR(256) NOT NULL;

INSERT INTO Assets (user_id, type, name, quantity, purchase_price, purchase_date, is_halal)
VALUES 
    (1, 'Stock', 'Apple Shares', 50, 150.75, '2023-02-20', 0),  
    (1, 'Real Estate', 'Apartment', 1, 200000.00, '2023-03-10', 1);

-- Sample data
INSERT INTO Users (name, email, password) 
VALUES ('Shosho', 'Shosho@lolo.com', '1234');

INSERT INTO Assets (user_id, asset_type, name, quantity, purchase_price, purchase_date)
VALUES (1, 'Gold', 'Gold Bars', 100.50, 50000.00, '2023-01-15');

select * from Users;

delete from Assets
where id=1 or id=2 or id=3;

UPDATE Users
SET password = 'Pass@1234' , email = 'Beroo@gmail.com'
WHERE user_id = 1;

INSERT INTO Assets (user_id, type, name, quantity, purchase_price, purchase_date)
VALUES (1, 'Gold', 'Gold Bars', 100.50, 50000.00, '2023-01-15'),
(1, 'Stocks', 'Apple Inc.', 50, 150.00, '2023-02-10'),
(1, 'Crypto', 'Bitcoin', 0.75, 28000.00, '2023-03-05'),
(1, 'RealEstate', 'Rental Apartment', 1, 750000.00, '2022-11-01');

select*from Assets;

-- Banks table
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Banks' AND xtype='U')
CREATE TABLE Banks (
    bank_id INT IDENTITY(1,1) PRIMARY KEY,
    bank_name NVARCHAR(100) NOT NULL UNIQUE,
    api_endpoint NVARCHAR(255),
    is_active BIT DEFAULT 1
);

-- UserBankAccounts table
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='UserBankAccounts' AND xtype='U')
CREATE TABLE UserBankAccounts (
    account_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    bank_id INT NOT NULL,
    account_number NVARCHAR(50) NOT NULL,
    account_holder_name NVARCHAR(100) NOT NULL,
    last_balance DECIMAL(15,2) DEFAULT 0.00,
    last_sync_date DATETIME NULL,
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_User FOREIGN KEY (user_id) REFERENCES Users(user_id),
    CONSTRAINT FK_Bank FOREIGN KEY (bank_id) REFERENCES Banks(bank_id),
    CONSTRAINT UQ_UserBank UNIQUE (user_id, bank_id, account_number)
);

-- Liabilities table
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Liabilities' AND xtype='U')
CREATE TABLE Liabilities (
    liability_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    liability_name NVARCHAR(100) NOT NULL,
    liability_type NVARCHAR(50) NOT NULL, -- بدل ENUM
    amount DECIMAL(15,2) NOT NULL,
    due_date DATE,
    is_current BIT DEFAULT 1,
    notes NVARCHAR(MAX),
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_LiabilityUser FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- ZakatCalculations table
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='ZakatCalculations' AND xtype='U')
CREATE TABLE ZakatCalculations (
    calculation_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    calculation_date DATETIME DEFAULT GETDATE(),
    cash_value DECIMAL(15,2) NOT NULL,
    gold_value DECIMAL(15,2) NOT NULL,
    silver_value DECIMAL(15,2) NOT NULL,
    investments_value DECIMAL(15,2) NOT NULL,
    debts_value DECIMAL(15,2) NOT NULL,
    total_wealth DECIMAL(15,2) NOT NULL,
    net_wealth DECIMAL(15,2) NOT NULL,
    zakat_due DECIMAL(15,2) NOT NULL,
    nisab_threshold DECIMAL(15,2) DEFAULT 5000.00,
    notes NVARCHAR(MAX),
    CONSTRAINT FK_ZakatUser FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- Insert initial data into Banks
INSERT INTO Banks (bank_name) VALUES 
('National Bank of Egypt'),
('Banque Misr'),
('Commercial International Bank'),
('QNB Al Ahli');

USE IslamicFinanceDB;
GO

-- إنشاء جدول Transactions إذا لم يكن موجودًا
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Transactions' AND xtype='U')
CREATE TABLE Transactions (
    transaction_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    account_id INT NOT NULL,
    transaction_date DATE NOT NULL,
    description NVARCHAR(255) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_TransactionUser FOREIGN KEY (user_id) REFERENCES Users(user_id),
    CONSTRAINT FK_TransactionAccount FOREIGN KEY (account_id) REFERENCES UserBankAccounts(account_id)
);

-- إضافة بيانات للتجربة
INSERT INTO Transactions (user_id, account_id, transaction_date, description, amount)
VALUES 
    (1, 1, '2025-05-01', 'Salary Deposit', 2000.00),
    (1, 1, '2025-05-03', 'Grocery Payment', -150.00),
    (1, 1, '2025-05-05', 'Utility Bill', -80.00);
