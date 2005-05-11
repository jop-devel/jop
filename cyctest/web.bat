rem Is doit.bat set to COM1?
rem Is jopser.bat called?
rem Is there no jop directory in \tmp?
pause
cd ..\..
xcopy /s jop \tmp\jop\
cd \tmp\jop
cd modelsim
call clean
cd ..
for /R . %%x IN (.) DO call max_del %%x
cd \tmp\jop
for /R . %%x IN (*.jop) DO del %%x
for /R . %%x IN (*.ttf) DO del %%x
for /R . %%x IN (*.sof) DO del %%x
cd java\target\src\app
del /s/q kfl
del /s/q oebb
del /s/q tal
cd \tmp
rem start /w \programme\winzip\winzip32 -a jop.zip -r jop
