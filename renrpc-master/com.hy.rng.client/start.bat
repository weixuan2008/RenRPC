@ECHO OFF

set mainClasspath= target\com.hy.rng.client-1.0-SNAPSHOT.jar
set mainClass=com.hy.rng.client.RNGClient

echo "The Rng client is running ......"

if exist %JAVA_HOME%\bin\java.exe (
    %JAVA_HOME%\bin\java -Xms256m -Xmx512m -cp %mainClasspath% %mainClass%
) else (
    java -Xms256m -Xmx512m -cp %mainClasspath% %mainClass%
)
