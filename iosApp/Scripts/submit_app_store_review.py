#!/usr/bin/env python3
"""Inspect or submit the configured iOS version to App Review."""

from __future__ import annotations

import json
import os
import sys
import time
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode
from urllib.request import Request, urlopen

import jwt


API_ROOT = "https://api.appstoreconnect.apple.com/v1"
APP_ID = "6813681707"
BUNDLE_ID = "br.com.receitasairfyer"
ACTIVE_SUBMISSION_STATES = {
    "READY_FOR_REVIEW",
    "WAITING_FOR_REVIEW",
    "IN_REVIEW",
    "UNRESOLVED_ISSUES",
    "CANCELING",
    "COMPLETING",
}
VERSION_LOCALIZATION_FIELDS = (
    "description",
    "keywords",
    "marketingUrl",
    "promotionalText",
    "supportUrl",
    "whatsNew",
)
RELEASE_NOTES = {
    "pt-BR": (
        "Timer com notificação mesmo com o app fechado ou a tela bloqueada. "
        "Guia rápido com tempos e temperaturas para 15 alimentos. "
        "Sugestões de receitas diferentes a cada dia. "
        "Agora as receitas mostram calorias estimadas, dificuldade, pré-aquecimento, "
        "se precisa virar e o tempo de cada passo."
    ),
    "en-US": (
        "Timer notifications now work with the app closed or the screen locked. "
        "Added a quick guide with times and temperatures for 15 foods, daily recipe "
        "suggestions, and more recipe details including estimated calories, difficulty, "
        "preheating, turning, and step timing."
    ),
}


def make_token() -> str:
    required = ("ASC_KEY_ID", "ASC_ISSUER_ID", "ASC_P8")
    missing = [name for name in required if not os.environ.get(name)]
    if missing:
        raise RuntimeError(f"Missing GitHub Actions values: {', '.join(missing)}")
    now = int(time.time())
    return jwt.encode(
        {
            "iss": os.environ["ASC_ISSUER_ID"],
            "iat": now,
            "exp": now + 15 * 60,
            "aud": "appstoreconnect-v1",
        },
        os.environ["ASC_P8"],
        algorithm="ES256",
        headers={"kid": os.environ["ASC_KEY_ID"], "typ": "JWT"},
    )


def api_request(token: str, path: str, method: str = "GET", body: dict | None = None) -> dict:
    data = json.dumps(body).encode() if body is not None else None
    request = Request(
        path if path.startswith("https://") else f"{API_ROOT}{path}",
        data=data,
        method=method,
        headers={
            "Authorization": f"Bearer {token}",
            "Accept": "application/json",
            "Content-Type": "application/json",
        },
    )
    try:
        with urlopen(request, timeout=30) as response:
            return json.load(response)
    except HTTPError as error:
        detail = error.read().decode("utf-8", errors="replace")
        try:
            messages = [item.get("detail", "") for item in json.loads(detail).get("errors", [])]
        except (json.JSONDecodeError, AttributeError):
            messages = []
        reason = "; ".join(message for message in messages if message) or "Apple returned an API error"
        raise RuntimeError(f"App Store Connect returned HTTP {error.code}: {reason}") from None
    except URLError as error:
        raise RuntimeError(f"Could not reach App Store Connect: {error.reason}") from None


def list_pages(token: str, path: str) -> list[dict]:
    items: list[dict] = []
    next_url: str | None = path
    while next_url:
        response = api_request(token, next_url)
        items.extend(response.get("data", []))
        next_url = response.get("links", {}).get("next")
    return items


def app_store_versions(token: str) -> list[dict]:
    query = urlencode({"limit": 200, "include": "build"})
    return list_pages(token, f"/apps/{APP_ID}/appStoreVersions?{query}")


def app_store_version(versions: list[dict], version_string: str) -> dict | None:
    matches = [
        version
        for version in versions
        if version.get("attributes", {}).get("platform") == "IOS"
        and version.get("attributes", {}).get("versionString") == version_string
    ]
    if len(matches) != 1:
        if matches:
            raise RuntimeError(f"App Store Connect returned multiple iOS versions {version_string}.")
        return None
    return matches[0]


