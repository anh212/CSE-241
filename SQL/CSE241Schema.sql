-- dropping all tables
drop table pay_credit;
drop table withdrawal;
drop table deposit;
drop table pay_loan;
drop table account_trans;
drop table credit_payment;
drop table loan_payment;
drop table transactions;
drop table has_loan;
drop table unsecured_loan;
drop table mortgage;
drop table loan;
drop table debit_purchase;
drop table credit_purchase;
drop table debit_card;
drop table owns_account;
drop table savings;
drop table checking;
drop table account;
drop table purchases;
drop table credit_card;
drop table atm;
drop table has_customers;
drop table teller;
drop table trans_method;
drop table branch;
drop table customer;

-- customer
create table customer (
    customer_id number(8) generated by default as identity,
    first_name varchar(20) not null,
    last_name varchar(20) not null,
    phone_number char(12) not null,
    date_of_birth varchar(10) not null,
    primary key (customer_id)
);

-- branch
create table branch (
    branch_id number(6) generated by default as identity,
    street_number varchar(5),
    street_name varchar(20),
    city varchar(20),
    state varchar(20),
    zip varchar(5),
    primary key (branch_id)
);

-- trans_method
create table trans_method (
    method_id number(6) generated by default as identity,
    branch_id number(6),
    primary key (method_id),
    foreign key (branch_id) references branch on delete cascade
);

-- teller
create table teller (
    method_id number(6),
    name varchar(50),
    primary key (method_id),
    foreign key (method_id) references trans_method(method_id) on delete cascade
);

-- atm
create table atm (
    method_id number(6),
    primary key (method_id),
    foreign key (method_id) references trans_method(method_id) on delete cascade
);

-- has_customers
create table has_customers (
    branch_id number(6),
    customer_id number(8),
    primary key (branch_id, customer_id),
    foreign key (branch_id) references branch on delete cascade,
    foreign key (customer_id) references customer on delete cascade
);

-- credit_card
create table credit_card (
    credit_id number(8) generated by default as identity,
    customer_id number(8),
    interest_rate number(3,2),
    credit_limit number(8,2),
    balance number(8,2),
    balance_due number(8,2),
    primary key (credit_id),
    check (balance <= credit_limit),
    check (balance_due <= credit_limit),
    check (interest_rate < 100)
);

-- purchases
create table purchases (
    purchase_id number(8) generated by default as identity,
    vendor_id number(6),
    vendor_name varchar(50),
    amount number(8,2),
    purchase_date varchar(10),
    primary key (purchase_id)
);

-- account
create table account (
    account_id number(8) generated by default as identity,
    interest_rate number(4,2),
    balance number(8,2),
    primary key (account_id),
    check (balance > 0),
    check (interest_rate < 100)
);

-- owns_account
create table owns_account (
    customer_id number(8),
    account_id number(8),
    primary key (customer_id, account_id),
    foreign key (customer_id) references customer on delete cascade,
    foreign key (account_id) references account on delete cascade
);

-- checking
create table checking (
    account_id number(8),
    primary key (account_id),
    foreign key (account_id) references account on delete cascade
);

-- savings
create table savings (
    account_id number(8),
    min_balance number(7,2),
    primary key (account_id),
    foreign key (account_id) references account on delete cascade
);

-- debit_card
create table debit_card (
    debit_id number(8),
    account_id number(8),
    primary key (debit_id),
    foreign key (account_id) references checking on delete cascade
);

-- credit_purchase
-- No need to do on delete cascade / set null
-- due to using credit purchases for bank management
create table credit_purchase (
    purchase_id number(8),
    credit_id number(8),
    primary key (purchase_id, credit_id),
    foreign key (purchase_id) references purchases on delete cascade,
    foreign key (credit_id) references credit_card on delete set null
);

-- debit_purchase
create table debit_purchase (
    purchase_id number(8),
    debit_id number(8),
    primary key (purchase_id),
    foreign key (purchase_id) references purchases on delete cascade,
    foreign key (debit_id) references debit_card on delete set null
);

-- loan
create table loan (
    loan_id number(8) generated by default as identity,
    amount number(7,2),
    monthly_payment number(7,2),
    interest_rate number(3,2),
    primary key (loan_id),
    check (interest_rate < 100)
);

