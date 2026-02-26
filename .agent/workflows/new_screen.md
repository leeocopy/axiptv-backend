---
description: How to create a new screen following skill_2.md spec
---

Always follow `skill_2.md` as single source of truth.

## Steps

1. Read `skill_2.md` § 6 for the target screen spec.
2. Create the screen file under `ui/screens/<screen_name>/`.
3. Apply the design tokens from § 1 (colors, typography, spacing, radius).
4. Implement TV focus states per § 2 (scale 1.08, glow ring, left accent bar).
5. Add TV safe margins (48 dp horizontal, 27 dp vertical).
6. Register the route in `AppNavGraph.kt` using the sealed `Screen` class (§ 9 Navigation).
7. Verify against the Acceptance Criteria in § 10.