def version_number(version_string: str) -> tuple[int, ...]:
    try:
        return tuple(int(part) for part in version_string.split("."))
    except ValueError:
        raise RuntimeError(f"App Store version {version_string!r} is not numeric.") from None


def latest_ready_version(versions: list[dict], target: str) -> dict:
    candidates = [
        version
        for version in versions
        if version.get("attributes", {}).get("platform") == "IOS"
        and version.get("attributes", {}).get("appStoreState") == "READY_FOR_SALE"
    ]
    if not candidates:
        raise RuntimeError("No iOS version in Ready for Distribution state is available to copy metadata from.")
    source = max(candidates, key=lambda item: version_number(item["attributes"]["versionString"]))
    if version_number(target) <= version_number(source["attributes"]["versionString"]):
        raise RuntimeError(
            f"Target version {target} must be newer than the latest released version "
            f"{source['attributes']['versionString']}."
        )
    return source


def create_app_store_version(token: str, version_string: str, source: dict) -> dict:
    source_attributes = source.get("attributes", {})
    attributes: dict = {"platform": "IOS", "versionString": version_string}
    for key in ("copyright", "releaseType", "usesIdfa"):
        if source_attributes.get(key) is not None:
            attributes[key] = source_attributes[key]
    created = api_request(
        token,
        "/appStoreVersions",
        method="POST",
        body={
            "data": {
                "type": "appStoreVersions",
                "attributes": attributes,
                "relationships": {"app": {"data": {"type": "apps", "id": APP_ID}}},
            }
        },
    )
    return created["data"]


def version_localizations(token: str, app_store_version_id: str) -> list[dict]:
    query = urlencode(
        {
            "limit": 200,
            "include": "appScreenshotSets",
            "fields[appStoreVersionLocalizations]": ",".join(("locale", *VERSION_LOCALIZATION_FIELDS, "appScreenshotSets")),
        }
    )
    return list_pages(token, f"/appStoreVersions/{app_store_version_id}/appStoreVersionLocalizations?{query}")


def ensure_version_localizations(token: str, target_version: dict, source_version: dict) -> list[dict]:
    source_localizations = version_localizations(token, source_version["id"])
    if not source_localizations:
        raise RuntimeError("The latest released version has no localized App Store metadata to carry forward.")

    target_localizations = version_localizations(token, target_version["id"])
    by_locale = {item.get("attributes", {}).get("locale"): item for item in target_localizations}
    for source_localization in source_localizations:
        source_attributes = source_localization.get("attributes", {})
        locale = source_attributes.get("locale")
        if not locale:
            continue
        notes = RELEASE_NOTES.get(locale)
        if locale not in by_locale:
            attributes = {
                key: source_attributes[key]
                for key in ("locale", *VERSION_LOCALIZATION_FIELDS)
                if source_attributes.get(key) is not None
            }
            if notes:
                attributes["whatsNew"] = notes
            created = api_request(
                token,
                "/appStoreVersionLocalizations",
                method="POST",
                body={
                    "data": {
                        "type": "appStoreVersionLocalizations",
                        "attributes": attributes,
                        "relationships": {
                            "appStoreVersion": {"data": {"type": "appStoreVersions", "id": target_version["id"]}}
                        },
                    }
                },
            )
            by_locale[locale] = created["data"]
        elif notes and by_locale[locale].get("attributes", {}).get("whatsNew") in {
            None,
            source_attributes.get("whatsNew"),
        }:
            localization = by_locale[locale]
            api_request(
                token,
                f"/appStoreVersionLocalizations/{localization['id']}",
                method="PATCH",
                body={
                    "data": {
                        "type": "appStoreVersionLocalizations",
                        "id": localization["id"],
                        "attributes": {"whatsNew": notes},
                    }
                },
            )

    final_localizations = version_localizations(token, target_version["id"])
    missing_locales = sorted(
        {item.get("attributes", {}).get("locale") for item in source_localizations}
        - {item.get("attributes", {}).get("locale") for item in final_localizations}
    )
    if missing_locales:
        raise RuntimeError(f"New version is missing App Store localizations: {', '.join(missing_locales)}.")

    missing_screenshots: list[str] = []
    for localization in final_localizations:
        locale = localization.get("attributes", {}).get("locale", "unknown")
        relationship = localization.get("relationships", {}).get("appScreenshotSets", {}).get("data")
        if not relationship:
            # Some list responses omit relationship linkage even when the set exists.
            sets = list_pages(token, f"/appStoreVersionLocalizations/{localization['id']}/appScreenshotSets?limit=200")
        else:
            sets = relationship
        if not sets:
            missing_screenshots.append(locale)
            continue
        if relationship and isinstance(relationship[0], dict):
            set_ids = [item["id"] for item in relationship]
        else:
            set_ids = [item["id"] for item in sets]
        if not set_ids and sets:
            set_ids = [item["id"] for item in sets]
        screenshot_count = 0
        for screenshot_set_id in set_ids:
            screenshots = list_pages(token, f"/appScreenshotSets/{screenshot_set_id}/appScreenshots?limit=200")
            screenshot_count += len(screenshots)
        if screenshot_count == 0:
            missing_screenshots.append(locale)
    if missing_screenshots:
        raise RuntimeError(
            "The new App Store version has no screenshots for these localizations: "
            f"{', '.join(missing_screenshots)}. Add screenshots in App Store Connect before submitting."
        )
    return final_localizations


