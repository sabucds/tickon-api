# Project Context – Concert Ticket Booking Platform

## 1. Project Overview

This project is an enterprise-grade concert ticket booking platform designed as a case study to demonstrate strong system design skills in highly scalable, distributed microservices architectures. The primary goal is to showcase the ability to design and implement a production-style ticketing system with realistic constraints, failure modes, and non-functional requirements, similar in spirit to large platforms like Ticketmaster, but scoped for a single developer portfolio project.

The platform will be built using an AI-first development workflow: large language models help with architecture exploration, coding, refactoring, and documentation, but AI features are not part of the end-user product in the initial version.

---

## 2. Primary Users and Goals

### 2.1 Concert fan / end-user

**Objective:** Buy tickets for high-demand events without crashes, scalping, or confusing flows.

**Business expectations:**

- The system must remain responsive during major on-sale events with large virtual queues.
- Users should experience a clear, fair waiting room/queue and a seamless booking process, including a 10-minute reservation window to complete payment.

### 2.2 Event organizer / promoter

**Objective:** Publish and sell tickets for events with minimal friction and no dependency on a central “super admin” to manage basic event data.

**Business expectations (v1 plus roadmap):**

- In v1, organizers can at least rely on a basic internal admin or scripts to set up events, zones, and capacities (full self-service UI can be a later milestone).
- In later versions, organizers should manage their own events (create/edit events, zones, pricing, capacities) without central intervention.

### 2.3 Operations / support staff

**Objective:** Handle refunds, cancellations, fraud checks, and manual overrides safely, with as much automation as possible.

**Business expectations:**

- For v1, core flows focus on correct reservations and bookings; refunds/cancellations and rich support tooling are treated as future enhancements.
- The system design should make it straightforward to add automated refund workflows and fraud signals in later iterations (event-driven design, clear audit trails).

### 2.4 Platform owner (developer/portfolio)

**Objective:** Demonstrate mastery of system design, microservices patterns, and high-scale architecture, and use this project in interviews and case studies.

**Business expectations:**

- The codebase and documentation should clearly surface architectural decisions (queueing, consistency, data modeling, failure handling).
- The system should be “LLM-friendly”: configuration, structure, and docs optimized so AI coding assistants can navigate and modify it effectively.

---

## 3. Business Scope (v1)

### 3.1 In scope for MVP

**User sign-up/login**

- Basic user accounts (email/password or equivalent) so users can own orders and tickets.
- Simple, secure authentication sufficient for demos and interviews (no need for full identity provider integration initially).

**Event browsing and details**

- Users can browse a list of upcoming events and view details: date, venue, zones, pricing, and availability summaries.
- Performance under load is important, but this is primarily a read-heavy workload that can be cached.

**Real-time waiting room / queue**

- For major on-sale events, all users enter a waiting room.
- A controlled subset of users is admitted into the booking flow at a time to protect core services and databases.
- Queue position and admission state should be visible and updated in near real time.

**Seat selection (specific seats)**

- For seated zones, users can select specific seats (e.g., “A-101”, “A-102”).
- The system must prevent double booking of the same seat and ensure that once a seat is reserved, it is held for a 10-minute window.

**General admission tickets (no specific seats)**

- For general admission zones, tickets are sold from a shared pool without seat assignments.
- The system tracks remaining capacity per zone and must avoid overselling.

**Ticket reservation with time-bound hold and payment**

- When users select seats or GA quantities, the system creates a reservation with a 10-minute hold.
- If the user completes payment within the window, the reservation becomes a confirmed order and tickets are issued.
- If the user fails to pay in time, the reservation expires and tickets are returned to the inventory pool.

### 3.2 Later / out of scope for v1

These capabilities are important for a real product but are explicitly planned as later phases and not initial implementation goals:

**Organizer backoffice UI**

- Full self-service web UI for organizers to create and manage events, zones, and pricing is out of scope for v1.
- v1 may rely on seeded data, scripts, or a simple internal admin endpoint for configuration.

**Refunds and cancellations**

- Automated refund flows, cancellation policies, and customer-initiated cancellations are treated as future features.
- The MVP design should, however, keep data models and events flexible enough to support this later.

**Advanced fraud/bot protections**

