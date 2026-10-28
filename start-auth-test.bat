@echo off
setlocal
set "CATALINA_HOME=C:\Users\KRISHNA A L\Downloads\apache-tomcat-10.1.60-windows-x64\apache-tomcat-10.1.60"
set "CATALINA_BASE=C:\Users\KRISHNA A L\Downloads\apache-tomcat-10.1.60-auth-test"
set "EXPENSE_DB_CONFIG=C:\Users\KRISHNA A L\Downloads\Daily expense tracker\test-config\daily-expense-tracker-test.properties"
call "%CATALINA_HOME%\bin\catalina.bat" run
exit /b %ERRORLEVEL%
