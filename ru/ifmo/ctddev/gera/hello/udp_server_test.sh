cd ../../../../../..
javac -cp ./test/HelloUDPTest.jar \
      -d out/production/java-advanced \
      src/ru/ifmo/ctddev/gera/hello/*.java \
      src/info/kgeorgiy/java/advanced/hello/*.java
java -cp ./lib/hamcrest-core-1.3.jar:./lib/junit-4.11.jar:./lib/jsoup-1.8.1.jar:./lib/quickcheck-0.6.jar:./test/HelloUDPTest.jar:./out/production/java-advanced/ \
     info.kgeorgiy.java.advanced.hello.Tester server \
     ru.ifmo.ctddev.gera.hello.HelloUDPServer
