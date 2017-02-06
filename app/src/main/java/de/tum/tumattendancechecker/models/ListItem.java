package de.tum.tumattendancechecker.models;

public class ListItem {

    public final long id;
    public final String name;

    public ListItem(final long id, final String name) {
        this.id = id;
        this.name = name;
    }

    public static ListItem getInstance(final long id, final String name) {
        return new ListItem(id, name);
    }
}
