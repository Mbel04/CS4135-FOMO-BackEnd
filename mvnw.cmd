@echo off
@SET "MVN_BIN=mvn"
@IF EXIST "C:\maven\apache-maven-3.9.6\bin\mvn.cmd" SET "MVN_BIN=C:\maven\apache-maven-3.9.6\bin\mvn.cmd"
@CALL "%MVN_BIN%" %*
