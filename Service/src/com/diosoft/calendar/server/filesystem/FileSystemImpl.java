package com.diosoft.calendar.server.filesystem;

import com.diosoft.calendar.server.common.Event;
import com.diosoft.calendar.server.exception.DateTimeFormatException;

import javax.xml.bind.JAXBException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public class FileSystemImpl implements FileSystem{

    final private JAXBHelperImpl jaxbHelper;
    final private String pathToEvents;
    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public FileSystemImpl(JAXBHelperImpl jaxbHelper, String pathToEvents) {
        this.jaxbHelper = jaxbHelper;
        this.pathToEvents = pathToEvents;
    }

    @Override
    public void write(Event event) {
        final Event e = event;
        executorService.submit(new Runnable() {
            public void run() {
                StringBuilder sb = new StringBuilder();
                sb.append(pathToEvents).append(e.getId()).append(".xml");
                Path filePath = Paths.get(sb.toString());
                Charset charset = Charset.forName("UTF-8");
                BufferedWriter writer = null;
                try {
                    writer = Files.newBufferedWriter(filePath, charset);
                    jaxbHelper.write(e, writer);
                    writer.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (JAXBException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    @Override
    public Event read(UUID id) throws DateTimeFormatException, IOException, JAXBException, ExecutionException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        sb.append(pathToEvents).append(id).append(".xml");
        return read(Paths.get(sb.toString()));
    }

    @Override
    public Event read(Path pathToFile) throws DateTimeFormatException, IOException, JAXBException, ExecutionException, InterruptedException {
        final Path file = pathToFile;
        Future<Event> future = executorService.submit(new Callable<Event>() {
            @Override
            public Event call() throws IOException, JAXBException, DateTimeFormatException {
                return jaxbHelper.read(Files.newBufferedReader(file));
            }
        });
        return future.get();
    }

    @Override
    public boolean delete(UUID id) throws IOException {
        final UUID eventId = id;
        executorService.submit(new Runnable() {
            public void run() {
                StringBuilder sb = new StringBuilder();
                sb.append(pathToEvents).append(eventId).append(".xml");
                Path path = Paths.get(sb.toString());
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return true;
    }

    @Override
    public List<Event> readAllEventsFromXMLResources() throws IOException, DateTimeFormatException, ExecutionException, InterruptedException {
        EventFileVisitor eventFileVisitor = new EventFileVisitor();
        Files.walkFileTree(Paths.get(pathToEvents), eventFileVisitor);
        return eventFileVisitor.getEventList();
    }
}
