IF OBJECT_ID('dbo.customers') IS NULL
CREATE TABLE customers
(
CustomerID int IDENTITY(1, 1) PRIMARY KEY,
FirstName VARCHAR(50),
LastName VARCHAR(75) NOT NULL,
Email VARCHAR(70) NOT NULL UNIQUE
);

IF OBJECT_ID('dbo.feedbacklog') IS NULL
CREATE TABLE feedbacklog (
FeedbackID int IDENTITY(1,1) PRIMARY KEY,
Date datetime2 DEFAULT GETDATE() NOT NULL,
FeedbackContent VARCHAR(1000) NOT NULL,
CustomerID int,
CONSTRAINT fk_customerid
FOREIGN KEY (CustomerID)
REFERENCES customers(CustomerID)
);