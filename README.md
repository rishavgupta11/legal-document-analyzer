# Legal Document Analyzer

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

A Spring Boot application that automates legal contract risk assessment by analyzing uploaded documents and identifying potentially risky clauses.

## 🌟 Features

- **Document Upload**: Support for PDF, DOC, and DOCX files (up to 10MB)
- **Text Extraction**: Automatic text extraction using Apache Tika
- **Risk Analysis**: Identifies 12+ clause types including:
  - Non-Compete Clauses
  - Indemnity Agreements
  - Confidentiality Clauses
  - Payment Terms
  - Termination Clauses
  - Liability Limitations
- **Risk Scoring**: Automated scoring system (0-100 scale)
- **Recommendations**: AI-generated actionable recommendations
- **RESTful APIs**: 8 well-documented endpoints
- **Swagger UI**: Interactive API documentation

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.1.5, Spring Security
- **Database**: MySQL 8.0, Hibernate/JPA
- **Libraries**: Apache Tika 2.9.0, Maven
- **API Documentation**: Swagger/OpenAPI
- **Testing**: Postman, JUnit

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Postman (for testing)

## 🚀 Installation & Setup

### 1. Clone the repository
```bash
git clone https://github.com/rishavgupta11/legal-document-analyzer.git
cd legal-document-analyzer
```

### 2. Create MySQL database
```sql
CREATE DATABASE legal_analyzer_db;
```

### 3. Update application.properties
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/legal_analyzer_db
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
```

### 4. Build and run
```bash
mvn clean install
mvn spring-boot:run
```

### 5. Access the application
- **API Base URL**: http://localhost:8081
- **Swagger UI**: http://localhost:8081/swagger-ui.html

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/legal-documents/upload` | Upload document for analysis |
| GET | `/api/legal-documents/report/{id}` | Get analysis report |
| GET | `/api/legal-documents/recommendations/{id}` | Get recommendations |
| GET | `/api/legal-documents/status/{id}` | Get processing status |
| GET | `/api/legal-documents/user/documents` | Get user's documents |
| DELETE | `/api/legal-documents/{id}` | Delete document |
| POST | `/api/legal-documents/{id}/reanalyze` | Reanalyze document |
| GET | `/api/legal-documents/health` | Health check |

## 📸 Screenshots

### Upload Document
![Upload](screenshots/upload.png)

### Analysis Report
![Report](screenshots/report.png)

### Swagger UI
![Swagger](screenshots/swagger.png)

## 🧪 Testing

### Using Postman
1. Import the Postman collection from `/postman/collection.json`
2. Upload a document to `/api/legal-documents/upload`
3. Use the returned `documentId` to fetch reports

### Using cURL
```bash
# Upload document
curl -X POST http://localhost:8081/api/legal-documents/upload \
  -F "file=@contract.pdf"

# Get report
curl http://localhost:8081/api/legal-documents/report/{documentId}
```

## 🗄️ Database Schema
```sql
documents
├── id (PK)
├── filename
├── original_filename
├── file_path
├── file_size
├── content_type
├── status
└── upload_time

analysis_results
├── id (PK)
├── document_id (FK)
├── risk_score
├── total_clauses
├── compliance_score
└── overall_risk_level

risk_clauses
├── id (PK)
├── analysis_result_id (FK)
├── clause_type
├── risk_level
└── content

recommendations
├── id (PK)
├── analysis_result_id (FK)
├── type
├── priority
└── description
```

## 🎯 Future Enhancements

- [ ] Integrate real AI/ML models (OpenAI GPT, spaCy)
- [ ] Add JWT authentication
- [ ] Create React/Angular frontend
- [ ] Implement async processing with queues
- [ ] Add email notifications
- [ ] Export reports to PDF
- [ ] Support more document formats

## 👨‍💻 Author

**Your Name**
- GitHub: [@rishavgupta11](https://github.com/rishavgupta11)
- LinkedIn: [Your LinkedIn](https://linkedin.com/in/rishavv1/)
- Email: rishav.mh103@gmail.com

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Spring Boot Team
- Apache Tika Project
- Legal contract templates from Docracy

---

⭐ Star this repo if you found it helpful!





