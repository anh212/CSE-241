-- Updating credit_cards so that balance_due < credit_limit
update credit_card 
set balance_due = credit_limit
where balance_due > credit_limit

-- Updating credit_cards so that balances < credit_limit
update credit_card
set balance = credit_limit
where balance > credit_limit

-- Make sql delete statement that deletes branches that are not in has_customers
delete from branches where branch_id not in (select branch_id from has_customers)

-- Make sql delete statement for deleting customers that do not appear in owns_account
delete from customer where customer_id not in (select customer_id from owns_account)

-- Add constraint for credit_card so that balance and balance due are <= credit limit
ALTER TABLE credit_card
ADD CHECK (balance_due <= credit_limit)

ALTER TABLE credit_card
ADD CHECK (balance <= credit_limit);

-- Add constraint to balance for accounts
ALTER TABLE account
ADD CHECK (balance >= 0);
