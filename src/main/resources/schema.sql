IF OBJECT_ID('dbo.customers') IS NULL
CREATE TABLE customers
(
customerID int IDENTITY(1, 1) PRIMARY KEY,
firstName VARCHAR(50),
lastName VARCHAR(75) NOT NULL,
email VARCHAR(70) NOT NULL UNIQUE
);

IF OBJECT_ID('dbo.feedbacklog') IS NULL
CREATE TABLE feedbacklog (
feedbackID int IDENTITY(1,1) PRIMARY KEY,
submitted datetime2 DEFAULT GETDATE() NOT NULL,
feedbackContent VARCHAR(1000) NOT NULL,
customerID int,
CONSTRAINT fk_customerid
FOREIGN KEY (customerID)
REFERENCES customers(customerID)
);