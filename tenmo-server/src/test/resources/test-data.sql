TRUNCATE users, accounts, transfers CASCADE;

INSERT INTO users (user_id, username, password_hash)
VALUES (1001, 'test 1', 'password 1'),
       (1002, 'test 2', 'password 2'),
       (1003, 'test 3', 'password 3');

INSERT INTO accounts (account_id, user_id, balance)
VALUES (2001, 1001, 970),
       (2002, 1002, 1040),
       (2003, 1003, 990);

INSERT INTO transfers (transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount)
VALUES (3001, 2, 2, 2001, 2002, 50),
       (3002, 2, 2, 2002, 2003, 10),
       (3003, 2, 2, 2003, 2001, 20);