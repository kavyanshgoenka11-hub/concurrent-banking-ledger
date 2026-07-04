# High-Concurrency Transaction Ledger Engine

A high-performance, concurrent banking transaction backend built in pure Java and MySQL. This system safely processes simultaneous cross-account fund transfers under immense multi-threaded loads while strictly guaranteeing data consistency, transaction isolation, and absolute financial accountability.

---

## 🛠️ Tech Stack & Architecture
* **Language:** Java (Multithreading, Concurrency Utilities)
* **Database:** MySQL 8.x+ (Relational Schema, ACID Compliant Engines)
* **Connectivity:** JDBC (Java Database Connectivity)
* **Build/Execution Environment:** Raw Terminal Compilation via `javac` & `java` CLIs

---

## ⚡ Core Technical Engineering Challenges Handled

### 1. Concurrency Control & Race Conditions
To prevent "double-spending" vulnerabilities—where concurrent threads attempt to debit the same account balance simultaneously—the engine explicitly disables auto-commit and implements **MySQL Pessimistic Database Locking (`SELECT ... FOR UPDATE`)**. This constructs an isolation barrier around active records, forcing overlapping threads to safely queue rather than modifying dirty state data.

### 2. Deadlock Prevention Hierarchy
In a high-frequency financial environment, simultaneous bidirectional transfers (e.g., User A transferring to User B while User B transfers to User A) can cause a relational database deadlock, freezing execution indefinitely. This system guarantees deadlock immunity by implementing a **deterministic locking hierarchy** via account ID numeric sorting (`Math.min()` and `Math.max()`), forcing a strict global lock acquisition sequence.

### 3. Financial Auditing Precision
Eliminated dangerous floating-point tracking vulnerabilities (inherent to Java's `float`/`double` binary fraction roundings) by strictly enforcing precise database `DECIMAL(15,2)` allocation types. This ensures complete arithmetic integrity down to the exact penny across all ledger audits.

### 4. Relational Transaction Boundaries (ACID Atomicity)
By managing transaction boundaries programmatically (`conn.setAutoCommit(false)`), a multi-step operation—comprising a sender debit, a receiver credit, and an append-only audit trail write—behaves as a single **atomic unit**. If a system failure or network interruption occurs mid-transit, a robust `conn.rollback()` path catches the exception and resets the infrastructure state cleanly.

---

## 📊 System Execution & Verification

### Thread Execution Output (Console)
Below is the execution window demonstrating the multi-threaded thread pool (`ExecutorService`) successfully serializing overlapping race conditions and gracefully rejecting invalid transactions once liquid capital is exhausted:
(images/output.png)

### MySQL Ledger State Post-Execution
Database audit snapshot proving transaction log compliance, exact relational tracking, and balance accuracy following heavy simulated traffic:
(images/After Execution.png)

---

## 🚀 How to Set Up and Run Locally

### Prerequisites
* MySQL Server installed and running locally.
* Java Development Kit (JDK 11 or higher) installed.
* MySQL Connector/J `.jar` driver file downloaded.

### 1. Initialize the Database Schema
Execute the following statements inside your MySQL instance to construct the database schema and plant mock data:
```sql
CREATE DATABASE swift_bank;
USE swift_bank;

CREATE TABLE accounts (
    account_id INT PRIMARY KEY,
    owner_name VARCHAR(50),
    balance DECIMAL(15, 2) NOT NULL CHECK (balance >= 0)
);

CREATE TABLE transfer_log (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    from_account INT,
    to_account INT,
    amount DECIMAL(15,2),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO accounts VALUES (101, 'Alice', 1000.00), (102, 'Bob', 500.00);

### 2. Configure Credentials
private static final String URL = "jdbc:mysql://localhost:3306/swift_bank";
private static final String USER = "YOUR_MYSQL_USERNAME";
private static final String PASS = "YOUR_MYSQL_PASSWORD";

### 3. Compile and Run via Terminal
Place your downloaded MySQL connector .jar file directly into the same workspace directory as your Java file and execute:
Compile:
javac -cp ".;mysql-connector-j-9.7.0.jar" BankSimulation.java
Run:
java -cp ".;mysql-connector-j-9.7.0.jar" BankSimulation
