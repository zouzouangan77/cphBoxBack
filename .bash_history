ls
echo $JAVA_HOMe
echo $JAVA_HOME
pwd
echo $GRADLE_USER_HOME
export GRADLE_USER_HOME=`pwd`/.gradle
echo $GRADLE_USER_HOME
./gradlew checkstyleNohttp --no-daemon
./gradlew compileJava -x check -PnodeInstall --no-daemon
docker-compose -f src/main/docker/sonar.yml up -d
dockerd
sudo dockerd
docker ps
ls
ls /opt/java/
ls /opt/java/openjdk/
ls /opt/java/openjdk/bin/
 -la ls /opt/java/openjdk/bin/java
ls -la /opt/java/openjdk/bin/java
ls -la /opt/java/openjdk/bin/
echo $JAVA_HOME
ls -la /opt/
ls -la /opt/java/openjdk
ls -la /opt/java/openjdk/bin/java
ls
