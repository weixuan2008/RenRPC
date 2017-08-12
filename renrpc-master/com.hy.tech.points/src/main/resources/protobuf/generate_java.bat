@echo off
@echo Generating java code using google protobuf.

@echo generating java code for Login
protoc.exe --java_out=../../java ./msg_def/com.hy.tech.points002.protobuf.login.proto

@echo generating java code for Player
protoc.exe --java_out=../../java ./msg_def/com.hy.tech.points002.protobuf.person.proto