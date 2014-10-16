package com.diosoft.calendar.server.service;

import com.diosoft.calendar.server.common.Event;
import com.diosoft.calendar.server.common.Person;
import com.diosoft.calendar.server.datastore.DataStore;
import com.diosoft.calendar.server.exception.DateTimeFormatException;
import com.diosoft.calendar.server.exception.OrderOfArgumentsException;
import com.diosoft.calendar.server.exception.ValidationException;
import com.diosoft.calendar.server.util.DateParser;
import com.diosoft.calendar.server.util.EventValidator;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class CalendarServiceImpl implements CalendarService {

    private static final Logger logger = Logger.getLogger(CalendarServiceImpl.class);
    private final DataStore dataStore;
    final static int MINUTE_INTERVAL = 15;

    public CalendarServiceImpl(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public void add(Event event) throws IOException, IllegalArgumentException, ValidationException, JAXBException {
        if (event == null) throw new IllegalArgumentException();

//  Validate
        logger.info("Validation event with title '" + event.getTitle() + "'");
        EventValidator.validate(event);
        logger.info("Event successfully validated");
//  Add
        logger.info("Adding event with title '" + event.getTitle() + "'");
        dataStore.publish(event);
        logger.info("Event successfully added");
    }

    @Override
    public Event createEvent(String[] descriptions, Set<Person> attenders) throws IOException, IllegalArgumentException,
            DateTimeFormatException, ValidationException, JAXBException {
        if (descriptions == null || attenders == null || descriptions.length != 4) throw new IllegalArgumentException();

        LocalDateTime startDate = DateParser.stringToDate(descriptions[2]);
        LocalDateTime endDate = DateParser.stringToDate(descriptions[3]);

        logger.info("Creating event with title '" + descriptions[0] + "'");
        Event event = new Event.EventBuilder()
                .id(UUID.randomUUID()).title(descriptions[0])
                .description(descriptions[1])
                .startDate(startDate)
                .endDate(endDate)
                .attendersSet(attenders).build();
        logger.info("Event successfully created");

        add(event);
        return event;
    }

    @Override
    public Event createEventForAllDay(String[] descriptions, Set<Person> attenders) throws IOException, IllegalArgumentException,
            DateTimeFormatException, ValidationException, JAXBException {
        if (descriptions == null || attenders == null || descriptions.length < 3 || descriptions.length > 4)
            throw new IllegalArgumentException();

        String startDay = descriptions[2] + " 00:00";
        String endDate = null;

//  one day "for all day"
        if (descriptions.length == 3) {
            LocalDateTime tempStartDate = DateParser.stringToDate(startDay);
            LocalDateTime tempEndDate = tempStartDate.plusDays(1);
            endDate = DateParser.dateToString(tempEndDate);
        }

//  interval of days "for all day"
        if (descriptions.length == 4) {
            String endDay = descriptions[3] + " 00:00";
            LocalDateTime tempEndDate = DateParser.stringToDate(endDay);
            tempEndDate = tempEndDate.plusDays(1);
            endDate = DateParser.dateToString(tempEndDate);
        }

        String[] preparedDescriptions = {descriptions[0], descriptions[1], startDay, endDate};
        return createEvent(preparedDescriptions, attenders);
    }

    @Override
    public Event remove(UUID id) throws IOException, IllegalArgumentException, JAXBException {
        if (id == null) throw new IllegalArgumentException();

        logger.info("Removing event with id: '" + id + "'");
        Event event = dataStore.remove(id);
        if (event == null) {
            logger.info("There is no such Event");
        } else {
            logger.info("Event successfully removed");
        }
        return event;
    }

    @Override
    public void edit(Event event) throws IOException, IllegalArgumentException, ValidationException, JAXBException {
        if (event == null) throw new IllegalArgumentException();

        logger.info("Edit event with title '" + event.getTitle() + "'");
        remove(event.getId());
        add(event);
        logger.info("Event successfully edited");
    }

    @Override
    public List<Event> searchByTitle(String title) throws IllegalArgumentException {
        if (title == null) throw new IllegalArgumentException();

        logger.info("Searching by title '" + title + "':");
        List<Event> events = dataStore.getEventByTitle(title);
        if (events.size() < 1) {
            logger.info("Events not found!");
        } else {
            logger.info("Found " + events.size() + " events");
        }
        return events;
    }

    @Override
    public List<Event> searchByDay(LocalDate day) throws IllegalArgumentException {
        if (day == null) throw new IllegalArgumentException();

        logger.info("Searching by day '" + day + "':");
        List<Event> events = dataStore.getEventByDay(day);
        if (events.size() < 1) {
            logger.info("Events not found!");
            return events;
        }
        logger.info("Found " + events.size() + " events");

        return events;
    }

    @Override
    public List<Event> searchByAttender(Person attender) throws IllegalArgumentException {
        if (attender == null) throw new IllegalArgumentException();

        logger.info("Searching by attender '" + attender.getName() + "':");
        List<Event> events = dataStore.getEventByAttender(attender);
        if (events.size() < 1) {
            logger.info("Events not found!");
            return events;
        }
        logger.info("Found " + events.size() + " events");

        return events;
    }

    @Override
    public List<Event> searchByAttenderIntoPeriod(Person attender, LocalDateTime startDate, LocalDateTime endDate)
            throws IllegalArgumentException, OrderOfArgumentsException {
        if (attender == null || startDate == null || endDate == null) throw new IllegalArgumentException();
        if (startDate.isAfter(endDate)) throw new OrderOfArgumentsException();

        logger.info("Searching events by attender '" + attender.getName() + " " + attender.getLastName() + "' into period from " +
                DateParser.dateToString(startDate) + " to " + DateParser.dateToString(endDate));
        List<Event> eventListByAttender = searchByAttender(attender);
        List<Event> eventListByAttenderIntoPeriod = new ArrayList<Event>();
        for (Event event : eventListByAttender) {
            if (isEventAndPeriodCrossing(event, startDate, endDate)) {
                eventListByAttenderIntoPeriod.add(event);
            }
        }
        if (eventListByAttenderIntoPeriod.isEmpty())
            logger.info("Events not found!");
        else
            logger.info("Found " + eventListByAttenderIntoPeriod.size() + " events");

        return eventListByAttenderIntoPeriod;
    }

    @Override
    public Set<Event> searchIntoPeriod(LocalDate startDay, LocalDate endDay) throws IllegalArgumentException, OrderOfArgumentsException {
        if (startDay == null || endDay == null) throw new IllegalArgumentException();
        if (startDay.isAfter(endDay)) throw new OrderOfArgumentsException();

        logger.info("Searching events into period from '" + startDay + "' to" + endDay);
        Set<Event> eventSetIntoPeriod = new HashSet<Event>();

//  get all events from period without time (use getEventByDay method of DataStore which use index map)
        while (startDay.isBefore(endDay) || startDay.equals(endDay)) {
            List<Event> tempEventList = searchByDay(startDay);
            eventSetIntoPeriod.addAll(tempEventList);
            startDay = startDay.plusDays(1);
        }
        logger.info("Found " + eventSetIntoPeriod.size() + " events");
        return eventSetIntoPeriod;
    }

    @Override
    public List<List<LocalDateTime>> searchFreeTime(LocalDateTime startDate, LocalDateTime endDate)
            throws IllegalArgumentException, OrderOfArgumentsException {
        if (startDate == null || endDate == null) throw new IllegalArgumentException();
        if (startDate.isAfter(endDate)) throw new OrderOfArgumentsException();

        List<List<LocalDateTime>> freeTimeList = new LinkedList<List<LocalDateTime>>();
        freeTimeList.add(Arrays.asList(startDate, endDate));
        Set<Event> eventSet = searchIntoPeriod(startDate.toLocalDate(), endDate.toLocalDate());

        logger.info("Searching free time into period from '" + startDate + "' to" + endDate);
        for(Event event:eventSet) {
            ListIterator<List<LocalDateTime>> it = freeTimeList.listIterator();
            while (it.hasNext()) {
                List<LocalDateTime> freeTimeInterval = it.next();
                if (isEventIncludesFreeInterval(event, freeTimeInterval))
                    it.remove();
                else {
                    if (isEventAndFreeIntervalCrossingInStartOfEvent(event, freeTimeInterval))
                        freeTimeList.get(freeTimeList.indexOf(freeTimeInterval)).set(0, event.getEndDate());
                    if (isEventAndFreeIntervalCrossingInEndOfEvent(event, freeTimeInterval))
                        freeTimeList.get(freeTimeList.indexOf(freeTimeInterval)).set(1, event.getStartDate());
                    if ( isFreeIntervalIncludesEvent(event, freeTimeInterval)) {
                        it.add(Arrays.asList(event.getEndDate(), freeTimeInterval.get(1)));
                        freeTimeList.get(freeTimeList.indexOf(freeTimeInterval)).set(1, event.getStartDate());
                    }
                }
            }
        }
        logger.info("Found "  + freeTimeList.size() + " free intervals");
        return freeTimeList;
    }

    @Override
    public List<List<LocalDateTime>> searchFreeTime2(LocalDateTime startDate, LocalDateTime endDate)
            throws IllegalArgumentException, OrderOfArgumentsException {
        if (startDate == null || endDate == null) throw new IllegalArgumentException();
        if (startDate.isAfter(endDate)) throw new OrderOfArgumentsException();

        Set<Event> eventListIntoPeriod = searchIntoPeriod(startDate.toLocalDate(), endDate.toLocalDate());

        List<List<LocalDateTime>> freeIntervalList = new ArrayList<List<LocalDateTime>>();
        LocalDateTime tempStartDate = startDate;
        logger.info("Searching free time into period from " +
                DateParser.dateToString(startDate) + " to " + DateParser.dateToString(endDate));
        while (tempStartDate.isBefore(endDate)) {
            LocalDateTime tempEndDate = tempStartDate.plusMinutes(MINUTE_INTERVAL);
            boolean isFree = true;
            for (Event event : eventListIntoPeriod) {
                if (isEventAndPeriodCrossing(event, tempStartDate, tempEndDate)) {
                    isFree = false;
                    break;
                }
            }
            if (isFree) {
                freeIntervalList.add(Arrays.asList(tempStartDate,tempEndDate));
            }
            tempStartDate = tempStartDate.plusMinutes(MINUTE_INTERVAL);
        }
        logger.info("Found "  + mergeSolidInterval(freeIntervalList).size() + " free intervals");
        return mergeSolidInterval(freeIntervalList);
    }

    @Override
    public List<List<LocalDateTime>> searchFreeTimeForEvent(Event event, LocalDateTime startDate, LocalDateTime endDate) throws OrderOfArgumentsException {
        if (event == null || startDate == null || endDate == null) throw new IllegalArgumentException();
        if (startDate.isAfter(endDate)) throw new OrderOfArgumentsException();

        List<List<LocalDateTime>> freeIntervalList = searchFreeTime(startDate, endDate);
        List<List<LocalDateTime>> freeIntervalListForEvent = new ArrayList<List<LocalDateTime>>();

        logger.info("Searching free time for event '" +  event.getTitle() + "' into period from " +
                DateParser.dateToString(startDate) + " to " + DateParser.dateToString(endDate));
        Duration durationEvent = Duration.between(event.getStartDate(), event.getEndDate());
        for (List<LocalDateTime> freeInterval : freeIntervalList) {
            Duration durationFreeInterval = Duration.between(freeInterval.get(0), freeInterval.get(1));
            if (durationEvent.toMinutes() <= durationFreeInterval.toMinutes()) {
                freeIntervalListForEvent.add(freeInterval);
            }
        }
        logger.info("Found "  + freeIntervalListForEvent.size() + " free intervals for event");
        return freeIntervalListForEvent;
    }

    @Override
    public boolean isAttenderFree(Person attender, LocalDateTime startDate, LocalDateTime endDate)
            throws IllegalArgumentException, OrderOfArgumentsException {
        if (attender == null || startDate == null || endDate == null) throw new IllegalArgumentException();
        if (startDate.isAfter(endDate)) throw new OrderOfArgumentsException();

        logger.info("Checking is attender '" + attender.getName() + " " + attender.getLastName() + "' free from " +
                DateParser.dateToString(startDate) + " to " + DateParser.dateToString(endDate));
        List<Event> eventListByAttender = searchByAttenderIntoPeriod(attender, startDate, endDate);
        if (eventListByAttender.isEmpty()) {
            logger.info("Attender free");
            return true;
        }
        logger.info("Attender not free");

        return false;
    }

    @Override
    public List<Event> searchEventByTitleStartWith(String prefix) throws IllegalArgumentException {
        if (prefix == null) throw new IllegalArgumentException();

        logger.info("Searching events by title start with '" + prefix + "'");
        List<Event> presentInEventList = dataStore.searchEventByTitleStartWith(prefix);

        if (presentInEventList.isEmpty())
            logger.info("Events not found!");
        else
            logger.info("Found " + presentInEventList.size() + " events");

        return presentInEventList;
    }

    private List<List<LocalDateTime>> mergeSolidInterval(List<List<LocalDateTime>> intervalList) {

        List<List<LocalDateTime>> solidFreeIntervalList = new ArrayList<List<LocalDateTime>>();
        LocalDateTime left  = intervalList.get(0).get(0);
        LocalDateTime right = intervalList.get(0).get(1);

        for (int i = 0; i < intervalList.size()-1; i++) {
            List<LocalDateTime> leftInterval = intervalList.get(i);
            List<LocalDateTime> rightInterval = intervalList.get(i+1);
            if (leftInterval.get(1).equals(rightInterval.get(0))) {
                right = rightInterval.get(1);
            } else {
                solidFreeIntervalList.add(Arrays.asList(left,right));
                left = intervalList.get(i+1).get(0);
                right = intervalList.get(i+1).get(1);
            }
        }

        solidFreeIntervalList.add(Arrays.asList(left,intervalList.get(intervalList.size()-1).get(1)));
        return solidFreeIntervalList;
    }

    private boolean isEventAndPeriodCrossing(Event event, LocalDateTime startDate, LocalDateTime endDate)
            throws OrderOfArgumentsException, IllegalArgumentException {
        if (event == null || startDate == null || endDate == null) throw new IllegalArgumentException();
        if (startDate.isAfter(endDate)) throw new OrderOfArgumentsException();

        if (event.getStartDate().equals(startDate))
            return true;
        if (event.getEndDate().equals(endDate))
            return true;
        if (isDateIntoPeriod(event.getStartDate(), startDate, endDate))
            return true;
        if (isDateIntoPeriod(event.getEndDate(), startDate, endDate))
            return true;
        if (isPeriodIntoEvent(event, startDate, endDate))
            return true;

        return false;
    }

    private boolean isPeriodIntoEvent(Event event, LocalDateTime startDate, LocalDateTime endDate)
            throws OrderOfArgumentsException, IllegalArgumentException {
        if (event == null || startDate == null || endDate == null) throw new IllegalArgumentException();
        if (startDate.isAfter(endDate)) throw new OrderOfArgumentsException();

        if (event.getStartDate().isBefore(startDate) && event.getEndDate().isAfter(endDate))
            return true;
        else
            return false;
    }

    private boolean isDateIntoPeriod(LocalDateTime date, LocalDateTime startDate, LocalDateTime endDate)
            throws OrderOfArgumentsException, IllegalArgumentException{
        if (date == null || startDate == null || endDate == null) throw new IllegalArgumentException();
        if (startDate.isAfter(endDate)) throw new OrderOfArgumentsException();

        if (date.isAfter(startDate) && date.isBefore(endDate))
            return true;
        else
            return false;
    }

    private boolean isEventIncludesFreeInterval(Event event, List<LocalDateTime> interval) throws IllegalArgumentException {
        if (event == null || interval == null) throw new IllegalArgumentException();

        if (event.getStartDate().isBefore(interval.get(0).plusMinutes(MINUTE_INTERVAL))
                && event.getEndDate().isAfter(interval.get(1).minusMinutes(MINUTE_INTERVAL)))
            return true;
        else
            return false;
    }

    private boolean isEventAndFreeIntervalCrossingInStartOfEvent(Event event, List<LocalDateTime> interval) throws IllegalArgumentException {
        if (event == null || interval == null) throw new IllegalArgumentException();
        if (event.getStartDate().isBefore(interval.get(0).plusMinutes(MINUTE_INTERVAL))
                && event.getEndDate().isAfter(interval.get(0))
                && isEndOfEventBeforeEndOfIntervalMinusMinuteInterval(event, interval))
            return true;
        else
            return false;
    }

    private boolean isEventAndFreeIntervalCrossingInEndOfEvent(Event event, List<LocalDateTime> interval) throws IllegalArgumentException {
        if (event == null || interval == null) throw new IllegalArgumentException();
        if (isStartOfEventAfterStartOfIntervalPlusMinuteInterval(event, interval)
                && event.getStartDate().isBefore(interval.get(1))
                && event.getEndDate().isAfter(interval.get(1).minusMinutes(MINUTE_INTERVAL)))
            return true;
        else
            return false;
    }

    private boolean isFreeIntervalIncludesEvent(Event event, List<LocalDateTime> interval) throws IllegalArgumentException {
        if (event == null || interval == null) throw new IllegalArgumentException();
        if (isStartOfEventAfterStartOfIntervalPlusMinuteInterval(event, interval)
                && isEndOfEventBeforeEndOfIntervalMinusMinuteInterval(event, interval))
            return true;
        else
            return false;
    }

    private boolean isStartOfEventAfterStartOfIntervalPlusMinuteInterval(Event event, List<LocalDateTime> interval)
            throws IllegalArgumentException {
        if (event == null || interval == null) throw new IllegalArgumentException();
        if (event.getStartDate().isAfter(interval.get(0).plusMinutes(MINUTE_INTERVAL))
                || event.getStartDate().isEqual(interval.get(0).plusMinutes(MINUTE_INTERVAL)))
            return true;
        else
            return false;
    }

    private boolean isEndOfEventBeforeEndOfIntervalMinusMinuteInterval(Event event, List<LocalDateTime> interval)
            throws IllegalArgumentException {
        if (event == null || interval == null) throw new IllegalArgumentException();
        if (event.getEndDate().isBefore(interval.get(1).minusMinutes(MINUTE_INTERVAL))
                || event.getEndDate().isEqual(interval.get(1).minusMinutes(MINUTE_INTERVAL)))
            return true;
        else
            return false;
    }
}


