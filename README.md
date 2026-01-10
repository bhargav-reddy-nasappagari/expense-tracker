# 💰 Expense Tracker - Personal Finance Management System

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Jakarta EE](https://img.shields.io/badge/Jakarta%20EE-10-blue.svg)](https://jakarta.ee/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A full-stack web application for tracking personal expenses, managing budgets, and generating detailed financial reports. Built with Jakarta EE servlets, MySQL, and vanilla JavaScript.

---

## 📋 Table of Contents

- [Features](#-features)
- [Architecture Overview](#-architecture-overview)
- [Technology Stack](#-technology-stack)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Installation & Setup](#-installation--setup)
- [Configuration](#-configuration)
- [Database Schema](#-database-schema)
- [API Endpoints](#-api-endpoints)
- [Running Tests](#-running-tests)
- [Security Features](#-security-features)
- [Future Enhancements](#-future-enhancements)
- [Contributing](#-contributing)
- [License](#-license)

---

## ✨ Features

### Core Functionality
- ✅ **User Authentication** - Registration, login with BCrypt password hashing
- ✅ **Email Verification** - Token-based account verification system
- ✅ **Password Recovery** - Secure password reset via email
- ✅ **Session Management** - "Remember Me" functionality with secure cookies
- ✅ **Profile Management** - Update name, email, phone number, change password

### Expense Management
- ✅ **CRUD Operations** - Create, read, update, delete expenses
- ✅ **Category System** - Default categories + custom user-defined categories
- ✅ **Advanced Filtering** - By date range, amount range, keyword, category
- ✅ **Sorting** - By date, amount, description, category
- ✅ **Pagination** - Configurable page sizes (10, 20, 50, 100)

### Budgeting
- ✅ **Budget Creation** - Set monthly/custom period budgets per category
- ✅ **Progress Tracking** - Visual progress bars with percentage used
- ✅ **Budget Alerts** - Color-coded warnings (green/yellow/red)
- ✅ **Recurring Budgets** - Auto-renew monthly budgets

### Reports & Analytics
- ✅ **Dashboard** - Real-time spending overview with charts
- ✅ **Spending Reports** - Monthly, quarterly, yearly, custom date ranges
- ✅ **Category Analysis** - Breakdown by spending category
- ✅ **Trend Analysis** - Multi-month spending trends with comparisons
- ✅ **Heatmap Visualization** - Daily spending intensity calendar
- ✅ **Spending Insights** - AI-like pattern detection and recommendations

### Data Export
- ✅ **CSV Export** - Cumulative budget tracking with running totals
- ✅ **PDF Reports** - Professional financial statements with charts

---

## 🏗️ Architecture Overview

### **Three-Tier Architecture**
```
┌──────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                         │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐             │
│  │  Servlets  │  │    JSPs    │  │  Filters   │             │
│  │ (Controllers)  │  (Views)   │  │  (Auth)    │             │
│  └────────────┘  └────────────┘  └────────────┘             │
│         │               │                │                    │
│         └───────────────┴────────────────┘                    │
│                         │                                     │
└─────────────────────────┼─────────────────────────────────────┘
                          ↓
┌──────────────────────────────────────────────────────────────┐
│                    SERVICE LAYER                              │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐             │
│  │UserService │  │ExpenseServ │  │BudgetServ  │             │
│  │            │  │            │  │            │             │
│  └────────────┘  └────────────┘  └────────────┘             │
│                                                               │
│  • Business Logic    • Validation    • Transaction Mgmt     │
│                         │                                     │
└─────────────────────────┼─────────────────────────────────────┘
                          ↓
┌──────────────────────────────────────────────────────────────┐
│                 DATA ACCESS LAYER                             │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐             │
│  │  UserRepo  │  │ExpenseRepo │  │BudgetRepo  │             │
│  │            │  │            │  │            │             │
│  └────────────┘  └────────────┘  └────────────┘             │
│                         │                                     │
│  • SQL Queries    • HikariCP    • Prepared Statements       │
└─────────────────────────┼─────────────────────────────────────┘
                          ↓
                   ┌─────────────┐
                   │   MySQL DB  │
                   └─────────────┘
```

### **Key Design Patterns**

1. **MVC (Model-View-Controller)**
   - Models: `User`, `Expense`, `Category`, `Budget`
   - Views: JSP files in `/WEB-INF/views/`
   - Controllers: Servlet classes

2. **Repository Pattern**
   - Abstracts database operations
   - Single source of truth for queries
   - Centralized connection management

3. **Service Layer Pattern**
   - Business logic isolation
   - Transaction boundaries
   - Reusable across controllers

4. **DTO (Data Transfer Objects)**
   - `LoginRequest`, `RegisterRequest`, `ReportData`
   - Clean data transfer between layers

5. **Filter Chain Pattern**
   - `AuthenticationFilter` - Session validation
   - `NoCacheFilter` - Cache control
   - `VerificationFilter` - Email verification enforcement

---

## 🛠️ Technology Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Backend** | Java 21 | Core application logic |
| | Jakarta EE 10 (Servlets 6.0) | Web framework |
| | JDBC | Database connectivity |
| | HikariCP 5.0.1 | Connection pooling |
| | BCrypt | Password hashing |
| **Database** | MySQL 8.0+ | Data persistence |
| | Flyway 10.20.0 | Schema migrations |
| **Frontend** | HTML5 + CSS3 | Structure & styling |
| | Vanilla JavaScript | Interactivity |
| | Chart.js | Data visualization |
| | Bootstrap Icons | UI icons |
| **Email** | Jakarta Mail 2.0.3 | Email notifications |
| **PDF** | Apache PDFBox 3.0.3 | Report generation |
| **Build** | Maven 3.9+ | Dependency management |
| **Server** | Apache Tomcat 10.1+ | Servlet container |

---

## 📁 Project Structure
```
expense-tracker/
│
├── src/
│   ├── main/
│   │   ├── java/com/expensetracker/
│   │   │   ├── controller/          # Servlets (HTTP handlers)
│   │   │   │   ├── LoginServlet.java
│   │   │   │   ├── RegisterServlet.java
│   │   │   │   ├── DashboardServlet.java
│   │   │   │   ├── AddExpenseServlet.java
│   │   │   │   ├── EditExpenseServlet.java
│   │   │   │   ├── ListExpensesServlet.java
│   │   │   │   ├── DeleteExpenseServlet.java
│   │   │   │   ├── BudgetServlet.java
│   │   │   │   ├── ReportServlet.java
│   │   │   │   ├── CategoryServlet.java
│   │   │   │   ├── ProfileServlet.java
│   │   │   │   ├── ExportServlet.java
│   │   │   │   ├── ForgotPasswordServlet.java
│   │   │   │   ├── ResetPasswordServlet.java
│   │   │   │   ├── VerifyEmailServlet.java
│   │   │   │   └── ResendVerificationServlet.java
│   │   │   │
│   │   │   ├── service/              # Business logic layer
│   │   │   │   ├── UserService.java          # User authentication & management
│   │   │   │   ├── ExpenseService.java       # Expense CRUD operations
│   │   │   │   ├── BudgetService.java        # Budget management
│   │   │   │   ├── CategoryService.java      # Category management
│   │   │   │   ├── ReportService.java        # Analytics & report generation
│   │   │   │   ├── ValidationService.java    # Input validation rules
│   │   │   │   ├── AuthService.java          # Session helpers
│   │   │   │   ├── PdfReportGenerator.java   # PDF export logic
│   │   │   │   └── TransactionManager.java   # DB transaction wrapper
│   │   │   │
│   │   │   ├── repository/           # Data access layer
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── ExpenseRepository.java
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   └── BudgetRepository.java
│   │   │   │
│   │   │   ├── model/                # Domain entities
│   │   │   │   ├── User.java
│   │   │   │   ├── Expense.java
│   │   │   │   ├── Category.java
│   │   │   │   └── Budget.java
│   │   │   │
│   │   │   ├── dto/                  # Data transfer objects
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── ReportData.java
│   │   │   │   ├── CategorySummary.java
│   │   │   │   ├── ExpenseDetail.java
│   │   │   │   ├── TrendPoint.java
│   │   │   │   ├── PeriodComparison.java
│   │   │   │   ├── SpendingInsight.java
│   │   │   │   ├── DaySpending.java
│   │   │   │   ├── MonthlyTrend.java
│   │   │   │   └── CategoryPerformance.java
│   │   │   │
│   │   │   ├── filter/               # Request filters
│   │   │   │   ├── AuthenticationFilter.java
│   │   │   │   ├── NoCacheFilter.java
│   │   │   │   └── VerificationFilter.java
│   │   │   │
│   │   │   └── util/                 # Utility classes
│   │   │       ├── ConfigLoader.java         # Configuration management
│   │   │       ├── HikariCPDataSource.java   # DB connection pool
│   │   │       ├── EmailUtil.java            # Email sending
│   │   │       ├── TokenUtil.java            # Token generation/hashing
│   │   │       ├── CSRFUtil.java             # CSRF protection
│   │   │       ├── DateUtils.java            # Date helpers
│   │   │       ├── PagedResult.java          # Pagination wrapper
│   │   │       ├── ChartDataBuilder.java     # Chart data formatting
│   │   │       └── FlywayInitializer.java    # DB migration listener
│   │   │
│   │   ├── resources/
│   │   │   ├── config.properties.template    # Configuration template
│   │   │   └── db/migration/                 # Flyway SQL migrations
│   │   │       ├── V1__create_users_table.sql
│   │   │       ├── V2__create_categories_table.sql
│   │   │       ├── V3__create_expenses_table.sql
│   │   │       ├── V5__create_budgets_table.sql
│   │   │       ├── V6__add_remember_me_columns.sql
│   │   │       ├── V7__add_password_reset_columns.sql
│   │   │       ├── V8__fix_reset_expiry_timezone.sql
│   │   │       └── V9__add_email_verification.sql
│   │   │
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   ├── web.xml                   # Servlet mappings
│   │       │   └── views/                    # JSP templates
│   │       │       ├── layout/
│   │       │       │   ├── header.jsp
│   │       │       │   ├── footer.jsp
│   │       │       │   └── message.jsp
│   │       │       ├── login.jsp
│   │       │       ├── register.jsp
│   │       │       ├── dashboard.jsp
│   │       │       ├── expenses.jsp
│   │       │       ├── add-expense.jsp
│   │       │       ├── edit-expense.jsp
│   │       │       ├── budgets.jsp
│   │       │       ├── budget-details.jsp
│   │       │       ├── reports.jsp
│   │       │       ├── heatmap.jsp
│   │       │       ├── trends.jsp
│   │       │       ├── category-report.jsp
│   │       │       ├── manage-categories.jsp
│   │       │       ├── profile.jsp
│   │       │       ├── forgot-password.jsp
│   │       │       ├── reset-password.jsp
│   │       │       ├── verification-pending.jsp
│   │       │       └── error.jsp
│   │       │
│   │       └── css/
│   │           └── style.css              # Main stylesheet
│   │
│   └── test/
│       └── java/com/expensetracker/
│           ├── service/
│           │   ├── ValidationServiceTest.java
│           │   ├── UserServiceTest.java
│           │   ├── ExpenseServiceTest.java
│           │   ├── BudgetServiceTest.java
│           │   └── ReportServiceTest.java
│           ├── repository/
│           │   ├── UserRepositoryTest.java
│           │   ├── ExpenseRepositoryTest.java
│           │   └── BudgetRepositoryTest.java
│           └── util/
│               ├── ConfigLoaderTest.java
│               ├── TokenUtilTest.java
│               └── ValidationServiceTest.java
│
├── pom.xml                           # Maven dependencies
├── .gitignore                        # Git ignore rules
└── README.md                         # This file
```

---

## 📦 Prerequisites

- **Java 21** or higher ([OpenJDK](https://openjdk.java.net/))
- **Apache Maven 3.9+** ([Download](https://maven.apache.org/download.cgi))
- **MySQL 8.0+** ([Download](https://dev.mysql.com/downloads/mysql/))
- **Apache Tomcat 10.1+** (Optional - Maven can run embedded Tomcat)
- **Gmail Account** (for email functionality)

---

## 🚀 Installation & Setup

### **1. Clone Repository**
```bash
git clone https://github.com/yourusername/expense-tracker.git
cd expense-tracker
```

### **2. Create MySQL Database**
```sql
CREATE DATABASE expense_tracker_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### **3. Configure Application**
```bash
# Copy configuration template
cp src/main/resources/config.properties.template src/main/resources/config.properties
```

Edit `config.properties`:
```properties
# Database
db.url=jdbc:mysql://localhost:3306/expense_tracker_db
db.username=root
db.password=YOUR_PASSWORD

# Email (Gmail App Password - NOT regular password)
email.smtp.host=smtp.gmail.com
email.smtp.port=587
email.smtp.username=your.email@gmail.com
email.smtp.password=YOUR_APP_PASSWORD

# Application
app.base.url=http://localhost:8080/expense-tracker
```

**Generate Gmail App Password**:
1. Go to Google Account → Security
2. Enable 2-Step Verification
3. Search "App passwords"
4. Generate password for "Mail"
5. Use this 16-character password in config

### **4. Run Database Migrations**
```bash
mvn flyway:migrate
```

Expected output:
```
[INFO] Successfully validated 9 migrations
[INFO] Current schema version: 9
```

### **5. Build Application**
```bash
mvn clean package
```

### **6. Run Application**

**Option A: Using Maven Cargo Plugin**
```bash
mvn cargo:run
```

**Option B: Deploy to Tomcat**
```bash
# Copy WAR to Tomcat
cp target/expense-tracker.war $TOMCAT_HOME/webapps/
$TOMCAT_HOME/bin/startup.sh
```

Application runs at: **http://localhost:8080/expense-tracker**

---

## ⚙️ Configuration

### **Environment Variable Overrides**

Any property in `config.properties` can be overridden via environment variables:
```bash
# Property: db.password → Environment: DB_PASSWORD
export DB_PASSWORD=prod_password

# Property: email.smtp.username → Environment: EMAIL_SMTP_USERNAME
export EMAIL_SMTP_USERNAME=prod@example.com
```

### **Available Configuration Keys**

| Key | Default | Description |
|-----|---------|-------------|
| `db.url` | - | JDBC connection URL |
| `db.username` | - | Database username |
| `db.password` | - | Database password |
| `email.smtp.host` | smtp.gmail.com | SMTP server |
| `email.smtp.port` | 587 | SMTP port |
| `email.smtp.username` | - | Email account |
| `email.smtp.password` | - | Email password |
| `app.base.url` | http://localhost:8080/expense-tracker | Base URL for emails |
| `security.token.expiry.hours` | 24 | Email verification token expiry |
| `security.remember.me.days` | 15 | Remember Me cookie duration |
| `security.password.reset.hours` | 1 | Password reset link expiry |

---

## 🗄️ Database Schema

### **Entity Relationship Diagram**
```
┌─────────────────┐
│     USERS       │
├─────────────────┤
│ id (PK)         │
│ username        │
│ password        │
│ email           │
│ phone           │
│ email_verified  │
│ created_at      │
└────────┬────────┘
         │
         │ 1:N
         │
    ┌────┴────┬──────────────┬──────────────┐
    │         │              │              │
┌───▼──────┐ │         ┌────▼────┐    ┌────▼────┐
│CATEGORIES│ │         │EXPENSES │    │BUDGETS  │
├──────────┤ │         ├─────────┤    ├─────────┤
│id (PK)   │ │         │id (PK)  │    │id (PK)  │
│name      │─┼────────▶│user_id  │    │user_id  │
│user_id   │ │         │category │◀───│category │
│is_default│ │         │amount   │    │amount   │
└──────────┘ │         │date     │    │period   │
             │         └─────────┘    └─────────┘
             │
             └─ FK relationship
```

### **Table Definitions**

#### **users**
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(10),
    email_verified BOOLEAN DEFAULT FALSE,
    verification_token_hash VARCHAR(64),
    token_created_at TIMESTAMP,
    legacy_unverified BOOLEAN DEFAULT FALSE,
    remember_token VARCHAR(64),
    remember_expires_at TIMESTAMP,
    reset_token VARCHAR(255),
    reset_expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### **categories**
```sql
CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_category_name (user_id, name)
);
```

#### **expenses**
```sql
CREATE TABLE expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    category_id INT NOT NULL,
    expense_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    INDEX idx_user_date (user_id, expense_date DESC)
);
```

#### **budgets**
```sql
CREATE TABLE budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE,
    is_recurring BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_category_period (user_id, category_id, period_start)
);
```

---

## 🔌 API Endpoints

### **Authentication**

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/login` | Display login form |
| POST | `/login` | Authenticate user |
| GET | `/register` | Display registration form |
| POST | `/register` | Create new user account |
| GET | `/logout` | End user session |
| GET | `/forgot-password` | Password reset form |
| POST | `/forgot-password` | Send reset email |
| GET | `/reset-password?token=...` | Verify reset token |
| POST | `/reset-password` | Update password |
| GET | `/verify-email?token=...` | Verify email address |
| POST | `/resend-verification` | Resend verification email |

### **Expense Management**

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/expenses` | List expenses (paginated, filterable) |
| GET | `/expense/add` | Display add form |
| POST | `/expense/add` | Create new expense |
| GET | `/expense/edit?id={id}` | Display edit form |
| POST | `/expense/edit` | Update expense |
| POST | `/expense/delete` | Delete expense |
| GET | `/expenses/export?format={csv\|pdf}` | Export expenses |

### **Budget Management**

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/budgets` | List all budgets |
| POST | `/budgets?action=create` | Create budget |
| POST | `/budgets?action=update` | Update budget |
| POST | `/budgets?action=delete` | Delete budget |
| GET | `/budgets?action=detail&id={id}` | View budget details |

### **Reports & Analytics**

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/reports` | Main reports dashboard |
| GET | `/reports/heatmap?year={y}&month={m}` | Spending heatmap |
| GET | `/reports/trends?months={n}` | Multi-month trends |
| GET | `/reports/categories` | Category performance |

### **Profile & Settings**

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/profile` | User profile page |
| POST | `/profile?action=updateProfile` | Update profile info |
| POST | `/profile?action=changePassword` | Change password |
| GET | `/category` | Manage categories |
| POST | `/category?action=add` | Create category |
| POST | `/category?action=rename` | Rename category |
| POST | `/category?action=delete` | Delete category |

---

## 🧪 Running Tests

### **Setup Test Configuration**

Create `src/test/resources/config.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/expense_tracker_test
db.username=root
db.password=YOUR_PASSWORD
```

Create test database:
```sql
CREATE DATABASE expense_tracker_test;
```

### **Run All Tests**
```bash
mvn test
```

### **Run Specific Test Class**
```bash
mvn test -Dtest=ValidationServiceTest
```

### **Run with Coverage**
```bash
mvn jacoco:prepare-agent test jacoco:report
```

View coverage: `target/site/jacoco/index.html`

---

## 🔐 Security Features

### **Implemented**

✅ **Password Security**
- BCrypt hashing (cost factor 12)
- Password strength validation (8+ chars, uppercase, lowercase, digit, special char)
- Secure password reset flow

✅ **Session Security**
- HttpOnly cookies (XSS protection)
- Secure flag for HTTPS
- Session timeout (30 minutes)
- Session regeneration on login (session fixation prevention)

✅ **CSRF Protection**
- Token validation on all state-changing requests
- Token stored in session
- Validated on POST/PUT/DELETE

✅ **SQL Injection Prevention**
- Prepared statements for all queries
- Input validation via `ValidationService`
- Parameterized queries only

✅ **Email Verification**
- SHA-256 hashed tokens
- 24-hour expiry
- One-time use tokens

✅ **Authorization**
- User-resource ownership checks
- Authentication filters
- Verification filters for sensitive features

### **Best Practices Applied**

- No sensitive data in logs
- Configuration externalized
- Error messages don't leak information
- Rate limiting on password reset (5-second cooldown)
- Input sanitization and validation

---

## 🔄 Future Enhancements

### **Planned Features**
- [ ] Tags system for cross-category expense grouping
- [ ] CSV import functionality
- [ ] Budget exceeded email notifications
- [ ] Receipt image attachments (file upload)
- [ ] Payment method tracking
- [ ] Multi-currency support
- [ ] Timezone support
- [ ] Collaborative budgets (family accounts)
- [ ] Mobile app (React Native)
- [ ] REST API for third-party integrations

### **Technical Improvements**
- [ ] Migrate to Spring Boot
- [ ] Add Redis for session management
- [ ] Implement caching layer
- [ ] Add API rate limiting
- [ ] Set up CI/CD pipeline (GitHub Actions)
- [ ] Docker containerization
- [ ] Kubernetes deployment
- [ ] Elasticsearch for full-text search

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Write tests for new functionality
4. Ensure all tests pass (`mvn test`)
5. Commit changes (`git commit -m 'Add amazing feature'`)
6. Push to branch (`git push origin feature/amazing-feature`)
7. Open Pull Request

### **Code Style**
- Follow Java naming conventions
- Use descriptive variable names
- Write JavaDoc for public APIs
- Keep methods under 50 lines
- Maximum line length: 120 characters

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Bhargav Reddy Nasappagari**
- GitHub: [@bhargav-reddy-nasappagari](https://github.com/bhargav-reddy-nasappagari)
- LinkedIn: [Bhargav Reddy Nasappagari](https://linkedin.com/in/bhargav-reddy-nasappagari)
- Email: bhargavreddynasappagari24@gmail.com

---

## 🙏 Acknowledgments

- [Jakarta EE Documentation](https://jakarta.ee/specifications/)
- [HikariCP](https://github.com/brettwooldridge/HikariCP)
- [Chart.js](https://www.chartjs.org/)
- [Apache PDFBox](https://pdfbox.apache.org/)
- [BCrypt](https://en.wikipedia.org/wiki/Bcrypt)

---

## 📞 Support

If you encounter any issues or have questions:

1. Check [existing issues](https://github.com/bhargav-reddy-nasappagari/expense-tracker/issues)
2. Create a [new issue](https://github.com/bhargav-reddy-nasappagari/expense-tracker/issues/new)
3. Contact via email: bhargavreddynasappagari24@gmail.com

---

**Forged with Ardor by Bhargav Reddy Nasappagari**