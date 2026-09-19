# Receitas Airfryer iOS

Native SwiftUI foundation for App Store ID `6813681707`.

- Bundle identifier: `br.com.receitasairfyer`
- Offline JSON catalog generated from Android `RecipeCatalog.kt`
- 31 recipes, 15 guide entries, favorites in `UserDefaults`
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
