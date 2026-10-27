param(
    [Parameter(Mandatory = $true)][string]$Device,
    [string]$Adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
    [int]$AdbServerPort = 5037,
    [ValidateRange(1, 10)][int]$Repetitions = 3,
    [ValidateRange(1000, 30000)][int]$DurationMs = 2000,
    [ValidateSet('BlurryGlass', 'Acrylic', 'Mica', 'Premium')]
    [string[]]$Materials = @('BlurryGlass', 'Acrylic', 'Mica', 'Premium'),
    [ValidateSet('Small', 'Large', 'Shared')]
    [string[]]$Scenes = @('Small', 'Large', 'Shared'),
    [switch]$SkipBuild,
    [string]$TargetApk,
    [string]$Label = 'current',
    [string]$BaselineReport
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
if (-not $TargetApk) {
    $TargetApk = Join-Path $projectRoot 'androidApp/build/outputs/apk/benchmark/androidApp-benchmark.apk'
}
$testApk = Join-Path $projectRoot 'androidApp/build/outputs/apk/androidTest/benchmark/androidApp-benchmark-androidTest.apk'
$outputRoot = Join-Path $projectRoot 'androidApp/build/reports/material-cache-performance'
$runDirectory = Join-Path $outputRoot (Get-Date -Format 'yyyyMMdd-HHmmss')
New-Item -ItemType Directory -Path $runDirectory -Force | Out-Null

function Invoke-AdbChecked {
    param([string[]]$Arguments)
    $output = & $Adb -P $AdbServerPort -s $Device @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "adb failed: $Arguments`n$output"
    }
    return $output
}

function Get-Median {
    param([double[]]$Values)
    $sorted = @($Values | Sort-Object)
    $middle = [int][Math]::Floor($sorted.Count / 2)
    if ($sorted.Count % 2 -eq 1) {
        return $sorted[$middle]
    }
    return ($sorted[$middle - 1] + $sorted[$middle]) / 2
}

