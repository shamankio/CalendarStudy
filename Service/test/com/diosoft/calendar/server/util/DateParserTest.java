package com.diosoft.calendar.server.util;

import com.diosoft.calendar.server.exception.DateTimeFormatException;
import org.junit.Test;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import static org.junit.Assert.*;

public class DateParserTest {

    @Test
    public void testStringToDate() throws DateTimeFormatException, IllegalArgumentException {

        String stringDate = "2020-01-01 00:00";
        LocalDateTime expectedDateTime = LocalDateTime.of(2020,1,1,0,0);

        LocalDateTime actualDateTime = DateParser.stringToDate(stringDate);

        assertEquals(expectedDateTime,actualDateTime);
    }

    @Test(expected = DateTimeFormatException.class)
    public void testStringToDateWrongFormat() throws DateTimeFormatException, IllegalArgumentException {

        String stringDate = "2020-1-1 00:00";
        DateParser.stringToDate(stringDate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStringToDateWithNullArg() throws DateTimeFormatException, IllegalArgumentException {

        String stringDate = null;
        DateParser.stringToDate(stringDate);
    }

    @Test
    public void testDateToString() throws DateTimeException, IllegalArgumentException {

        LocalDateTime dateTime = LocalDateTime.of(2020,1,1,0,0);
        String expectedStrindDateTime = "2020-01-01 00:00";
        String actualDateTime = DateParser.dateToString(dateTime);

        assertEquals(expectedStrindDateTime,actualDateTime);
    }

    @Test(expected = DateTimeException.class)
    public void testDateToStringWrongFormat() throws DateTimeException, IllegalArgumentException {

        LocalDateTime dateTime = LocalDateTime.of(2020,15,1,0,0);
        DateParser.dateToString(dateTime);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testDateToStringWithNullArg() throws DateTimeException, IllegalArgumentException {

        LocalDateTime dateTime = null;
        DateParser.dateToString(dateTime);
    }
}
