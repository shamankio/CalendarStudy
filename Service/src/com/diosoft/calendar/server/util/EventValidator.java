package com.diosoft.calendar.server.util;

import com.diosoft.calendar.server.common.Event;
import com.diosoft.calendar.server.exception.ValidationException;
import java.time.LocalDateTime;

public class EventValidator {

    public static void validate(Event event) throws IllegalArgumentException, ValidationException {
//null check
        if (event==null) throw new IllegalArgumentException();
        if(event.getId()==null) throw new ValidationException("Null value of Id of event");
        if(event.getTitle()==null) throw new ValidationException("Null value of Title of event");
        if(event.getDescription()==null) throw new ValidationException("Null value of Description of event");
        if(event.getStartDate()==null) throw new ValidationException("Null value of StartDate of event");
        if(event.getEndDate()==null) throw new ValidationException("Null value of EndDate of event");
        if(event.getAttenders()==null) throw new ValidationException("Null value of Attenders of event");

//not specified
        if(event.getTitle().length()==0) throw new ValidationException("Not specified event name");
        if(event.getDescription().length()==0) throw new ValidationException("Not specified event description");

//mistakes of logic
        if(event.getStartDate().isAfter(event.getEndDate())) throw new ValidationException("startDate after endDate");
        if(event.getStartDate().isBefore(LocalDateTime.now())) throw new ValidationException("startDate before current date");
    }
}
