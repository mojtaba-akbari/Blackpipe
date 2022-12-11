package org.HelperModels;

public class ColumnStructure {
    public String name;
    public int offset;
    public int len;

    public ColumnStructure(String name, int offset, int len) {
        this.name = name;
        this.offset = offset;
        this.len = len;
    }
}
