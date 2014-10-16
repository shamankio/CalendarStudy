package com.diosoft.calendar.server.service;

import com.diosoft.calendar.server.common.Event;
import com.diosoft.calendar.server.common.Person;
import com.diosoft.calendar.server.exception.DateTimeFormatException;
import com.diosoft.calendar.server.exception.OrderOfArgumentsException;
import com.diosoft.calendar.server.exception.ValidationException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface CalendarService extends Remote {

    /**
     * Provides ability to publish event to the data store.
     * Uses method of dataStoreImpl: void publish(Event event)
     * @param event which adds
     * @throws RemoteException, IllegalArgumentException
     */
    void add(Event event) throws RemoteException, IOException, IllegalArgumentException, ValidationException, JAXBException;

    /**
     * Creates event with given array descriptions and adds it into data store.
     * @param descriptions [0]: "title", [1]: "description", [2]: "startDate" , [3]: "endDate";
     * format of 'startDate' and 'endDate': "yyyy-MM-dd HH:mm". Example: "2014-01-05 10:00".
     * @param persons attenders
     * @return event
     * @throws RemoteException, IllegalArgumentException, DateTimeFormatException
     */
    Event createEvent(String[] descriptions, Set<Person> attenders) throws RemoteException, IOException, IllegalArgumentException, DateTimeFormatException, ValidationException, JAXBException;

    /**
     * Creates event with given array descriptions and adds it into data store. Two variant create event "for all day": use one day or interval of days.
     * @param descriptions First variant [0]: "title", [1]: "description", [2]: "day";
     * @param descriptions Second variant [0]: "title", [1]: "description", [2]: "startDay" , [3]: "endDay";
     * @param attenders
     * @return Event
     * @throws RemoteException
     * @throws IllegalArgumentException
     * @throws DateTimeFormatException
     */
    Event createEventForAllDay (String[] descriptions, Set<Person> attenders) throws RemoteException, IOException, IllegalArgumentException, DateTimeFormatException, ValidationException, JAXBException;

    /**
     * Provides ability to remove event from the data store.
     * Uses method of dataStoreImpl: void remove(UUID id)
     * @param id of event
     * @return removed event or null if there was no mapping for
     * @throws RemoteException, IllegalArgumentException
     */
    Event remove(UUID id) throws RemoteException, IOException, IllegalArgumentException, JAXBException;

    /**
     * Provides ability to edit event in the data store.
     * @param event which edits
     * @throws RemoteException, IllegalArgumentException
     */
    void edit(Event event) throws RemoteException, IOException, IllegalArgumentException, ValidationException, JAXBException;

    /**
     * Provides ability to search events by title from the data store.
     * Uses method of dataStoreImpl: List<Event> searchByTitle(String title)
     * @param title for search
     * @return List of events by title
     * @throws RemoteException, IllegalArgumentException
     */
    List<Event> searchByTitle(String title) throws RemoteException, IllegalArgumentException;

    /**
     * Provides ability to search events by date from the data store.
     * Uses method of dataStoreImpl: List<Event> searchByDay(LocalDate day)
     * @param day for search
     * @return List of events by day
     * @throws RemoteException, IllegalArgumentException
     */
    List<Event> searchByDay(LocalDate day) throws RemoteException, IllegalArgumentException;

    /**
     * Provides ability to search events by attender from the data store.
     * Uses method of dataStoreImpl: List<Event> searchByAttender(Person attender)
     * @param attender for search
     * @return List of events by attender
     * @throws RemoteException, IllegalArgumentException
     */
    List<Event> searchByAttender(Person attender) throws RemoteException, IllegalArgumentException;

    /**
     * Provides ability to search events by attender from the data store in a given period
     * @param attender for search
     * @param startDate for search
     * @param endDate for search
     * @return List of events by attender into period
     * @throws RemoteException, IllegalArgumentException, OrderOfArgumentsException
     */
    List<Event> searchByAttenderIntoPeriod(Person attender, LocalDateTime startDate, LocalDateTime endDate) throws RemoteException, IllegalArgumentException, OrderOfArgumentsException;

    /**
     * Provides ability to search events from the data store in a given period
     * @param startDate for search
     * @param endDate for search
     * @return
     * @throws RemoteException
     * @throws IllegalArgumentException
     * @throws OrderOfArgumentsException
     */
    Set<Event> searchIntoPeriod(LocalDate startDate, LocalDate endDate) throws RemoteException, IllegalArgumentException, OrderOfArgumentsException;

    /**
     * Provides ability to search free time from the data store in a given period
     * @param startDate for search
     * @param endDate for search
     * @return set of events
     * @throws RemoteException
     * @throws IllegalArgumentException
     * @throws OrderOfArgumentsException
     */
    List<List<LocalDateTime>> searchFreeTime2(LocalDateTime startDate, LocalDateTime endDate) throws RemoteException, IllegalArgumentException, OrderOfArgumentsException;

    /**
     * Provides ability to search free time from the data store in a given period
     * @param startDate for search
     * @param endDate for search
     * @return list of free periods
     * @throws RemoteException
     * @throws IllegalArgumentException
     * @throws OrderOfArgumentsException
     */
    List<List<LocalDateTime>> searchFreeTime(LocalDateTime startDate, LocalDateTime endDate) throws RemoteException, IllegalArgumentException, OrderOfArgumentsException;

    /**
     * Provides ability to search free time for Event from the data store in a given period
     * @param event
     * @param startDate
     * @param endDate
     * @return list of free periods
     * @throws RemoteException
     * @throws IllegalArgumentException
     * @throws OrderOfArgumentsException
     */
    List<List<LocalDateTime>> searchFreeTimeForEvent(Event event, LocalDateTime startDate, LocalDateTime endDate) throws RemoteException, IllegalArgumentException, OrderOfArgumentsException;

    /**
     * Check whether a person is free to participate in events in a given period
     * @param attender for search
     * @param startDate for search
     * @param endDate for search
     * @return true or false
     * @throws RemoteException, IllegalArgumentException, OrderOfArgumentsException
     */
    boolean isAttenderFree(Person attender, LocalDateTime startDate, LocalDateTime endDate) throws RemoteException, IllegalArgumentException, OrderOfArgumentsException;

    /**
     * Search event by title that starts with the prefix
     * Uses method of dataStoreImpl:searchEventByTitleStartWith(String prefix)
     * @param prefix
     * @return List of events
     * @throws IllegalArgumentException
     */
    List<Event> searchEventByTitleStartWith(String prefix) throws RemoteException, IllegalArgumentException;

}
