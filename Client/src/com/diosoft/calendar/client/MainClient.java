package com.diosoft.calendar.client;

import com.diosoft.calendar.server.common.Event;
import com.diosoft.calendar.server.common.Person;
import com.diosoft.calendar.server.exception.DateTimeFormatException;
import com.diosoft.calendar.server.exception.OrderOfArgumentsException;
import com.diosoft.calendar.server.exception.ValidationException;
import com.diosoft.calendar.server.service.CalendarService;
import com.diosoft.calendar.server.util.DateParser;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.log4j.Logger.getLogger;

public class MainClient {
    private static final Logger logger = getLogger(MainClient.class);

    public static void main(String[] args) throws IOException, DateTimeFormatException, OrderOfArgumentsException, ValidationException, JAXBException {

        ApplicationContext factory = new ClassPathXmlApplicationContext("app-context-client.xml");
        CalendarService calendarService = (CalendarService) factory.getBean("calendarService");

// Create person1 (attender)
        Person person1 = new Person.PersonBuilder()
                .name("Alexandr").lastName("Alexandrenko")
                .email("alex_alex@ukr.net")
                .build();
// Create person2 (attender)
        Person person2 = new Person.PersonBuilder()
                .name("Igor").lastName("Igorov")
                .email("igor_igor@ukr.net")
                .build();
// Create person3 (attender)
        Person person3 = new Person.PersonBuilder()
                .name("Sergey").lastName("Sergeev")
                .email("sergey_sergey@ukr.net")
                .build();
// Create List attenders
        Set<Person> attenders = new HashSet<Person>();
        logger.info("Addind 3 attenders...");
        attenders.add(person1);
        attenders.add(person2);
        attenders.add(person3);
        logger.info("Added 3 attenders.");
// Create and add events
        logger.info("Creating and adding event...");
        String[] descriptions1 = {"Mega Party", "It will be a great party!", "2020-09-07 15:00", "2020-09-07 19:00"};
        Event event1 = calendarService.createEvent(descriptions1, attenders);
        logger.info("event created and added.");

        logger.info("Creating and adding event...");
        String[] descriptions2 = {"Mega Party 2", "It will be a second great party!", "2020-09-09 13:00", "2020-09-09 18:00"};
        Event event2 = calendarService.createEvent(descriptions2, attenders);
        logger.info("event created and added.");

        logger.info("Creating and adding event...");
        String[] descriptions3 = {"New Year", "Happy New Year!", "2020-01-01 00:00", "2020-01-01 00:01"};
        Event event3 = calendarService.createEvent(descriptions3, attenders);
        logger.info("event created and added.");

// remove event
        logger.info("Removing event...");
        calendarService.remove(event3.getId());
        logger.info("Event removed");
// searchByTitle
        logger.info("Searching event by title 'Mega Party':");
        List<Event> events1 = calendarService.searchByTitle("Mega Party");
        for (Event event: events1) {
            System.out.println(event);
        }
// searchByDate
        logger.info("Searching event by day '2020-09-07':");
        List<Event> events2 = calendarService.searchByDay(LocalDate.of(2020, 9, 7));
        for (Event event: events2) {
            System.out.println(event);
        }
// searchByAttender
        logger.info("Searching event by attender 'Alexandr':");
        List<Event> events3 = calendarService.searchByAttender(person1);
        for (Event event: events3) {
            System.out.println(event);
        }
// searchByAttenderIntoPeriod
        logger.info("Searching event by attender 'Alexandr' from 2020-09-07 12:00 to 2020-09-09 16:00:");
        List<Event> events4 = calendarService.searchByAttenderIntoPeriod(person1, DateParser.stringToDate("2020-09-07 12:00"), DateParser.stringToDate("2020-09-09 16:00"));
        for (Event event: events4) {
            System.out.println(event);
        }
// isAttenderFree
        logger.info("Checking is attender 'Alexandr' free from 2020-09-07 19:00 to 2020-09-09 13:00:");
        boolean isFree = calendarService.isAttenderFree(person1, DateParser.stringToDate("2020-09-07 19:00"), DateParser.stringToDate("2020-09-09 13:00"));
        System.out.println(isFree?"Free":"Not free");

// Create event "for all day"
        logger.info("Creating event 'for all day':");
        String[] descriptions4 = {"Mega Party", "It will be a great party!", "2020-09-07"};
        Event event = calendarService.createEventForAllDay(descriptions4, attenders);
        System.out.println(event);

// SearchFreeTime1 into period
        logger.info("SearchFreeTime1 into period from 2020-09-08 12:00 to 2020-09-10 21:00");
        List<List<LocalDateTime>> freeTimeIntervalList1 = calendarService.searchFreeTime(DateParser.stringToDate("2020-09-08 12:00"), DateParser.stringToDate("2020-09-10 21:00"));
        for (List<LocalDateTime> list : freeTimeIntervalList1)
            System.out.println(list);

// SearchFreeTime2 into period
        logger.info("SearchFreeTime2 into period from 2020-09-08 12:00 to 2020-09-10 21:00");
        List<List<LocalDateTime>> freeTimeIntervalList2 = calendarService.searchFreeTime2(DateParser.stringToDate("2020-09-08 12:00"), DateParser.stringToDate("2020-09-10 21:00"));
        for (List<LocalDateTime> list : freeTimeIntervalList2)
            System.out.println(list);

// SearchFreeTimeForEvent into period
        logger.info("SearchFreeTime for Event into period:");
        List<List<LocalDateTime>> freeTimeForEventIntervalList = calendarService.searchFreeTimeForEvent(event1, DateParser.stringToDate("2020-09-08 12:00"), DateParser.stringToDate("2020-09-10 21:00"));
        for (List<LocalDateTime> list : freeTimeForEventIntervalList)
            System.out.println(list);

    /*
// Edit event
        Event eventForEdit = calendarService.searchByTitle("Edited event").get(0);
        logger.info("Edit event id=" + eventForEdit.getId());
        Event eventEdited = new Event.EventBuilder()
                .id(eventForEdit.getId()).title(eventForEdit.getTitle())
                .description(eventForEdit.getDescription() + "!")
                .startDate(LocalDateTime.now().plusHours(1))
                .endDate(LocalDateTime.now().plusHours(2))
                .attendersSet(attenders).build();
        calendarService.edit(eventEdited);
        logger.info("event edited");
        */
    }
}
