# Project Context – Concert Ticket Booking Platform

## 1. Overview

Enterprise‑grade concert ticket booking platform, built as a portfolio project to showcase high‑scale, distributed microservice system design (similar class of problems as Ticketmaster). The app is built with an AI‑first development workflow (LLMs for design, coding, refactoring, docs).

---

## 2. Users and Goals

### Concert fan / end‑user
- Buy tickets for high‑demand events without crashes or confusing flows.
- Clear, fair virtual queue and seamless booking with a 10‑minute reservation window.

### Event organizer / promoter
- Publish and sell tickets without depending on a central “super admin.”
- v1: events/zones may be configured via scripts/internal tools; later: self‑service UI.

### Operations / support (later)
- Eventually handle refunds, cancellations, fraud checks, and overrides safely.
- v1 focuses on correctness of reservations/bookings; operations tooling is future work.

### Platform owner (you)
- Demonstrate mastery of scalable microservices, queues, consistency, and failure handling.
- Keep the project **LLM‑friendly**: clear docs, boundaries, and constraints so AI tools can work effectively.

---

## 3. Business Scope (v1)

### In scope (MVP)

- **Auth & users**  
  - Basic sign‑up/login so users own orders and tickets.

- **Event browsing & details**  
  - List events and show date, venue, zones, pricing, and availability summary (read‑heavy, cacheable).

- **Real‑time waiting room / queue**  
  - Waiting room for big drops; only a controlled subset of users admitted to booking at a time.  
  - Show queue position and admission state with near real‑time updates.

- **Seat selection (specific seats)**  
  - Users can pick exact seats in seated zones (e.g., A‑101).  
  - No double‑booking; seats are held for 10 minutes.

- **General admission (GA)**  
  - Zones sold from a shared pool without seat assignment.  
  - Track remaining capacity per zone; avoid overselling.

- **Reservation + payment**  
  - Create a reservation (seats or GA quantity) with a 10‑minute hold.  
  - On successful payment: confirm order and issue tickets.  
  - On timeout: expire reservation and return tickets to inventory.

### Later / out of scope for v1

- **Organizer backoffice UI** (full self‑service event/zones/pricing management).
- **Refunds and cancellations** (automated flows and customer‑initiated cancellations).
- **Advanced fraud/bot protection** beyond basic rate limiting / simple measures.
- **Organizer analytics dashboards** (sales over time, per zone, etc.).

---

## 4. Non‑functional Goals

- **Scale & load profile**  
  - ~10M DAU, up to 100k peak concurrent users (queue + browsing).  
  - Approx. 5:1 read/write ratio; most traffic is browsing, with write spikes on reservations and payments.

- **Availability & reliability**  
  - Target ≥ 99.9% availability for core booking flows during on‑sale windows.  
  - Under extreme load, prioritize keeping the waiting room responsive and pause new admissions rather than failing in‑flight bookings.

- **Latency**  
  - Reservation/booking APIs: aim for p95 < 300 ms for admitted users under expected peak.  
  - Queue and availability updates should feel near real‑time (sub‑second UX where feasible).

- **Consistency & correctness**  
  - Never oversell specific seats; a seat can belong to only one confirmed order.  
  - GA zones must not oversell from the user’s perspective (strong consistency for inventory; analytics can be eventual).  
  - Reservation expiry and ticket return to inventory must be reliable and auditable.

- **Security & compliance (MVP)**  
  - Secure user data (TLS, hashed passwords, no sensitive data in logs).  
  - Payments via third‑party PSP; avoid storing card data, follow PCI best practices by design.

---

## 5. Architectural Intent

- **Style**: Microservices, domain‑driven, with bounded contexts:  
  - User/Auth, Catalog/Events, Queue/WaitingRoom, Reservation/Inventory, Payment/Orders, Notification/Realtime.

- **Data**:  
  - Relational DB (e.g., PostgreSQL) as system of record for users, events, reservations, orders, tickets.  
  - Redis (or similar) for high‑contention and real‑time state: queues, holds, counters.

- **Communication**:  
  - REST/gRPC for user‑facing synchronous flows.  
  - Event streaming (Kafka or similar) for reservations, expiries, payments, and future analytics/notifications.

- **Scalability**:  
  - Horizontally scale stateless services.  
  - Use queue‑based admission to shield core booking and DBs.  
  - Heavy caching and read replicas for read paths; strong consistency on reservation/inventory path.

---

## 6. AI‑First Development

- Keep this file and related docs updated so LLMs understand the product goals and constraints.  
- Use AI for boilerplate, refactors, tests, and docs, while preserving domain boundaries and invariants.
