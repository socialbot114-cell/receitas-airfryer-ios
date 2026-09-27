# Receitas Airfryer iOS

Native SwiftUI foundation for App Store ID `6813681707`.

- Bundle identifier: `br.com.receitasairfyer`
- Offline JSON catalog generated from Android `RecipeCatalog.kt`
- 300 recipes, 15 guide entries, favorites in `UserDefaults`
- Home, Discover, Favorites, Profile, recipe detail, and background-resilient cooking timers
- Native iPhone and iPad layouts with Dynamic Type and VoiceOver support
- No network permission, account, analytics, or embedded credentials

## Generate and test on macOS

```bash
xcodegen generate
xcodebuild test -project ReceitasAirfryer.xcodeproj -scheme ReceitasAirfryer -destination 'platform=iOS Simulator,name=iPhone 15'
```

Linux can run `python3 Scripts/validate_ios.py`, but cannot compile Swift or
run XcodeGen/Xcodebuild. GitHub Actions generates the project and runs the full
`xcodebuild test` suite on an iOS Simulator.

## CI, screenshots, and TestFlight

The checked-in release path is GitHub Actions (the workflows below are manually
dispatched on the selected branch):

- `Airfryer iOS Screenshots` runs `xcodegen generate` and the complete
  `xcodebuild test` suite on macOS simulators (iPhone 17 and iPad Air 13-inch
  M4). UI-test screenshots are exported from the `.xcresult` attachments; the
  attachment manifest maps the generated filenames to screen names.
- `Receitas iOS TestFlight` signs and archives the app, exports and validates
  the IPA, then uploads it to App Store Connect using the configured API key.
  The release workflow currently sets marketing version `1.0.1`; its build
  number is `GITHUB_RUN_NUMBER`. Update the workflow's marketing version for a
  future release.

A successful upload means Apple accepted the binary for processing; it does
not mean the build is immediately available in TestFlight or published on the
App Store. After processing completes, select the build for the corresponding
App Store version and submit that version separately for App Review. The
checked-in pipeline uploads to TestFlight; it does not publish to the public
App Store.
