#!/usr/bin/env bash
# Renders kong/kong.yml (the file Kong actually mounts) from kong/kong.yml.template + .env.
# Kong does NOT substitute ${VAR} in a bind-mounted declarative config file itself —
# env_file only sets env vars inside the container process, it never touches the
# file contents. So this explicit envsubst pass is the step that was missing.
set -euo pipefail
cd "$(dirname "$0")/.."   # run from repo root regardless of where it's called from

if [ ! -f .env ]; then
  echo "Missing .env at repo root — copy .env.example to .env and fill in real values first." >&2
  exit 1
fi

if ! command -v envsubst >/dev/null 2>&1; then
  echo "envsubst not found. Install it: sudo apt-get install gettext-base" >&2
  exit 1
fi

set -a
source .env
set +a

envsubst < kong/kong.yml.template > kong/kong.yml

echo "kong/kong.yml generated from kong/kong.yml.template using .env"
