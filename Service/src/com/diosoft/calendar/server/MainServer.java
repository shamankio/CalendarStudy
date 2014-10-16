package com.diosoft.calendar.server;

import com.diosoft.calendar.server.exception.DateTimeFormatException;
import com.diosoft.calendar.server.exception.OrderOfArgumentsException;
import com.diosoft.calendar.server.exception.ValidationException;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;

public class MainServer {
    private static final Logger logger = Logger.getLogger(MainServer.class);

    public static void main(String[] args) throws DateTimeFormatException, IOException, OrderOfArgumentsException, ValidationException, JAXBException {
//  Start server
        logger.info("Server starting...");
        new ClassPathXmlApplicationContext("app-context-server.xml");
        logger.info("Server started.");
    }
}
