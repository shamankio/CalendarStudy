package com.diosoft.calendar.server.datastore;

import com.diosoft.calendar.server.common.Event;
import com.diosoft.calendar.server.common.Person;
import com.diosoft.calendar.server.filesystem.FileSystem;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class DataStoreImplTest {

    Person testPerson = new Person.PersonBuilder()
            .name("Denis")
            .lastName("Milyaev")
            .email("denis@ukr.net")
            .build();

    Set<Person> attenders = new HashSet<Person>();

    Event testEvent = new Event.EventBuilder()
            .id(UUID.randomUUID()).title("TestEvent")
            .description("Description of testEvent")
            .startDate(LocalDateTime.of(2020, 1, 1, 0, 0))
            .endDate(LocalDateTime.of(2020, 1, 2, 0, 0))
            .attendersSet(attenders).build();

    private FileSystem mockFileSystem;
    private DataStore dataStore;

    @Before
    public void setUp() {
        mockFileSystem = mock(FileSystem.class);
        dataStore = new DataStoreImpl(mockFileSystem);
    }

    @Test
    public void testPublish() throws IllegalArgumentException, IOException, JAXBException {

        attenders.add(testPerson);
        Event expectedEvent = testEvent;

        dataStore.publish(testEvent);
        Event actualEvent = dataStore.getEventById(testEvent.getId());

        assertEquals(expectedEvent,actualEvent);
        verify(mockFileSystem).write(testEvent);
    }

    @Test(expected = IllegalArgumentException.class )
    public void testPublishWithNullArg() throws IllegalArgumentException, IOException, JAXBException {

        dataStore.publish(null);
        verify(mockFileSystem,never()).write(testEvent);
    }

    @Test
    public void testRemove() throws IllegalArgumentException, IOException, JAXBException {

        attenders.add(testPerson);
        Event expectedRemovedEvent = testEvent;

        dataStore.publish(testEvent);
        Event actualRemovedEvent = dataStore.remove(testEvent.getId());

        assertEquals(expectedRemovedEvent,actualRemovedEvent);
        verify(mockFileSystem).delete(testEvent.getId());
    }

    @Test
    public void testRemoveNotExistsEvent() throws IllegalArgumentException, JAXBException, IOException {

        Event actualRemovedEvent = dataStore.remove(testEvent.getId());
        assertNull(actualRemovedEvent);
        verify(mockFileSystem,never()).delete(testEvent.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveWithNullArg() throws IllegalArgumentException, JAXBException, IOException {

        dataStore.remove(null);
        verify(mockFileSystem,never()).delete(null);
    }

    @Test
    public void testGetEventById() throws IllegalArgumentException, IOException, JAXBException {

        attenders.add(testPerson);
        Event expectedEvent = testEvent;

        dataStore.publish(testEvent);
        Event actualEvent = dataStore.getEventById(testEvent.getId());

        assertEquals(expectedEvent,actualEvent);
    }

    @Test
    public void testGetEventByIdNotExistsEvent() throws IllegalArgumentException  {

        Event actualEvent = dataStore.getEventById(testEvent.getId());
        assertNull(actualEvent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEventByIdWithNullArg() throws IllegalArgumentException {

        dataStore.getEventById(null);
    }

    @Test
    public void testGetEventByTitle() throws IllegalArgumentException, IOException, JAXBException {

        attenders.add(testPerson);
        List<Event> expectedEvents = new ArrayList<Event>();
        expectedEvents.add(testEvent);

        dataStore.publish(testEvent);
        List<Event> actualEvents = dataStore.getEventByTitle(testEvent.getTitle());

        assertEquals(expectedEvents,actualEvents);
    }

    @Test
    public void testGetEventByTitleNotExistsEvent() throws IllegalArgumentException  {

//  empty list
        List<Event> expectedEvents = new ArrayList<Event>();

        List<Event> actualEvents = dataStore.getEventByTitle(testEvent.getTitle());

        assertEquals(expectedEvents,actualEvents);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEventByTitleWithNullArg() throws IllegalArgumentException  {

        dataStore.getEventByTitle(null);
    }

    @Test
    public void testGetEventByDay() throws IllegalArgumentException, IOException, JAXBException {

        attenders.add(testPerson);
        List<Event> expectedEvents = new ArrayList<Event>();
        expectedEvents.add(testEvent);

        dataStore.publish(testEvent);
        List<Event> actualEvents = dataStore.getEventByDay(testEvent.getStartDate().toLocalDate());

        assertEquals(expectedEvents,actualEvents);
    }

    @Test
    public void testGetEventByDayNotExistsEvent() throws IllegalArgumentException  {

//  empty list
        List<Event> expectedEvents = new ArrayList<Event>();

        List<Event> actualEvents = dataStore.getEventByDay(testEvent.getStartDate().toLocalDate());

        assertEquals(expectedEvents,actualEvents);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEventByDayWithNullArg() throws IllegalArgumentException  {

        dataStore.getEventByDay(null);
    }

    @Test
    public void testGetEventByAttender() throws IllegalArgumentException, IOException, JAXBException {

        attenders.add(testPerson);
        List<Event> expectedEvents = new ArrayList<Event>();
        expectedEvents.add(testEvent);

        dataStore.publish(testEvent);
        List<Event> actualEvents = dataStore.getEventByAttender(testPerson);

        assertEquals(expectedEvents,actualEvents);
    }

    @Test
    public void testGetEventByAttenderNotExistsEvent() throws IllegalArgumentException  {

// empty list
        List<Event> expectedEvents = new ArrayList<Event>();

        List<Event> actualEvents = dataStore.getEventByAttender(testPerson);

        assertEquals(expectedEvents,actualEvents);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testGetEventByAttenderWithNullArg() throws IllegalArgumentException  {

        dataStore.getEventByAttender(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSearchEventByTitleStartWithNullString() throws IllegalArgumentException  {

        dataStore.searchEventByTitleStartWith(null);
    }


    @Test
    public void testSearchEventByTitleStartWith () throws IOException, JAXBException {
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
        dataStore.publish(testEvent);

        List<Event> resultEventList = dataStore.searchEventByTitleStartWith(prefix);

        assertEquals(expectedEventList, resultEventList);
    }
}
