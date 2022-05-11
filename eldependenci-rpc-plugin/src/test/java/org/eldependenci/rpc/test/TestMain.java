package org.eldependenci.rpc.test;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class TestMain {



    public static void main(String[] args) throws Exception {
        var mapper = new ObjectMapper();
        var m = TestMain.class.getMethod("a", String.class, List.class);
        System.out.println(m.getGenericReturnType());
        for (Parameter parameter : m.getParameters()) {
            System.out.println(parameter.getType());
            System.out.println(parameter.getParameterizedType());
        }

        System.out.println(m.getGenericReturnType());
        var o = m.invoke(new TestMain(), "", List.of());
        System.out.println(o.getClass().getTypeName());
        System.out.println(m.getReturnType());
        System.out.println(o.getClass() == m.getReturnType());
        System.out.println(mapper.constructType(m.getGenericReturnType()).getContentType().getRawClass());
    }


    public List<String> a(String a, List<String> w){
        return (List<String>) new ArrayList<String>();
    }
}
