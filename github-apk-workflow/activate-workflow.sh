#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p .github/workflows
cp github-apk-workflow/build-apk.yml .github/workflows/build-apk.yml
if [ -f gradlew ]; then
  chmod +x gradlew
fi
echo "Da tao .github/workflows/build-apk.yml thanh cong! Ban co the push len GitHub ngay."
