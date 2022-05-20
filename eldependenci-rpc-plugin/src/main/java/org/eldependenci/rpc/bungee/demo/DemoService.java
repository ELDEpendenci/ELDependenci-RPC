package org.eldependenci.rpc.bungee.demo;

import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class DemoService {


    public String sayHelloTo(String name) {
        return String.format("hello, %s!", name);
    }


    public String response(String text, int seconds) throws Exception {
        Thread.sleep(seconds * 1000L);
        return text;
    }

    public List<String> testGeneric(List<Integer> list) {
        return list.stream().map(i -> String.valueOf(i * 2)).collect(Collectors.toList());
    }


    public Book getBookFromAuthor(Author author) {
        var b = new Book();
        b.name = String.format("%s's book", author.name);
        b.pages = author.age * 2;
        return b;
    }

    public void testVoidMethod(String say) {
        LoggerFactory.getLogger(DemoService.class).info(say);
    }


    public static class Author {
        public String name;
        public int age;
    }

    public static class Book {
        public String name;
        public int pages;


        @Override
        public String toString() {
            return "Book{" +
                    "name='" + name + '\'' +
                    ", pages=" + pages +
                    '}';
        }
    }
}
