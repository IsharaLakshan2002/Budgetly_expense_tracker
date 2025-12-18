📌 Project Overview

Budgetly is a mobile expense tracking application built with an offline-first approach. It synchronizes local SQLite data with an Oracle backend using ORDS REST APIs, ensuring data consistency across devices while supporting offline usage.

🏗 System Architecture

Client (Android + SQLite): Stores user data locally

Server (Oracle DB): Central data source and reporting engine

API Layer (ORDS): REST endpoints for secure sync operations

🔄 Synchronization Strategy

Push: Upload unsynced local changes to Oracle

Pull: Download latest server data to client

Conflict Resolution: Last-write-wins using timestamps

Soft Deletes: is_deleted flag ensures safe record removal

📊 Database Design

User-centric relational model

One-to-many relationships (Users → Expenses, Budgets, Savings)

Identical schemas for SQLite and Oracle (data-type adjusted)

📈 Reports (Oracle PL/SQL)

Monthly Expenditure Report

Budget Adherence Tracking

Savings Goal Progress

Category-wise Expense Distribution

🔐 Security & Data Handling

Authentication via local DB and ORDS APIs

Sync status flags for consistency

Backup & recovery via Oracle RMAN

Offline data recovery through full sync

🚀 Key Learnings

Distributed databases · Data synchronization · PL/SQL · REST APIs · Offline-first design · Enterprise DB concepts
