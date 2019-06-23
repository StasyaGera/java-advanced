cd ../../../../..
javadoc -link https://docs.oracle.com/javase/8/docs/api/ \
        -protected -source ru.ifmo.ctddev.gera.crawler \
        ru/ifmo/ctddev/gera/crawler/WebCrawler.java \
        info/kgeorgiy/java/advanced/crawler/*.java \
        -classpath info/kgeorgiy/java/advanced/crawler/jsoup-1.8.1.jar
