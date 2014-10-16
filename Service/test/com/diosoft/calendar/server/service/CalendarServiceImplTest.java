package com.diosoft.calendar.server.service;

import com.diosoft.calendar.server.common.Event;
import com.diosoft.calendar.server.common.Person;
import com.diosoft.calendar.server.datastore.DataStore;
import com.diosoft.calendar.server.exception.DateTimeFormatException;
import com.diosoft.calendar.server.exception.OrderOfArgumentsException;
import com.diosoft.calendar.server.exception.ValidationException;
import com.diosoft.calendar.server.util.DateParser;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class CalendarServiceImplTest {

    private Person testPerson = new Person.PersonBuilder()
            .name("Denis")
            .lastName("Milyaev")
            .email("denis@ukr.net")
            .build();

    private Set<Person> attenders = new HashSet<Person>();

    private Event testEvent = new Event.EventBuilder()
            .id(UUID.randomUUID()).title("TestEvent")
            .description("Description of testEvent")
            .startDate(LocalDateTime.of(2020, 1, 1, 0, 0))
            .endDate(LocalDateTime.of(2020, 1, 2, 0, 0))
            .attendersSet(attenders).build();

    private DataStore mockDataStore;
    private CalendarService calendarService;


    @Before
    public void setUp() {
        mockDataStore = mock(DataStore.class);
        calendarService = new CalendarServiceImpl(mockDataStore);
    }


    @Test
    public void testAdd() throws IOException, IllegalArgumentException, ValidationException, JAXBException {

        calendarService.add(testEvent);
        verify(mockDataStore,times(1)).publish(testEvent);
    }

    @Test(expected = IllegalArgumentException.class )
    public void testAddWithNullArg() throws IOException, IllegalArgumentException, ValidationException, JAXBException {

        doThrow(new IllegalArgumentException()).when(mockDataStore).publish(null);
        calendarService.add(null);
        verify(mockDataStore, times(1)).publish(null);
    }

    @Test
    public void testRemove() throws IOException, IllegalArgumentException, JAXBException {

        when(mockDataStore.remove(testEvent.getId())).thenReturn(testEvent);
        Event actualEvent = calendarService.remove(testEvent.getId());
        assertEquals(testEvent, actualEvent);
        verify(mockDataStore,times(1)).remove(testEvent.getId());
    }

    @Test
    public void testRemoveNotExistsEvent() throws IOException, IllegalArgumentException, JAXBException {

        when(mockDataStore.remove(testEvent.getId())).thenReturn(null);
        Event actualEvent = calendarService.remove(testEvent.getId());
        assertNull(actualEvent);
        verify(mockDataStore,times(1)).remove(testEvent.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveWithNullArg() throws IOException, IllegalArgumentException, JAXBException {

        doThrow(new IllegalArgumentException()).when(mockDataStore).remove(null);
        calendarService.remove(null);
        verify(mockDataStore,times(1)).remove(null);
    }

    @Test
    public void testEdit() throws IOException, IllegalArgumentException, ValidationException, JAXBException, DateTimeFormatException {
        calendarService.add(testEvent);
        Event expectedEvent = new Event.EventBuilder()
                .id(testEvent.getId()).title("Edited event")
                .description("It is edited event")
                .startDate(DateParser.stringToDate("2020-08-07 10:00"))
                .endDate(DateParser.stringToDate("2020-08-07 20:00"))
                .attendersSet(attenders).build();

        calendarService.edit(expectedEvent);
        verify(mockDataStore).remove(testEvent.getId());
        verify(mockDataStore).publish(expectedEvent);
    }

    @Test
    public void testSearchByTitle() throws RemoteException, IllegalArgumentException  {

        attenders.add(testPerson);
        List<Event> expectedEvents = new ArrayList<Event>();
        expectedEvents.add(testEvent);

        when(mockDataStore.getEventByTitle(testEvent.getTitle())).thenReturn(expectedEvents);
        List<Event> actualEvents = calendarService.searchByTitle(testEvent.getTitle());

        assertEquals(expectedEvents, actualEvents);
        verify(mockDataStore,times(1)).getEventByTitle(testEvent.getTitle());
    }

    @Test
    public void testSearchByTitleNotExistsEvent() throws RemoteException, IllegalArgumentException {

//  empty list
        List<Event> expectedEvents = new ArrayList<Event>();
        when(mockDataStore.getEventByTitle(testEvent.getTitle())).thenReturn(expectedEvents);
        List<Event> actualEvents = calendarService.searchByTitle(testEvent.getTitle());
        assertEquals(expectedEvents,actualEvents);
        verify(mockDataStore,times(1)).getEventByTitle(testEvent.getTitle());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSearchByTitleWithNullArg() throws RemoteException, IllegalArgumentException {

        when(mockDataStore.getEventByTitle(null)).thenThrow(new IllegalArgumentException());
        calendarService.searchByTitle(null);
    }

    @Test
    public void testSearchByDay() throws RemoteException, IllegalArgumentException  {

        attenders.add(testPerson);
        List<Event> expectedEvents = new ArrayList<Event>();
        expectedEvents.add(testEvent);

        when(mockDataStore.getEventByDay(testEvent.getStartDate().toLocalDate())).thenReturn(expectedEvents);
        List<Event> actualEvents = calendarService.searchByDay(testEvent.getStartDate().toLocalDate());

        assertEquals(expectedEvents, actualEvents);
        verify(mockDataStore,times(1)).getEventByDay(testEvent.getStartDate().toLocalDate());
    }

    @Test
    public void testSearchByDayNotExistsEvent() throws RemoteException, IllegalArgumentException {

//  empty list
        List<Event> expectedEvents = new ArrayList<Event>();
        when(mockDataStore.getEventByDay(testEvent.getStartDate().toLocalDate())).thenReturn(expectedEvents);
        List<Event> actualEvents = calendarService.searchByDay(testEvent.getStartDate().toLocalDate());
        assertEquals(expectedEvents,actualEvents);
        verify(mockDataStore,times(1)).getEventByDay(testEvent.getStartDate().toLocalDate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSearchByDayWithNullArg() throws RemoteException, IllegalArgumentException {

        when(mockDataStore.getEventByDay(null)).thenThrow(new IllegalArgumentException());
        calendarService.searchByDay(null);
    }

    @Test
    public void testSearchByAttender() throws RemoteException, IllegalArgumentException  {

        attenders.add(testPerson);
        List<Event> expectedEvents = new ArrayList<Event>();
        expectedEvents.add(testEvent);

        when(mockDataStore.getEventByAttender(testPerson)).thenReturn(expectedEvents);
        List<Event> actualEvents = calendarService.searchByAttender(testPerson);

        assertEquals(expectedEvents, actualEvents);
        verify(mockDataStore,times(1)).getEventByAttender(testPerson);
    }

    @Test
    public void testSearchByAttenderNotExistsEvent() throws RemoteException, IllegalArgumentException {

//  empty list
        List<Event> expectedEvents = new ArrayList<Event>();
        when(mockDataStore.getEventByAttender(testPerson)).thenReturn(expectedEvents);
        List<Event> actualEvents = calendarService.searchByAttender(testPerson);
        assertEquals(expectedEvents,actualEvents);
        verify(mockDataStore,times(1)).getEventByAttender(testPerson);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSearchByAttenderWithNullArg() throws RemoteException, IllegalArgumentException {

        when(mockDataStore.getEventByAttender(null)).thenThrow(new IllegalArgumentException());
        calendarService.searchByAttender(null);
    }

    @Test
    public void testCreateEvent() throws IOException, IllegalArgumentException, DateTimeFormatException, ValidationException, JAXBException {

        Person attender = new Person.PersonBuilder()
                .name("Denis")
                .lastName("Milyaev")
                .email("denis@ukr.net")
                .build();

        Set<Person> attendersTest = new HashSet<Person>();
        attendersTest.add(attender);

        Event expectedEvent = new Event.EventBuilder()
                .id(UUID.randomUUID()).title("Happy Birthday")
                .description("Happy Birthday Denis")
                .startDate(LocalDateTime.of(2020, 10, 15, 15, 0))
                .endDate(LocalDateTime.of(2020, 10, 15, 20, 0))
                .attendersSet(attendersTest).build();

        String[] descriptions = {"Happy Birthday", "Happy Birthday Denis", "2020-10-15 15:00", "2020-10-15 20:00"};
        Event createdEvent = calendarService.createEvent(descriptions, attendersTest);

        assertEquals(expectedEvent, createdEvent);
        verify(mockDataStore,times(1)).publish(createdEvent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEventWithNullDescriptionsArg() throws IOException, IllegalArgumentException, DateTimeFormatException, ValidationException, JAXBException {

        Person attender = new Person.PersonBuilder()
                .name("Denis")
                .lastName("Milyaev")
                .email("denis@ukr.net")
                .build();

        Set<Person> attendersTest = new HashSet<Person>();
        attendersTest.add(attender);

        String[] descriptions = null;
        calendarService.createEvent(descriptions, attendersTest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEventWithIllegalDescriptionsArg() throws IOException, IllegalArgumentException, DateTimeFormatException, ValidationException, JAXBException {

        Person attender = new Person.PersonBuilder()
                .name("Denis")
                .lastName("Milyaev")
                .email("denis@ukr.net")
                .build();

        Set<Person> attendersTest = new HashSet<Person>();
        attendersTest.add(attender);

        String[] descriptions = {"Happy Birthday", "Happy Birthday Denis", "2020-10-15 15:00" };
        calendarService.createEvent(descriptions, attendersTest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEventWithNullAttendersArg() throws IOException, IllegalArgumentException, DateTimeFormatException, ValidationException, JAXBException {

        Set<Person> attendersTest = null;

        String[] descriptions = {"Happy Birthday", "Happy Birthday Denis", "2020-10-15 15:00", "2020-10-15 20:00"};
        calendarService.createEvent(descriptions, attendersTest);
    }

    @Test
    public void testCreateEventForAllDay() throws IOException, IllegalArgumentException, DateTimeFormatException, ValidationException, JAXBException {

        Person attender = new Person.PersonBuilder()
                .name("Denis")
                .lastName("Milyaev")
                .email("denis@ukr.net")
                .build();

        Set<Person> attendersTest = new HashSet<Person>();
        attendersTest.add(attender);

        Event expectedEvent = new Event.EventBuilder()
                .id(UUID.randomUUID()).title("Happy Birthday")
                .description("Happy Birthday Denis")
                .startDate(LocalDateTime.of(2020, 10, 15, 0, 0))
                .endDate(LocalDateTime.of(2020, 10, 16, 0, 0))
                .attendersSet(attendersTest).build();

        String[] descriptions = {"Happy Birthday", "Happy Birthday Denis", "2020-10-15"};
        Event createdEvent = calendarService.createEventForAllDay(descriptions, attendersTest);

        assertEquals(expectedEvent, createdEvent);
        verify(mockDataStore,times(1)).publish(createdEvent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEventForAllDayWithNullDescriptionsArg() throws IOException, IllegalArgumentException, DateTimeFormatException, ValidationException, JAXBException {

    Person attender = new Person.PersonBuilder()
            .name("Denis")
            .lastName("Milyaev")
            .email("denis@ukr.net")
            .build();

    Set<Person> attendersTest = new HashSet<Person>();
    attendersTest.add(attender);

    String[] descriptions = null;
    calendarService.createEventForAllDay(descriptions, attendersTest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEventForAllDayWithIllegalDescriptionsArg() throws IOException, IllegalArgumentException, DateTimeFormatException, ValidationException, JAXBException {

        Person attender = new Person.PersonBuilder()
                .name("Denis")
                .lastName("Milyaev")
                .email("denis@ukr.net")
                .build();

        Set<Person> attendersTest = new HashSet<Person>();
        attendersTest.add(attender);

        String[] descriptions = {"Happy Birthday", "Happy Birthday Denis"};
        calendarService.createEventForAllDay(descriptions, attendersTest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEventForAllDayWithNullAttendersArg() throws IOException, IllegalArgumentException, DateTimeFormatException, ValidationException, JAXBException {

        Set<Person> attendersTest = null;

        String[] descriptions = {"Happy Birthday", "Happy Birthday Denis", "2020-10-15", "2020-10-17"};
        calendarService.createEventForAllDay(descriptions, attendersTest);
    }

    @Test
    public void testIsAttenderFreeWithoutEventIntoGivenPeriod() throws DateTimeFormatException, IOException, OrderOfArgumentsException, ValidationException, JAXBException {
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
        Event event2 = new Event.EventBuilder()
                .id(UUID.randomUUID()).title("New Year 2021")
                .description("Happy New Year 2021")
                .startDate(DateParser.stringToDate("2020-12-31 20:00"))
                .endDate(DateParser.stringToDate("2021-01-01 12:00"))
                .attendersSet(attendersTest).build();
        LocalDateTime startDate = DateParser.stringToDate("2020-10-15 20:00");
        LocalDateTime endDate = DateParser.stringToDate("2020-12-31 20:00");
        calendarService.add(event1);
        calendarService.add(event2);
        List<Event> eventList = new ArrayList<Event>();
        eventList.add(event1);
        eventList.add(event2);

        when(mockDataStore.getEventByAttender(attender)).thenReturn(eventList);
        boolean isFreeResult = calendarService.isAttenderFree(attender, startDate ,endDate);

        Assert.assertTrue(isFreeResult);
        verify(mockDataStore).getEventByAttender(attender);
    }

    @Test
    public void testIsAttenderFreeWithStartEventIntoGivenPeriod() throws DateTimeFormatException, IOException, OrderOfArgumentsException, ValidationException, JAXBException {
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
        Event event2 = new Event.EventBuilder()
                .id(UUID.randomUUID()).title("New Year 2021")
                .description("Happy New Year 2021")
                .startDate(DateParser.stringToDate("2020-12-31 20:00"))
                .endDate(DateParser.stringToDate("2021-01-01 12:00"))
                .attendersSet(attendersTest).build();
        LocalDateTime startDate = DateParser.stringToDate("2020-09-20 15:45");
        LocalDateTime endDate = DateParser.stringToDate("2020-10-15 15:45");
        calendarService.add(event1);
        calendarService.add(event2);
        List<Event> eventList = new ArrayList<Event>();
        eventList.add(event1);
        eventList.add(event2);

        when(mockDataStore.getEventByAttender(attender)).thenReturn(eventList);
        boolean isFreeResult = calendarService.isAttenderFree(attender, startDate ,endDate);

        Assert.assertFalse(isFreeResult);
        verify(mockDataStore).getEventByAttender(attender);
    }

    @Test
    public void testIsAttenderFreeWithEndAndStartEventsIntoGivenPeriod() throws DateTimeFormatException, IOException, OrderOfArgumentsException, ValidationException, JAXBException {
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
        Event event2 = new Event.EventBuilder()
                .id(UUID.randomUUID()).title("New Year 2021")
                .description("Happy New Year 2021")
                .startDate(DateParser.stringToDate("2020-12-31 20:00"))
                .endDate(DateParser.stringToDate("2021-01-01 12:00"))
                .attendersSet(attendersTest).build();
        LocalDateTime startDate = DateParser.stringToDate("2020-10-15 18:45");
        LocalDateTime endDate = DateParser.stringToDate("2020-12-31 21:45");
        calendarService.add(event1);
        calendarService.add(event2);
        List<Event> eventList = new ArrayList<Event>();
        eventList.add(event1);
        eventList.add(event2);

        when(mockDataStore.getEventByAttender(attender)).thenReturn(eventList);
        boolean isFreeResult = calendarService.isAttenderFree(attender, startDate ,endDate);

        Assert.assertFalse(isFreeResult);
        verify(mockDataStore).getEventByAttender(attender);
    }

    @Test
    public void testIsAttenderFreeWithEventIntoGivenPeriod() throws DateTimeFormatException, IOException, OrderOfArgumentsException, ValidationException, JAXBException {
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
        Event event2 = new Event.EventBuilder()
                .id(UUID.randomUUID()).title("New Year 2021")
                .description("Happy New Year 2021")
                .startDate(DateParser.stringToDate("2020-12-31 20:00"))
                .endDate(DateParser.stringToDate("2021-01-01 12:00"))
                .attendersSet(attendersTest).build();
        LocalDateTime startDate = DateParser.stringToDate("2020-09-20 14:45");
        LocalDateTime endDate = DateParser.stringToDate("2020-10-30 14:45");
        calendarService.add(event1);
        calendarService.add(event2);
        List<Event> eventList = new ArrayList<Event>();
        eventList.add(event1);
        eventList.add(event2);

        when(mockDataStore.getEventByAttender(attender)).thenReturn(eventList);
        boolean isFreeResult = calendarService.isAttenderFree(attender, startDate ,endDate);

        Assert.assertFalse(isFreeResult);
        verify(mockDataStore).getEventByAttender(attender);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsAttenderFreeWithIllegalArg() throws RemoteException, IllegalArgumentException, DateTimeFormatException, OrderOfArgumentsException {
        LocalDateTime startDate = DateParser.stringToDate("2020-09-20 14:45");
        LocalDateTime endDate = DateParser.stringToDate("2020-09-30 14:45");
        calendarService.isAttenderFree(null, startDate, endDate);
    }

    @Test(expected = OrderOfArgumentsException.class)
    public void testIsAttenderFreeWithWrongOrderOfDate() throws RemoteException, OrderOfArgumentsException, DateTimeFormatException {
        Person attender = new Person.PersonBuilder()
                .name("Denis")
                .lastName("Milyaev")
                .email("denis@ukr.net")
                .build();
        LocalDateTime startDate = DateParser.stringToDate("2020-10-20 14:45");
        LocalDateTime endDate = DateParser.stringToDate("2020-09-30 14:45");
        calendarService.isAttenderFree(attender, startDate, endDate);
    }

    @Test
    public void testSearchByAttenderIntoPeriodWithoutEventIntoGivenPeriod() throws DateTimeFormatException, IOException, OrderOfArgumentsException, ValidationException, JAXBException {
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
        Event event2 = new Event.EventBuilder()
                .id(UUID.randomUUID()).title("New Year 2021")
                .description("Happy New Year 2021")
                .startDate(DateParser.stringToDate("2020-12-31 20:00"))
                .endDate(DateParser.stringToDate("2021-01-01 12:00"))
                .attendersSet(attendersTest).build();
        LocalDateTime startDate = DateParser.stringToDate("2020-10-15 20:00");
        LocalDateTime endDate = DateParser.stringToDate("2020-12-31 20:00");
        calendarService.add(event1);
        calendarService.add(event2);
        List<Event> expectedList = new ArrayList<Event>();

        when(mockDataStore.getEventByAttender(attender)).thenReturn(expectedList);
        List<Event> resultList = calendarService.searchByAttenderIntoPeriod(attender, startDate ,endDate);

        Assert.assertEquals(expectedList, resultList);
        verify(mockDataStore).getEventByAttender(attender);
    }

    @Test
    public void testSearchByAttenderIntoPeriodWithStartEventIntoGivenPeriod() throws DateTimeFormatException, IOException, OrderOfArgumentsException, ValidationException, JAXBException {
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
        Event event2 = new Event.EventBuilder()
                .id(UUID.randomUUID()).title("New Year 2021")
                .description("Happy New Year 2021")
                .startDate(DateParser.stringToDate("2020-12-31 20:00"))
                .endDate(DateParser.stringToDate("2021-01-01 12:00"))
                .attendersSet(attendersTest).build();
        LocalDateTime startDate = DateParser.stringToDate("2020-09-20 15:45");
        LocalDateTime endDate = DateParser.stringToDate("2020-10-15 15:45");
        calendarService.add(event1);
        calendarService.add(event2);
        List<Event> expectedList = new ArrayList<Event>();
        expectedList.add(event1);

        when(mockDataStore.getEventByAttender(attender)).thenReturn(expectedList);
        List<Event> resultList = calendarService.searchByAttenderIntoPeriod(attender, startDate ,endDate);

        Assert.assertEquals(expectedList, resultList);
        verify(mockDataStore).getEventByAttender(attender);
    }

    @Test
    public void testSearchByAttenderIntoPeriodWithEndAndStartEventsIntoGivenPeriod() throws DateTimeFormatException, IOException, OrderOfArgumentsException, ValidationException, JAXBException, JAXBException {
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
        Event event2 = new Event.EventBuilder()
                .id(UUID.randomUUID()).title("New Year 2021")
                .description("Happy New Year 2021")
                .startDate(DateParser.stringToDate("2020-12-31 20:00"))
                .endDate(DateParser.stringToDate("2021-01-01 12:00"))
                .attendersSet(attendersTest).build();
        LocalDateTime startDate = DateParser.stringToDate("2020-10-15 18:45");
        LocalDateTime endDate = DateParser.stringToDate("2020-12-31 21:45");
        calendarService.add(event1);
        calendarService.add(event2);
        List<Event> expectedList = new ArrayList<Event>();
        expectedList.add(event1);
        expectedList.add(event2);

        when(mockDataStore.getEventByAttender(attender)).thenReturn(expectedList);
        List<Event> resultList = calendarService.searchByAttenderIntoPeriod(attender, startDate ,endDate);

        Assert.assertEquals(expectedList, resultList);
        verify(mockDataStore).getEventByAttender(attender);
    }

    @Test
    public void testSearchByAttenderIntoPeriodWithEventIntoGivenPeriod() throws DateTimeFormatException, IOException, OrderOfArgumentsException, ValidationException, JAXBException {
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
        Event event2 = new Event.EventBuilder()
                .id(UUID.randomUUID()).title("New Year 2021")
                .description("Happy New Year 2021")
                .startDate(DateParser.stringToDate("2020-12-31 20:00"))
                .endDate(DateParser.stringToDate("2021-01-01 12:00"))
                .attendersSet(attendersTest).build();
        LocalDateTime startDate = DateParser.stringToDate("2020-10-15 14:45");
        LocalDateTime endDate = DateParser.stringToDate("2020-10-16 14:45");
        calendarService.add(event1);
        calendarService.add(event2);
        List<Event> expectedList = new ArrayList<Event>();
        expectedList.add(event1);

        when(mockDataStore.getEventByAttender(attender)).thenReturn(expectedList);
        List<Event> resultList = calendarService.searchByAttenderIntoPeriod(attender, startDate ,endDate);

        Assert.assertEquals(expectedList, resultList);
        verify(mockDataStore).getEventByAttender(attender);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSearchByAttenderIntoPeriodWithIllegalArg() throws RemoteException, IllegalArgumentException, DateTimeFormatException, OrderOfArgumentsException {
        LocalDateTime startDate = DateParser.stringToDate("2020-09-20 14:45");
        LocalDateTime endDate = DateParser.stringToDate("2020-09-30 14:45");
        calendarService.isAttenderFree(null, startDate, endDate);
    }

    @Test(expected = OrderOfArgumentsException.class)
    public void testSearchByAttenderIntoPeriodWithWrongOrderOfDate() throws RemoteException, OrderOfArgumentsException, DateTimeFormatException {
        Person attender = new Person.PersonBuilder()
                .name("Denis")
                .lastName("Milyaev")
                .email("denis@ukr.net")
                .build();
        LocalDateTime startDate = DateParser.stringToDate("2020-10-20 14:45");
        LocalDateTime endDate = DateParser.stringToDate("2020-09-30 14:45");
        calendarService.isAttenderFree(attender, startDate, endDate);
    }

    @Test
    public void testSearchFreeTime() throws OrderOfArgumentsException, IOException, ValidationException, DateTimeFormatException, JAXBException {
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
                .startDate(DateParser.stringToDate("2020-10-31 15:00"))
                .endDate(DateParser.stringToDate("2020-10-31 20:00"))
                .attendersSet(attendersTest).build();

        Event event2 = new Event.EventBuilder()
                .id(UUID.randomUUID()).title("Conference")
                .description("Java conference")
                .startDate(DateParser.stringToDate("2020-11-01 09:00"))
                .endDate(DateParser.stringToDate("2020-11-02 12:00"))
                .attendersSet(attendersTest).build();

        Event event3 = new Event.EventBuilder()
                .id(UUID.randomUUID()).title("Meeting")
                .description("Meeting with Ivan")
                .startDate(DateParser.stringToDate("2020-11-02 12:10"))
                .endDate(DateParser.stringToDate("2020-11-02 12:30"))
                .attendersSet(attendersTest).build();

        calendarService.add(event1);
        calendarService.add(event2);
        calendarService.add(event3);

        LocalDateTime startDate = DateParser.stringToDate("2020-10-31 14:45");
        LocalDateTime endDate = DateParser.stringToDate("2020-11-02 14:45");

        List<List<LocalDateTime>> expectedList = new ArrayList<List<LocalDateTime>>();

        List<LocalDateTime> timeList1 = new ArrayList<LocalDateTime>();
        timeList1.add(DateParser.stringToDate("2020-10-31 14:45"));
        timeList1.add(DateParser.stringToDate("2020-10-31 15:00"));

        List<LocalDateTime> timeList2 = new ArrayList<LocalDateTime>();
        timeList2.add(DateParser.stringToDate("2020-10-31 20:00"));
        timeList2.add(DateParser.stringToDate("2020-11-01 09:00"));

        List<LocalDateTime> timeList3 = new ArrayList<LocalDateTime>();
        timeList3.add(DateParser.stringToDate("2020-11-02 12:30"));
        timeList3.add(DateParser.stringToDate("2020-11-02 14:45"));

        expectedList.add(timeList1);
        expectedList.add(timeList2);
        expectedList.add(timeList3);

        List<Event> list1 = new ArrayList<Event>();
        list1.add(event1);
        when(mockDataStore.getEventByDay(DateParser.stringToDate("2020-10-31 00:00").toLocalDate())).thenReturn(list1);

        List<Event> list2 = new ArrayList<Event>();
        list2.add(event2);
        when(mockDataStore.getEventByDay(DateParser.stringToDate("2020-11-01 00:00").toLocalDate())).thenReturn(list2);

        List<Event> list3 = new ArrayList<Event>();
        list3.add(event2);
        list3.add(event3);
        when(mockDataStore.getEventByDay(DateParser.stringToDate("2020-11-02 00:00").toLocalDate())).thenReturn(list3);

        long start = System.nanoTime();
        List<List<LocalDateTime>> resultList = calendarService.searchFreeTime(startDate ,endDate);
        long finish = System.nanoTime();
        long timeConsumedMillis = finish - start;
        System.out.println("SearchFreeTime: " + timeConsumedMillis + " nanosec!!!!!!!!!!!!!!!");
        Assert.assertEquals(expectedList, resultList);
        verify(mockDataStore,times(3)).getEventByDay(Matchers.any(LocalDate.class));
    }

    @Test
    public void testSearchFreeTime2() throws OrderOfArgumentsException, IOException, ValidationException, DateTimeFormatException, JAXBException {
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
                .startDate(DateParser.stringToDate("2020-10-31 15:00"))
                .endDate(DateParser.stringToDate("2020-10-31 20:00"))
                .attendersSet(attendersTest).build();

        Event event2 = new Event.EventBuilder()
                .id(UUID.randomUUID()).title("Conference")
                .description("Java conference")
                .startDate(DateParser.stringToDate("2020-11-01 09:00"))
                .endDate(DateParser.stringToDate("2020-11-02 12:00"))
                .attendersSet(attendersTest).build();

        Event event3 = new Event.EventBuilder()
                .id(UUID.randomUUID()).title("Meeting")
                .description("Meeting with Ivan")
                .startDate(DateParser.stringToDate("2020-11-02 12:10"))
                .endDate(DateParser.stringToDate("2020-11-02 12:30"))
                .attendersSet(attendersTest).build();

        calendarService.add(event1);
        calendarService.add(event2);
        calendarService.add(event3);

        LocalDateTime startDate = DateParser.stringToDate("2020-10-31 14:45");
        LocalDateTime endDate = DateParser.stringToDate("2020-11-02 14:45");

        List<List<LocalDateTime>> expectedList = new ArrayList<List<LocalDateTime>>();

        List<LocalDateTime> timeList1 = new ArrayList<LocalDateTime>();
        timeList1.add(DateParser.stringToDate("2020-10-31 14:45"));
        timeList1.add(DateParser.stringToDate("2020-10-31 15:00"));

        List<LocalDateTime> timeList2 = new ArrayList<LocalDateTime>();
        timeList2.add(DateParser.stringToDate("2020-10-31 20:00"));
        timeList2.add(DateParser.stringToDate("2020-11-01 09:00"));

        List<LocalDateTime> timeList3 = new ArrayList<LocalDateTime>();
        timeList3.add(DateParser.stringToDate("2020-11-02 12:30"));
        timeList3.add(DateParser.stringToDate("2020-11-02 14:45"));

        expectedList.add(timeList1);
        expectedList.add(timeList2);
        expectedList.add(timeList3);

        List<Event> list1 = new ArrayList<Event>();
        list1.add(event1);
        when(mockDataStore.getEventByDay(DateParser.stringToDate("2020-10-31 00:00").toLocalDate())).thenReturn(list1);

        List<Event> list2 = new ArrayList<Event>();
        list2.add(event2);
        when(mockDataStore.getEventByDay(DateParser.stringToDate("2020-11-01 00:00").toLocalDate())).thenReturn(list2);

        List<Event> list3 = new ArrayList<Event>();
        list3.add(event2);
        list3.add(event3);
        when(mockDataStore.getEventByDay(DateParser.stringToDate("2020-11-02 00:00").toLocalDate())).thenReturn(list3);

        long start = System.nanoTime();
        List<List<LocalDateTime>> resultList = calendarService.searchFreeTime2(startDate, endDate);
        long finish = System.nanoTime();
        long timeConsumedMillis = finish - start;
        System.out.println("SearchFreeTime2: " + timeConsumedMillis + " nanosec!!!!!!!!!!!!!!!");
        Assert.assertEquals(expectedList, resultList);
        verify(mockDataStore,times(3)).getEventByDay(Matchers.any(LocalDate.class));
    }

    @Test
    public void testSearchFreeTimeForEvent() throws OrderOfArgumentsException, IOException, ValidationException, DateTimeFormatException, JAXBException {
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
                .endDate(DateParser.stringToDate("2020-10-15 22:00"))
                .attendersSet(attendersTest).build();

        Event event2 = new Event.EventBuilder()
                .id(UUID.randomUUID()).title("Conference")
                .description("Java conference")
                .startDate(DateParser.stringToDate("2020-10-16 09:00"))
                .endDate(DateParser.stringToDate("2020-10-16 18:00"))
                .attendersSet(attendersTest).build();

        Event eventForSearch = new Event.EventBuilder()
                .id(UUID.randomUUID()).title("Meeting")
                .description("Command meeting")
                .startDate(DateParser.stringToDate("2020-10-17 10:00"))
                .endDate(DateParser.stringToDate("2020-10-17 21:00"))
                .attendersSet(attendersTest).build();

        calendarService.add(event1);
        calendarService.add(event2);

        LocalDateTime startDate = DateParser.stringToDate("2020-10-15 00:00");
        LocalDateTime endDate = DateParser.stringToDate("2020-10-18 00:00");

        List<List<LocalDateTime>> expectedList = new ArrayList<List<LocalDateTime>>();

        List<LocalDateTime> timeList1 = new ArrayList<LocalDateTime>();
        timeList1.add(DateParser.stringToDate("2020-10-15 00:00"));
        timeList1.add(DateParser.stringToDate("2020-10-15 15:00"));

        List<LocalDateTime> timeList2 = new ArrayList<LocalDateTime>();
        timeList2.add(DateParser.stringToDate("2020-10-15 22:00"));
        timeList2.add(DateParser.stringToDate("2020-10-16 09:00"));

        List<LocalDateTime> timeList3 = new ArrayList<LocalDateTime>();
        timeList3.add(DateParser.stringToDate("2020-10-16 18:00"));
        timeList3.add(DateParser.stringToDate("2020-10-18 00:00"));

        expectedList.add(timeList1);
        expectedList.add(timeList2);
        expectedList.add(timeList3);

        List<Event> list1 = new ArrayList<Event>();
        list1.add(event1);
        when(mockDataStore.getEventByDay(DateParser.stringToDate("2020-10-15 00:00").toLocalDate())).thenReturn(list1);

        List<Event> list2 = new ArrayList<Event>();
        list2.add(event2);
        when(mockDataStore.getEventByDay(DateParser.stringToDate("2020-10-16 00:00").toLocalDate())).thenReturn(list2);

        when(mockDataStore.getEventByDay(DateParser.stringToDate("2020-10-17 00:00").toLocalDate())).thenReturn(new ArrayList<Event>());
        when(mockDataStore.getEventByDay(DateParser.stringToDate("2020-10-18 00:00").toLocalDate())).thenReturn(new ArrayList<Event>());

        List<List<LocalDateTime>> resultList = calendarService.searchFreeTimeForEvent(eventForSearch, startDate ,endDate);

        Assert.assertEquals(expectedList, resultList);
        verify(mockDataStore,times(4)).getEventByDay(Matchers.any(LocalDate.class));
    }

    @Test
    public void testSearchEventByTitleStartWith () throws IOException, JAXBException, ValidationException {
        Person testPerson = new Person.PersonBuilder()
                .name("Denis")
                .lastName("Milyaev")
                .email("denis@ukr.net")
                .build();

        Set<Person> attenders = new HashSet<Person>();
        attenders.add(testPerson);

        Event testEvent = new Event.EventBuilder()
                .id(UUID.randomUUID()).title("TestEvent")
                .description("Description of testEvent")
                .startDate(LocalDateTime.of(2020, 1, 1, 0, 0))
                .endDate(LocalDateTime.of(2020, 1, 2, 0, 0))
                .attendersSet(attenders).build();

        List<Event> expectedEventList = new ArrayList<Event>();
        expectedEventList.add(testEvent);
        String prefix = "Tes";
        calendarService.add(testEvent);

        when(mockDataStore.searchEventByTitleStartWith(prefix)).thenReturn(expectedEventList);

        List<Event> resultEventList = calendarService.searchEventByTitleStartWith(prefix);

        assertEquals(expectedEventList, resultEventList);
        verify(mockDataStore).searchEventByTitleStartWith(prefix);
    }
}
