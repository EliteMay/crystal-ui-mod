# Verify the exact Minecraft runtime methods used by the built Crystal UI JAR.
# Read-only validation; does not start Minecraft or touch saves.
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$jar = Join-Path $root 'build\libs\prismglass-0.9.0.jar'
$runtime = Join-Path $env:APPDATA 'ATLauncher\libraries\net\minecraftforge\forge\1.21.1-52.1.0\forge-1.21.1-52.1.0-client.jar'
$jdk = 'D:\AI\tools\runtimes\prismglass-jdk21\jdk-21.0.12.1+1\bin\javap.exe'
foreach ($path in @($jar, $runtime, $jdk)) {
    if (!(Test-Path -LiteralPath $path -PathType Leaf)) { throw "Missing mapping test input: $path" }
}
$vanilla = (& $jdk -classpath $runtime net.minecraft.resources.ResourceLocation 2>&1 | Out-String)
if ($LASTEXITCODE -ne 0) { throw 'Cannot inspect ATLauncher Minecraft runtime' }
if ($vanilla -notmatch 'ResourceLocation fromNamespaceAndPath\(java.lang.String, java.lang.String\)') {
    throw 'Actual ATLauncher ResourceLocation API differs: check runtime first'
}
if ($vanilla -match 'ResourceLocation m_339182_\(') {
    throw 'Actual runtime unexpectedly has SRG ResourceLocation method: check mappings'
}
$mod = (& $jdk -classpath $jar -c dev.kaito.prismglass.GlassShaders 2>&1 | Out-String)
if ($LASTEXITCODE -ne 0) { throw 'Cannot inspect compiled Prism Glass shader registration' }
if ($mod -notmatch 'ResourceLocation.fromNamespaceAndPath:') {
    throw 'Compiled Prism Glass JAR does not call the runtime ResourceLocation method'
}
if ($mod -match 'ResourceLocation.m_339182_:') {
    throw 'Regression: production JAR contains the crashing SRG call'
}
Write-Output 'PASS: Prism Glass method call matches Forge 52.1.0 ResourceLocation runtime'
