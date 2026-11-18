/*
* Junk data for trying the owner panel out, run by hand, nothing here runs on
* startup while spring.sql.init.mode stays never. Safe to run twice, a row that
* is already there is skipped instead of inserted again.
* */

INSERT INTO dbo.customers (firstName, lastName, email)
SELECT s.firstName, s.lastName, s.email
FROM (VALUES
    ('Marcus',   'Webb',        'marcus.webb@example.com'),
    ('Priya',    'Raghunathan', 'praghunathan@example.com'),
    ('Dee',      'Okonkwo',     'dee.okonkwo@example.com'),
    ('Tomasz',   'Wisniewski',  'twisniewski@example.com'),
    ('Hannah',   'Boyd',        'hboyd@example.com'),
    ('Luis',     'Ferreira',    'lferreira@example.com'),
    ('Junk',     'Tester',      'junk.tester@example.com'),
    ('',         'Nameless',    'nameless@example.com')
) AS s(firstName, lastName, email)
WHERE NOT EXISTS (SELECT 1 FROM dbo.customers c WHERE c.email = s.email);

INSERT INTO dbo.feedbacklog (feedbackContent, submitted, customerID)
SELECT s.feedbackContent, s.submitted, c.customerID
FROM (VALUES
    ('The breakfast bar had solid bagels, genuinely the best part of the stay.',
        '2026-10-27T08:14:00', 'marcus.webb@example.com'),
    ('Front desk got me checked in early without me even asking. Small thing, made the whole trip.',
        '2026-10-26T15:42:00', 'marcus.webb@example.com'),
    ('Room was clean but the air conditioning rattled all night and I barely slept.',
        '2026-10-25T22:05:00', 'praghunathan@example.com'),
    ('Showers were cold on the third floor. Called the desk twice, nobody picked up, and by the time someone came up it was almost midnight. I understand things break, but leaving the phone unanswered for two hours on a weekday is what turned a minor annoyance into the reason I probably will not book here again. The staff member who eventually showed up was apologetic and clearly doing his best with no backup, so this is not on him.',
        '2026-10-24T23:18:00', 'praghunathan@example.com'),
    ('Parking garage gate was stuck open the entire weekend. Felt uneasy leaving the car there.',
        '2026-10-23T19:30:00', 'dee.okonkwo@example.com'),
    ('Great location, walked everywhere.',
        '2026-10-22T11:00:00', 'dee.okonkwo@example.com'),
    ('Wifi kept dropping during a work call.',
        '2026-10-21T14:22:00', 'dee.okonkwo@example.com'),
    ('Checked in late, room was ready, no complaints at all.',
        '2026-10-20T20:47:00', 'twisniewski@example.com'),
    ('Pool was closed for maintenance and that was not mentioned anywhere when I booked. Would have picked a different weekend.',
        '2026-10-18T09:55:00', 'hboyd@example.com'),
    ('Housekeeping skipped my room two days running.',
        '2026-10-17T16:10:00', 'hboyd@example.com'),
    ('Bed was comfortable, pillows were not. Bring your own.',
        '2026-10-15T07:33:00', 'lferreira@example.com'),
    ('Elevator out of service, carried luggage up four flights.',
        '2026-10-12T18:20:00', 'lferreira@example.com'),
    ('Staff remembered my name on day three. Nice touch.',
        '2026-10-09T12:05:00', 'marcus.webb@example.com'),
    ('Lobby coffee ran out every morning before eight.',
        '2026-10-05T07:50:00', 'junk.tester@example.com'),
    ('Short one.',
        '2026-09-30T13:00:00', 'junk.tester@example.com'),
    ('Testing a submission with no first name on the account.',
        '2026-09-28T10:15:00', 'nameless@example.com')
) AS s(feedbackContent, submitted, email)
JOIN dbo.customers c ON c.email = s.email
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.feedbacklog f
    WHERE f.customerID = c.customerID AND f.feedbackContent = s.feedbackContent);
