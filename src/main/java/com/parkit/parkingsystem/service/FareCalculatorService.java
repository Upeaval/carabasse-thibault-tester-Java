package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        double duration = outHour - inHour;
        duration = duration / 3600000;

        double price;
        //commenter

        //évite de re écrire les conditon if/else pour les deux cas
        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                price = duration * Fare.CAR_RATE_PER_HOUR;   
                break;  
            }
            case BIKE: {
                price = duration * Fare.BIKE_RATE_PER_HOUR;
                break;
            }
        default: throw new IllegalArgumentException("Unkown Parking Type");
        }
        ticket.setPrice(price);
    }
}