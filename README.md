# Fitness Gym Management System (Design Patterns LA-2)

A enterprise-grade **Gym Management System** built with **Java 17**, **Spring Boot 3**, **Plain JDBC**, and **SQLite**, specifically developed to demonstrate 5 core Gang of Four (GoF) design patterns with zero ORM overhead.

---

## 🎯 Architectural Design Patterns

This system was engineered around five foundational Design Patterns:

```
+-----------------------------------------------------------------------------------+
|                            GANG OF FOUR DESIGN PATTERNS                           |
+-----------------------------------------------------------------------------------+
| 1. Builder Pattern        | Membership.Builder                                   |
| 2. Strategy Pattern       | FeeStrategy (Monthly, Yearly, Student, Couple)       |
| 3. Template Method        | EnrollmentProcess (NewEnrollment, RenewalEnrollment) |
| 4. Mediator Pattern       | GymCoordinator (MemberService, TrainerService, Class) |
| 5. Memento Pattern        | MemberPlan.Memento & PlanManagementService (Undo/Redo)|
+-----------------------------------------------------------------------------------+
```

### 1. Builder Pattern (`Membership.Builder`)
- **Package:** `com.gym.model.entity.Membership`
- **Purpose:** Solves the telescoping constructor anti-pattern by safely constructing complex `Membership` objects with validated invariants.
- **Constraints Enforced:**
  - Mandatory: `memberId`, `planType`, `startDate`
  - Optional Add-ons: `trainerId`, `locker`, `dietPlan`, `ptSessions`, `feeStrategy`, `baseFee`, `totalFee`
  - Invariant validation: Throws `IllegalStateException` on negative sessions, empty plan types, or invalid IDs.

### 2. Strategy Pattern (`FeeStrategy`)
- **Package:** `com.gym.pattern.strategy`
- **Classes:** `MonthlyFee`, `YearlyFee`, `StudentFee`, `CoupleFee`, resolved via `FeeStrategyFactory`.
- **Purpose:** Eliminates brittle `switch` and `if-else` pricing ladders. Calculates base rates, duration extensions, and add-on rates (locker $15/mo, diet plan $200, PT sessions $30/session) dynamically.

### 3. Template Method Pattern (`EnrollmentProcess`)
- **Package:** `com.gym.pattern.templatemethod`
- **Classes:** `EnrollmentProcess` (abstract), `NewEnrollment`, `RenewalEnrollment`.
- **Workflow:**
  1. `verifyMember()` (abstract) — Checks existence and eligibility.
  2. `collectFee()` (abstract) — Applies standard strategy or 5% loyalty discounts for renewals.
  3. `activateMembership()` (concrete) — Builds membership and records payment.
  4. `sendWelcomeMessage()` (hook) — Dispatches tailored messages.

### 4. Mediator Pattern (`GymCoordinator`)
- **Package:** `com.gym.pattern.mediator`
- **Colleagues:** `MemberService`, `TrainerService`, `ClassScheduleService`.
- **Purpose:** Colleague services do NOT communicate directly with each other. All cross-cutting operations route through `GymCoordinator`.
- **Mediated Business Rules:**
  - **Rule 1 (Capacity):** Rejects class bookings if capacity is reached (e.g., HIIT 12/12 spots full).
  - **Rule 2 (Availability):** Rejects booking if the assigned instructor is marked off-duty or unavailable.
  - **Rule 3 (Duplicate Check):** Rejects duplicate member bookings.
  - **Rule 4 (Post-Enrollment):** Automatically pairs members with default introductory classes.

### 5. Memento Pattern (`MemberPlan.Memento`)
- **Package:** `com.gym.pattern.memento`
- **Classes:** `MemberPlan` (Originator), `MemberPlan.Memento` (State Snapshot), `PlanManagementService` (Caretaker).
- **Purpose:** Preserves historical plan snapshots in SQLite table `plan_history`. Enables instant rollback (`undoPlanChange()`) with zero internal encapsulation leakage.

---

## 🏗️ Project Structure

