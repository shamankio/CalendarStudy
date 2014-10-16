package com.diosoft.calendar.server.filesystem;

import com.diosoft.calendar.server.common.Event;
import com.diosoft.calendar.server.exception.DateTimeFormatException;

import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public interface JAXBHelper {
    void write(Event event, BufferedWriter writer) throws IOException, JAXBException;

    Event read(BufferedReader reader) throws JAXBException, DateTimeFormatException;
}