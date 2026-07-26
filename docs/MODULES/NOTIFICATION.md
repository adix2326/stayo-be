# Notification Module

## Business Purpose
The Notification module handles communication with the user. Currently, its primary responsibility is sending SMS via Twilio (such as OTPs) and eventually managing in-app notifications.

## Responsibilities
- Integrate with external SMS providers (Twilio).
- Send OTPs for authentication.
- Track unread in-app notification counts.

## Folder Structure
```text
com.stayo.stayo.notification
├── service/
│   ├── NotificationService.java
│   └── SmsService.java
```

## Key Components

### `SmsService`
- Reads configuration properties (`twilio.account-sid`, `twilio.auth-token`, `twilio.phone-number`) from `application.properties`.
- Exposes a `sendSms(String to, String message)` method.
- Currently invoked by the `OtpService` in the Auth module to physically dispatch the OTP code.

### `NotificationService`
- Currently provides stubbed/mock methods like `getUnreadCount(userId)` which is consumed by the `DashboardService`.

## Future Improvements
- Implement a `Notification` entity to persist in-app alerts (e.g., "Booking Confirmed", "Rent Due").
- Integrate Firebase Cloud Messaging (FCM) to push real-time alerts to the user's device.
