# AI Context Guidelines

Because StayO heavily utilizes AI-assisted development, strict boundaries and contexts have been established to prevent hallucination, generic boilerplate generation, or architectural drift.

## The Context Files
There are two critical files located in the root of the project repositories. **These files serve as the absolute Single Source of Truth for any AI agent interacting with the codebase.**

1. `stayo-be/AI_BE_CONTEXT.md` (Backend)
2. `stayO-Frontend/stayo-fe/AI_FE_CONTEXT.md` (Frontend)

## Rules for AI Generation
Whenever an AI (or developer) implements a new feature, they must abide by the rules defined in those contexts:

1. **No Duplication**: The AI must search the project for existing implementations, components, DTOs, or Services before creating new ones.
2. **Strict Domain Enforcement**: The AI must remember that StayO is exclusively for **PGs (Paying Guests)**. It must never hallucinate features for Apartments, Hotels, or generic rentals.
3. **Architectural Purity**: The AI must maintain the Modular Monolith boundaries in the backend, and the Feature-based folder structure in the frontend.
4. **Mocking Restrictions**: The AI should avoid hardcoding business data (like lists of cities or amenities) into the frontend; it should architect solutions to fetch these from the backend API.
5. **Quality**: The AI must generate production-quality code complete with exception handling, logging, validation, and adherence to the StayO Design System (Tailwind colors/spacing).

*Note: If an AI proposes an architectural shift that violates the Context files, the PR/Code should be rejected unless the Context file is explicitly updated to reflect the new paradigm.*
