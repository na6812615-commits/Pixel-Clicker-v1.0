@echo off
cd /d "%~dp0\.."
if not exist ".github\workflows" mkdir ".github\workflows"
copy /Y "github-apk-workflow\build-apk.yml" ".github\workflows\build-apk.yml" >nul
echo Da tao file .github\workflows\build-apk.yml thanh cong!
echo Bay gio ban co the git add -A, git commit va git push len GitHub.
pause
