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

for path in ROOT.rglob("*"):
    if path.is_file() and path.name != "validate_ios.py" and path.suffix not in {".png", ".json"}:
        text = path.read_text(encoding="utf-8", errors="ignore")
        assert not re.search(r"(?i)(api[_-]?key|secret|password|private[_-]?key|BEGIN [A-Z ]+ KEY)", text), path

print(f"validated 31 recipes, {len(resource_names)} image resources, PrivacyInfo, plist, and secret safety")
