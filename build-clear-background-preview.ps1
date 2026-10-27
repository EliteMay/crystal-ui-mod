# Build an opt-in, test-only clear background pack from the user-owned Forge client JAR.
# No world / settings / resource-pack selection is changed.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.IO.Compression.FileSystem
$client = Join-Path $env:APPDATA 'ATLauncher\libraries\net\minecraftforge\forge\1.21.1-52.1.0\forge-1.21.1-52.1.0-client.jar'
$target = 'D:\AI\exports\MinecraftMods\PrismGlass_ClearBackground_Preview_1.21.1.zip'
if (!(Test-Path -LiteralPath $client -PathType Leaf)) { throw 'Forge client JAR not found' }
if (Test-Path -LiteralPath $target) { throw 'Preview ZIP already exists; refusing overwrite' }
$zipSource = [IO.Compression.ZipFile]::OpenRead($client)
$preview = [IO.Compression.ZipFile]::Open($target, [IO.Compression.ZipArchiveMode]::Create)
try {
    $meta = '{"pack":{"pack_format":34,"description":"Prism Glass experimental clear special-screen backgrounds (MC 1.21.1). Enable manually for modtest."}}'
    $mc = $preview.CreateEntry('pack.mcmeta')
    $mw = [IO.StreamWriter]::new($mc.Open(), [Text.UTF8Encoding]::new($false))
    try { $mw.Write($meta) } finally { $mw.Dispose() }
    foreach ($name in @('enchanting_table','stonecutter','smithing','grindstone')) {
        $entryPath = "assets/minecraft/textures/gui/container/$name.png"
        $original = $zipSource.GetEntry($entryPath)
        if (!$original) { throw "Vanilla GUI texture not found: $name" }
        $ms = [IO.MemoryStream]::new()
        $stream = $original.Open()
        try { $stream.CopyTo($ms) } finally { $stream.Dispose() }
        $ms.Position=0
        $originalBmp = [Drawing.Bitmap]::new($ms)
        try {
            if ($originalBmp.Width -ne 256 -or $originalBmp.Height -ne 256) {
                throw "Unexpected texture dimensions for $name"
            }
            $bmp = [Drawing.Bitmap]::new(256,256,[Drawing.Imaging.PixelFormat]::Format32bppArgb)
            try {
                # These 1.21.1 GUI screens draw their main background from the
                # top-left 176x166 pixels. Clear only that opaque panel, not
                # textures elsewhere in the atlas.
                for ($y=0; $y -lt 256; $y++) {
                    for ($x=0; $x -lt 256; $x++) {
                        if ($x -lt 176 -and $y -lt 166) {
                            $bmp.SetPixel($x,$y,[Drawing.Color]::Transparent)
                        } else {
                            $bmp.SetPixel($x,$y,$originalBmp.GetPixel($x,$y))
                        }
                    }
                }
                $entry = $preview.CreateEntry($entryPath,[IO.Compression.CompressionLevel]::Optimal)
                $png = [IO.MemoryStream]::new()
                try {
                    $bmp.Save($png,[Drawing.Imaging.ImageFormat]::Png)
                    $png.Position=0
                    $dest=$entry.Open()
                    try { $png.CopyTo($dest) } finally { $dest.Dispose() }
                } finally { $png.Dispose() }
                "CLEAR_TEXTURE=$name"
            } finally { $bmp.Dispose() }
        } finally { $originalBmp.Dispose();$ms.Dispose() }
    }
} finally { $preview.Dispose(); $zipSource.Dispose() }
$z = [IO.Compression.ZipFile]::OpenRead($target)
try {
    if ($z.Entries.Count -ne 5) { throw 'Unexpected preview ZIP entries' }
    foreach ($e in $z.Entries) {
        if ($e.Length -le 0) { throw "Empty pack entry $($e.FullName)" }
    }
    "PREVIEW_ENTRIES=$($z.Entries.Count)"
} finally { $z.Dispose() }
"PREVIEW_ZIP=$target"
"PREVIEW_SHA256=$((Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash)"
