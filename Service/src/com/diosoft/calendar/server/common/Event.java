package com.diosoft.calendar.server.common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Event implements Comparable<Event>, Serializable {

    private final UUID id;
    private final String title;
    private final String description;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final Set<Person> attenders;

    public UUID getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public LocalDateTime getStartDate() {
        return startDate;
    }
    public LocalDateTime getEndDate() {
        return endDate;
    }
    public Set<Person> getAttenders() {
        return attenders;
    }

    private Event(EventBuilder eventBuilder) {
        this.id = eventBuilder.id;
        this.title = eventBuilder.title;
        this.description = eventBuilder.description;
        this.startDate = eventBuilder.startDate;
        this.endDate = eventBuilder.endDate;
        this.attenders = eventBuilder.attenders;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Event)) return false;
        if (this == obj) return true;

        Event event = (Event) obj;

        if (attenders != null ? !attenders.equals(event.attenders) : event.attenders != null) return false;
        if (description != null ? !description.equals(event.description) : event.description != null) return false;
        if (endDate != null ? !endDate.equals(event.endDate) : event.endDate != null) return false;
        if (startDate != null ? !startDate.equals(event.startDate) : event.startDate != null) return false;
        if (title != null ? !title.equals(event.title) : event.title != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (attenders != null ? attenders.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Event event) {
        if (event == null) return 1;
        int result = startDate.compareTo(event.startDate);
        if (result != 0) return (int) (result / Math.abs(result));
        result = endDate.compareTo(event.endDate);
        if (result != 0) return (int) (result / Math.abs(result));
        result = title.compareTo(event.title);
        if (result != 0) return (int) (result / Math.abs(result));
        result = description.compareTo(event.description);

        return (result != 0) ? (int) (result / Math.abs(result)) : 0;
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("Event { ");

        if (startDate.getHour() == 0 && startDate.getMinute() == 0
                && endDate.getHour() == 0 && endDate.getMinute() == 0) {
            sb.append(id).append(", ")
              .append(title).append(", ")
              .append(description).append(", ")
              .append(startDate.toLocalDate()).append(", ")
              .append(endDate.toLocalDate().minusDays(1)).append(", ")
              .append(attenders).append(" } \n");
        } else {
            sb.append(id).append(", ")
              .append(title).append(", ")
              .append(description).append(", ")
              .append(startDate).append(", ")
              .append(endDate).append(", ")
              .append(attenders).append(" } \n");
        }

        return sb.toString();
    }

    public static class EventBuilder {
        private UUID id;
        private String title;
        private String description;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Set<Person> attenders;

        public EventBuilder() {
        }

        public EventBuilder(Event originalEvent) {
            this.id = originalEvent.id;
            this.title = originalEvent.title;
            this.description = originalEvent.description;
            this.startDate = originalEvent.startDate;
            this.endDate = originalEvent.endDate;
            this.attenders = originalEvent.attenders;
        }

        public EventBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public EventBuilder title(String title) {
            this.title = title;
            return this;
        }

        public EventBuilder description(String description) {
            this.description = description;
            return this;
        }

        public EventBuilder startDate(LocalDateTime  startDate) {
            this.startDate = startDate;
            return this;
        }

        public EventBuilder endDate(LocalDateTime  endDate) {
            this.endDate = endDate;
            return this;
        }

        public EventBuilder attendersSet(Set<Person> attenders) {
            this.attenders = attenders;
            return this;
        }

        public Event build() {
            return new Event(this);
        }
    }
}



