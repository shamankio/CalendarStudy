package com.diosoft.calendar.server.util;

import com.diosoft.calendar.server.exception.DateTimeFormatException;
import org.apache.log4j.Logger;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateParser {

    private static final Logger logger = Logger.getLogger(DateParser.class);

    public static LocalDateTime stringToDate(String stringDate) throws DateTimeFormatException, IllegalArgumentException {

        if (stringDate==null) throw new IllegalArgumentException();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = null;

        try {
            dateTime = LocalDateTime.parse(stringDate, formatter);
        } catch (DateTimeParseException dtpe) {
            logger.error(dtpe.getMessage());
            throw new DateTimeFormatException("Wrong format of date/time");
        }

        return dateTime;
    }

    public static String dateToString(LocalDateTime dateTime) throws DateTimeException, IllegalArgumentException {

        if (dateTime==null) throw new IllegalArgumentException();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        try {
            String stringDate = dateTime.format(formatter);
            return stringDate;
        } catch (DateTimeException dte) {
            logger.error(dte.getMessage());
            throw new DateTimeException("Wrong value of year, month, day, hour or minute");
        }
    }


}