def build_for_release(token: str, version_string: str, build_number: str) -> dict:
    builds = list_pages(token, f"/apps/{APP_ID}/builds?limit=200")
    candidates = [
        build
        for build in builds
        if str(build.get("attributes", {}).get("version", "")) == build_number
        and build.get("attributes", {}).get("processingState") == "VALID"
        and not build.get("attributes", {}).get("expired", False)
    ]
    matching: list[dict] = []
    for build in candidates:
        relation = api_request(token, f"/builds/{build['id']}/preReleaseVersion").get("data", {})
        if relation.get("attributes", {}).get("version") == version_string:
            matching.append(build)
    if len(matching) != 1:
        states = ", ".join(
            f"{item.get('id')} ({item.get('attributes', {}).get('processingState')})"
            for item in candidates
        ) or "none"
        raise RuntimeError(
            f"Expected one VALID App Store Connect build {version_string} ({build_number}); "
            f"found {len(matching)}. Matching build-number candidates: {states}. "
            "No submission was made."
        )
    return matching[0]


def submission_items(token: str, submission_id: str) -> list[dict]:
    query = urlencode(
        {
            "limit": 200,
            "include": "appStoreVersion",
            "fields[reviewSubmissionItems]": "state,appStoreVersion",
        }
    )
    return list_pages(token, f"/reviewSubmissions/{submission_id}/items?{query}")


def item_app_store_version_id(item: dict) -> str | None:
    return (
        item.get("relationships", {})
        .get("appStoreVersion", {})
        .get("data", {})
        .get("id")
    )


def active_submissions(token: str) -> list[tuple[dict, list[dict]]]:
    submissions = list_pages(token, f"/apps/{APP_ID}/reviewSubmissions?filter[platform]=IOS&limit=200")
    active: list[tuple[dict, list[dict]]] = []
    for submission in submissions:
        if submission.get("attributes", {}).get("state") in ACTIVE_SUBMISSION_STATES:
            active.append((submission, submission_items(token, submission["id"])))
    return active


def write_summary(lines: list[str]) -> None:
    message = "\n".join(lines)
    print(message)
    summary_path = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary_path:
        with open(summary_path, "a", encoding="utf-8") as summary:
            summary.write(message + "\n")


