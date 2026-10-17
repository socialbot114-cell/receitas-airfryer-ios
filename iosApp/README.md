# Receitas Airfryer iOS

Native SwiftUI foundation for App Store ID `6813681707`.

- Bundle identifier: `br.com.receitasairfyer`
- Offline JSON catalog generated from Android `RecipeCatalog.kt`
- 300 recipes, 15 guide entries, favorites in `UserDefaults`
- Home with daily rotating suggestions, Discover, Favorites, Profile, cooking guide, recipe detail
- Cooking sessions and step timers that restore after app relaunch and alert with local notifications
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
  The release workflow sets marketing version `1.2.0`; its build number is
  `GITHUB_RUN_NUMBER`.
- `iOS CI` can also trigger the connected Xcode Cloud workflow after its
  simulator tests pass. Manually dispatch it with `run_xcode_cloud` enabled;
  if more than one Xcode Cloud workflow is enabled, provide its exact name.
  This requires the app and GitHub repository to be connected in Xcode Cloud
  and the App Store Connect API credentials already stored in GitHub Actions.
  For a new app connection, create the first workflow in Xcode via
  **Product > Xcode Cloud > Create Workflow**, grant it access to this GitHub
  repository, and enable App Store Connect/TestFlight distribution. Apple’s
  [first workflow guide](https://developer.apple.com/documentation/xcode/configuring-your-first-xcode-cloud-workflow)
  covers that one-time setup.
  Xcode Cloud runs `ci_scripts/ci_post_clone.sh` to generate the Xcode project
  with the Apple team ID supplied by Xcode Cloud for automatic signing. The
  trigger verifies the selected workflow, branch, source commit, and resulting
  TestFlight build before the job succeeds. Its version and build number come
  from `project.yml`; increment `CURRENT_PROJECT_VERSION` before requesting
  another Xcode Cloud distribution.

A successful upload means Apple accepted the binary for processing; it does
not mean the build is immediately available in TestFlight or published on the
App Store. After processing completes, select the build for the corresponding
App Store version and submit that version separately for App Review. The
checked-in pipeline uploads to TestFlight; it does not publish to the public
App Store.
