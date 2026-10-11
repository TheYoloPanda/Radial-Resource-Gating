# Radial Resource Gating

Radial Resource Gating is a NeoForge 1.21.1 mod for modpack makers.

It constrains ore generation around the world spawn or configured dimension
centers. Each dimension can block exact block ids inside its configured area.
Outside that area, ore features whose generated blocks match the global block
tags or block ids are cancelled.

The mod is intended to be invisible to players. Its main public surface is the
common TOML config.

## What It Does

- Supports the Overworld, Nether, and End.
- Uses the world spawn as the default center, with manual centers available.
- Uses a separate center mode and range for each dimension.
- Blocks configured exact block ids inside each dimension's square area.
- Blocks ore features outside the configured square area.
- Matches blocked ores through block tags and explicit block ids.
- Allows specific blocks to generate outside the area through an override list.
- Applies only to newly generated chunks.

Normal ore features are decided from their origin. Inside, one target matching
`insideBlockedBlocks` cancels the whole feature. Outside, one target matching
the global outside rules cancels it. The border is not cut block-by-block.

## Configuration

After the mod runs once, NeoForge creates:

```text
config/radialresourcegating-common.toml
```

Default config:

```toml
blockedTags = ["c:ores"]
additionalBlockedBlocks = []
outsideAllowedBlocks = []

[overworld]
enabled = true
centerMode = "world_spawn"
centerX = 0
centerZ = 0
range = 256
insideBlockedBlocks = []

[nether]
enabled = true
centerMode = "world_spawn"
centerX = 0
centerZ = 0
range = 256
insideBlockedBlocks = []

[end]
enabled = true
centerMode = "world_spawn"
centerX = 0
centerZ = 0
range = 256
insideBlockedBlocks = []
```

## Dimension Areas

Each dimension section controls one square area:

- `enabled`: turns gating on or off for that dimension.
- `centerMode`: `world_spawn` uses the saved Overworld spawn; `fixed` uses
  `centerX` and `centerZ`.
- `centerX`: center X coordinate of the allowed area when `centerMode = "fixed"`.
- `centerZ`: center Z coordinate of the allowed area when `centerMode = "fixed"`.
- `range`: distance from the center to each side of the square.
- `insideBlockedBlocks`: exact block ids blocked when a normal feature's origin
  is inside this dimension's square.

With `centerMode = "world_spawn"` and `range = 256`, coordinates within 256
blocks of the saved world spawn are inside the gate. In the Nether and other
scaled dimensions, the spawn center is converted with Minecraft's dimension
coordinate scale, so an Overworld spawn at `X 4000`, `Z 800` centers the Nether
gate around `X 500`, `Z 100`.

With `centerMode = "fixed"`, `centerX = 0`, `centerZ = 0`, and `range = 256`,
the inside square runs from `X -256` to `X 256` and from `Z -256` to `Z 256`.

## Inside Matching

`insideBlockedBlocks` is separate for the Overworld, Nether, and End. It uses
exact block ids, not tags. An empty list preserves the pre-0.2.0 behavior.

```toml
[overworld]
insideBlockedBlocks = [
    "minecraft:diamond_ore",
    "minecraft:deepslate_diamond_ore"
]
```

For normal `OreFeature` and `ScatteredOreFeature` configurations, one matching
generated target cancels the whole feature when its origin is inside. The
outside lists are ignored for this internal decision.

Vanilla large iron and copper veins use a per-block noise hook. Inside, each
result from `OreVeinifier` is compared exactly with `insideBlockedBlocks`.
Listing `minecraft:deepslate_iron_ore` does not also remove
`minecraft:raw_iron_block` or `minecraft:tuff`; list every result to remove.

## Outside Matching

`blockedTags` controls which generated ore blocks are blocked outside the area.
By default, the mod blocks blocks tagged as `c:ores`.

```toml
blockedTags = ["c:ores"]
```

`additionalBlockedBlocks` adds exact blocks that should also be blocked outside
the area, even if they are not covered by a tag.

```toml
additionalBlockedBlocks = ["examplemod:rich_copper_ore"]
```

`outsideAllowedBlocks` is an override. If a generated block is listed here, it is
allowed outside the area even if it also matches `blockedTags` or
`additionalBlockedBlocks`.

```toml
outsideAllowedBlocks = ["minecraft:ancient_debris"]
```

Invalid block ids are ignored and logged once during config loading. Normal
generation-time behavior stays silent.

The three outside lists are global, while `insideBlockedBlocks` is
dimension-specific. To make diamond available only outside the Overworld
square, configure both surfaces:

```toml
outsideAllowedBlocks = [
    "minecraft:diamond_ore",
    "minecraft:deepslate_diamond_ore"
]

[overworld]
insideBlockedBlocks = [
    "minecraft:diamond_ore",
    "minecraft:deepslate_diamond_ore"
]
```

Without the global `outsideAllowedBlocks` entries, the default `c:ores` tag
normally blocks those ores outside too, removing them everywhere. The global
allowlist also applies in every other enabled dimension.

## Notes For Pack Makers

This mod affects new world generation only. Existing chunks are not changed.

For testing, generate fresh chunks after changing the config. If you test in
already generated terrain, old ore placement will still be there.

Setting a dimension's `enabled = false` disables both its inside and outside
rules and leaves vanilla generation untouched.