def submit_review(token: str, app_version: dict, build: dict, submissions: list[tuple[dict, list[dict]]]) -> str:
    target_version_id = app_version["id"]
    target_submission: tuple[dict, list[dict]] | None = None
    for submission, items in submissions:
        item_version_ids = {item_app_store_version_id(item) for item in items}
        if target_version_id in item_version_ids:
            if target_submission:
                raise RuntimeError("More than one active submission contains the target version; no changes were made.")
            target_submission = (submission, items)
            continue
        if item_version_ids:
            version_ids = ", ".join(sorted(item_version_ids - {None})) or "unknown"
            raise RuntimeError(
                "Another iOS App Review submission is active for a different App Store version "
                f"({version_ids}); resolve it before submitting {app_version['attributes']['versionString']}."
            )
        if submission.get("attributes", {}).get("state") == "READY_FOR_REVIEW" and not target_submission:
            target_submission = (submission, items)
            continue
        raise RuntimeError("An active iOS review submission has no detectable app version; no changes were made.")

    state = app_version.get("attributes", {}).get("appStoreState", "UNKNOWN")
    if state in {"WAITING_FOR_REVIEW", "IN_REVIEW", "PENDING_DEVELOPER_RELEASE", "PENDING_APPLE_RELEASE", "READY_FOR_SALE"}:
        return f"Version is already beyond submission: {state}."
    if target_submission and target_submission[0].get("attributes", {}).get("state") != "READY_FOR_REVIEW":
        return (
            f"Version already belongs to submission {target_submission[0]['id']} "
            f"with state {target_submission[0].get('attributes', {}).get('state')}."
        )

    attached_build_id = (
        app_version.get("relationships", {}).get("build", {}).get("data", {}) or {}
    ).get("id")
    if attached_build_id != build["id"]:
        api_request(
            token,
            f"/appStoreVersions/{target_version_id}/relationships/build",
            method="PATCH",
            body={"data": {"type": "builds", "id": build["id"]}},
        )
        write_summary([f"Attached validated build {build['attributes']['version']} to App Store version {app_version['attributes']['versionString']}."])

    if target_submission:
        submission = target_submission[0]
        submission_state = submission.get("attributes", {}).get("state")
        if submission_state != "READY_FOR_REVIEW":
            return f"Version already belongs to submission {submission['id']} with state {submission_state}."
        submission_id = submission["id"]
        if target_version_id not in {item_app_store_version_id(item) for item in target_submission[1]}:
            api_request(
                token,
                "/reviewSubmissionItems",
                method="POST",
                body={
                    "data": {
                        "type": "reviewSubmissionItems",
                        "relationships": {
                            "reviewSubmission": {"data": {"type": "reviewSubmissions", "id": submission_id}},
                            "appStoreVersion": {"data": {"type": "appStoreVersions", "id": target_version_id}},
                        },
                    }
                },
            )
    else:
        created = api_request(
            token,
            "/reviewSubmissions",
            method="POST",
            body={
                "data": {
                    "type": "reviewSubmissions",
                    "attributes": {"platform": "IOS"},
                    "relationships": {"app": {"data": {"type": "apps", "id": APP_ID}}},
                }
            },
        )
        submission_id = created["data"]["id"]
        api_request(
            token,
            "/reviewSubmissionItems",
            method="POST",
            body={
                "data": {
                    "type": "reviewSubmissionItems",
                    "relationships": {
                        "reviewSubmission": {"data": {"type": "reviewSubmissions", "id": submission_id}},
                        "appStoreVersion": {"data": {"type": "appStoreVersions", "id": target_version_id}},
                    },
                }
            },
        )

    updated = api_request(
        token,
        f"/reviewSubmissions/{submission_id}",
        method="PATCH",
        body={
            "data": {
                "type": "reviewSubmissions",
                "id": submission_id,
                "attributes": {"submitted": True},
            }
        },
    )
    new_state = updated.get("data", {}).get("attributes", {}).get("state", "unknown")
    if new_state == "READY_FOR_REVIEW":
        refreshed = api_request(token, f"/reviewSubmissions/{submission_id}")
        new_state = refreshed.get("data", {}).get("attributes", {}).get("state", new_state)
    if new_state not in {"WAITING_FOR_REVIEW", "IN_REVIEW", "COMPLETE"}:
        raise RuntimeError(
            f"Apple accepted the submission request but reported state {new_state!r}. "
            f"Check submission {submission_id} in App Store Connect."
        )
    return f"Submitted App Store version {app_version['attributes']['versionString']} (build {build['attributes']['version']}) to App Review. Submission state: {new_state}."


