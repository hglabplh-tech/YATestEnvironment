#!/usr/bin/env bash
set -euo pipefail
project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
lein_cmd="${LEIN_CMD:-lein}"
# lein-shell exports its own classpath; children must resolve their own.
unset CLASSPATH
action="${1:-test}"
if [ "$#" -gt 0 ]; then shift; fi
case "$action" in
  test|jar|pom|install|clean|deploy) ;;
  *) echo "Usage: lein modules {test|jar|pom|install|clean|deploy} [task arguments]" >&2; exit 2 ;;
esac
# Build prerequisites before dependents. Never continue after a failed task.
for module in inspect reflect productive test; do
  echo "=== $module: $action ==="
  (
    cd "$project_root/modules/$module"
    "$lein_cmd" "$action" "$@"
    # Make this version available for downstream library builds.
    case "$action" in
      test|jar|deploy) "$lein_cmd" install ;;
    esac
  )
done
