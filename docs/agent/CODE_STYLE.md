# Code Style (Canonical)

## Comments policy (strict)
- Default: **no comments**.
- Comments are allowed only when they add information that code cannot express:
  - non-obvious business rule / domain rationale
  - security considerations / threat mitigation
  - tricky algorithmic reasoning with invariants
  - workaround for external/system limitation (with link or reference)
- Never comment obvious code, parameters, getters/setters, control flow, or “what this line does”.
- Never add “TODO” comments without an issue/ticket reference.

## Naming over comments
- Prefer descriptive names over comments.
- Prefer small functions over commented blocks.

## Documentation placement
- Public APIs: Javadoc only when the contract is not obvious from types/names.
- Everything else: rely on tests to document behavior.