- Beyond simple rate limiting and basic protective measures (e.g., CAPTCHAs), sophisticated anti-bot and fraud systems are out of scope for v1.
- The architecture will assume rate limiting, queueing, and simple throttling as baseline protection.

**Analytics dashboards for organizers**

- Rich analytics and dashboards (sales over time, per zone, conversion metrics) will not be implemented in v1.
- Some data collection and event logging can be included to make future analytics straightforward.

---

## 4. Non-functional Goals

These are target characteristics for the system design; the actual implementation may simulate them at smaller scale, but the architecture should be credible at these levels.

### 4.1 Scale and load profile

- The platform targets an estimated 10 million daily active users (DAU) across events.
- It should support 100,000 peak concurrent users in a single high-demand on-sale scenario (queue + browsing combined).
- The expected read/write ratio is approximately 5:1, meaning most traffic is read-heavy (browsing events, viewing availability) with bursts of write-heavy activity around reservations and payments.
- Even if the deployed environment runs at a lower absolute scale, the architecture, data models, and service boundaries must be designed to scale to these targets (e.g., through horizontal scaling, caching, and queueing).

### 4.2 Availability and reliability

- Target availability during critical on-sale windows: at least 99.9% for core booking flows (queue, reservation, payment confirmation).
- The system should degrade gracefully under extreme load:
  - Prefer keeping the waiting room responsive and temporarily pausing new admissions rather than failing existing in-progress sessions.

### 4.3 Latency

- Reservation and booking APIs should aim for p95 latency under 300 ms under expected peak load for admitted users.
- Queue status and availability updates should feel “near real-time” to users (sub-second where possible), but exact strict SLAs can be relaxed, as long as the experience is smooth and predictable.

### 4.4 Consistency and correctness

- The system must never oversell specific seats: a seat can be part of only one confirmed order, with strong consistency and clear conflict handling.
- General admission zones must not oversell from the user’s perspective; strong consistency is required for inventory changes, while analytics can be eventually consistent.
- Reservation expiry and ticket return to inventory must be reliable and auditable (e.g., via background workers and event logs).

### 4.5 Security and compliance (MVP level)

- User data (accounts, orders, tickets) must be stored and transmitted securely (TLS, hashed passwords, etc.).
- Payments are expected to be delegated to a third-party payment service provider (PSP); the platform should avoid storing raw card data and follow common best practices for PCI compliance by design.
- Basic privacy practices (e.g., ability to delete test users, avoid logging sensitive data) should be considered to align with modern expectations like GDPR, at least at a conceptual level.

---

## 5. Architectural Intent (High Level)

This section is to guide AI agents and contributors on the intended architectural style, without going into full LLD detail.

**Architecture style:** Microservices-oriented, domain-driven, with clear bounded contexts such as:

- User/Auth
- Catalog/Events
- Queue/WaitingRoom
- Reservation/Inventory
- Payment/Orders
- Notification/Realtime

**Data storage:**

- Relational database (e.g., PostgreSQL) as the system of record for users, events, reservations, orders, and tickets.
- In-memory data store (e.g., Redis) for high-contention operations and real-time state: queue positions, seat/GA holds, and counters.

**Communication patterns:**

- Synchronous APIs (REST/gRPC) for user-facing flows where immediate feedback is needed.
- Asynchronous event streaming (e.g., Kafka or a similar broker) for state changes (reservations created, expired, payments succeeded) and for future extensibility (analytics, notifications).

**Scalability strategies:**

- Horizontal scaling of stateless services.
- Queue-based admission to protect core booking services and databases.
- Aggressive caching and read replicas for event/catalog reads, with strong consistency focused on the reservation/inventory path.

---

## 6. AI-First Development Workflow

While the product itself does not expose AI features in v1, the development process is explicitly AI-assisted:

- The repository will include clear context documents (such as this file), architecture overviews, and module-level READMEs to help LLM agents generate and refactor code safely.

AI assistants are expected to help with:

- Generating boilerplate for services, schemas, and tests.
- Refactoring code while preserving domain boundaries and invariants.
- Producing documentation, diagrams, and example sequences based on the domain model.

The project will be structured to minimize ambiguity for AI tools: consistent naming, explicit non-functional requirements, and clear statements of “what is in scope vs. out of scope” for each version.
