
# Ticket Master Entity Relationship Diagram (ERD)

The diagram below shows the main entities in the Ticket Master database and their relationships.

```mermaid
erDiagram
    USERS ||--o| CUSTOMER_PROFILES : has
    USERS ||--o{ EVENTS : organizes
    USERS ||--o{ ORDERS : places
    USERS ||--o{ CUSTOMER_INTERESTS : selects
    INTERESTS ||--o{ CUSTOMER_INTERESTS : includes
    EVENTS ||--o{ TICKET_TYPES : offers
    ORDERS ||--|{ ORDER_ITEMS : contains
    TICKET_TYPES ||--o{ ORDER_ITEMS : purchased_as
    ORDERS ||--o| PAYMENTS : has
    ORDERS ||--o{ TICKETS : issues
    TICKET_TYPES ||--o{ TICKETS : classifies
    USERS ||--o{ TICKETS : owns
    USERS o|--o{ TICKETS : validates

    USERS {
        BIGINT id PK
        VARCHAR full_name
        VARCHAR email UK
        VARCHAR password_hash
        ENUM role
        DATETIME created_at
    }

    CUSTOMER_PROFILES {
        BIGINT user_id PK, FK
        VARCHAR phone
        VARCHAR city
        DECIMAL latitude
        DECIMAL longitude
    }

    INTERESTS {
        BIGINT id PK
        VARCHAR name UK
    }

    CUSTOMER_INTERESTS {
        BIGINT customer_id PK, FK
        BIGINT interest_id PK, FK
    }

    EVENTS {
        BIGINT id PK
        BIGINT organizer_id FK
        VARCHAR title
        VARCHAR category
        VARCHAR venue
        VARCHAR location
        DATETIME start_time
        DATETIME end_time
        ENUM status
    }

    TICKET_TYPES {
        BIGINT id PK
        BIGINT event_id FK
        VARCHAR name
        DECIMAL price
        INT total_quantity
        INT quantity_reserved
        INT quantity_sold
    }

    ORDERS {
        BIGINT id PK
        BIGINT customer_id FK
        ENUM status
        DECIMAL total_amount
        DATETIME created_at
    }

    ORDER_ITEMS {
        BIGINT id PK
        BIGINT order_id FK
        BIGINT ticket_type_id FK
        INT quantity
        DECIMAL unit_price
    }

    PAYMENTS {
        BIGINT id PK
        BIGINT order_id FK, UK
        DECIMAL amount
        VARCHAR method
        ENUM status
        VARCHAR reference UK
    }

    TICKETS {
        BIGINT id PK
        BIGINT order_id FK
        BIGINT ticket_type_id FK
        BIGINT owner_id FK
        BIGINT validated_by FK
        VARCHAR ticket_code UK
        ENUM status
        DATETIME issued_at
        DATETIME used_at
    }
```

## Notes

- `PK` means primary key.
- `FK` means foreign key.
- `UK` means unique key.
- A customer profile is optional, but each profile belongs to one user.
- Each event has one organizer.
- Each ticket type belongs to one event.
- An order may have zero or one payment record in the current design.
- A ticket's `validated_by` field is optional until a validator checks the ticket.
