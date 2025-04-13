# Payroll-Management-System
This is a simple Employee-Manager Payroll System written in java with applying leaves feature built-in.

## Prerequisites

Install [IntelliJ IDEA](https://www.jetbrains.com/idea/) and Java in our system.

## Step-1 : Clone this repository
Clone this repository using github desktop or git terminal.

## Step-2 : Downloading DB Browser
1) Download [DB Browser](https://sqlitebrowser.org/dl/) for SQLite.
2) Open DB Browser and in top left press "New Database"
![image](https://github.com/user-attachments/assets/e03a5ef5-580d-428f-ad0c-cf532539d8be)<br />

3) Now goto your repository cloned folder, inside the root directory and outside the src folder
4) Create a payroll.db file.
5) In "Execute SQL" section paste the following sql code
Code:
```sql
DROP TABLE IF EXISTS leave_requests;
DROP TABLE IF EXISTS employee_salary;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    role TEXT NOT NULL CHECK(role IN ('admin', 'employee')),
    department TEXT
);

CREATE TABLE employee_salary (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    emp_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    base_salary REAL NOT NULL,
    allowances REAL NOT NULL,
    deductions REAL NOT NULL,
    FOREIGN KEY(emp_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE leave_requests (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    emp_id INTEGER NOT NULL,
    emp_name TEXT NOT NULL,
    from_date TEXT NOT NULL,
    to_date TEXT NOT NULL,
    reason TEXT,
    status TEXT DEFAULT 'Pending',
    FOREIGN KEY(emp_id) REFERENCES users(id) ON DELETE CASCADE
);

INSERT INTO users (name, username, password, role, department)
VALUES ('Admin User', 'admin', 'admin123', 'admin', 'HR');

```

![image](https://github.com/user-attachments/assets/823db37d-30e9-4e77-a325-3624d9d49cea)<br />
![image](https://github.com/user-attachments/assets/58ed22e8-ebf6-4d01-a22a-3ef0fdb34038)<br />
6) Now press "Execute All" and then File > Wrtie Changes
![image](https://github.com/user-attachments/assets/ce8254c6-70de-4ef3-8d22-516c9f5a0c72)<br />
![image](https://github.com/user-attachments/assets/e1344867-84df-49b0-b557-5e56af5f7bf9)<br />



## Step-3 : Now in IDE, right click on project folder and goto 
```bash
Open Module Settings > Libraries > Press "+" icon > From maven > Add these both
org.xerial:sqlite-jdbc:3.43.2.1
com.itextpdf:itextpdf:5.5.13.3
```


# Step-3 : Now Run Main.java
