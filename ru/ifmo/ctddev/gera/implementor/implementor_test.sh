cd ../../../../../..
javac -d out/production/java-advanced \
      src/info/kgeorgiy/java/advanced/implementor/*.java \
      src/ru/ifmo/ctddev/gera/implementor/Implementor.java
java -cp ./lib/hamcrest-core-1.3.jar:./lib/junit-4.11.jar:./lib/jsoup-1.8.1.jar:./lib/quickcheck-0.6.jar:./test/JarImplementorTest.jar:./out/production/java-advanced/ \
     info.kgeorgiy.java.advanced.implementor.Tester jar-class \
     ru.ifmo.ctddev.gera.implementor.Implementor
