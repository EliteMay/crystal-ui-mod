param([string]$Out='shot.png', [string]$Keys='e')
Add-Type -AssemblyName System.Windows.Forms,System.Drawing
Add-Type @"
using System; using System.Runtime.InteropServices;
public class W3 { [DllImport("user32.dll")] public static extern bool SetForegroundWindow(IntPtr h); [DllImport("user32.dll")] public static extern bool GetWindowRect(IntPtr h, out RECT r); public struct RECT { public int L,T,R,B; } }
"@
$p = Get-Process java,javaw | Where-Object { $_.MainWindowTitle -like '*Minecraft*' } | Select-Object -First 1
[W3]::SetForegroundWindow($p.MainWindowHandle) | Out-Null; Start-Sleep 1
if ($Keys) { [System.Windows.Forms.SendKeys]::SendWait($Keys); Start-Sleep 2 }
$r = New-Object W3+RECT; [W3]::GetWindowRect($p.MainWindowHandle, [ref]$r) | Out-Null
$w=$r.R-$r.L; $h=$r.B-$r.T
$bmp=New-Object System.Drawing.Bitmap $w,$h
$g=[System.Drawing.Graphics]::FromImage($bmp); $g.CopyFromScreen($r.L,$r.T,0,0,$bmp.Size)
$bmp.Save($Out)
