@REM 7z a -t7z -m0=lzma2 -mx=9 -p'hello' ..\time_clock.7z .

del time_clock.7z

cd time_clock

certUtil -hashfile time_clock.jar SHA256 | findstr /v ":" > time_clock.sha
"c:\Program Files\7-Zip\7z.exe" a -t7z -m0=lzma2 -mx=9 -pASman@8282 ..\time_clock.7z .
