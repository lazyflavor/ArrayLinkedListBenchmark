package org.example;

public class BigObject implements Cloneable{
    private int randomInt;
    private String randomString;

    public BigObject() {

    }

    public BigObject(int randomInt, String randomString) {
        this.randomInt = randomInt;
        this.randomString = randomString;
    }

    public int getRandomInt() {
        return randomInt;
    }

    public String getRandomString() {
        return randomString;
    }

    @Override
    public BigObject clone() {
        try {
            return (BigObject) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Unable to clone BigObject", e);
        }
    }

    public BigObject clone(BigObject bigObject) {
        this.randomInt = bigObject.randomInt;
        this.randomString = bigObject.randomString;
        return this;
    }
}
