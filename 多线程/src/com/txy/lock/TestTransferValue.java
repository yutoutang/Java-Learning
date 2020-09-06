package com.txy.lock;

class Person{
    String name;
    Integer age;

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


public class TestTransferValue {

    public void changeValue1(int age){
        age = 30;
    }
    public void changeValue2(Person person){
        person.setName("xxx");
    }
    public void changeValue3(String str){
        str = "xxx";
    }

    public static void main(String[] args) {
        TestTransferValue test = new TestTransferValue();
        // jvm 的 main 线程中
        int age = 20;
        test.changeValue1(age);
        System.out.println(age); // 20

        Person person = new Person("abc");
        test.changeValue2(person);  // 传内存地址
        System.out.println(person.getName()); // xxx

        String abc = "abc";
        test.changeValue3(abc);
        System.out.println(abc); // abc
    }

}
