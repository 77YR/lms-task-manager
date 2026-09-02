# LMS Task Manager

An Android app (Kotlin, Jetpack Compose) for tracking coursework, assignments, and schedule in one place — built as a personal tool during UTD's transition from Blackboard to Canvas.

## Status: In Progress

The core app — task board, course tracking, scheduling, persistence, and secure credential storage — is functional. **LMS integration (the original goal) is blocked**, currently walled by outside factors.

## What's Built

- **Task board** with drag-and-reorder Kanban-style UI (Compose), backed by Room for local persistence
- **Course and schedule tracking**, with per-course color coding and a weekly schedule view
- **Encrypted credential storage** (`TokenManager`) using AndroidX Security Crypto (AES256-GCM/SIV) — built ahead of the OAuth integration it's meant to support, so the storage layer is ready the moment API access is
- **Layered architecture**: clean separation across `model`, `repository`, `database` (Room entities/DAOs), `settings`, `navigation`, and `ui` — built to make swapping the current mock data source for a real API client a contained change, not a rewrite

## Why LMS Integration Isn't Live

This app was started during UTD's Blackboard → Canvas migration, with the goal of pulling real assignments/due dates via the Canvas API. I contacted UTD IT about API access; the response was noncommittal ("maybe, likely not") and no key was made available. Rather than block the whole project on institutional access I didn't control, I built the app around a `DataSource` abstraction (`LOCAL`, `BLACKBOARD`, `CANVAS`) and encrypted token infrastructure so that real integration can be dropped in later without touching the rest of the app. Currently, `TaskRepository` runs on seeded mock data standing in for that future API response.

## Stack

Kotlin, Jetpack Compose, Room, AndroidX Security Crypto, Navigation Compose.

## If API Access Opens Up

The integration point is isolated: implement a Canvas API client behind the existing `DataSource` abstraction, wire `TokenManager` into a real OAuth flow, and replace the mock seed in `TaskRepository`/`DatabaseManager` with live API responses. No architectural changes needed elsewhere.
