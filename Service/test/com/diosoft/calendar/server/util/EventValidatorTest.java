package com.diosoft.calendar.server.util;

import com.diosoft.calendar.server.common.Event;
import com.diosoft.calendar.server.common.Person;
import com.diosoft.calendar.server.exception.DateTimeFormatException;
import com.diosoft.calendar.server.exception.ValidationException;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EventValidatorTest {

    @Test
    public void testValidate() throws DateTimeFormatException, ValidationException {
        Person attender = new Person.PersonBuilder()
                .name("Denis")
                .lastName("Milyaev")
                .email("denis@ukr.net")
                .build();
        Set<Person> attendersTest = new HashSet<Person>();
        attendersTest.add(attender);
        Event event1 = new Event.EventBuilder()
                .id(UUID.randomUUID()).title("Happy Birthday")
                .description("Happy Birthday Denis")
                .startDate(DateParser.stringToDate("2020-10-15 15:00"))
                .endDate(DateParser.stringToDate("2020-10-15 20:00"))
                .attendersSet(attendersTest).build();

        EventValidator.validate(event1);
    }

    @Test(expected = ValidationException.class)
    public void testValidateWithNullAttenders() throws DateTimeFormatException, ValidationException {
        Set<Person> attendersTest = null;
        Event event1 = new Event.EventBuilder()
                .id(UUID.randomUUID()).title("Happy Birthday")
                .description("Happy Birthday Denis")
                .startDate(DateParser.stringToDate("2020-10-15 15:00"))
                .endDate(DateParser.stringToDate("2020-10-15 20:00"))
                .attendersSet(attendersTest).build();

        EventValidator.validate(event1);
    }

    @Test(expected = ValidationException.class)
    public void testValidateWithNullSomeFieldOfEvent() throws DateTimeFormatException, ValidationException {
        Person attender = new Person.PersonBuilder()
                .name("Denis")
                .lastName("Milyaev")
                .email("denis@ukr.net")
                .build();
        Set<Person> attendersTest = new HashSet<Person>();
        attendersTest.add(attender);
        Event event1 = new Event.EventBuilder()
                .id(UUID.randomUUID()).title(null)
                .description("Happy Birthday Denis")
                .startDate(DateParser.stringToDate("2020-10-15 15:00"))
                .endDate(DateParser.stringToDate("2020-10-15 20:00"))
                .attendersSet(attendersTest).build();

        EventValidator.validate(event1);
    }

    @Test(expected = ValidationException.class)
    public void testValidateWithNotSpecifiedTitle() throws DateTimeFormatException, ValidationException {
        Person attender = new Person.PersonBuilder()
                .name("Denis")
                .lastName("Milyaev")
                .email("denis@ukr.net")
                .build();
        Set<Person> attendersTest = new HashSet<Person>();
        attendersTest.add(attender);
        Event event1 = new Event.EventBuilder()
                .id(UUID.randomUUID()).title("")
                .description("Happy Birthday Denis")
                .startDate(DateParser.stringToDate("2020-10-15 15:00"))
                .endDate(DateParser.stringToDate("2020-10-15 20:00"))
                .attendersSet(attendersTest).build();

        EventValidator.validate(event1);
    }

    @Test(expected = ValidationException.class)
    public void testValidateWithNotSpecifiedDescription() throws DateTimeFormatException, ValidationException {
        Person attender = new Person.PersonBuilder()
                .name("Denis")
                .lastName("Milyaev")
                .email("denis@ukr.net")
                .build();
        Set<Person> attendersTest = new HashSet<Person>();
        attendersTest.add(attender);
        Event event1 = new Event.EventBuilder()
                .id(UUID.randomUUID()).title("Happy Birthday")
                .description("")
                .startDate(DateParser.stringToDate("2020-10-15 15:00"))
                .endDate(DateParser.stringToDate("2020-10-15 20:00"))
                .attendersSet(attendersTest).build();

        EventValidator.validate(event1);
    }

    @Test(expected = ValidationException.class)
    public void testValidateWithStartDateAfterEndDate() throws DateTimeFormatException, ValidationException {
        Person attender = new Person.PersonBuilder()
                .name("Denis")
                .lastName("Milyaev")
                .email("denis@ukr.net")
                .build();
        Set<Person> attendersTest = new HashSet<Person>();
        attendersTest.add(attender);
        Event event1 = new Event.EventBuilder()
                .id(UUID.randomUUID()).title("Happy Birthday")
                .description("Happy Birthday Denis")
                .startDate(DateParser.stringToDate("2020-10-15 15:00"))
                .endDate(DateParser.stringToDate("2020-10-15 10:00"))
                .attendersSet(attendersTest).build();

        EventValidator.validate(event1);
    }

    @Test(expected = ValidationException.class)
    public void testValidateWithStartDateBeforeCurrent() throws DateTimeFormatException, ValidationException {
        Person attender = new Person.PersonBuilder()
                .name("Denis")
                .lastName("Milyaev")
                .email("denis@ukr.net")
                .build();
        Set<Person> attendersTest = new HashSet<Person>();
        attendersTest.add(attender);
        Event event1 = new Event.EventBuilder()
                .id(UUID.randomUUID()).title("Happy Birthday")
                .description("Happy Birthday Denis")
                .startDate(DateParser.stringToDate("2013-10-15 15:00"))
                .endDate(DateParser.stringToDate("2015-10-15 10:00"))
                .attendersSet(attendersTest).build();

        EventValidator.validate(event1);
    }
}
