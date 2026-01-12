# Seminar Management System (Java Swing)

## Overview
This is a full implementation of the Seminar Management System for the Faculty of Computing and Informatics (FCI). The system is built strictly using Java Swing and follows the provided UML Class and Sequence diagrams.

## Features
- **Role-Based Access Control**: Specialized interfaces for Students, Evaluators, and Coordinators.
- **Student Module**: Seminar registration, material upload, and presentation preference selection.
- **Evaluator Module**: Rubric-based assessment and feedback entry for assigned presenters.
- **Coordinator Module**: Session management, resource allocation, award computation, and report generation.
- **Data Persistence**: Serialization-based data storage.

## Project Structure
- `com.sms.model`: Domain entities (User, Student, Evaluator, Coordinator, Session, etc.)
- `com.sms.view`: Java Swing GUI components.
- `com.sms.controller`: Business logic and workflow management.
- `com.sms.data`: Data persistence layer.

## How to Run
1. Compile the project:
   ```bash
   javac -d bin src/com/sms/*.java src/com/sms/model/*.java src/com/sms/view/*.java src/com/sms/controller/*.java src/com/sms/data/*.java
   ```
2. Run the application:
   ```bash
   java -cp bin com.sms.Main
   ```

## Default Credentials
- **Student**: `student@fci.edu` / `pass123`
- **Evaluator**: `evaluator@fci.edu` / `pass123`
- **Coordinator**: `coordinator@fci.edu` / `pass123`
