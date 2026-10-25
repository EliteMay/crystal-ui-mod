# Regression guard for GUI expansion. No Minecraft launches, world writes or keyboard input.
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$src = Join-Path $root 'src\main\java\dev\kaito\prismglass'
$events = Get-Content -LiteralPath (Join-Path $src 'ClientScreenEvents.java') -Raw
$classes = @(
    @('InventoryScreen','FrostedInventoryScreen'),
    @('ContainerScreen','FrostedChestScreen'),
    @('CraftingScreen','FrostedCraftingScreen'),
    @('HopperScreen','FrostedHopperScreen'),
    @('DispenserScreen','FrostedDispenserScreen'),
    @('ShulkerBoxScreen','FrostedShulkerBoxScreen')
)
foreach ($pair in $classes) {
    $original = $pair[0]
    $replacement = $pair[1]
    if (-not $events.Contains('screen.getClass() == ' + $original + '.class')) {
        throw "Missing exact vanilla-only screen guard: $original"
    }
    if (-not $events.Contains('new ' + $replacement + '(')) {
        throw "Missing replacement construction: $replacement"
    }
    $source = Get-Content -LiteralPath (Join-Path $src ($replacement+'.java')) -Raw
    if ($source -notmatch 'extends (InventoryScreen|ContainerScreen|CraftingScreen|HopperScreen|DispenserScreen|ShulkerBoxScreen)') {
        throw "Replacement does not inherit vanilla screen: $replacement"
    }
    if ($source -notmatch 'super\.renderBg\(' -or $source -notmatch 'GlassContainerSkin\.draw\(' -and $replacement -ne 'FrostedInventoryScreen') {
        if ($replacement -ne 'FrostedInventoryScreen') {
            throw "Vanilla-disabled fallback or glass background missing: $replacement"
        }
    }
    if ($source -match '(?m)\b(?:void|boolean)\s+(?:mouseClicked|slotClicked|mouseReleased|mouseDragged|handleSlotClick|keyPressed)\s*\(') {
        throw "Replacement overrides interactive input unexpectedly: $replacement"
    }
}
$decor = Get-Content -LiteralPath (Join-Path $src 'CrystalGlobalDecor.java') -Raw
if ($decor -notmatch 'className\.startsWith\("net\.minecraft\.client\.gui\.screens\."') {
    throw 'Global decoration not limited to Minecraft vanilla screen namespace'
}
if ($decor -notmatch 'isEnabled\(\)' -or $decor -notmatch 'isExcluded\(') {
    throw 'Disable and critical-screen exclusions missing'
}
if ($events -notmatch 'onRenderPost\(' -or $events -notmatch 'ScreenEvent\.Render\.Post') {
    throw 'Global screen render event is missing'
}
if ($events -notmatch 'screen\.getClass\(\) == TitleScreen\.class' -or
    $events -notmatch 'screen\.getClass\(\) == PauseScreen\.class') {
    throw 'Quick settings access from title and pause menu missing'
}
Write-Output ('PASS: '+ $classes.Count + ' exact vanilla screen replacements; global chrome; no input overrides')
