# Changelog

## [0.2.0]

### Added

- Added `insideBlockedBlocks` to each vanilla dimension, allowing specific
  blocks to be disabled inside its configured area.
- Added exact inside filtering for vanilla large iron and copper veins. Ore,
  raw blocks, and filler rock can be configured independently.

### Compatibility

- Existing `0.1.0` configs receive empty `insideBlockedBlocks` lists
  automatically, preserving their previous generation behavior.
- Existing chunks are unchanged; all gating still applies only to newly
  generated chunks.

## [0.1.0]

### Added

- Initial development release.
- Added configurable ore gating around the world spawn or fixed coordinates.
- Added separate settings for the Overworld, Nether, and End.
- Added outside matching through block tags, exact block ids, and an allowlist.
