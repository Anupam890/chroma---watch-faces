# Future Enhancements

Prioritised backlog for Chrona. Items marked **(infra)** need a backend; everything else is client-only.

## P0 — ship-blockers after first build
1. **Validation tokens at runtime** — load `assets/faces/tokens.json` in `FaceRepository` instead of hard-coding in `Catalog.kt`.
2. **Remote catalog (infra)** — Ktor/Retrofit client + Room cache; `FaceRepository.faces` becomes `network → db → UI`. Catalog JSON schema = `WatchFaceItem`.
3. **CDN APK download** — fetch face APK on demand with resume support, cache by SHA-256, honour the "large faces on Wi-Fi only" setting.
4. **Instrumented Data Layer tests** — two-emulator test using `WearableListenerService` fakes.

## P1 — product
5. **In-app customiser** — edit `UserConfigurations` from the phone (Watch Face Push exposes `WatchFacePushManager` editing intents on Wear OS 6; mirror the Flavor picker and theme chips in `FaceDetailScreen`).
6. **Photo picker for Memories** — phone-side photo selection, crop to 450×450, transfer via `DataClient` asset and apply through the WFF `PhotosConfiguration`.
7. **Premium / IAP** — Google Play Billing for `isPremium` faces; entitlement cached in DataStore and checked before transfer.
8. **Face packs & collections** — curated bundles ("Monochrome Week"), seasonal drops, "new this week" rail on Discover.
9. **Deep links & sharing** — `chrona://face/<id>` already wired; add App Links + share sheet with preview image.
10. **Wear OS 4/5 fallback flow** — per-face Play Store deep link button and a "Request this face on Play" telemetry event.

## P2 — platform surfaces
11. **Tile** — "Switch face" tile on the watch cycling through installed Chrona faces (ProtoLayout).
12. **Complication data source** — Chrona battery/steps complication styled to match the faces.
13. **Phone home-screen widget** — Glance widget showing the active face and a one-tap switch.
14. **Watch-side browsing** — thumbnail carousel of the catalog on the watch, requesting install from the phone over `MessageClient`.

## P3 — faces
15. More categories: **Information** (dense 6-slot dashboard), **Art** (generative shapes driven by `[STEP_COUNT]`), **Minimal Analog**.
16. WFF v4 **colour transforms** for time-of-day tinting; **Reference** elements to de-duplicate transforms.
17. Localised strings for every face (`values-xx/strings.xml`).
18. Accessibility: `screenReaderText` on all configurations; high-contrast Flavors.

## P4 — engineering
19. Convention plugins (`build-logic/`) to remove duplicated Gradle config across face modules.
20. Baseline profiles + Macrobenchmark for Discover grid scroll.
21. Crash/analytics (Firebase or self-hosted) with install success-rate dashboards.
22. Fastlane lanes: `faces` (build+validate), `beta` (internal track), screenshots.
23. Renovate for dependency updates; detekt + ktlint in CI.

---

## Origin prompt
This project was generated from the following request (kept verbatim as a record of intent and so the next iteration can be regenerated or extended with the same brief):

> create end to end application that can be synced phone and watch too create everything from scratch to advanced after that create a and pload to github I will share at the lst make the app in kotlin and make good design and app logo too everything and in the feature enhancements add that prompt also

Interpreted scope: Kotlin phone + Wear OS watch apps that sync over the Data Layer, a catalog of watch faces across categories with customisation, polished design and an original logo, repo ready to push to GitHub, and this enhancement list including the original prompt.

### Suggested follow-up prompt for the next iteration
> Using the Chrona repo, implement P0 items 1–3: load validation tokens from assets, add a remote catalog with Room caching behind FaceRepository, and download face APKs from a CDN with resume + SHA-256 verification. Keep the existing UI and Data Layer contract unchanged.
