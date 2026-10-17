#!/bin/sh
set -eu

repo_path="${CI_PRIMARY_REPOSITORY_PATH:?CI_PRIMARY_REPOSITORY_PATH is required}"
team_id="${CI_TEAM_ID:?CI_TEAM_ID is required for automatic signing}"
spec_path="$repo_path/iosApp/project.xcode-cloud.yml"
cd "$repo_path"
trap 'rm -f "$spec_path"' EXIT

python3 - "$repo_path/iosApp/project.yml" "$spec_path" "$team_id" <<'PY'
import re
import sys
from pathlib import Path

source = Path(sys.argv[1]).read_text(encoding="utf-8")
team_id = sys.argv[3]
if not re.fullmatch(r"[A-Z0-9]{10}", team_id):
    raise SystemExit("CI_TEAM_ID is not a valid Apple Developer team identifier")
updated, replacements = re.subn(
    r'(?m)^(\s*DEVELOPMENT_TEAM:)\s*""\s*$',
    rf'\1 "{team_id}"',
    source,
    count=1,
)
if replacements != 1:
    raise SystemExit("Expected one empty DEVELOPMENT_TEAM setting in iosApp/project.yml")
Path(sys.argv[2]).write_text(updated, encoding="utf-8")
PY
if ! command -v xcodegen >/dev/null 2>&1; then
  if ! command -v brew >/dev/null 2>&1; then
    printf 'XcodeGen and Homebrew are unavailable in this Xcode Cloud environment.\n' >&2
    exit 1
  fi
  brew install xcodegen
fi

xcodegen generate --spec "$spec_path"
