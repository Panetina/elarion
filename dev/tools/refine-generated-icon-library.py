"""Refine names inside the generated Elarion icon library.

This pass intentionally uses only the generated project library:

platform/core/src/main/resources/assets/elarion_core/textures/gui/library

It renames PNG files already copied there and updates manifest.json to match.
It does not read or depend on the original Desktop icon packs.
"""

from __future__ import annotations

import argparse
import json
import re
from pathlib import Path


def slug(value: str) -> str:
    value = re.sub(r"(?<=[a-z0-9])(?=[A-Z])", "_", value)
    value = re.sub(r"[^a-zA-Z0-9]+", "_", value).strip("_").lower()
    return re.sub(r"_+", "_", value)


def numbered(labels: list[str], start: int, category: str) -> dict[int, tuple[str, str]]:
    return {start + index: (category, label) for index, label in enumerate(labels)}


PACK_1_REFINEMENTS: dict[int, tuple[str, str]] = {}
PACK_1_REFINEMENTS.update(numbered([
    "roast meat", "fried egg", "rice roll", "wrapped rice", "cake slice",
    "rice bowl", "stew bowl", "noodle bowl", "dumpling", "skewer",
    "green pepper", "white rice", "soup jar", "green bean", "corn",
    "roasted nut", "drumstick", "potion bottle", "lettuce leaf", "ice cube",
], 351, "food"))
PACK_1_REFINEMENTS.update(numbered([
    "red pepper", "burger", "bun", "sausage", "pancakes",
    "frosted pastry", "sandwich stack", "strawberry cake", "blueberry cake", "cream cake",
], 401, "food"))
PACK_1_REFINEMENTS.update(numbered([
    "shrimp", "dumpling", "roast meat", "cream bowl", "wrapped vegetable",
    "fish steak", "cake bowl", "dumpling", "fruit skewer", "wrap",
], 491, "food"))
PACK_1_REFINEMENTS.update(numbered([
    "milk carton", "wrapped candy", "pizza slice", "leaf wrap", "waffle",
    "sandwich", "candle dessert", "blue bottle", "wrapped candy", "vegetable wrap",
], 521, "food"))
PACK_1_REFINEMENTS.update(numbered([
    "parchment", "parchment", "wax seal", "open envelope", "paperclip",
    "sealed parchment", "golden seal stamp", "red book", "tied scroll", "blue quill",
    "ink bottle", "blue book", "green book", "blue book", "rolled scroll",
    "red map pin", "magnifying glass", "red book", "folded map", "open map",
], 641, "documents"))
PACK_1_REFINEMENTS.update(numbered([
    "fish trap", "green hook", "notice board", "rolled scroll", "gift box",
    "wooden chest", "bottle", "lantern", "white pouch", "wooden stand",
    "brown basket", "chain hook", "bucket", "wooden bucket", "satchel",
    "green pouch", "barrel frame", "gold lantern", "teapot", "clay pot",
], 661, "tools"))
PACK_1_REFINEMENTS.update(numbered([
    "cooking pot", "wooden crate", "silver pouch", "bone handle", "bone handle",
    "carved handle", "red broom", "red heart", "stone weight", "metal hook",
], 681, "tools"))
PACK_1_REFINEMENTS.update(numbered([
    "bundle", "fruit basket", "wooden handle", "flower basket", "bucket",
    "red axe", "metal axe", "green branch", "red axe", "blue hatchet",
    "white pelt", "silver needle", "gold ladle", "wooden box", "red chest",
    "green lantern", "cardboard box", "wooden crate", "torch", "lit torch",
], 701, "tools"))
PACK_1_REFINEMENTS.update(numbered([
    "silver shield", "gold idol", "green cage", "red cap", "iron helm",
    "painted disc", "silver blade", "wood club", "spiked shield", "wood staff",
    "ice shield", "white wing", "wood tripod", "potion bomb", "blue shield",
    "pink bomb", "flower shield", "iron shackle", "chain coil", "white axe",
], 761, "weapons"))
PACK_1_REFINEMENTS.update(numbered([
    "hammer head", "gold medal", "green cage", "red cap", "silver charm",
    "painted compass", "silver wand", "wooden torch", "spiked ring", "golden cage",
    "blue cylinder", "blue orb cage", "medical kit", "silver badge", "red vial",
    "golden box", "green crate", "silver axe", "green handle", "silver hook",
], 901, "tools"))
PACK_1_REFINEMENTS.update(numbered([
    "green crate", "silver crate", "white parcel", "golden parcel", "metal cylinder",
    "green ring frame", "green ring frame", "green ring frame", "green ring frame", "bronze ring frame",
    "bronze ring frame", "bronze ring frame", "bronze ring frame", "dark plate", "brown handle",
    "silver frame", "silver box", "wood peg", "wooden barrel", "drill bit",
], 961, "materials"))
PACK_1_REFINEMENTS.update(numbered([
    "blue fish", "gold fish", "pink jellyfish", "blue shark", "silver eel",
    "orange crab", "silver shellfish", "striped shellfish", "blue shellfish", "gold shell",
    "red squid", "silver squid", "starfish", "pink shrimp", "brown shell",
    "ice fish", "ice fish", "coral", "orange coral", "blue fish",
], 1081, "fish"))
PACK_1_REFINEMENTS.update(numbered([
    "orange coral", "red worm", "blue hook", "silver hook", "silver hook",
    "chain hook", "silver hook", "red hook", "blue hook", "red hook",
    "silver hook", "hook cluster", "helmet shell", "life ring", "wooden mallet",
    "golden gear", "blue shard", "glass box", "wrapped bundle", "golden nugget",
], 1101, "materials"))
PACK_1_REFINEMENTS.update(numbered([
    "golden nugget", "green crystal", "red coral", "blue shell", "green shell",
    "black stone", "silver ore", "brown stone", "blue crystal", "blue shell",
    "blue ore", "red crystal", "orange cluster", "golden rune", "red rune",
    "black stone", "gray shard", "ice crystal", "red rune", "gray feather",
    "silver spiral shell", "white emblem", "ice spikes", "red crystal medallion", "red feather",
], 1121, "magic"))

