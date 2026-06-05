# Germany Citizen Portal - Secure Digital Portal with EUDI Wallet Integration

Germany Citizen Portal is a state-of-the-art secure digital portal for public services in Saxony, Germany. It demonstrates modern EUDI Wallet verification and credential issuance, dynamic multi-step public service applications, and a premium glassmorphic UI.

---

## 🌟 Key Features

### 1. 🛡️ EUDI Wallet Integration
* **EUDI Wallet Login & Authentication:** High-security passwordless authentication via **OpenID4VP** (Direct Post & JARM) with verifiable credential presentation.
* **Multi-Document Verification:** Capability to request multiple credentials concurrently (e.g., Personal ID (PID) + University Diploma) during a single verification session.
* **EUDI Wallet Dynamic Issuance:** Dynamic issuance of digital documents (e.g., *Meldebestätigung* - Proof of Residence) via **OpenID4VCI** using scanned QR codes.
* **Pre-fill with EUDI:** Form pre-filling leveraging claims securely shared from the user's wallet (e.g., Full Name, Street, Postal Code, City, Registration Date).

### 2. 📂 Service Application Portal
* **Mandatory Authorization:** Users are securely prompted to authenticate with their EUDI Wallet before they can access and fill out online application forms.
* **Dynamic Multi-step Forms:** Guided wizard workflow for completing complex public services.
* **User Profile & Status Tracking:** Dashboard summarizing submitted applications (e.g., `APPROVED`, `SUBMITTED`) and displaying dynamic issuance options for approved entries.
* **Result Document Viewer:** Profile page displays generated PDF decision notices (*Bescheid*) while filtering out temporary user-uploaded attachments.
* **Local Filesystem Storage:** Uploaded files and generated PDF outcomes are stored securely in the local filesystem rather than database BLOBs.

### 3. 🎨 Premium User Experience & Aesthetics
* **Saxony Portal Style Guide:** Clean, professional interface with modern typography (Inter), rich CSS gradients, glassmorphism elements, and smooth micro-animations.
* **Material Design Icons:** Standardized, high-quality Material Symbols for categories and controls.
* **Conditional Businesses Tab:** Tabbed navigation between Citizens (*Bürger*) and Businesses (*Unternehmen*). The Businesses view features a premium "Under Construction / Ще в роботі" placeholder card.
* **Custom Wallpaper Background:** Hero search section customized with a high-resolution background (`site_wallpaper.png`).

---

## 🛠️ Technology Stack

### Backend
* **Language:** Kotlin
* **Framework:** Spring Boot 3+ (Spring Web, Spring Data JPA)
* **Database:** SQLite (integrated via JDBC & Hibernate)
* **Encryption & Signature:** Custom cryptographic verify/issue logic for SD-JWT, JWS, and JWE wrappers.

### Frontend
* **Framework:** Angular (v17+, Standalone Components)
* **Styling:** CSS Custom Properties (Vanilla CSS) for flexibility and responsive layouts
* **Icons:** Google Fonts Material Icons

---

## 🚀 Getting Started

### Prerequisites
* **Java Development Kit (JDK):** Version 17 or higher
* **Node.js & npm:** Node.js 18+ (tested up to 25.x)

---

### Running the Backend
1. From the project root, build and start the Spring Boot application:
   ```bash
   ./gradlew bootRun
   ```
2. The backend server will run on `http://localhost:8081`.

### Running the Frontend
1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Run the development server:
   ```bash
   npm run start
   ```
4. Access the web portal in your browser at `http://localhost:4200`.

---

## 📂 Project Structure

```
germany-citizen-portal/
├── src/                          # Backend Kotlin Spring Boot Source Code
│   ├── main/
│   │   ├── kotlin/com/deutrust/  # Core Application logic, Controllers, Services
│   │   └── resources/            # Config files, application.properties
├── frontend/                     # Angular Single Page Application
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/       # Reusable components (e.g. EUDI authentication modal)
│   │   │   ├── pages/            # Core views (Main, Category, Service, Profile)
│   │   │   └── services/         # State management & Translation services
│   │   ├── public/               # Static assets (favicons, site_wallpaper.png)
│   │   └── index.html            # Main HTML Shell
├── uploads/                      # Local filesystem storage for attachments & PDFs
└── README.md                     # Project Overview
```

---

## 🔒 Security & EUDI Protocols
* **Direct Post JARM Response Verification:** Parsed tokens extract credential payloads, support SD-JWT disclosures validation, check signature segments, and verify session binding parameters.
* **Key-Binding:** Verifier inspects cryptographic key-binding of the presentation signature.
* **Dynamic PDF Generation:** Approved documents generate a high-quality PDF outcome dynamically linked to the user record.