Push-Location $projectRoot
try {
    if (-not $SkipBuild) {
        & .\gradlew.bat -PmaterialBenchmark=true :androidApp:assembleBenchmark `
            :androidApp:assembleBenchmarkAndroidTest --console=plain
        if ($LASTEXITCODE -ne 0) {
            throw 'Benchmark build failed'
        }
    }

    $metadata = [ordered]@{
        device = $Device
        model = (Invoke-AdbChecked @('shell', 'getprop', 'ro.product.model')) -join "`n"
        android = (Invoke-AdbChecked @('shell', 'getprop', 'ro.build.version.release')) -join "`n"
        size = (Invoke-AdbChecked @('shell', 'wm', 'size')) -join "`n"
        density = (Invoke-AdbChecked @('shell', 'wm', 'density')) -join "`n"
        graphics = (Invoke-AdbChecked @('shell', 'dumpsys', 'SurfaceFlinger') |
            Select-String 'GLES:|refresh-rate') -join "`n"
        repetitions = $Repetitions
        durationMs = $DurationMs
        build = 'benchmark: non-debuggable, release fallback, no minification'
        startedAt = (Get-Date).ToString('o')
        label = $Label
        sourceRevision = (git rev-parse HEAD)
        sourceDirty = @(git status --porcelain).Count -gt 0
        apkSha256 = (Get-FileHash -LiteralPath $TargetApk).Hash
        testApkSha256 = (Get-FileHash -LiteralPath $testApk).Hash
        summaryPssCheckpoint = 'memoryAfter: after measured animation and requested GC'
        totalFrames = 0
    }
    $metadata | ConvertTo-Json | Set-Content (Join-Path $runDirectory 'environment.json')
    Invoke-AdbChecked @('install', '-r', $TargetApk)
    Invoke-AdbChecked @('install', '-r', $testApk)

    $validation = Invoke-AdbChecked @(
        'shell', 'am', 'instrument', '-w', '-r',
        '-e', 'class', 'com.moriafly.salt.sample.ui.MaterialHardwareRenderingTest',
        'com.moriafly.salt.ui.test/androidx.test.runner.AndroidJUnitRunner'
    )
    $validation | Set-Content (Join-Path $runDirectory 'correctness.log')
    if (-not ($validation -match '^OK \(\d+ tests?\)\s*$')) {
        throw "Material rendering validation failed. See $runDirectory/correctness.log"
    }

    $samples = [System.Collections.Generic.List[object]]::new()
    $sampleCount = $Materials.Count * $Scenes.Count * $Repetitions * 2
    foreach ($material in $Materials) {
        foreach ($scene in $Scenes) {
            for ($iteration = 1; $iteration -le $Repetitions; $iteration++) {
                $modes = if ($iteration % 2 -eq 1) {
                    @('Dynamic', 'Static')
                } else {
                    @('Static', 'Dynamic')
                }
                foreach ($mode in $modes) {
                    Invoke-AdbChecked @('shell', 'am', 'force-stop', 'com.moriafly.salt.ui') |
                        Out-Null
                    $sampleName = "$material-$scene-$iteration-$mode"
                    $output = Invoke-AdbChecked @(
                        'shell', 'am', 'instrument', '-w', '-r',
                        '-e', 'class', 'com.moriafly.salt.sample.ui.MaterialPerformanceBenchmark',
                        '-e', 'materialBenchmark', 'true',
                        '-e', 'materialType', $material,
                        '-e', 'scene', $scene,
                        '-e', 'mode', $mode,
                        '-e', 'iteration', "$iteration",
                        '-e', 'durationMs', "$DurationMs",
                        'com.moriafly.salt.ui.test/androidx.test.runner.AndroidJUnitRunner'
                    )
                    $output | Set-Content (Join-Path $runDirectory "$sampleName.log")
                    $jsonLine = $output | Where-Object {
                        $_ -match '^INSTRUMENTATION_STATUS: materialBenchmark='
                    } | Select-Object -Last 1
                    if (-not $jsonLine -or -not ($output -match '^OK \(\d+ tests?\)\s*$')) {
                        throw "Benchmark failed: $sampleName. See $runDirectory"
                    }
                    $sample = $jsonLine.Substring($jsonLine.IndexOf('=') + 1) | ConvertFrom-Json
                    $samples.Add($sample)
                    $metadata.totalFrames += $sample.frameCount
                    $metadata | ConvertTo-Json |
                        Set-Content (Join-Path $runDirectory 'environment.json')
                    $samples.ToArray() | ConvertTo-Json -Depth 10 |
                        Set-Content (Join-Path $runDirectory 'samples.json')
                    Write-Output (
                        '[{0}/{1}] {2}: frames={3}, GPU median={4:N2} ms, P95={5:N2} ms, PSS={6:N1} MiB' -f
                        $samples.Count, $sampleCount, $sampleName, $sample.frameCount,
                        $sample.gpuMs.median, $sample.totalMs.p95,
                        ($sample.memoryAfter.'total-pssKb' / 1024)
                    )
                }
            }
        }
    }

    $summary = foreach ($material in $Materials) {
        foreach ($scene in $Scenes) {
            $matching = @($samples | Where-Object {
                $_.materialType -eq $material -and $_.scene -eq $scene
            })
            $dynamic = @($matching | Where-Object mode -eq 'Dynamic')
            $cached = @($matching | Where-Object mode -eq 'Static')
            $liveGpu = Get-Median @($dynamic | ForEach-Object { $_.gpuMs.median })
            $cachedGpu = Get-Median @($cached | ForEach-Object { $_.gpuMs.median })
            $liveP95 = Get-Median @($dynamic | ForEach-Object { $_.totalMs.p95 })
            $cachedP95 = Get-Median @($cached | ForEach-Object { $_.totalMs.p95 })
            $livePss = Get-Median @($dynamic | ForEach-Object { $_.memoryAfter.'total-pssKb' })
            $cachedPss = Get-Median @($cached | ForEach-Object { $_.memoryAfter.'total-pssKb' })
            $readbacks = @($cached | Where-Object { $_.readbackMs.Count -gt 0 })
            [pscustomobject][ordered]@{
                materialType = $material
                scene = $scene
                dynamicGpuMedianMs = $liveGpu
                staticGpuMedianMs = $cachedGpu
                gpuTimeReductionPercent = if ($liveGpu -gt 0) {
                    100 * ($liveGpu - $cachedGpu) / $liveGpu
                } else { $null }
                dynamicTotalP95Ms = $liveP95
                staticTotalP95Ms = $cachedP95
                totalP95ReductionPercent = 100 * ($liveP95 - $cachedP95) / $liveP95
                dynamicCpuMsPerFrame = Get-Median @($dynamic | ForEach-Object cpuMsPerFrame)
                staticCpuMsPerFrame = Get-Median @($cached | ForEach-Object cpuMsPerFrame)
                dynamicRenderedFps = Get-Median @($dynamic | ForEach-Object {
                    1000.0 * $_.frameCount / $_.wallMs
                })
                staticRenderedFps = Get-Median @($cached | ForEach-Object {
                    1000.0 * $_.frameCount / $_.wallMs
                })
                dynamicPssMiB = $livePss / 1024
                staticPssMiB = $cachedPss / 1024
                pssIncreaseMiB = ($cachedPss - $livePss) / 1024
                dynamicWarmupPssMiB = (Get-Median @($dynamic | ForEach-Object {
                    $_.memoryBefore.'total-pssKb'
                })) / 1024
                staticWarmupPssMiB = (Get-Median @($cached | ForEach-Object {
                    $_.memoryBefore.'total-pssKb'
                })) / 1024
                bitmapMiB = (Get-Median @($cached | ForEach-Object cacheBitmapBytes)) / 1MB
                cacheReadyMs = Get-Median @($cached | ForEach-Object cacheReadyMs)
                readbackTotalMs = if ($readbacks.Count -gt 0) {
                    Get-Median @($readbacks | ForEach-Object {
                        ($_.readbackMs | Measure-Object -Sum).Sum
                    })
                } else { $null }
                bakeFrameMaxMs = Get-Median @($cached | ForEach-Object { $_.bakeTotalMs.max })
                dynamicDeadlineMissPercent = Get-Median @($dynamic | ForEach-Object {
                    100.0 * $_.deadlineMisses / $_.frameCount
                })
                staticDeadlineMissPercent = Get-Median @($cached | ForEach-Object {
                    100.0 * $_.deadlineMisses / $_.frameCount
                })
            }
        }
    }
    $summary | ConvertTo-Json -Depth 5 | Set-Content (Join-Path $runDirectory 'summary.json')
    $summary | Export-Csv -NoTypeInformation (Join-Path $runDirectory 'summary.csv')
    $report = [System.Collections.Generic.List[string]]::new()
    $report.Add('# Material cache measurements')
    $report.Add('')
    $report.Add("Label: $Label. Device: $Device. $Repetitions repeats per mode and case.")
    $report.Add('Values are medians across runs; GPU reduction compares Dynamic with Static.')
    $report.Add('')
    $report.Add('| Material | Scene | GPU ms Dynamic / Static | Reduction | Frame P95 ms Dynamic / Static | PSS delta MiB | Bitmap MiB | Ready ms | Baking max frame ms |')
    $report.Add('|---|---|---:|---:|---:|---:|---:|---:|---:|')
    foreach ($row in $summary) {
        $report.Add((
            '| {0} | {1} | {2:N2} / {3:N2} | {4:N1}% | {5:N2} / {6:N2} | {7:N2} | {8:N2} | {9:N1} | {10:N1} |' -f
            $row.materialType, $row.scene, $row.dynamicGpuMedianMs, $row.staticGpuMedianMs,
            $row.gpuTimeReductionPercent, $row.dynamicTotalP95Ms, $row.staticTotalP95Ms,
            $row.pssIncreaseMiB, $row.bitmapMiB, $row.cacheReadyMs, $row.bakeFrameMaxMs
        ))
    }
    $report.Add('')
    $report.Add('Bitmap allocations and process PSS measure different things. Driver/GPU memory may be absent from emulator PSS; do not treat PSS delta as total graphics cost.')
    $report.Add('Cache readiness includes frame scheduling and the settle delay. Readback latency is separate; baking frame metrics measure visible-frame cost. A longest frame is not the same as a readback duration.')
    $report.Add('The bitmap budget excludes transient capture resources. Emulator/SwiftShader measurements do not predict device GPU performance.')
    $report.Add('Raw frames, memory checkpoints and readback durations: samples.json. APK hashes, source revision and total frame count: environment.json. Correctness gate: correctness.log.')
    $report | Set-Content (Join-Path $runDirectory 'REPORT.md')
    if ($BaselineReport) {
        $baselineEnvironment = Get-Content -Raw (Join-Path $BaselineReport 'environment.json') |
            ConvertFrom-Json
        if ($baselineEnvironment.testApkSha256 -ne $metadata.testApkSha256) {
            throw 'Comparison requires the same instrumentation APK; raw current results are saved.'
        }
        $baselineRows = Get-Content -Raw (Join-Path $BaselineReport 'summary.json') |
            ConvertFrom-Json
        $comparison = foreach ($row in $summary) {
            $before = $baselineRows | Where-Object {
                $_.materialType -eq $row.materialType -and $_.scene -eq $row.scene
            }
            if (@($before).Count -ne 1) {
                throw "No unique baseline for $($row.materialType)/$($row.scene)"
            }
            [pscustomobject][ordered]@{
                materialType = $row.materialType
                scene = $row.scene
                oldStaticGpuMs = $before.staticGpuMedianMs
                newDynamicGpuMs = $row.dynamicGpuMedianMs
                newStaticGpuMs = $row.staticGpuMedianMs
                newGpuSavedVsDynamicPercent = $row.gpuTimeReductionPercent
                staticGpuSavedVsOldPercent = 100 * (
                    $before.staticGpuMedianMs - $row.staticGpuMedianMs
                ) / $before.staticGpuMedianMs
                newDynamicP95Ms = $row.dynamicTotalP95Ms
                newStaticP95Ms = $row.staticTotalP95Ms
                oldBitmapMiB = $before.bitmapMiB
                newBitmapMiB = $row.bitmapMiB
                newDynamicPssMiB = $row.dynamicPssMiB
                newStaticPssMiB = $row.staticPssMiB
                newPssDeltaMiB = $row.pssIncreaseMiB
                oldReadyMs = $before.cacheReadyMs
                newReadyMs = $row.cacheReadyMs
                oldBakeFrameMaxMs = $before.bakeFrameMaxMs
                newBakeFrameMaxMs = $row.bakeFrameMaxMs
                newReadbackMs = $row.readbackTotalMs
            }
        }
        $comparison | ConvertTo-Json -Depth 5 |
            Set-Content (Join-Path $runDirectory 'comparison.json')
        $comparison | Export-Csv -NoTypeInformation (Join-Path $runDirectory 'comparison.csv')
        $comparisonReport = [System.Collections.Generic.List[string]]::new()
        $comparisonReport.Add('# Material cache: before and after review fixes')
        $comparisonReport.Add('')
        $comparisonReport.Add("Baseline: $BaselineReport. Current: $runDirectory.")
        $comparisonReport.Add("Both runs use the same instrumentation APK. Current run: $Repetitions repeats, $DurationMs ms per measurement. Check both environment.json files for APK hashes and protocol details.")
        $comparisonReport.Add('')
        $comparisonReport.Add('| Material | Scene | New GPU ms Dynamic / Static | GPU saved vs Dynamic | Old Static GPU ms | Static GPU saved vs old | New frame P95 ms Dynamic / Static |')
        $comparisonReport.Add('|---|---|---:|---:|---:|---:|---:|')
        foreach ($row in $comparison) {
            $comparisonReport.Add((
                '| {0} | {1} | {2:N2} / {3:N2} | {4:N1}% | {5:N2} | {6:N1}% | {7:N2} / {8:N2} |' -f
                $row.materialType, $row.scene, $row.newDynamicGpuMs, $row.newStaticGpuMs,
                $row.newGpuSavedVsDynamicPercent, $row.oldStaticGpuMs,
                $row.staticGpuSavedVsOldPercent, $row.newDynamicP95Ms, $row.newStaticP95Ms
            ))
        }
        $comparisonReport.Add('')
        $comparisonReport.Add('| Material | Scene | Bitmap MiB old / new | New PSS delta MiB vs Dynamic | Ready ms old / new | Baking max frame ms old / new | New readback ms |')
        $comparisonReport.Add('|---|---|---:|---:|---:|---:|---:|')
        foreach ($row in $comparison) {
            $comparisonReport.Add((
                '| {0} | {1} | {2:N2} / {3:N2} | {4:N2} | {5:N1} / {6:N1} | {7:N1} / {8:N1} | {9:N1} |' -f
                $row.materialType, $row.scene, $row.oldBitmapMiB, $row.newBitmapMiB,
                $row.newPssDeltaMiB, $row.oldReadyMs, $row.newReadyMs,
                $row.oldBakeFrameMaxMs, $row.newBakeFrameMaxMs, $row.newReadbackMs
            ))
        }
        $comparisonReport.Add('')
        $comparisonReport.Add('ARGB width and height are halved: retained bitmap bytes fall by 75%. The source bitmap budget is 16 MiB; transient capture and driver resources are outside this budget.')
        $comparisonReport.Add('PSS is a separate process measurement. Emulator graphics accounting is incomplete; negative or small deltas are measurement variation, not evidence that cached images are free. Read samples.json for memory checkpoints and callback drops.')
        $comparisonReport.Add('Ready latency includes the intentional size-settle delay. Readback is still synchronous on Android; the baking frame column exposes the remaining one-off cost. Frame maxima are medians of the per-run maxima.')
        $comparisonReport.Add('Small gains and regressions may be noise. GPU-time reduction is not an equal FPS increase. SwiftShader results require confirmation on physical devices before drawing device-level conclusions.')
        $comparisonReport | Set-Content (Join-Path $runDirectory 'COMPARISON.md')
    }
    $summary | Format-Table materialType, scene, gpuTimeReductionPercent, pssIncreaseMiB,
        bitmapMiB, cacheReadyMs -AutoSize | Out-String | Write-Output
    Write-Output "Results: $runDirectory"
} finally {
    Pop-Location
}
