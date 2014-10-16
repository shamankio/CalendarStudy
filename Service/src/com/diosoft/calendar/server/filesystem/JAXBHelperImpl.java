package com.diosoft.calendar.server.filesystem;

import com.diosoft.calendar.server.adapter.EventAdapter;
import com.diosoft.calendar.server.adapter.EventListAdapter;
import com.diosoft.calendar.server.adapter.PersonAdapter;
import com.diosoft.calendar.server.common.Event;
import com.diosoft.calendar.server.common.Person;
import com.diosoft.calendar.server.exception.DateTimeFormatException;
import com.diosoft.calendar.server.util.DateParser;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class JAXBHelperImpl implements JAXBHelper {

    @Override
    public void write(Event event, BufferedWriter writer) throws IOException, JAXBException {
        EventAdapter eventAdapter = new EventAdapter(event);
        JAXBContext context = JAXBContext.newInstance(EventAdapter.class);

        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(eventAdapter, writer);
    }

    @Override
    public Event read(BufferedReader reader) throws JAXBException, DateTimeFormatException {
        JAXBContext context = JAXBContext.newInstance(EventListAdapter.class);
        Unmarshaller um = context.createUnmarshaller();
        EventAdapter eventAdapter = (EventAdapter) um.unmarshal(reader);

        return eventAdapterToEvent(eventAdapter);
    }

    private Event eventAdapterToEvent(EventAdapter eventAdapter) throws DateTimeFormatException {

        Set<PersonAdapter> personAdapterList = eventAdapter.getAttenders();
        Set<Person> attenderSet = new HashSet<Person>();

        for (PersonAdapter personAdapter: personAdapterList){
            Person attender = new Person.PersonBuilder()
                    .name(personAdapter.getName())
                    .lastName(personAdapter.getLastName())
                    .email(personAdapter.getEmail())
                    .build();
            attenderSet.add(attender);
        }

        Event event = new Event.EventBuilder()
                .id(eventAdapter.getId())
                .title(eventAdapter.getTitle())
                .description(eventAdapter.getDescription())
                .startDate(DateParser.stringToDate(eventAdapter.getStartDate()))
                .endDate(DateParser.stringToDate(eventAdapter.getEndDate()))
                .attendersSet(attenderSet)
                .build();

        return event;
    }
}