:: makecert -r -pe -n "CN=PAYable" -ss CA -sr CurrentUser -a sha256 -cy authority -sky signature -sv MyCA.pvk MyCA.cer
:: certutil -user -addstore Root MyCA.cer
:: makecert -pe -n "CN=PAYable" -a sha256 -cy end -sky signature -ic MyCA.cer -iv MyCA.pvk -sv MySPC.pvk MySPC.cer
:: pvk2pfx -pvk MySPC.pvk -spc MySPC.cer -pfx MySPC.pfx
signtool sign /f MySPC.pfx /tr http://timestamp.digicert.com /td sha256 /fd sha256 /a ../setup-win32.exe
:: -po fess
pause