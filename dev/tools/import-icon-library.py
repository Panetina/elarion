"""Build Elarion's indexed UI icon library from locally supplied PNG packs.

The importer is intentionally deterministic: source-pack identity is retained,
same-named files from different packs never collide, and every output is listed
in the generated JSON manifest.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import shutil
from collections import defaultdict
from dataclasses import dataclass
from pathlib import Path

from PIL import Image


SUPPORTED_ICON_SIZES = {16, 32, 48, 64}


@dataclass(frozen=True)
class VisualMetadata:
    category: str
    description: str
    label_source: str


def slug(value: str) -> str:
    value = re.sub(r"(?<=[a-z0-9])(?=[A-Z])", "_", value)
    value = re.sub(r"[^a-zA-Z0-9]+", "_", value).strip("_").lower()
    return re.sub(r"_+", "_", value)


def digest(path: Path) -> str:
    result = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(65536), b""):
            result.update(chunk)
    return result.hexdigest()


def base_key(path: Path) -> str:
    name = path.stem
    # Pack 2 uses fa/fb/fc for 16/32/64 variants of one numbered icon.
    match = re.fullmatch(r"f[abc](\d+)", name, re.IGNORECASE)
    if match:
        return match.group(1)
    match = re.fullmatch(r"item(\d+)", name, re.IGNORECASE)
    if match:
        return match.group(1)
    return slug(name)


def inferred_category(pack: str, source_name: str) -> str:
    name = slug(source_name)
    first = name.split("_", 1)[0]
    aliases = {
        "acc": "accessories", "armor": "armor", "arrow": "weapons",
        "bar": "ui", "smallbar": "ui", "b": "ui", "book": "documents",
        "bow": "weapons", "gem": "gems", "gui": "ui",
        "inventory": "ui", "potion": "consumables", "quiver": "weapons",
        "rune": "magic", "shield": "armor", "skill": "skills",
        "staff": "magic", "sword": "weapons", "tool": "tools",
        "amulet": "jewelry", "necklace": "jewelry", "ring": "jewelry",
        "mask": "accessories", "ancientmask": "accessories",
        "butterflygem": "gems", "scarab": "gems", "stele": "relics",
        "banner": "relics", "scroll": "documents", "notebook": "documents",
        "flask": "consumables", "coin": "currency", "key": "keys",
        "food": "food", "portrait": "portraits",
    }
    if pack == "portrait":
        return "portraits"
    if first in aliases:
        return aliases[first]
    tokens = set(name.split("_"))
    groups = [
        ("magic", {"wand", "staff", "orb", "rune", "spell", "wisp"}),
        ("weapons", {"sword", "dagger", "blade", "bow", "arrow", "spear", "knife", "axe", "mace", "hammer"}),
        ("armor", {"armor", "shield", "boots", "glove", "guard", "headgear", "helmet"}),
        ("jewelry", {"amulet", "necklace", "ring", "circlet"}),
        ("food", {
            "apple", "banana", "bread", "cake", "carrot", "cheese", "cookies", "dessert",
            "drink", "egg", "fish", "fruit", "ingredient", "meat", "mushroom", "onigiri",
            "pepper", "sandwich", "seafood", "tomato", "veggie", "vegetable", "watermelon",
        }),
        ("materials", {"ingot", "ore", "fabric", "cloth", "fur", "log", "stone", "dust", "herb", "feather"}),
        ("tools", {"anvil", "gear", "hammer", "hook", "pickaxe", "scissors", "scythe", "scethe", "hoe", "compass", "watering"}),
        ("documents", {"book", "map", "paper", "scroll", "quill", "pencil", "notebook"}),
    ]
    for category, words in groups:
        if tokens & words:
            return category
    if tokens & {"gem", "crystal", "scarab"}:
        return "gems"
    if tokens & {"mask", "glasses", "belt", "hat", "watch"}:
        return "accessories"
    if tokens & {"potion", "flask"}:
        return "consumables"
    if tokens & {"banner", "stele", "vinyl", "skull"}:
        return "relics"
    if "key" in tokens:
        return "keys"
    # Opaque packs remain explicitly searchable by pack and visual id rather
    # than receiving fabricated object labels.
    return "assorted"


def visual_bucket(category: str, description: str) -> VisualMetadata:
    return VisualMetadata(category, description, "contact_sheet_bucket")


def visual_exact(category: str, description: str) -> VisualMetadata:
    return VisualMetadata(category, description, "manual_contact_sheet")


def range_lookup(
    pack: str,
    number: int,
    ranges: list[tuple[int, int, str, str]],
) -> VisualMetadata:
    for start, end, category, subject in ranges:
        if start <= number <= end:
            return visual_bucket(category, subject)
    raise ValueError(f"no visual metadata range for {pack} icon {number}")


EXACT_OPAQUE_SUBJECTS: dict[str, dict[int, tuple[str, str]]] = {
    "pack_1": {
        326: ("food", "carrot"),
        336: ("food", "watermelon"),
        337: ("food", "watermelon"),
        338: ("food", "egg"),
        341: ("food", "kiwi"),
        342: ("food", "eggplant"),
        343: ("food", "cucumber"),
        344: ("food", "banana"),
        347: ("food", "donut"),
        348: ("food", "ice cream"),
        357: ("food", "pancakes"),
        358: ("food", "cookie"),
        365: ("food", "apple"),
        372: ("food", "cherries"),
        386: ("food", "sandwich"),
        390: ("food", "carrot"),
        418: ("food", "tomato"),
        419: ("food", "broccoli"),
        465: ("food", "rice bowl"),
        466: ("food", "soup bowl"),
        471: ("food", "mushroom"),
        475: ("food", "pie slice"),
        481: ("food", "watermelon"),
        488: ("food", "herb"),
        489: ("food", "carrot"),
        490: ("food", "pizza"),
        499: ("magic", "purple vial"),
        500: ("magic", "green vial"),
    },
    "pack_2": {
        1: ("world_objects", "bag"),
        2: ("armor", "armor"),
        3: ("ui", "direction arrow sign"),
        4: ("world_objects", "mirror"),
        5: ("world_objects", "fountain"),
        6: ("world_objects", "chest"),
        7: ("documents", "quill"),
        8: ("documents", "parchment"),
        9: ("world_objects", "treasure chest"),
        10: ("documents", "blue note"),
        11: ("documents", "red note"),
        12: ("ui", "star"),
        13: ("ui", "question mark"),
        14: ("world_objects", "trophy"),
        15: ("world_objects", "gold trophy"),
        16: ("world_objects", "door"),
        17: ("world_objects", "mountain"),
        18: ("world_objects", "mountain"),
        19: ("world_objects", "mountain"),
        20: ("gems", "blue crystal"),
        21: ("world_objects", "ship"),
        22: ("flora", "palm tree"),
        23: ("world_objects", "campfire"),
        24: ("world_objects", "stone doorway"),
        25: ("world_objects", "crate"),
        26: ("gems", "ice crystal"),
        27: ("world_objects", "rock"),
        28: ("world_objects", "rock"),
        29: ("world_objects", "rock"),
        30: ("world_objects", "rock"),
        31: ("world_objects", "rock"),
        32: ("gems", "green crystal"),
        33: ("world_objects", "building"),
        34: ("world_objects", "stone tower"),
        35: ("world_objects", "hut"),
        36: ("world_objects", "purple doorway"),
        37: ("world_objects", "building"),
        38: ("world_objects", "tower"),
        39: ("world_objects", "stone slab"),
        40: ("materials", "log"),
        41: ("flora", "grass patch"),
        42: ("flora", "grass patch"),
        43: ("materials", "red cloth"),
        44: ("materials", "red cloth"),
        45: ("world_objects", "basin"),
        46: ("flora", "tree"),
        47: ("clothing", "coat"),
        48: ("flora", "bush"),
        49: ("tools", "spoon"),
        50: ("tools", "net"),
        51: ("tools", "net"),
        52: ("world_objects", "goblet"),
        53: ("tools", "pipe"),
        54: ("documents", "scroll"),
        55: ("documents", "scroll"),
        56: ("world_objects", "goblet"),
        57: ("world_objects", "bowl"),
        58: ("gems", "crystal goblet"),
        59: ("gems", "crystal"),
        60: ("world_objects", "goblet"),
        61: ("tools", "pipe"),
        62: ("gems", "green crystal"),
        63: ("gems", "green crystal"),
        64: ("world_objects", "goblet"),
        65: ("world_objects", "gold trophy"),
        66: ("world_objects", "gold trophy"),
        67: ("world_objects", "gold trophy"),
        68: ("world_objects", "blue trophy"),
        69: ("world_objects", "blue trophy"),
        70: ("world_objects", "blue trophy"),
        71: ("keys", "key"),
        72: ("keys", "key"),
        73: ("keys", "key"),
        74: ("keys", "key"),
        75: ("keys", "key ring"),
        76: ("keys", "red key"),
        77: ("keys", "red key"),
        78: ("keys", "red key"),
        79: ("keys", "red key"),
        80: ("keys", "key ring"),
        81: ("magic", "flame wand"),
        82: ("tools", "torch"),
        83: ("magic", "altar candle"),
        84: ("magic", "altar candle"),
        85: ("world_objects", "lantern"),
        86: ("world_objects", "lantern"),
        87: ("magic", "blue feather"),
        88: ("tools", "torch"),
        89: ("magic", "purple candle"),
        90: ("magic", "purple candle"),
        91: ("world_objects", "purple lantern"),
        92: ("world_objects", "purple lantern"),
        93: ("armor", "cloak"),
        94: ("relics", "tablet"),
        95: ("armor", "hood"),
        96: ("relics", "red tablet"),
        97: ("gems", "red gem"),
        98: ("documents", "open book"),
        99: ("gems", "purple gem"),
        100: ("documents", "open book"),
        101: ("gems", "blue gem"),
        102: ("documents", "open book"),
        103: ("gems", "green gem"),
        104: ("documents", "open book"),
        105: ("armor", "boot"),
        106: ("documents", "scroll"),
        107: ("documents", "scroll"),
        108: ("documents", "scroll"),
        109: ("documents", "scroll"),
        110: ("documents", "scroll"),
        111: ("documents", "scroll"),
        112: ("documents", "scroll"),
        113: ("currency", "coin stack"),
        114: ("gems", "green crystals"),
        115: ("food", "berries"),
        116: ("jewelry", "necklace"),
        117: ("jewelry", "amulet"),
        118: ("jewelry", "amulet"),
        119: ("keys", "key"),
        120: ("documents", "ticket"),
        121: ("consumables", "green potion"),
        122: ("consumables", "purple potion"),
        123: ("consumables", "blue potion"),
        124: ("world_objects", "scales"),
        125: ("tools", "anvil"),
        126: ("tools", "signpost"),
        127: ("currency", "coin pouch"),
        128: ("tools", "hook"),
        129: ("documents", "red stamp"),
        130: ("documents", "silver stamp"),
        131: ("documents", "gold stamp"),
        132: ("documents", "blue stamp"),
        133: ("documents", "silver paper"),
        134: ("documents", "red paper"),
        135: ("documents", "silver paper"),
        136: ("documents", "gold paper"),
        137: ("documents", "blue paper"),
        138: ("world_objects", "red chalice"),
        139: ("world_objects", "silver chalice"),
        140: ("world_objects", "gold chalice"),
        141: ("world_objects", "silver chalice"),
        142: ("world_objects", "red chalice"),
        143: ("world_objects", "silver chalice"),
        144: ("world_objects", "gold chalice"),
        145: ("world_objects", "blue chalice"),
        265: ("consumables", "red potion"),
        266: ("consumables", "red potion"),
        267: ("consumables", "red potion"),
        268: ("consumables", "red potion"),
        269: ("consumables", "yellow potion"),
        270: ("consumables", "yellow potion"),
        271: ("consumables", "yellow potion"),
        272: ("consumables", "yellow potion"),
        273: ("consumables", "red vial"),
        274: ("consumables", "red vial"),
        275: ("consumables", "red vial"),
        276: ("consumables", "red potion"),
        277: ("consumables", "red potion"),
        278: ("consumables", "red crystal"),
        279: ("consumables", "red jar"),
        280: ("consumables", "pink flask"),
        281: ("consumables", "red jar"),
        282: ("consumables", "red jar"),
        283: ("consumables", "red splash potion"),
        284: ("consumables", "red jar"),
        285: ("consumables", "red jar"),
        286: ("consumables", "red jar"),
        287: ("consumables", "red jar"),
        288: ("consumables", "red bottle"),
        289: ("documents", "red book"),
        290: ("documents", "black book"),
        297: ("documents", "purple scroll"),
        298: ("documents", "blue scroll"),
        299: ("documents", "green scroll"),
        300: ("documents", "gray scroll"),
    },
}


def opaque_visual_metadata(pack: str, key: str) -> VisualMetadata | None:
    if not key.isdigit() or pack not in {"pack_1", "pack_2"}:
        return None
    number = int(key)
    if number in EXACT_OPAQUE_SUBJECTS.get(pack, {}):
        category, subject = EXACT_OPAQUE_SUBJECTS[pack][number]
        return visual_exact(category, subject)
    ranges: dict[str, list[tuple[int, int, str, str]]] = {
        "pack_1": [
            (1, 12, "weapons", "sword"),
            (13, 20, "weapons", "dagger"),
            (21, 28, "weapons", "battle axe"),
            (29, 40, "tools", "pickaxe"),
            (41, 50, "weapons", "axe"),
            (51, 64, "weapons", "hammer"),
            (65, 70, "weapons", "mace"),
            (71, 90, "magic", "magic staff"),
            (91, 114, "magic", "magic wand"),
            (115, 124, "weapons", "spear"),
            (125, 130, "weapons", "arrow"),
            (131, 146, "magic", "magic wand"),
            (147, 156, "weapons", "sword"),
            (157, 165, "weapons", "dagger"),
            (166, 170, "jewelry", "medal"),
            (171, 188, "weapons", "sword"),
            (189, 196, "weapons", "dagger"),
            (197, 200, "armor", "shield"),
            (201, 220, "armor", "helmet"),
            (221, 240, "clothing", "robe"),
            (241, 260, "armor", "boots"),
            (261, 280, "armor", "glove"),
            (281, 300, "accessories", "mask"),
            (301, 320, "accessories", "hat"),
            (321, 335, "food", "fruit"),
            (336, 340, "food", "watermelon"),
            (341, 345, "food", "vegetable"),
            (346, 350, "food", "dessert"),
            (351, 360, "food", "prepared food"),
            (361, 370, "food", "ingredient"),
            (371, 380, "food", "fruit"),
            (381, 390, "food", "vegetable"),
            (391, 400, "food", "dessert"),
            (401, 410, "food", "prepared food"),
            (411, 420, "food", "vegetable"),
            (421, 430, "food", "bread"),
            (431, 450, "food", "meat"),
            (451, 470, "food", "seafood"),
            (471, 490, "food", "fruit"),
            (491, 498, "food", "prepared food"),
            (499, 500, "magic", "magic vial"),
            (501, 510, "food", "drink"),
            (511, 520, "food", "dessert"),
            (521, 530, "food", "ingredient"),
            (531, 545, "gems", "gem"),
            (546, 560, "gems", "crystal"),
            (561, 570, "documents", "book"),
            (571, 580, "armor", "armor"),
            (581, 590, "armor", "boots"),
            (591, 600, "magic", "gift"),
            (601, 620, "magic", "orb"),
            (621, 640, "documents", "paper"),
            (641, 660, "adventure", "satchel"),
            (661, 680, "tools", "tool"),
            (681, 690, "tools", "device"),
            (691, 694, "keys", "key"),
            (695, 700, "weapons", "arrow"),
            (701, 720, "tools", "tool"),
            (721, 740, "keys", "key"),
            (741, 760, "world_objects", "box"),
            (761, 780, "weapons", "weapon"),
            (781, 790, "creature_parts", "wing"),
            (791, 800, "ui", "dice"),
            (801, 820, "weapons", "sword"),
            (821, 840, "weapons", "dagger"),
            (841, 860, "magic", "magic wand"),
            (861, 880, "accessories", "hat"),
            (881, 900, "materials", "feather"),
            (901, 920, "tools", "device"),
            (921, 940, "documents", "book"),
            (941, 960, "world_objects", "box"),
            (961, 980, "materials", "material"),
            (981, 1000, "flora", "plant"),
            (1001, 1020, "flora", "flower"),
            (1021, 1043, "flora", "herb"),
            (1044, 1080, "fish", "fish"),
            (1081, 1100, "fish", "aquatic creature"),
            (1101, 1120, "materials", "material"),
            (1121, 1145, "magic", "magical component"),
            (1146, 1159, "jewelry", "ring"),
            (1160, 1166, "creature_parts", "bone"),
            (1167, 1170, "creature_parts", "skull"),
            (1171, 1174, "creature_parts", "eye"),
            (1175, 1195, "creature_parts", "feather"),
            (1196, 1215, "creature_parts", "tail"),
            (1216, 1230, "creature_parts", "shell"),
            (1231, 1244, "creature_parts", "claw"),
        ],
        "pack_2": [
            (1, 20, "world_objects", "bag"),
            (21, 40, "armor", "helmet"),
            (41, 60, "world_objects", "chest"),
            (61, 80, "documents", "map"),
            (81, 100, "world_objects", "trophy"),
            (101, 120, "keys", "key"),
            (121, 145, "world_objects", "building"),
            (146, 170, "gems", "gem"),
            (171, 192, "gems", "crystal"),
            (193, 210, "materials", "bone"),
            (211, 230, "creature_parts", "creature part"),
            (231, 240, "materials", "crafted material"),
            (241, 264, "tools", "gear"),
            (265, 280, "consumables", "potion"),
            (281, 300, "documents", "scroll"),
            (301, 313, "documents", "book"),
            (314, 334, "magic", "stone rune"),
            (335, 360, "magic", "magical accessory"),
            (361, 380, "magic", "wisp"),
            (381, 400, "tools", "fishing hook"),
            (401, 420, "tools", "tool"),
            (421, 440, "food", "fruit"),
            (441, 465, "food", "vegetable"),
            (466, 485, "food", "meat"),
            (486, 505, "food", "bread"),
            (506, 525, "food", "drink"),
            (526, 545, "food", "cake"),
            (546, 565, "food", "egg"),
            (566, 585, "food", "dessert"),
            (586, 600, "food", "ingredient"),
            (601, 620, "ui", "status arrow"),
            (621, 640, "ui", "shield icon"),
            (641, 660, "ui", "heart icon"),
            (661, 680, "ui", "attribute icon"),
            (681, 700, "ui", "interface glyph"),
            (701, 720, "skills", "element glyph"),
            (721, 760, "skills", "spell effect"),
            (761, 800, "skills", "projectile effect"),
            (801, 850, "skills", "ability glyph"),
            (851, 870, "armor", "helmet"),
            (871, 885, "armor", "shield"),
            (886, 900, "armor", "armor"),
            (901, 980, "skills", "element glyph"),
            (981, 1060, "skills", "spell effect"),
            (1061, 1140, "skills", "ability glyph"),
            (1141, 1220, "skills", "green spell effect"),
            (1221, 1300, "skills", "red spell effect"),
            (1301, 1380, "skills", "orange spell effect"),
            (1381, 1440, "skills", "dark spell effect"),
            (1441, 1480, "weapons", "sword"),
            (1481, 1520, "weapons", "dagger"),
            (1521, 1560, "weapons", "axe"),
            (1561, 1600, "tools", "hammer"),
            (1601, 1640, "weapons", "spear"),
            (1641, 1680, "weapons", "bow"),
            (1681, 1720, "magic", "magic staff"),
            (1721, 1760, "magic", "magic wand"),
            (1761, 1810, "weapons", "weapon"),
            (1811, 1825, "jewelry", "ring"),
            (1826, 1840, "jewelry", "medal"),
            (1841, 1860, "jewelry", "amulet"),
            (1861, 1900, "armor", "helmet"),
            (1901, 1940, "armor", "armor"),
            (1941, 1980, "clothing", "hat"),
            (1981, 2020, "clothing", "boots"),
            (2021, 2060, "clothing", "robe"),
            (2061, 2112, "clothing", "shirt"),
            (2113, 2144, "gems", "gem"),
            (2145, 2160, "gems", "blue crystal shard"),
            (2161, 2170, "clothing", "shoes"),
            (2171, 2180, "accessories", "belt"),
            (2181, 2192, "jewelry", "ring"),
        ],
    }
    return range_lookup(pack, number, ranges[pack])


PACK_3_SUBJECTS = {
    1: "short sword", 2: "curved blade", 3: "magic wand", 4: "sickle",
    5: "sword", 6: "shovel", 7: "pickaxe", 8: "golden sword",
    9: "arrow", 10: "fishing rod", 11: "helmet", 12: "statue",
    13: "armor", 14: "coin", 15: "anvil", 16: "helmet",
    17: "hook", 18: "magic wand", 19: "cauldron", 20: "scissors",
    21: "red potion", 22: "pink potion", 23: "blue potion",
    24: "green bottle", 25: "clover", 26: "orange gem", 27: "chest",
    28: "key", 29: "meat", 30: "magic wand", 31: "apple",
    32: "carrot", 33: "sunflower", 34: "save scroll", 35: "gear",
    36: "speech bubble", 37: "heart", 38: "crescent moon",
    39: "feather", 40: "meat", 41: "white flame", 42: "orange flame",
    43: "white flame", 44: "compass", 45: "medal", 46: "target",
    47: "healing cross", 48: "sword", 49: "bomb", 50: "sword",
    51: "dung pile", 52: "target", 53: "brown inventory slot",
    54: "gray inventory slot", 55: "brown inventory slot",
}


def subject_category(subject: str) -> str:
    return inferred_category("named", subject)


def visual_metadata(pack: str, key: str, source_name: str) -> VisualMetadata:
    opaque = opaque_visual_metadata(pack, key)
    if opaque is not None:
        return opaque
    if pack == "pack_3" and key.isdigit():
        subject = PACK_3_SUBJECTS[int(key)]
        category = "ui" if "slot" in subject or subject in {"speech bubble", "save scroll"} else subject_category(subject)
        return visual_exact(category, subject)
    category = inferred_category(pack, source_name)
    description = slug(source_name)
    for suffix in ("_rare_collectibles_big_wander", "_big_wander"):
        description = description.removesuffix(suffix)
    if pack == "portrait":
        description = f"character portrait {key}"
    return VisualMetadata(category, description.replace("_", " "), "source_filename")


@dataclass(frozen=True)
class Source:
    path: Path
    pack: str
    key: str
    width: int
    height: int
    mode: str
    sha256: str

    @property
    def dimensions(self) -> str:
        return f"{self.width}x{self.height}"


def scan(source_root: Path) -> list[Source]:
    result: list[Source] = []
    for path in sorted(source_root.rglob("*.png")):
        relative = path.relative_to(source_root)
        pack = slug(relative.parts[0])
        with Image.open(path) as image:
            width, height = image.size
            mode = image.mode
        if width > 192 or height > 66:
            raise ValueError(f"unsupported resource dimensions {width}x{height}: {path}")
        result.append(Source(path, pack, base_key(path), width, height, mode, digest(path)))
    return result


def build(source_root: Path, output_root: Path) -> None:
    sources = scan(source_root)
    grouped: dict[tuple[str, str], list[Source]] = defaultdict(list)
    for source in sources:
        grouped[(source.pack, source.key)].append(source)

    if output_root.exists():
        shutil.rmtree(output_root)
    output_root.mkdir(parents=True)
    records = []
    for (pack, key), variants in sorted(grouped.items()):
        metadata = visual_metadata(pack, key, variants[0].path.stem)
        category = metadata.category
        description = metadata.description
        # Pack-qualified names preserve distinct same-numbered/similar assets.
        subject_slug = slug(description)
        stable_key = f"{int(key):04}" if key.isdigit() else key
        asset_name = f"{pack}_{subject_slug}_{stable_key}"
        sizes = {}
        for variant in sorted(variants, key=lambda value: value.width):
            target = output_root / category / variant.dimensions / f"{asset_name}.png"
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(variant.path, target)
            sizes[variant.dimensions] = target.relative_to(output_root).as_posix()
        records.append({
            "id": f"{pack}/{stable_key}",
            "category": category,
            "name": asset_name,
            "description": description,
            "labelSource": metadata.label_source,
            "sourcePack": pack,
            "sourceFiles": [str(v.path.relative_to(source_root)).replace("\\", "/") for v in variants],
            "sizes": sizes,
            "sha256ByDimensions": {v.dimensions: v.sha256 for v in variants},
            "pixelModeByDimensions": {v.dimensions: v.mode for v in variants},
        })
    manifest = {
        "schemaVersion": 1,
        "assetRoot": "textures/gui/library",
        "provenance": "User-supplied local asset packs; source identity retained per asset.",
        "assetCount": len(records),
        "fileCount": len(sources),
        "supportedIconSizes": sorted(SUPPORTED_ICON_SIZES),
        "assets": records,
    }
    (output_root / "manifest.json").write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("source", type=Path)
    parser.add_argument("output", type=Path)
    args = parser.parse_args()
    build(args.source.resolve(), args.output.resolve())


if __name__ == "__main__":
    main()
