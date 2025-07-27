# Eagle Bank API

## Introduction
This repo holds source code for an API of a fictional bank - Eagle Bank. It will allow a user to add their details, fetch, update, detail their details. A user can also create, fetch, update and delete their own bank
accounts and deposit or withdraw money from the account. 

## How to navigate the repo

### The development process
I started off by reviewing the requirements and making some initial design decisions. These can be found in `docs/DESIGN-DECISIONS.md`.

Given the dependent relationship where an account needs a user and a transaction needs an account, I decided to handle endpoint implementation in the order of the rubric:

## User Management
- [ ] Create a user
- [ ] Create a new user without supplying all required data
- [ ] Authenticate a user
- [ ] Fetch a user
- [ ] Fetch another user's details (should be forbidden)
- [ ] Fetch a non-existent user
- [ ] Update a user
- [ ] Update another user's details (should be forbidden)
- [ ] Update a non-existent user
- [ ] Delete a user without a bank account
- [ ] Delete a user with a bank account (should conflict)
- [ ] Delete another user's details (should be forbidden)
- [ ] Delete a non-existent user

## Bank Accounts
- [ ] Create a bank account
- [ ] Create a bank account without all required data
- [ ] List bank accounts for a user
- [ ] Fetch a bank account
- [ ] Fetch another user's bank account (should be forbidden)
- [ ] Fetch a non-existent bank account
- [ ] Update a bank account
- [ ] Update another user's bank account (should be forbidden)
- [ ] Update a non-existent bank account
- [ ] Delete a bank account
- [ ] Delete another user's bank account (should be forbidden)
- [ ] Delete a non-existent bank account

## Transactions
- [ ] Create a deposit transaction
- [ ] Create a withdrawal transaction with sufficient funds
- [ ] Create a withdrawal transaction with insufficient funds (should fail)
- [ ] Create a transaction on another user's account (should be forbidden)
- [ ] Create a transaction on a non-existent account
- [ ] Create a transaction without all required data
- [ ] List transactions on a bank account
- [ ] List transactions on another user's bank account (should be forbidden)
- [ ] List transactions on a non-existent bank account
- [ ] Fetch a transaction on own bank account
- [ ] Fetch a transaction on another user's bank account (should be forbidden)
- [ ] Fetch a transaction on a non-existent bank account
- [ ] Fetch a non-existent transaction on own account
- [ ] Fetch a transaction linked to a different bank account (should fail)

### Code structure

The code in this repo is arranged by feature (e.g. authentication) rather than layer (e.g. controller).

## How to run the project