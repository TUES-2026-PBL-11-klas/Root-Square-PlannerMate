package com.rootsquare.planyourday.constants;

import java.util.List;

public final class Status {

    private Status() {}

    public record State(String name) {}

    public static final List<State> ALL = List.of(
        new State("Pending"),
        new State("Accepted"),
        new State("Rejected")
    );
}