def main() -> None:
    mode = os.environ.get("SUBMISSION_MODE", "inspect")
    version_string = os.environ.get("APP_STORE_VERSION", "1.2.0")
    build_number = os.environ.get("APP_STORE_BUILD_NUMBER", "8")
    if mode not in {"inspect", "submit"}:
        raise RuntimeError("SUBMISSION_MODE must be 'inspect' or 'submit'.")

    token = make_token()
    app = api_request(token, f"/apps/{APP_ID}").get("data", {})
    if app.get("attributes", {}).get("bundleId") != BUNDLE_ID:
        raise RuntimeError(f"App Store Connect app {APP_ID} does not match bundle ID {BUNDLE_ID}.")

    build = build_for_release(token, version_string, build_number)
    versions = app_store_versions(token)
    version = app_store_version(versions, version_string)
    source_version = latest_ready_version(versions, version_string) if version is None else None
    if version is None and mode == "inspect":
        available = ", ".join(
            f"{item.get('attributes', {}).get('versionString', 'unknown')} "
            f"({item.get('attributes', {}).get('appStoreState', 'unknown')})"
            for item in versions
            if item.get("attributes", {}).get("platform") == "IOS"
        ) or "none"
        write_summary(
            [
                f"App: {app.get('attributes', {}).get('name', 'Receitas Airfryer')} ({BUNDLE_ID})",
                f"App Store version {version_string}: not created yet.",
                f"Valid uploaded build: {build_number} — id {build['id']}.",
                f"Latest released version to copy metadata from: {source_version['attributes']['versionString']}.",
                f"Versions in App Store Connect: {available}.",
                "Read-only inspection complete; no App Store Connect data was changed.",
            ]
        )
        return
    if version is None:
        version = create_app_store_version(token, version_string, source_version)
        version = api_request(token, f"/appStoreVersions/{version['id']}?include=build").get("data", version)
        write_summary(
            [
                f"Created App Store version {version_string} using metadata from released version {source_version['attributes']['versionString']}.",
            ]
        )
    version_state = version.get("attributes", {}).get("appStoreState", "UNKNOWN")
    if mode == "submit" and version_state == "PREPARE_FOR_SUBMISSION":
        if source_version is None:
            source_version = latest_ready_version(versions, version_string)
        ensure_version_localizations(token, version, source_version)
    attached_build = (version.get("relationships", {}).get("build", {}).get("data") or {}).get("id")
    active = active_submissions(token)
    active_summary = ", ".join(
        f"{submission['id']}:{submission.get('attributes', {}).get('state')}"
        for submission, _ in active
    ) or "none"

    lines = [
        f"App: {app.get('attributes', {}).get('name', 'Receitas Airfryer')} ({BUNDLE_ID})",
        f"App Store version: {version_string} — state {version_state}",
        f"Build: {build_number} — processing {build['attributes'].get('processingState')} — id {build['id']}",
        f"Attached to version: {'yes' if attached_build == build['id'] else 'no'}",
        f"Active iOS review submissions: {active_summary}",
    ]

    if mode == "inspect":
        for submission, items in active:
            if version["id"] in {item_app_store_version_id(item) for item in items}:
                lines.append(f"Target version is already in submission {submission['id']}.")
        lines.append("Read-only inspection complete; no App Store Connect data was changed.")
        write_summary(lines)
        return

    result = submit_review(token, version, build, active)
    lines.append(result)
    write_summary(lines)


if __name__ == "__main__":
    try:
        main()
    except Exception as error:  # GitHub Actions should show a concise release failure.
        print(f"::error::{error}", file=sys.stderr)
        raise
