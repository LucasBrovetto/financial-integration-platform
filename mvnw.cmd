@ECHO OFF
SET "ROOT_DIR=%~dp0"
CALL "%ROOT_DIR%services\transaction-service\mvnw.cmd" %*
