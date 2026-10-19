# Google Play internal testing releases

Pushing a new `v*.*.*` tag runs `.github/workflows/android-release.yml`:

1. Run lint and unit tests.
2. Build the signed APK and Android App Bundle (AAB).
3. Retain the AAB and R8 mapping as a GitHub Actions artifact for 14 days, and publish the APK on GitHub Releases.
4. Upload the AAB and mapping to Google Play **internal testing**, with release status `completed`.

The workflow also supports **Run workflow** with an optional version name. It defaults to the highest semantic version tag on HEAD, then the version in `app/build.gradle.kts`. Manual runs publish to internal testing and save the build artifact; they do not create a GitHub Release. Both triggers always use `internal`, with no production option. Concurrent release runs are serialized.

The GitHub release step updates the APK asset when a release already exists, and replaces its retained workflow artifact when the build job is retried.

The Play job depends on the existing GitHub release job. A Play failure leaves the GitHub release available; rerun the failed Play job after fixing credentials or permissions. Do not rerun a successful upload: Google Play cannot reuse a version code for another bundle.

## GitHub Actions secrets

Keep the existing signing secrets:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Add `PLAY_SERVICE_ACCOUNT_JSON` containing the complete JSON private key for the service account authorized to release Lexicardio. The workflow validates the JSON structure and required account fields, then Google Play verifies the account's authentication and application permissions. Its email is not hard-coded in the workflow.

Store the JSON directly as a GitHub Actions repository secret; an email address or base64-encoded JSON is not sufficient. Never commit the JSON, keystore, passwords, or tokens.

## Google Play setup

- The Play Console application must use `com.andongni.lexicardio` and have a first bundle uploaded manually.
- Enable the Google Play Android Developer API in the service account's Google Cloud project.
- Add the service account email in Play Console's Users and permissions, granting access to this application and permission to release apps to testing tracks. Merely creating a Google Cloud service account does not grant Play Console access.
- The CI keystore must match the **upload key certificate** in Play Console's App integrity page. Google Play's app signing key is separate: Google manages the final distribution signature, but the uploaded AAB still requires the registered upload key.
- Any required initial app setup, agreements, or declarations must be completed in Play Console.

The current GitHub release signing certificate SHA-256 is:

`A7:71:A9:60:DF:CA:94:B8:92:C5:F7:2A:AE:C3:AD:9D:83:3B:3F:8E:59:E2:6C:37:AD:85:76:C9:48:60:7A:F1`

The Play upload certificate has been confirmed to match this CI certificate (both SHA-256 above and SHA-1 `A6:D6:80:3C:D4:84:4F:CF:19:4B:A4:B2:EF:85:C2:71:55:42:45:45`). The existing signing secrets can be reused. Google manages a different app signing key for distributed installations; do not replace the upload keystore with that key.


## Versioning and triggering

`versionName` comes from the tag without `v`. `versionCode` comes from this workflow's `GITHUB_RUN_NUMBER`. The last known Play version code is 4, and the existing workflow has already reached run 14, so the next run will exceed 4. Continue using the same workflow file and keep its version codes above every bundle already uploaded to Play, including other tracks.

Use a **new** tag on a commit containing this workflow, for example the next release version. The existing `v0.15.0` tag does not contain Play publishing and must not be moved or recreated. A tag's workflow comes from its tagged commit, so tagging an older branch will not activate this setup.

Internal testing status `completed` activates the release in the internal track; it does not publish to production. Google may still require Console actions or processing before testers receive it. Tester membership and the opt-in link remain managed in Play Console.
