package com.bear.libstorage;

import java.util.ArrayList;
import java.util.List;

public class TestObj {
    private String name;

    private List<TestObj> list = new ArrayList<>();

    public TestObj(String name) {
        this.name = name;
    }

    public TestObj(String name, List<TestObj> list) {
        this.name = name;
        this.list = list;
    }

    @Override
    public String toString() {
        return "TestObj{" +
                "name='" + name + '\'' +
                ", list=" + list +
                '}';
    }
}
