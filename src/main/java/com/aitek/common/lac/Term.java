package com.aitek.common.lac;

public class Term {
    public String word;
    public Attribute tag;
    public int    offset;
    public int length() { return word.length();}

    @Override
    public String toString() {
        return word + "/" + tag;
    }
}
