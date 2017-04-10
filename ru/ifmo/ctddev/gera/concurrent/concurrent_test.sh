cd ../../../../../..
javac -d out/production/java-advanced \
      src/ru/ifmo/ctddev/gera/concurrent/*.java \
      src/info/kgeorgiy/java/advanced/concurrent/*.java \
      src/info/kgeorgiy/java/advanced/mapper/*.java
java -cp ./lib/hamcrest-core-1.3.jar:./lib/junit-4.11.jar:./lib/jsoup-1.8.1.jar:./lib/quickcheck-0.6.jar:./test/IterativeParallelismTest.jar:./out/production/java-advanced/ \
     info.kgeorgiy.java.advanced.concurrent.Tester list \
     ru.ifmo.ctddev.gera.concurrent.IterativeParallelism
