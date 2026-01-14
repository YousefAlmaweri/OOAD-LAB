# FCI Postgraduate Seminar Management System

A standalone Java Swing application designed to manage the full lifecycle of postgraduate research seminars at FCI — from student registration to evaluation, awards, and final reporting.

The system supports three roles (Student, Evaluator, Coordinator) and persists all data automatically using file-based storage.

## Features

### Student
- Submit and update:
  - Research title
  - Abstract
  - Supervisor name
  - Presentation type (Oral / Poster)
  - Material file path
  - Poster board ID
- View assigned presentation session and time slot
- Vote for People’s Choice Award

### Evaluator
- View assigned presentations
- Read:
  - Student abstract
  - Supervisor
  - Presentation material path
- Evaluate using rubrics:
  - Problem clarity
  - Methodology
  - Results
  - Presentation
- Add qualitative comments
- Automatically calculate average scores

### Coordinator
- Manage users (Students, Evaluators, Coordinators)
- Create seminar sessions (Oral / Poster)
- Add time slots
- Assign:
  - Students to slots
  - Evaluators to presentations
- Generate:
  - Seminar schedule
  - Final evaluation report
  - Award winners
- Export all data as CSV:
  - Users
  - Schedule
  - Evaluations
  - Awards

## Awards System
The system computes:

- Best Oral: Highest average score among oral presentations (requires at least 1 evaluation)
- Best Poster: Highest average score among poster presentations (requires at least 1 evaluation)
- People’s Choice: Highest number of student votes (tie-break by average score)

## Data Persistence
All data is stored in:
seminar_data.ser

This file is automatically created and updated every time the system changes.
No database is required.

## Project Structure

lab_exercise/
│
├── ChatGPTTest/
│   ├── SeminarManagementSystem.java   # Main entry point
│   ├── DataModels.java                # Users, submissions, sessions, evaluations
│   ├── DataStore.java                 # Persistence & data access
│   ├── UIUtil.java                    # UI helpers
│   ├── Panels.java                    # All Swing panels and dashboards
│   └── seminar_data.ser               # Auto-generated at runtime
│
└── README.md

## Default Accounts

Role         Username      Password
Coordinator  coordinator   admin123
Evaluator    evaluator1    pass123
Student      student1      pass123

You can create more users inside the Coordinator dashboard.

## How to Run

1) Compile
javac ChatGPTTest/*.java

2) Run
java ChatGPTTest.SeminarManagementSystem

## Requirements
- Java 17 or later
- Any OS (Windows, macOS, Linux)
- No external libraries

## License
This project is intended for academic and educational use.