-- mortgage
create table mortgage (
    loan_id number(8),
    street_number varchar(5),
    street_name varchar(20),
    city varchar(20),
    state varchar(20),
    zip varchar(20),
    primary key (loan_id),
    foreign key (loan_id) references loan on delete cascade
);

-- unsecured_loan
create table unsecured_loan (
    loan_id number(8),
    foreign key (loan_id) references loan on delete cascade
);

-- has_loan
create table has_loan (
    customer_id number(8),
    loan_id number(8),
    primary key (customer_id, loan_id),
    foreign key (customer_id) references customer on delete set null,
    foreign key (loan_id) references loan on delete cascade
);

-- transactions
create table transactions (
    trans_id number(8) generated by default as identity,
    amount number(7,2),
    month number(2,0),
    day number(2,0),
    year number(4,0),
    primary key (trans_id)
);

-- loan_payment
create table loan_payment (
    trans_id number(8),
    primary key (trans_id),
    foreign key (trans_id) references transactions on delete cascade
);

-- credit_payment
create table credit_payment (
    trans_id number(8),
    primary key (trans_id),
    foreign key (trans_id) references transactions on delete cascade
);

-- account_trans
create table account_trans (
    trans_id number(8),
    primary key (trans_id),
    foreign key (trans_id) references transactions on delete cascade
);

-- pay_loan
create table pay_loan (
    trans_id number(8),
    method_id number(6),
    loan_id number(8),
    primary key (trans_id, method_id, loan_id),
    foreign key (trans_id) references transactions on delete cascade,
    foreign key (method_id) references trans_method(method_id) on delete set null,
    foreign key (loan_id) references loan on delete set null
);

-- deposit
create table deposit (
    trans_id number(8),
    method_id number(6),
    account_id number(8),
    primary key (trans_id, method_id, account_id),
    foreign key (account_id) references account on delete set cascade,
    foreign key (trans_id) references transactions on delete cascade,
    foreign key (method_id) references trans_method(method_id) on delete set null
);

-- withdrawal
create table withdrawal (
    trans_id number(8),
    method_id number(6),
    account_id number(8),
    primary key (trans_id, method_id, account_id),
    foreign key (account_id) references account on delete set cascade,
    foreign key (trans_id) references transactions on delete cascade,
    foreign key (method_id) references trans_method(method_id) on delete set null
);

-- pay_credit
create table pay_credit (
    trans_id number(8),
    method_id number(6),
    credit_id number(8),
    primary key (trans_id, method_id, credit_id),
    foreign key (trans_id) references transactions on delete cascade,
    foreign key (method_id) references trans_method(method_id) on delete set null,
    foreign key (credit_id) references credit_card on delete set null
);

-- grant permissions to grader
grant select on account to grader;
grant select on account_trans to grader;
grant select on atm to grader;
grant select on branch to grader;
grant select on checking to grader;
grant select on credit_card to grader;
grant select on credit_payment to grader;
grant select on credit_purchase to grader;
grant select on customer to grader;
grant select on debit_card to grader;
grant select on debit_purchase to grader;
grant select on deposit to grader;
grant select on has_customers to grader;
grant select on has_loan to grader;
grant select on loan to grader;
grant select on loan_payment to grader;
grant select on mortgage to grader;
grant select on owns_account to grader;
grant select on pay_credit to grader;
grant select on pay_loan to grader;
grant select on purchases to grader;
grant select on savings to grader;
grant select on teller to grader;
grant select on trans_method to grader;
grant select on transactions to grader;
grant select on unsecured_loan to grader;
grant select on withdrawal to grader;

--Sync default values for identity sequence
ALTER TABLE account MODIFY account_id generated by default as identity (START WITH LIMIT VALUE);
ALTER TABLE branch MODIFY branch_id generated by default as identity (START WITH LIMIT VALUE);
ALTER TABLE credit_card MODIFY credit_id generated by default as identity (START WITH LIMIT VALUE);
ALTER TABLE customer MODIFY customer_id generated by default as identity (START WITH LIMIT VALUE);
ALTER TABLE loan MODIFY loan_id generated by default as identity (START WITH LIMIT VALUE);
ALTER TABLE purchases MODIFY purchase_id generated by default as identity (START WITH LIMIT VALUE);
ALTER TABLE transactions MODIFY trans_id generated by default as identity (START WITH LIMIT VALUE);
ALTER TABLE trans_method MODIFY method_id generated by default as identity (START WITH LIMIT VALUE);