rem xxx
cd ..\..
xcopy /s jop \tmp\jop\
cd \tmp\jop
for /R . %%x IN (.) DO call qu_del %%x
cd \tmp\jop
for /R . %%x IN (*.jop) DO del %%x
for /R . %%x IN (*.ttf) DO del %%x
for /R . %%x IN (*.sof) DO del %%x
for /R . %%x IN (*.class) DO del %%x
cd \tmp
rem start /w \programme\winzip\winzip32 -a jop.zip -r jop