PACK_2_REFINEMENTS: dict[int, tuple[str, str]] = {}
PACK_2_REFINEMENTS.update(numbered([
    "purple shard", "silver gem", "silver crystal", "silver shard", "silver diamond",
    "white pearl", "stone monolith", "silver ingot", "silver tablet", "gray hide",
    "gray plate", "bone fragment", "metal ring", "claw fragment", "skull",
    "skull", "skull", "large skull", "tooth", "tooth",
    "bone", "tusk", "horns", "tusk pair", "bone pile",
    "eye", "ear", "ear", "claw", "claw",
], 211, "creature_parts"))
PACK_2_REFINEMENTS.update(numbered([
    "blue crystal orb", "blue crescent amulet", "brown horn", "blue horn", "golden chain",
    "orange sphere", "red jar", "pink hook charm", "pink flower charm", "blue crystal charm",
    "golden crystal charm", "blue skull charm", "pink skull charm", "gold skull charm", "green gem charm",
    "brown charm", "gold charm", "red mask charm", "blue mask charm", "green mask charm",
    "orange spiral charm", "blue spiral charm", "gold spiral charm", "red crystal cluster", "purple crystal cluster", "gold crystal cluster",
], 335, "magic"))
PACK_2_REFINEMENTS.update(numbered([
    "iron spike", "iron spike", "blue iron spike", "blue iron spike", "bronze spike",
    "gold spike", "bone peg", "bone claw", "bone shard", "red pail",
    "gray bucket", "orange token", "green bowl", "orange bottle", "golden bowl",
    "orange bowl", "gold sparks", "orange sparks", "red sparks", "red spark cluster",
], 401, "tools"))
PACK_2_REFINEMENTS.update(numbered([
    "cheese pizza", "pepperoni pizza", "vegetable pizza", "seaweed pizza", "berry pizza",
    "corn cob", "croissant", "blue lollipop", "green heart", "blue orb",
    "silver cup", "blue cup", "croissant", "green crystal food", "croissant",
], 586, "food"))
PACK_2_REFINEMENTS.update(numbered([
    "flame blade", "green axe", "purple axe", "silver axe", "bone mace",
    "red mace", "red mace", "war axe", "green axe", "red axe",
    "blue sword", "green sword", "flame sword", "golden club", "green club",
    "purple mace", "silver dagger", "silver dagger", "silver spear", "silver spear",
    "bow", "red staff", "silver spear", "silver axe", "silver spear",
    "silver spear", "silver spear", "silver spear", "bow", "red staff",
    "silver spear", "silver axe", "orange shield", "green shield", "blue axe",
    "red axe", "green mace", "orange mace", "red dagger", "purple dagger",
], 1761, "weapons"))
PACK_2_REFINEMENTS.update(numbered([
    "red gem wand", "purple gem wand", "orange gem wand", "green gem wand", "green claw wand",
    "purple claw wand", "red orb wand", "green orb wand", "gold orb", "blue orb",
], 1801, "weapons"))

