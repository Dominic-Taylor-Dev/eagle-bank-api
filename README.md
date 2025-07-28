# Eagle Bank API

## Introduction
This repo holds source code for an API of a fictional bank - Eagle Bank. It will allow a user to add their details, fetch, update, detail their details. A user can also create, fetch, update and delete their own bank
accounts and deposit or withdraw money from the account. 

## How to navigate the repo

### The development process
I started off by reviewing the requirements and making some initial design decisions. These can be found in `docs/DESIGN-DECISIONS.md`.

Given the dependent relationship where an account needs a user and a transaction needs an account, I decided to handle endpoint implementation in the order of the rubric. I implemented a couple of example endpoints which include demonstrating my JWT auth setup. Although this is only a fraction of all of the endpoints, I would have followed similar patterns for the remainder. The points of interest, such as the need for transactions when it comes to some of the financial operations are discussed at a high level in the design decisions document.

## User Management
- [x] Create a user
- [x] Create a new user without supplying all required data
- [x] Authenticate a user
- [x] Fetch a user
- [x] Fetch another user's details (should be forbidden)
- [x] Fetch a non-existent user
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

### Pre-requisites
- Java JDK 21
- Docker (for test containers to spin up Postgres in integration tests)

### Running tests

There are multiple ways to run the tests:
- If using IntelliJ: right click `src/test/java` and select "Run 'Tests in 'java'''"
- Via command line: Use the appropriate Maven wrapper for your OS. `./mvnw test` for Linux/macOS or `.\mvnw test` on Windows (JAVA_HOME variable needs to be set and should point at a Java 21 distribution)

### Non-test local running

- The application is backed by Postgres. There is a Docker compose file which can set up a local database server.
- This is started by running `docker compose up` from the repository root
- Once the database is up and running you can start the main Spring application either:
  - If using IntelliJ: with the 'play' button on `src/main/java/com/eaglebank/EaglebankApiApplication.java`
  - Via command line: Use the appropriate Maven wrapper for your OS. `./mvnw spring-boot:run` for Linux/macOS or `.\mvnw spring-boot:run` on Windows (JAVA_HOME variable needs to be set and should point at a Java 21 distribution)
- From here you can perform manual testing with any client.
- To facilitate this, there are example requests in `docs/manual-testing.http` which can be used with the IntelliJ or VS Code integrated REST clients.