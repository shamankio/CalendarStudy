package com.diosoft.calendar.server.filesystem;

import com.diosoft.calendar.server.common.Event;
import com.diosoft.calendar.server.exception.DateTimeFormatException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class EventFileVisitor extends SimpleFileVisitor<Path> {

    private final List<Event> eventList = new ArrayList<Event>();
    private final JAXBHelper jaxbHelper = new JAXBHelperImpl();
    private final List<Future<Event>> futures = new ArrayList<Future<Event>>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException
    {
        final Path fileEvent = file;
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.xml");
        if (attrs.isRegularFile() && matcher.matches(fileEvent.getFileName())) {
           futures.add(executorService.submit(new Callable<Event>() {
                public Event call() throws IOException, JAXBException, DateTimeFormatException {
                    return jaxbHelper.read(Files.newBufferedReader(fileEvent));
                }
            }));
        }
        return FileVisitResult.CONTINUE;
    }

    public List<Event> getEventList() throws ExecutionException, InterruptedException {
        for (Future<Event> f : futures) eventList.add(f.get());
        executorService.shutdown();
        return eventList;
    }
}