PACK_6_TOOL_NAMES = {
    0: "wooden_pickaxe", 1: "wooden_pickaxe", 2: "stone_hammer", 3: "stone_pickaxe",
    4: "wooden_pickaxe", 5: "iron_pickaxe", 6: "stone_hoe", 7: "iron_pickaxe",
    8: "iron_pickaxe", 9: "stone_hammer", 10: "iron_pickaxe", 11: "iron_pickaxe",
    12: "iron_shears", 13: "stone_hammer", 14: "bronze_pickaxe",
    15: "golden_pickaxe", 16: "golden_hammer", 17: "copper_pickaxe",
    18: "bronze_pickaxe", 19: "golden_pickaxe", 20: "golden_hammer",
}


def refinement_for(asset: dict) -> tuple[str, str] | None:
    pack = asset["sourcePack"]
    raw_id = asset["id"].split("/", 1)[1]
    try:
        number = int(raw_id)
    except ValueError:
        number = None
    if pack == "pack_1" and number in PACK_1_REFINEMENTS:
        return PACK_1_REFINEMENTS[number]
    if pack == "pack_2" and number in PACK_2_REFINEMENTS:
        return PACK_2_REFINEMENTS[number]
    if pack == "pack_6" and raw_id.startswith("tool_"):
        tool_number = int(raw_id.removeprefix("tool_"))
        return "tools", PACK_6_TOOL_NAMES[tool_number].replace("_", " ")
    return None


def rename_asset_files(library_root: Path, asset: dict, category: str, description: str) -> dict[str, str]:
    stable_key = asset["id"].split("/", 1)[1]
    if stable_key.isdigit():
        stable_key = f"{int(stable_key):04}"
    asset_name = f"{asset['sourcePack']}_{slug(description)}_{stable_key}"
    updated_sizes: dict[str, str] = {}
    for dimensions, relative_path in sorted(asset["sizes"].items()):
        source = library_root / relative_path
        target = library_root / category / dimensions / f"{asset_name}.png"
        if source == target:
            updated_sizes[dimensions] = relative_path
            continue
        if not source.exists():
            raise FileNotFoundError(source)
        target.parent.mkdir(parents=True, exist_ok=True)
        if target.exists():
            raise FileExistsError(target)
        source.rename(target)
        updated_sizes[dimensions] = target.relative_to(library_root).as_posix()
    asset["category"] = category
    asset["description"] = description
    asset["name"] = asset_name
    asset["sizes"] = updated_sizes
    asset["labelSource"] = "generated_library_refinement"
    return updated_sizes


def remove_empty_dirs(root: Path) -> None:
    for path in sorted((p for p in root.rglob("*") if p.is_dir()), reverse=True):
        try:
            path.rmdir()
        except OSError:
            pass


def refine(library_root: Path) -> None:
    manifest_path = library_root / "manifest.json"
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    changed = 0
    for asset in manifest["assets"]:
        refinement = refinement_for(asset)
        if refinement is None:
            continue
        category, description = refinement
        if asset["category"] == category and asset["description"] == description:
            continue
        rename_asset_files(library_root, asset, category, description)
        changed += 1
    manifest_path.write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    remove_empty_dirs(library_root)
    print(f"refined {changed} generated library assets")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("library_root", type=Path)
    args = parser.parse_args()
    refine(args.library_root.resolve())


if __name__ == "__main__":
    main()
