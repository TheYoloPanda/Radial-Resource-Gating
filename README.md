# Radial Resource Gating

Radial Resource Gating is a NeoForge 1.21.1 mod for modpack makers.

It constrains ore generation around the world spawn or configured dimension
centers. Inside the configured area, normal ore generation is left alone.
Outside that area, ore features whose generated blocks match the configured
block tags or block ids are cancelled.

The mod is intended to be invisible to players. Its main public surface is the
common TOML config.

## What It Does

- Supports the Overworld, Nether, and End.
- Uses the world spawn as the default center, with manual centers available.
- Uses a separate center mode and range for each dimension.
- Blocks ore features outside the configured square area.
- Matches blocked ores through block tags and explicit block ids.
- Allows specific blocks to generate outside the area through an override list.
- Applies only to newly generated chunks.

The gate is based on the ore feature origin. If the origin is inside the allowed
area, the whole feature is allowed. If the origin is outside, the whole feature
is cancelled. This keeps generation fast, but the border is not cut
block-by-block.

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

[nether]
enabled = true
centerMode = "world_spawn"
centerX = 0
centerZ = 0
range = 256

[end]
enabled = true
centerMode = "world_spawn"
centerX = 0
centerZ = 0
range = 256
```

## Dimension Areas

Each dimension section controls one square area:

- `enabled`: turns gating on or off for that dimension.
- `centerMode`: `world_spawn` uses the saved Overworld spawn; `fixed` uses
  `centerX` and `centerZ`.
- `centerX`: center X coordinate of the allowed area when `centerMode = "fixed"`.
- `centerZ`: center Z coordinate of the allowed area when `centerMode = "fixed"`.
- `range`: distance from the center to each side of the square.

With `centerMode = "world_spawn"` and `range = 256`, ore generation is allowed
within 256 blocks of the saved world spawn. In the Nether and other scaled
dimensions, the spawn center is converted with Minecraft's dimension coordinate
scale, so an Overworld spawn at `X 4000`, `Z 800` centers the Nether gate around
`X 500`, `Z 100`.

With `centerMode = "fixed"`, `centerX = 0`, `centerZ = 0`, and `range = 256`,
ore generation is allowed from `X -256` to `X 256` and from `Z -256` to `Z 256`.

## Ore Matching

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

## Notes For Pack Makers

This mod affects new world generation only. Existing chunks are not changed.

For testing, generate fresh chunks after changing the config. If you test in
already generated terrain, old ore placement will still be there.
