cd ../../../../../..
javac -d out/production/java-advanced src/ru/ifmo/ctddev/gera/walk/RecursiveWalk.java
java -cp ./lib/hamcrest-core-1.3.jar:./lib/junit-4.11.jar:./test/WalkTest.jar:./out/production/java-advanced/ info.kgeorgiy.java.advanced.walk.Tester RecursiveWalk ru.ifmo.ctddev.gera.walk.RecursiveWalk
