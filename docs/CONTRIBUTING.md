# Contributing
1. Fork, branch from `main` (`feat/<thing>` or `face/<id>`).
2. New watch face: copy `watchfaces/mono_digital`, rename package to `com.chrona.watchfacepush.<id>`, add it to `settings.gradle.kts`, add a `WatchFaceItem` to `shared/.../Catalog.kt`, add a `FacePreview` drawing branch if it's a new category.
3. Run `./gradlew lint testDebugUnitTest` and, for faces, `scripts/build_faces.sh`.
4. PR description: screenshots of phone + watch, and the validator output for any face touched.
