package com.example.samuraitabelog.enums;

public enum DayOfWeek {
    SUN(0, "日曜日"),
    MON(1, "月曜日"),
    TUE(2, "火曜日"),
    WED(3, "水曜日"),
    THU(4, "木曜日"),
    FRI(5, "金曜日"),
    SAT(6, "土曜日");

    private final int id;
    private final String name;

    DayOfWeek(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static DayOfWeek fromId(int id) {
        for (DayOfWeek day : values()) {
            if (day.getId() == id) {
                return day;
            }
        }
        throw new IllegalArgumentException("Invalid Day ID: " + id);
    }
    
    public static String getNameById(int id) {
        for (DayOfWeek day : values()) {
            if (day.getId() == id) {
                return day.getName();
            }
        }
        throw new IllegalArgumentException("Invalid ID: " + id);
    }
}
