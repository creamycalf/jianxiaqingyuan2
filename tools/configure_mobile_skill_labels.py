#!/usr/bin/env python3
"""Apply opt-in captions to the mobile skill-button INI resources."""

from __future__ import annotations

import argparse
from pathlib import Path


LABELS = {
    "attack.ini": "攻击",
    "rightjump.ini": "跳跃",
    "sit.ini": "打坐",
    "skill1.ini": "武功1",
    "skill2.ini": "武功2",
    "skill3.ini": "武功3",
    "skill4.ini": "武功4",
    "skill5.ini": "武功5",
}


def update_ini(path: Path, label: str) -> bool:
    text = path.read_text(encoding="utf-8")
    lines = text.splitlines()
    section = ""
    values: dict[str, str] = {}
    for line in lines:
        stripped = line.strip()
        if stripped.startswith("[") and stripped.endswith("]"):
            section = stripped[1:-1].lower()
        elif "=" in stripped:
            key, value = stripped.split("=", 1)
            if section == "init":
                values[key.strip().lower()] = value.strip()

    if values.get("icon", "").lower() not in {
        "attack", "jump", "sit", "skill1", "skill2", "skill3", "skill4", "skill5"
    }:
        raise ValueError(f"unexpected or missing skill icon in {path}")

    target = {"label": "true", "labeltext": label}
    output: list[str] = []
    section = ""
    seen: set[str] = set()
    for line in lines:
        stripped = line.strip()
        if stripped.startswith("[") and stripped.endswith("]"):
            if section == "init":
                for key, value in target.items():
                    if key not in seen:
                        output.append(f"{key}={value}")
            section = stripped[1:-1].lower()
            seen = set()
            output.append(line)
            continue
        if section == "init" and "=" in line:
            key = line.split("=", 1)[0].strip().lower()
            if key in target:
                if key not in seen:
                    output.append(f"{key}={target[key]}")
                    seen.add(key)
                continue
        output.append(line)
    if section == "init":
        for key, value in target.items():
            if key not in seen:
                output.append(f"{key}={value}")

    updated = "\n".join(output) + ("\n" if text.endswith(("\n", "\r")) else "")
    if updated == text:
        return False
    path.write_text(updated, encoding="utf-8", newline="")
    return True


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "skills_dir",
        type=Path,
        help="skills INI directory, for example build-local/assets/common/ini/ui/mobile/skills",
    )
    args = parser.parse_args()
    directory = args.skills_dir.resolve()
    changed = []
    for filename, label in LABELS.items():
        path = directory / filename
        if not path.is_file():
            raise FileNotFoundError(path)
        if update_ini(path, label):
            changed.append(filename)
    print("Updated: " + (", ".join(changed) if changed else "none (already configured)"))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
