# StayO Backend Application

Find Smarter, Live Better. StayO is a modern co-living and property rental platform backend built with Spring Boot, Spring Security, MongoDB, and Twilio SMS.

---

## 🚀 Technology Stack

- **Core Framework**: Spring Boot 3.4+ / Java 25
- **Database**: MongoDB (Spring Data MongoDB)
- **Security**: Spring Security (Stateful blacklisting + stateless JWT claims-based authorization)
- **SMS / OTP Delivery**: Twilio SMS SDK
- **API Documentation**: Springdoc OpenAPI / Swagger UI
- **Build System**: Maven Wrapper

---

## 📁 Project Structure

The project follows a component-based package architecture:

```text
src/main/java/com/stayo/stayo
│
├── auth/                      # Authentication & Registration Module
│   ├── controller/            # Auth controllers (OTP endpoints)
│   ├── dto/                   # Request/Response DTOs (OtpRequestDto, UpdateUserDto, etc.)
│   ├── entity/                # Auth-specific entities (BlacklistedToken)
│   ├── repository/            # BlacklistedTokenRepository
│   └── service/               # AuthService & OtpService logic
│
├── user/                      # User Profile Module
│   ├── controller/            # UserProfileController
│   ├── service/               # UserProfileService (Profile logic, Image management)
│   ├── repository/            # UserRepository, OtpRepository
│   ├── entity/                # User & OtpRequest schemas
│   ├── dto/                   # UpdateProfileRequest, UserProfileResponse, etc.
│   └── enums/                 # Gender, Role enums
│
├── config/                    # Global Configuration
│   ├── SecurityConfig.java    # Spring Security & CORS configuration
│   └── WebConfig.java         # Static uploads folder serving configuration
│
└── common/                    # Shared Utility & Infrastructure Layer
    ├── exception/             # GlobalExceptionHandler and Custom Domain Exceptions
    ├── response/              # Standard ApiError and Response utilities
    ├── security/              # JwtProvider utilities
    ├── service/               # HealthCheckPingService
    └── util/                  # AuthUtil helper
```

---

## 🛠️ Configuration (`application.properties`)

Configure the following application properties (either directly or via environment variables):

### 1. Database
- `spring.mongodb.uri`: MongoDB Connection URI. Defaults to `mongodb://localhost:27017/stayo`.

### 2. JWT Configuration
- `jwt.secret`: Secret key used for signing JWTs.
- `jwt.expiration`: Access token duration (default: `86400000` ms / 24 hours).

### 3. Twilio SMS Integration
- `twilio.account-sid`: Your Twilio account SID.
- `twilio.auth-token`: Your Twilio auth token.
- `twilio.phone-number`: Your Twilio-provided virtual phone number.

### 4. OTP Settings
- `otp.expiry-minutes`: Lifetime of generated OTPs in minutes (default: `5`).
- `otp.max-attempts`: Maximum failed OTP verification attempts before invalidation (default: `3`).
- `otp.static-code`: Code used in Mock/Static OTP mode (default: `123456`).
- `otp.use-static`: Toggle whether to use mock local OTPs or send real SMS via Twilio. Defaults to `true` (Local Mock Mode) to conserve trial API credits. Set to `false` for real-device SMS testing.

---

## 🔌 API Endpoints Documentation

### 🔓 Authentication Endpoints (`/api/auth/**`)

#### 1. Request OTP
- **Endpoint**: `POST /api/auth/otp/send`
- **Body**:
  ```json
  {
    "mobileNumber": "+919876543210"
  }
  ```
- **Response**: String confirmation. Triggers Twilio SMS (or logs static code `123456`).

#### 2. Verify OTP
- **Endpoint**: `POST /api/auth/otp/verify`
- **Body**:
  ```json
  {
    "mobileNumber": "+919876543210",
    "otp": "123456"
  }
  ```
- **Response**: JWT access token, user ID, role, and basic profile info (signs up new users or logs in existing users).

#### 3. Complete Initial Profile
- **Endpoint**: `PUT /api/auth/update-details`
- **Headers**: `Authorization: Bearer <JWT>`
- **Body**:
  ```json
  {
    "name": "John Doe",
    "email": "john.doe@example.com"
  }
  ```
- **Response**: Updated user metadata. Marks `profileCompleted = true` once both fields are filled.

#### 4. Logout
- **Endpoint**: `POST /api/auth/logout`
- **Headers**: `Authorization: Bearer <JWT>`
- **Response**: Success status. Blacklists the JWT until its original expiration.

---

### 🔒 User Profile Endpoints (`/api/user/profile/**`)

*All profile endpoints require a valid `Authorization: Bearer <JWT>` header.*

#### 1. Get Profile
- **Endpoint**: `GET /api/user/profile`
- **Response**: Complete user details including `profileCompleted` (boolean) and `completionPercentage` (integer value out of 100).

#### 2. Update Profile Details
- **Endpoint**: `PUT /api/user/profile`
- **Body** (Partial updates supported):
  ```json
  {
    "occupation": "Software Engineer",
    "city": "Mumbai",
    "state": "Maharashtra",
    "country": "India",
    "bio": "Passionate about co-living spaces."
  }
  ```
- **Response**: Updated User Profile JSON. Re-calculates profile completion parameters on-the-fly.

#### 3. Upload Profile Image
- **Endpoint**: `POST /api/user/profile/image`
- **Content-Type**: `multipart/form-data`
- **Form Data Parameter**: `file` (Image file)
- **Response**: Relative image URL (e.g. `/uploads/319e7-49f3-...png`). Saves the file inside the local `./uploads` directory.

#### 4. Delete Profile Image
- **Endpoint**: `DELETE /api/user/profile/image`
- **Response**: `204 No Content`. Removes the physical local image file and updates the profile link to `null`.

---

## 💻 Local Development Setup

### Prerequisites
- JDK 25 installed.
- MongoDB running locally (default port `27017`).

### Steps
1. **Clone the repository**:
   ```bash
   git clone https://github.com/adix2326/stayo-be.git
   cd stayo-be
   ```

2. **Configure Environment Variables**:
   Set credentials in your environment:
   ```powershell
   $env:TWILIO_ACCOUNT_SID="ACxxxx"
   $env:TWILIO_AUTH_TOKEN="xxxx"
   $env:TWILIO_MOBILE_NUMBER="+1510xxxx"
   $env:OTP_USE_STATIC="false"  # Set to true to test without sending real SMS
   ```

3. **Run the application**:
   ```powershell
   $env:JAVA_HOME="C:\Program Files\Java\jdk-25.0.3"
   .\mvnw spring-boot:run
   ```

4. **Verify Swagger APIs**:
   Open [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html) in your browser.
   - **Bearer Authentication**: The Swagger UI is equipped with JWT Bearer Token authorization. Click the **"Authorize"** button at the top and paste your JWT token to test the protected `/api/user/profile/**` endpoints directly from the browser interface.

5. **Run the Test Suite**:
   ```powershell
   $env:JAVA_HOME="C:\Program Files\Java\jdk-25.0.3"
   .\mvnw test
   ```

---

## 🌐 Production Deployment (Render)

When deployed to Render, the backend and its developer tools can be accessed publicly:
- **Base API URL**: `https://stayo-be.onrender.com`
- **Deployed Swagger UI**: [https://stayo-be.onrender.com/swagger-ui/index.html](https://stayo-be.onrender.com/swagger-ui/index.html)
- **Deployed OpenAPI Docs JSON**: `https://stayo-be.onrender.com/v3/api-docs`

*Note: The Swagger UI on Render automatically routes all preflight and action requests through secure HTTPS to match the server environment.*
