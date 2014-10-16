package com.diosoft.calendar.server.datastore;

import com.diosoft.calendar.server.common.Event;
import com.diosoft.calendar.server.common.Person;
import com.diosoft.calendar.server.exception.DateTimeFormatException;
import com.diosoft.calendar.server.filesystem.FileSystem;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class DataStoreImpl implements DataStore {

    private Map<UUID,Event> eventStore = new HashMap<UUID,Event>();
    private Map<String, List<UUID>> indexTitle = new HashMap<String, List<UUID>>();
    private Map<LocalDate, List<UUID>> indexDate = new HashMap<LocalDate, List<UUID>>();
    private Map<Person, List<UUID>> indexAttender = new HashMap<Person, List<UUID>>();

    private final FileSystem fileSystem;

    public DataStoreImpl(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public void initDataStoreFromXMLResources() throws IOException, DateTimeFormatException, ExecutionException, InterruptedException {
        List<Event> eventList = fileSystem.readAllEventsFromXMLResources();
        for(Event event : eventList) {
            if (isEventDuplicate(event)) continue;
            eventStore.put(event.getId(), event);
            createIndexTitle(event);
            createIndexDate(event);
            createIndexAttender(event);
        }
    }

    @Override
    public void publish(Event event) throws IllegalArgumentException, IOException, JAXBException {
        if (event==null) throw new IllegalArgumentException();
        if (isEventDuplicate(event)) return;
// add event
        eventStore.put(event.getId(), event);
// index by title
        createIndexTitle(event);
// index by date
        createIndexDate(event);
// index by attender
        createIndexAttender(event);
// create xml file with event
        fileSystem.write(event);
   }

    @Override
   public Event remove(UUID id) throws IllegalArgumentException, JAXBException, IOException {
       if (id==null) throw new IllegalArgumentException();
// remove event
       Event event = eventStore.remove(id);
       if (event!=null) {
// remove index date
           removeIndexDate(event);
// remove index title
           removeIndexTitle(event);
// remove index attender
           removeIndexAttender(event);
// delete xml file with event
           fileSystem.delete(event.getId());
       }
      return event;
   }

   @Override
   public Event getEventById(UUID id) throws IllegalArgumentException  {
        if (id==null) throw new IllegalArgumentException();
        return eventStore.get(id);
   }

   @Override
   public List<Event> getEventByTitle(String title) throws IllegalArgumentException  {
        if (title==null) throw new IllegalArgumentException();

        List<UUID> ids = indexTitle.get(title);
        List<Event> events = new ArrayList<Event>();
        if (ids!=null) {
            for (UUID id : ids) {
                Event event = eventStore.get(id);
                events.add(event);
            }
        }
        return events;
   }

   @Override
   public List<Event> getEventByDay(LocalDate day) throws IllegalArgumentException   {
        if (day==null) throw new IllegalArgumentException();

        List<UUID> ids = indexDate.get(day);
        List<Event> events = new ArrayList<Event>();
        if (ids!=null) {
            for (UUID id : ids) {
                Event event = eventStore.get(id);
                events.add(event);
            }
        }
        return events;
   }

    @Override
    public List<Event> getEventByAttender(Person attender) throws IllegalArgumentException {
        if (attender==null) throw new IllegalArgumentException();

        List<UUID> ids = indexAttender.get(attender);
        List<Event> events = new ArrayList<Event>();
        if (ids!=null) {
            for (UUID id : ids) {
                Event event = eventStore.get(id);
                events.add(event);
            }
        }
        return events;
    }

    @Override
    public List<Event> searchEventByTitleStartWith(String prefix) throws IllegalArgumentException{
        if (prefix == null) throw new IllegalArgumentException();
        List<Event> presentInEventList = new ArrayList<Event>();
        for(String title :indexTitle.keySet()){
            if(title.startsWith(prefix)){
                for(UUID uuidTitle : indexTitle.get(title)) {
                    presentInEventList.add(eventStore.get(uuidTitle));
                }
            }
        }
        return presentInEventList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DataStoreImpl{");
        sb.append("eventStore=").append(eventStore);
        sb.append(", indexTitle=").append(indexTitle);
        sb.append(", indexDate=").append(indexDate);
        sb.append(", indexAttender=").append(indexAttender);
        sb.append('}');
        return sb.toString();
    }

    private void createIndexTitle(Event event) {
        List<UUID> idsTitle = indexTitle.get(event.getTitle());
        if (idsTitle == null) {
            idsTitle = new ArrayList<UUID>();
            idsTitle.add(event.getId());
            indexTitle.put(event.getTitle(), idsTitle);
        } else {
            idsTitle.add(event.getId());
        }
    }
    private void createIndexDate(Event event) {
        LocalDate startDay = event.getStartDate().toLocalDate();
        LocalDate endDay = event.getEndDate().toLocalDate();

        while(startDay.isBefore(endDay) || startDay.equals(endDay)) {
            List<UUID> idsDate = indexDate.get(startDay);
            if(idsDate==null) {
                idsDate = new ArrayList<UUID>();
                idsDate.add(event.getId());
                indexDate.put(startDay, idsDate);
            } else {
                idsDate.add(event.getId());
            }
            startDay = startDay.plusDays(1);
        }
    }
    private void createIndexAttender(Event event) {
        Set<Person> attenders = event.getAttenders();
        for (Person attender : attenders) {
            List<UUID> idsAttender = indexAttender.get(attender);
            if (idsAttender == null) {
                idsAttender = new ArrayList<UUID>();
                idsAttender.add(event.getId());
                indexAttender.put(attender, idsAttender);
            } else {
                idsAttender.add(event.getId());
            }
        }
    }

    private void removeIndexTitle(Event event) {
        List<UUID> idsTitle = indexTitle.get(event.getTitle());
        if (idsTitle.size() <= 1) {
            indexTitle.remove(event.getTitle());
        } else {
            idsTitle.remove(event.getId());
        }
    }
    private void removeIndexDate(Event event) {
        LocalDate startDay = event.getStartDate().toLocalDate();
        LocalDate endDay = event.getEndDate().toLocalDate();

        while(startDay.isBefore(endDay) || startDay.equals(endDay)) {
            List<UUID> idsDate = indexDate.get(startDay);

            if (idsDate.size() <= 1) {
                indexDate.remove(startDay);
            } else {
                idsDate.remove(event.getId());
            }
            startDay = startDay.plusDays(1);
        }
    }

    private void removeIndexAttender(Event event) {
        Set<Person> attenders = event.getAttenders();
        for (Person attender : attenders) {
            List<UUID> idsAttender = indexAttender.get(attender);
            if (idsAttender.size() <= 1) {
                indexTitle.remove(event.getTitle());
            } else {
                idsAttender.remove(event.getId());
            }
        }
    }

    private boolean isEventDuplicate(Event event) {
        for(Event e : searchEventByTitleStartWith(event.getTitle()))
            if (e.equals(event))
                return true;
        return false;
    }
}
