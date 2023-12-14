package pl.edu.amu.wmi.util;


import java.time.format.DateTimeFormatter;

public class CommonDateFormatter {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public static DateTimeFormatter commonDateFormatter() {
        return dateTimeFormatter;
    }

}
