/*
* Empties both tables but leaves them standing, for a clean run between tests.
* Use reset.sql instead when the tables themselves need to go.
* feedbacklog goes first, it holds the foreign key into customers.
* */

DELETE FROM dbo.feedbacklog;
DELETE FROM dbo.customers;

DBCC CHECKIDENT ('dbo.feedbacklog', RESEED, 0);
DBCC CHECKIDENT ('dbo.customers', RESEED, 0);