```
c:/Users/HP/OneDrive/Desktop/Kartikeya/DP PROJECT LA-2/
├── pom.xml                               # Spring Boot 3.2.5, SQLite JDBC, Thymeleaf
├── gym.db                                # SQLite Database
├── src/
│   ├── main/
│   │   ├── java/com/gym/
│   │   │   ├── GymManagementApplication.java
│   │   │   ├── config/DBInitializer.java     # SQLite Schema Bootstrap & Seeder
│   │   │   ├── controller/                   # Spring MVC Controllers
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── MemberController.java
│   │   │   │   ├── EnrollmentController.java
│   │   │   │   ├── ClassController.java
│   │   │   │   ├── PlanController.java
│   │   │   │   ├── PaymentController.java
│   │   │   │   └── FeeApiController.java
│   │   │   ├── dao/                          # Plain JDBC Data Access Objects
│   │   │   │   ├── MemberDAO.java
│   │   │   │   ├── TrainerDAO.java
│   │   │   │   ├── MembershipDAO.java
│   │   │   │   ├── GymClassDAO.java
│   │   │   │   ├── ClassBookingDAO.java
│   │   │   │   ├── PaymentDAO.java
│   │   │   │   └── PlanHistoryDAO.java
│   │   │   ├── model/
│   │   │   │   ├── entity/                   # Domain Entities
│   │   │   │   └── dto/                      # Form DTOs & API Responses
│   │   │   ├── pattern/
│   │   │   │   ├── mediator/                 # Mediator Pattern
│   │   │   │   ├── memento/                  # Memento Pattern
│   │   │   │   ├── strategy/                 # Strategy Pattern
│   │   │   │   └── templatemethod/           # Template Method Pattern
│   │   │   └── service/                      # Business & Coordination Services
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/
│   │       │   ├── css/style.css             # Dark Glassmorphism Design System
│   │       │   └── js/app.js                 # Live AJAX Pricing & Modal Scripts
│   │       └── templates/                    # Thymeleaf UI Templates
│   │           ├── dashboard.html
│   │           ├── members.html
│   │           ├── enroll.html
│   │           ├── classes.html
│   │           ├── plans.html
│   │           ├── payments.html
│   │           └── layout/fragments.html
│   └── test/java/com/gym/pattern/            # 25 Unit & Pattern Verification Tests
│       ├── MembershipBuilderTest.java
│       ├── FeeStrategyTest.java
│       ├── EnrollmentProcessTest.java
│       ├── GymMediatorTest.java
│       └── MemberPlanMementoTest.java
```

---

## 🚀 How to Run the Application

### Prerequisites
- **Java 17+** (OpenJDK / Oracle JDK)
- **Maven** (or use the included `mvnw.cmd` wrapper)

### Running Locally
```powershell
# Navigate to project root
cd "c:\Users\HP\OneDrive\Desktop\Kartikeya\DP PROJECT LA-2"

# Run tests to verify all 5 design patterns
.\mvnw.cmd test

# Start the Spring Boot Application
.\mvnw.cmd spring-boot:run
```

Once started, open your browser and navigate to:
👉 **`http://localhost:8080/`**

---

## 🧪 Running Automated Tests

All 25 unit and pattern verification tests run seamlessly via JUnit 5:
```powershell
.\mvnw.cmd test
```

| Test Class | Pattern Tested | Test Count | Status |
|:---|:---|:---:|:---:|
| `MembershipBuilderTest` | Builder Pattern | 7 | ✅ Passed |
| `FeeStrategyTest` | Strategy Pattern | 6 | ✅ Passed |
| `EnrollmentProcessTest` | Template Method | 3 | ✅ Passed |
| `GymMediatorTest` | Mediator Pattern | 5 | ✅ Passed |
| `MemberPlanMementoTest` | Memento Pattern | 4 | ✅ Passed |
| **Total** | | **25 / 25** | **100% Green** |

---

## 🌐 Endpoints & Feature Walkthrough

- **`/` (Dashboard):** Real-time metrics on members, classes, revenue, and active GoF patterns in production.
- **`/members`:** Member directory with live CRUD modal management.
- **`/enroll`:** Dynamic enrollment wizard with live AJAX fee estimation based on Strategy pattern.
- **`/classes`:** Class schedules, coach status toggling, and Mediator pattern booking validation.
- **`/plans/{memberId}`:** Plan upgrade/downgrade center with instant Memento undo rollback.
- **`/payments`:** Transaction ledger and receipt generator.
