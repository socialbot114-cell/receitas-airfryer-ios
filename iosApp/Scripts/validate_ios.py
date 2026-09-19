#!/usr/bin/env python3
"""Content-only validation for the offline iOS foundation."""
import json
import plistlib
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
payload_path = ROOT / "Resources/Recipes/recipes.json"
payload = json.loads(payload_path.read_text(encoding="utf-8"))
recipes = payload["recipes"]
assert len(recipes) == 31, len(recipes)
assert len({recipe["id"] for recipe in recipes}) == 31
assert all(recipe["ingredients"] and len(recipe["steps"]) >= 4 for recipe in recipes)
assert all(160 <= recipe["temperature"] <= 205 and recipe["minutes"] > 0 for recipe in recipes)
assert all(step["instruction"] and step["minutes"] >= 0 for recipe in recipes for step in recipe["steps"])
assert len(payload["guide"]) == 15

# Allergy/diet metadata is intentionally not presented: incomplete positive or
# negative claims are unsafe without supplier and cross-contamination data.
for recipe in recipes:
    assert isinstance(recipe.get("allergens"), list)
    assert isinstance(recipe.get("dietaryTags"), list)
    assert all(isinstance(value, str) and value.strip() for key in ("allergens", "dietaryTags") for value in recipe[key])
view_source = "\n".join(path.read_text(encoding="utf-8") for path in (ROOT / "Sources/Views").glob("*.swift"))
assert not re.search(r"\.(allergens|dietaryTags)\b", view_source)

resource_names = {path.stem for path in (ROOT / "Resources/Images").glob("*")}
for recipe in recipes:
    if recipe["imageName"]:
        assert recipe["imageName"] in resource_names, recipe["imageName"]

with (ROOT / "Info.plist").open("rb") as stream:
    info = plistlib.load(stream)
assert info["CFBundleIdentifier"] == "$(PRODUCT_BUNDLE_IDENTIFIER)"
with (ROOT / "Resources/PrivacyInfo.xcprivacy").open("rb") as stream:
    privacy = plistlib.load(stream)
assert privacy["NSPrivacyTracking"] is False
assert privacy["NSPrivacyCollectedDataTypes"] == []

project = (ROOT / "project.yml").read_text(encoding="utf-8")
assert "br.com.receitasairfyer" in project
assert "6813681707" in project
assert 'TARGETED_DEVICE_FAMILY: "1,2"' in project
assert "BUNDLE_LOADER: $(TEST_HOST)" in project
assert project.count("path: Resources/Images") == 2
assert (ROOT / "Resources/Assets.xcassets/LaunchBackground.colorset/Contents.json").is_file()

ci = (ROOT.parent / ".github/workflows/ios.yml").read_text(encoding="utf-8")
release = (ROOT.parent / ".github/workflows/ios-release.yml").read_text(encoding="utf-8")
assert "xcodebuild test" in ci
assert 'CURRENT_PROJECT_VERSION="$GITHUB_RUN_NUMBER"' in release

for path in ROOT.rglob("*"):
    if path.is_file() and path.name != "validate_ios.py" and path.suffix not in {".png", ".json"}:
        text = path.read_text(encoding="utf-8", errors="ignore")
        assert not re.search(r"(?i)(api[_-]?key|secret|password|private[_-]?key|BEGIN [A-Z ]+ KEY)", text), path

print(f"validated 31 recipes, {len(resource_names)} image resources, UI claims, CI, plists, and secret safety")
