package com.rootsquare.planmate.constants;

import java.util.List;

public final class Status {

    public static final String PENDING = "Pending";
    public static final String ACCEPTED = "Accepted";
    public static final String REJECTED = "Rejected";

    public static final List<String> ALL = List.of(PENDING, ACCEPTED, REJECTED);

    private Status() {
    }
}
