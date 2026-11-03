
# Ticket Master Database Documentation

## 1. Project Information

- **Project:** Ticket Master Backend
- **Group:** FF5
- **Database:** MariaDB
- **Database name:** `ticketmaster`
- **Database owner:** Ian Kabambe
- **Backend framework:** Spring Boot
- **Containerization:** Docker
- **Character set:** `utf8mb4`
- **Storage engine:** InnoDB

## 2. Purpose

The Ticket Master database supports event management, ticket pricing and capacity, customer accounts, customer interests, orders, payments, digital tickets, and attendance reporting.

The database is shared by the five backend modules. Each team member should coordinate changes to shared entities and foreign keys with the database owner.

## 3. Tables

### 3.1 `users`

Stores account and authentication information.

| Column | Purpose |
|---|---|
| `id` | Primary key |
| `full_name` | User's full name |
| `email` | Unique login email |
| `password_hash` | Hashed password |
| `role` | CUSTOMER, ORGANIZER, VALIDATOR, or ADMIN |
| `created_at` | Account creation timestamp |

### 3.2 `customer_profiles`

Stores additional customer information. `user_id` is both the primary key and a foreign key to `users.id`.

Includes phone, city, latitude, and longitude.

### 3.3 `interests`

Stores available interest categories, such as Music, Sports, Tech, and Community.

### 3.4 `customer_interests`

Associates customers with their selected interests. The composite primary key is (`customer_id`, `interest_id`).

### 3.5 `events`

Stores event information, including its organizer, title, description, category, venue, location, coordinates, start and end times, and status.

Allowed statuses:

- `DRAFT`
- `PUBLISHED`
- `CANCELLED`

New events must start as `DRAFT`. Only the event's organizer should be allowed to edit, publish, or cancel that event.

### 3.6 `ticket_types`

Stores ticket options for an event, such as Regular and VIP.

Important fields include:

- `price`: ticket price.
- `total_quantity`: maximum capacity for this ticket type.
- `quantity_reserved`: quantity held for pending orders.
- `quantity_sold`: quantity allocated to paid orders.

The remaining quantity is calculated as:

`total_quantity - quantity_reserved - quantity_sold`

Capacity changes must be performed inside transactions with appropriate row locking to prevent overselling.

### 3.7 `orders`

Stores customer orders and their total amounts.

Allowed statuses:

- `PENDING`
- `PAID`
- `FAILED`
- `CANCELLED`

An order belongs to a customer through `customer_id`.

### 3.8 `order_items`

Stores the ticket types and quantities included in an order, including the unit price recorded at purchase time.

### 3.9 `payments`

Stores payment results, amounts, methods, unique references, and timestamps. The current schema permits one payment record per order because `order_id` is unique.

### 3.10 `tickets`

Stores individual digital tickets, including a unique ticket code, owner, ticket type, order, issue time, and validation details.

Allowed statuses:

- `VALID`
- `USED`
- `CANCELLED`

`used_at` records when a ticket is checked in, and `validated_by` identifies the validating user.

## 4. Entity Relationships

- One user can organize many events.
- One user can have one customer profile.
- Customers and interests have a many-to-many relationship through `customer_interests`.
- One event can have many ticket types.
- One customer can place many orders.
- One order can contain many order items.
- One ticket type can appear in many order items.
- One order can have at most one payment record under the current schema.
- One order can issue many digital tickets.
- One ticket type can correspond to many digital tickets.
- One user can own many tickets.

## 5. Naming Conventions

1. Use lowercase `snake_case` for table and column names.
2. Use plural nouns for table names, such as `events` and `orders`.
3. Use `id` as the primary key column name for entities.
4. Use `<entity>_id` for foreign keys, such as `organizer_id`.
5. Use `BIGINT AUTO_INCREMENT` for numeric entity identifiers.
6. Use `DECIMAL(12,2)` for monetary values.
7. Use `DATETIME(6)` for timestamps.
8. Use `utf8mb4` character encoding and InnoDB tables.
9. Define foreign keys for relationships between tables.
10. Use explicit status values and validate transitions in the service layer.

## 6. Data Integrity and Business Rules

- Emails and ticket codes must be unique.
- Passwords must be stored as secure hashes, never as plain text.
- Event end time must be after its start time.
- Ticket prices must not be negative.
- Ticket capacity must be positive.
- Reserved and sold quantities must not exceed total capacity.
- Published event capacity must not be reduced below tickets already sold or reserved.
- Only the event's organizer can edit, publish, or cancel it.
- Only paid orders should produce digital tickets.
- A ticket can be checked in only once.
- Sales reports should use paid orders; attendance reports should use tickets marked `USED`.
- Capacity reservation and release must be transactional and safe under concurrent requests.

## 7. Database Setup

The database is run through Docker Compose using the MariaDB image.

To list tables:

```sql
USE ticketmaster;
SHOW TABLES;
```

To inspect the events table:

```sql
DESCRIBE events;
```

The current Spring Boot datasource should use:

- URL: `jdbc:mariadb://localhost:3306/ticketmaster`
- Driver: `org.mariadb.jdbc.Driver`

## 8. Schema Change Process

Because this is a shared database:

1. Discuss proposed table or column changes with the affected team member.
2. Update the schema and this document together.
3. Review foreign keys and entity mappings.
4. Test the changes against MariaDB.
5. Submit the changes through a feature branch and pull request.

## 9. Important Implementation Note

This document describes the intended database design and business rules. The database constraints alone do not implement all authorization, order transitions, ticket issuance, or concurrency requirements. Those must also be enforced in the Spring Boot services and security configuration.
