package org.eldependenci.rpc.bungee.demo;

import java.util.List;

public interface DemoRemoteService {

    String sayHelloTo(String name);

    String response(String text, int seconds);

    List<String> testGeneric(List<Integer> list);

    DemoService.Book getBookFromAuthor(DemoService.Author author);

    void testVoidMethod(String say);
}
