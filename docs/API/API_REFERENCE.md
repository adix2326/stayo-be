# API Reference

This document outlines the available REST API endpoints for the StayO backend. All responses (except file uploads/health checks) are wrapped in a standard `ApiResponse` format.

## Authentication
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/auth/otp/send` | Send OTP to mobile number | No |
| `POST` | `/api/auth/otp/verify` | Verify OTP and login/signup | No |
| `PUT`  | `/api/auth/update-details` | Update user details (name, email) | Yes (Token) |
| `POST` | `/api/auth/logout` | Logout user and blacklist token | Yes (Token) |

## User & Profile
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `GET`  | `/api/users/me` | Get current basic user details | Yes (Token) |
| `GET`  | `/api/user/profile` | Get full user profile | Yes (Token) |
| `PUT`  | `/api/user/profile` | Update user profile | Yes (Token) |
| `POST` | `/api/user/profile/image` | Upload profile image (multipart/form-data) | Yes (Token) |
| `DELETE` | `/api/user/profile/image` | Delete profile image | Yes (Token) |

## Dashboard
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `GET`  | `/api/user/dashboard` | Get user dashboard data (aggregates banners, categories, etc.) | Yes (Token) |

## Properties / Search
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `GET`  | `/api/properties/search` | Search and filter properties with pagination | Optional/Yes |

## Wishlist
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/wishlist/add/{propertyId}` | Add property to user wishlist | Yes (Token) |
| `POST` | `/api/wishlist/remove/{propertyId}` | Remove property from user wishlist | Yes (Token) |
| `GET`  | `/api/wishlist` | Get user's wishlist properties | Yes (Token) |

## System
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `GET`  | `/health` | Health Check Ping | No |
