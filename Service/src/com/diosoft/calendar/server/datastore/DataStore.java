package com.diosoft.calendar.server.datastore;

import com.diosoft.calendar.server.common.Event;
import com.diosoft.calendar.server.common.Person;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DataStore {

    /**
     * Adds given event to the data store
     * @param event which adds
     * @throws IllegalArgumentException
     */
    void publish(Event event) throws IllegalArgumentException, IOException, JAXBException;

    /**
     * Removes event for given id from the data store
     * @param id of event
     * @return removed event or null if there was no mapping for
     * @throws IllegalArgumentException
     */
    Event remove(UUID id) throws IllegalArgumentException, JAXBException, IOException;

    /**
     * Search event for given id in the data store and return it
     * @param id for search
     * @return event by id
     * @throws IllegalArgumentException
     */
    Event getEventById(UUID id) throws IllegalArgumentException;

    /**
     * Search event for given title in the data store and return it.
     * Uses index map.
     * @param title for search
     * @return List of events by title
     * @throws IllegalArgumentException
     */
    List<Event> getEventByTitle(String title) throws IllegalArgumentException;

    /**
     * Search event for given particular day in the data store and return it.
     * Uses index map.
     * @param day for search
     * @return List of events by date
     * @throws IllegalArgumentException
     */
    List<Event> getEventByDay(LocalDate day) throws IllegalArgumentException;

    /**
     * Search event for given attender in the data store and return it.
     * @param attender
     * @return List of events by attender
     * @throws IllegalArgumentException
     */
    List<Event> getEventByAttender(Person attender) throws IllegalArgumentException;

    /**
     * Search event by title that starts with the prefix
     * @param prefix
     * @return List of events
     * @throws IllegalArgumentException
     */
    List<Event> searchEventByTitleStartWith(String prefix) throws IllegalArgumentException;
}
