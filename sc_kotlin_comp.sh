#!/usr/bin/env bash
set -euo pipefail
if [ "$#" -ne 3 ]; then
  echo "Usage: $0 <scala-source-dir> <kotlin-source-dir> <output-dir>" >&2
  exit 2
fi
scala_sources="$1"
kotlin_sources="$2"
classes_dir="$3"
# Run from modules/reflect, where the polyglot profile is defined.
compile_classpath="$(lein with-profile +polyglot classpath)"
mkdir -p "$classes_dir"
source_list="$(mktemp)"
trap 'rm -f "$source_list"' EXIT
find "$scala_sources" -type f -name '*.scala' > "$source_list"
scalac -classpath "$compile_classpath" -d "$classes_dir" "@$source_list"
kotlinc "$kotlin_sources" -d "$classes_dir" -cp "$classes_dir:$compile_classpath"
