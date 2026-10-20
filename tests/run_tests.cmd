@echo off
setlocal
cd /d "%~dp0\.."
call mvn test-compile -q -B 1>&2
java -cp "target\classes;target\test-classes" com.debuglab.reloop.TestRunner
