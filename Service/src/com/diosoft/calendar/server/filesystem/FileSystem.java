package com.diosoft.calendar.server.filesystem;

import com.diosoft.calendar.server.common.Event;
import com.diosoft.calendar.server.exception.DateTimeFormatException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface FileSystem {

    void write(Event event) throws IOException, JAXBException;

    Event read(UUID id) throws DateTimeFormatException, IOException, JAXBException, ExecutionException, InterruptedException;

    Event read(Path pathToFile) throws DateTimeFormatException, IOException, JAXBException, ExecutionException, InterruptedException;

    boolean delete(UUID id) throws IOException;

    List<Event>  readAllEventsFromXMLResources() throws IOException, DateTimeFormatException, ExecutionException, InterruptedException;
}
