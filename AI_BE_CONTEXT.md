# StayO Backend AI Context

Version: 1.0

This document is the single source of truth for all backend development of StayO.

Every AI-generated code change must comply with the standards defined here.

---

# Project Overview

Project Name

StayO

StayO is a PG Discovery Platform built exclusively for Paying Guest accommodations.

StayO does NOT support

Apartments

Hostels

Hotels

Villas

Rental Homes

Flats

Co-living

Everything in the backend should revolve around PGs.

---

# Product Vision

StayO enables users to

Discover PGs

Compare PGs

Save PGs

Book PGs

Track Bookings

Receive Notifications

The platform should eventually become India's most trusted PG discovery platform.

---

# Technology Stack

Language

Java 25

Framework

Spring Boot

Database

MongoDB

Authentication

JWT

OTP Login

Build Tool

Maven

Documentation

Swagger/OpenAPI

Testing

JUnit

Mockito

Future

Redis

Firebase

ElasticSearch

---

# Architecture

Architecture Style

Modular Monolith

Reason

Easy Deployment

Fast Development

Low Infrastructure Cost

Excellent Maintainability

Modules should communicate only through Services.

Repositories should never be accessed outside their module.

---

# Module Structure

auth

user

dashboard

content

property

search

wishlist

notification

shared

common

config

Every module owns

Controller

Service

DTO

Entity

Repository

Mapper

Validator

---

# Coding Principles

Follow

SOLID

DRY

KISS

YAGNI

Clean Code

Clean Architecture

Never duplicate business logic.

Keep methods short.

Prefer composition over inheritance.

---

# Naming Conventions

Controllers

UserController

PropertyController

DashboardController

Services

UserService

DashboardService

Repositories

UserRepository

DTOs

UserResponseDTO

UserRequestDTO

Entities

User

Property

Notification

Never abbreviate names.

---

# API Response

Every API should return

{
success

message

data

timestamp
}

Never return entities directly.

Always use DTOs.

---

# Controller Standards

Controllers should

Validate request

Call service

Return response

Nothing else.

Never place business logic inside controllers.

---

# Service Standards

Service layer contains

Business Logic

Validation

Orchestration

Services may communicate with other services.

Services must never communicate with repositories of another module.

---

# Repository Standards

Repositories only interact with MongoDB.

No DTO creation.

No business logic.

---

# MongoDB Standards

Collections

users

properties

wishlist

notifications

banner

categories

Never embed extremely large documents.

Use references when appropriate.

Index frequently searched fields.

---

# Dashboard Module

Purpose

Aggregate dashboard data.

Dashboard should internally call

ContentService

PropertyService

WishlistService

SearchService

Dashboard must expose only aggregated DTOs.

Dashboard must never expose internal entities.

---

# Property Module

Everything here represents PGs.

No generic property types.

Property contains

Basic Information

Amenities

Pricing

Availability

Images

Verification

Nearby Colleges

Nearby Companies

Future

Room Availability

Beds

Occupancy

---

# Search Module

Responsibilities

Search PG

Autocomplete

Popular Areas

Budget Search

Nearby Search

Filters

Sort

Pagination

Future

Elastic Search

---

# Wishlist Module

Responsibilities

Add

Remove

Exists

Count

Listing

Nothing else.

---

# Notification Module

Responsibilities

Notifications

Unread Count

Mark Read

Delete

Future

Firebase Push

---

# User Module

Responsibilities

Profile

Preferences

Profile Completion

Profile Picture

Saved Searches

Recently Viewed

Future

Referral

---

# Authentication

Login

OTP

JWT

Refresh Token

Role

USER

OWNER

ADMIN

Future

Google Login

Apple Login

---

# Validation

Always use

@NotBlank

@NotNull

@Pattern

@Email

@Size

Never manually validate inside controllers.

---

# Exception Handling

Global Exception Handler.

Create custom exceptions.

Examples

UserNotFoundException

PropertyNotFoundException

WishlistException

OTPException

SearchException

---

# Logging

Use SLF4J.

Never use System.out.println().

Log

Authentication

Errors

Warnings

Business Events

---

# API Naming

GET

/api/dashboard

GET

/api/property/{id}

GET

/api/search

POST

/api/wishlist

DELETE

/api/wishlist/{id}

PUT

/api/user/profile

Naming must remain RESTful.

---

# Performance Goals

Dashboard

<150ms

Search

<150ms

Wishlist

<80ms

Profile

<50ms

Property Details

<120ms

Notification Count

<50ms

---

# Caching

Future Redis

Dashboard

Banner

Categories

Popular Areas

Amenities

Search Suggestions

---

# Security

JWT

Refresh Token

Role Based Authorization

Rate Limiting

Request Validation

CORS

Secure Headers

---

# Future Modules

Booking

Review

Owner

Admin

Analytics

Payments

Support

Referral

Do NOT implement unless required.

---

# AI Development Rules

Whenever implementing new features:

Understand the existing module before writing code.

Never create duplicate logic.

Never create duplicate DTOs.

Never create duplicate repositories.

Reuse existing utilities whenever possible.

Maintain modular boundaries.

Always search the project before creating a new class.

Follow existing naming conventions.

Avoid introducing breaking API changes.

Never hardcode business data.

Use configuration files for constants where appropriate.

Generate production-quality code with proper validation, logging, exception handling, and documentation.

Ensure the project builds successfully after every change.

Maintain consistency with the existing architecture and coding style.

The objective is long-term maintainability, readability, scalability, and clean modular design.