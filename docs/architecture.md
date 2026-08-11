# System Architecture

## High-Level Architecture

```text
                    ┌─────────────────┐
                    │   React Client  │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │  Spring Boot    │
                    │     Backend     │
                    └───────┬─────────┘
                            │
             ┌──────────────┼──────────────┐
             │              │              │
             ▼              ▼              ▼
       PostgreSQL       AI Service       AWS APIs
                         │
                         ▼
                       Qdrant
                         │
                         ▼
                         LLM