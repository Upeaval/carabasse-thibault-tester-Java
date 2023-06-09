package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.parkit.parkingsystem.model.Ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) //AJOUT
public class ParkingDataBaseIT {

    private static final Logger logger = LogManager.getLogger("ParkingService");

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        try{
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

            parkingService.processIncomingVehicle();

            Ticket savedTicket = ticketDAO.getTicket("ABCDEF");

            assertNotNull(savedTicket);
            assertEquals(savedTicket.getParkingSpot().isAvailable(), false);
        }catch(Exception e){
            logger.error("error testParkingCar", e);
        }
        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
    }

    @Test
    public void testParkingLotExit(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        try{
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(ticketDAO.getTicket("ABCDEF")).thenReturn(new Ticket());
            
            testParkingACar();

            parkingService.processExitingVehicle();

            Ticket savedTicket = ticketDAO.getTicket("ABCDEF");

            assertNotNull(savedTicket.getOutTime());
            assertNotNull(savedTicket.getPrice());

        }catch(Exception e){
            logger.error("error testParkingLotExit", e);
        }

        //TODO: check that the fare generated and out time are populated correctly in the database
    }

    @Test
    public void testParkingLotExitReccuringUser(){
        try{
            when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(50);

            testParkingLotExit();

            Ticket savedTicket = ticketDAO.getTicket("ABCDEF");

            assertEquals(true, savedTicket.getDiscount());
            assertNotNull(savedTicket.getOutTime());
        }catch(Exception e){
            logger.error("error testParkingLotExitReccuringUser", e);
        }
    }

}
