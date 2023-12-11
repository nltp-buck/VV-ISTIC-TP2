package fr.istic.vv.example;

public class Person {
    private int age;
    private String name;
    public String city;
    public String streetNum, streetName;

    public String getName() { return name; }

    public boolean isAdult() {
        return age > 17;
    }
}