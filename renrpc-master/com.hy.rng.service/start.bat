@ECHO OFF

set mainClasspath= target\com.hy.rng.service-1.0-SNAPSHOT.jar
set mainClass=com.hy.rng.service.boot.RNGService

echo "The Rng service is starting ......"

if exist %JAVA_HOME%\bin\java.exe (
    %JAVA_HOME%\bin\java -Xms256m -Xmx512m -cp %mainClasspath% %mainClass%
) else (
    java -Xms256m -Xmx512m -cp %mainClasspath% %mainClass%
)
