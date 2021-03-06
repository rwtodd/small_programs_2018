# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# A script to write binary bytes to a file, as-is.
# Usage: write-bin.ps1 FILENAME XX XX XX XX ...
# 
# Where XX are hex digits (e.g., 2f a9 etc.)
# The file is always written relative to the current
# directory.
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
param([string] $fname)

$fullpath = Join-Path (Get-Location) $fname
$bytes = foreach ($xx in $args) {
    [Convert]::ToUint32($xx,16)
}

[IO.File]::WriteAllBytes($fullpath, $bytes)

