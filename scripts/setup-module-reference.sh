#!/usr/bin/env bash
set -euo pipefail

# ========================================
# 模塊 reference 初始化 / Module Reference Setup
# ========================================

MODULE_REPO="${MODULE_REPO:-https://github.com/billwei1107/module.git}"
MODULE_TAG="${MODULE_TAG:-module-v2026.05.10.2}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="${PROJECT_ROOT:-$(cd "$SCRIPT_DIR/.." && pwd)}"
REFERENCE_ROOT="$PROJECT_ROOT/reference"
TARGET_DIR="$REFERENCE_ROOT/模塊化組件"

echo "==> Project root: $PROJECT_ROOT"
echo "==> Module repo: $MODULE_REPO"
echo "==> Module tag: $MODULE_TAG"

mkdir -p "$REFERENCE_ROOT"

if [ -e "$TARGET_DIR" ] && [ ! -d "$TARGET_DIR/.git" ]; then
  echo "Error: $TARGET_DIR already exists but is not a git repository." >&2
  echo "Move or remove it before running this setup script." >&2
  exit 1
fi

if [ ! -d "$TARGET_DIR/.git" ]; then
  echo "==> Cloning module reference..."
  git clone "$MODULE_REPO" "$TARGET_DIR"
else
  echo "==> Module reference already exists. Updating tags..."
fi

git -C "$TARGET_DIR" fetch --tags origin
git -C "$TARGET_DIR" checkout "$MODULE_TAG"

if [ ! -f "$TARGET_DIR/ai-handoff.md" ]; then
  echo "Error: ai-handoff.md not found after checkout. The selected tag may be invalid." >&2
  exit 1
fi

echo "==> Module reference is ready:"
git -C "$TARGET_DIR" --no-pager log -1 --oneline
echo "==> Next: read reference/模塊化組件/ai-handoff.md"
