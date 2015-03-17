@echo off

call ..\..\..\skripte-dosshell\einstellungen.bat

set cp=..\..\de.bsvrz.sys.funclib.bitctrl\de.bsvrz.sys.funclib.bitctrl-runtime.jar
set cp=%cp%;..\de.bsvrz.dua.mwelve-runtime.jar
set cp=%cp%;..\de.bsvrz.dua.mwelve-test.jar
set cp=%cp%;..\..\junit-4.1.jar

title Pruefungen SE4 - DUA, SWE 4.5

echo ========================================================
echo #  Pruefungen SE4 - DUA, SWE 4.5
echo #
echo #  Die Funktionalitaet wird entsprechend der
echo #  Pruefspezifikation ueberprueft.
echo ========================================================
echo.

%java% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.dua.mwelve.vew.MessWertErsetzungLVETest
pause

