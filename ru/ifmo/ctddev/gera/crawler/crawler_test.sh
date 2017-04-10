cd ../../../../../..
javac -d out/production/java-advanced src/ru/ifmo/ctddev/gera/crawler/WebCrawler.java src/info/kgeorgiy/java/advanced/crawler/*
java -cp ./lib/hamcrest-core-1.3.jar:./lib/junit-4.11.jar:./lib/jsoup-1.8.1.jar:./lib/quickcheck-0.6.jar:./test/WebCrawlerTest.jar:./out/production/java-advanced/ info.kgeorgiy.java.advanced.crawler.Tester easy ru.ifmo.ctddev.gera.crawler.WebCrawler