package com.uberTim12.ihor.controller.ride;

import com.uberTim12.ihor.dto.communication.PanicDTO;
import com.uberTim12.ihor.dto.communication.ReasonDTO;
import com.uberTim12.ihor.dto.ride.CreateRideDTO;
import com.uberTim12.ihor.dto.ride.RideFullDTO;
import com.uberTim12.ihor.dto.route.PathDTO;
import com.uberTim12.ihor.dto.users.UserRideDTO;
import com.uberTim12.ihor.model.communication.Panic;
import com.uberTim12.ihor.model.ride.Ride;
import com.uberTim12.ihor.model.ride.RideRejection;
import com.uberTim12.ihor.model.ride.RideStatus;
import com.uberTim12.ihor.model.route.Location;
import com.uberTim12.ihor.model.route.Path;
import com.uberTim12.ihor.model.users.Driver;
import com.uberTim12.ihor.model.users.Passenger;
import com.uberTim12.ihor.service.communication.impl.PanicService;
import com.uberTim12.ihor.service.ride.impl.RideSchedulingService;
import com.uberTim12.ihor.service.ride.impl.RideService;
import com.uberTim12.ihor.service.route.impl.LocationService;
import com.uberTim12.ihor.service.route.impl.PathService;
import com.uberTim12.ihor.service.users.impl.DriverService;
import com.uberTim12.ihor.service.users.impl.PassengerService;
import net.minidev.json.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping(value = "api/ride")
public class RideController {

    @Autowired
    private RideService rideService;
    @Autowired
    private RideSchedulingService rideSchedulingService;

    @Autowired
    private PathService pathService;

    @Autowired
    private PassengerService passengerService;

    @Autowired
    private DriverService driverService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private PanicService panicService;

    @PostMapping(consumes = "application/json")
    public ResponseEntity<?> createRide(@RequestBody CreateRideDTO rideDTO) {

        Ride ride = new Ride(rideDTO);

        Set<Path> paths = new HashSet<>();

        for (PathDTO pdto: rideDTO.getLocations()){
            Path path = new Path();

            Location departure = pdto.getDeparture().generateLocation();
            Location destination = pdto.getDestination().generateLocation();

            path.setStartPoint(departure);
            path.setEndPoint(destination);

            path = pathService.save(path);
            paths.add(path);
        }
        ride.setPaths(paths);

        Set<Passenger> passengers = new HashSet<>();
        for (UserRideDTO udto: rideDTO.getPassengers()){
            Passenger passenger = passengerService.findById(udto.getId());
            passengers.add(passenger);
        }
        ride.setPassengers(passengers);
        try{
            ride.setEstimatedTime(locationService.calculateEstimatedTime(ride.getPaths().iterator().next().getStartPoint(),ride.getPaths().iterator().next().getEndPoint()));
        }
        catch(ParseException | IOException e)
        {
            ride.setEstimatedTime(Double.MAX_VALUE);
        }


        ride=rideSchedulingService.findFreeVehicle(ride);

        if(ride==null)
              return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("driving is not possible");
        return new ResponseEntity<>(new RideFullDTO(ride), HttpStatus.OK);
    }

    @GetMapping(value = "/driver/{driverId}/active")
    public ResponseEntity<?> getActiveRideForDriver(@PathVariable Integer driverId) {

        if (driverId == 1)
            driverId++;

        Driver driver = driverService.findById(driverId);

        if (driver == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong format of some field");
        } else {
            Ride ride = rideService.findActiveByDriver(driver);
            if (ride == null){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(new RideFullDTO(ride), HttpStatus.OK);
            }
        }
    }

    @GetMapping(value = "/passenger/{passengerId}/active")
    public ResponseEntity<?> getActiveRideForPassenger(@PathVariable Integer passengerId) {

        Passenger passenger = passengerService.findById(passengerId);

        if (passenger == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong format of some field");
        } else {
            Ride ride = rideService.findActiveByPassenger(passenger);
            if (ride == null){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(new RideFullDTO(ride), HttpStatus.OK);
            }
        }
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<?> getRideById(@PathVariable Integer id) {

        Ride ride = rideService.findById(id);

        if (ride == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong format of some field");
        } else {
            return new ResponseEntity<>(new RideFullDTO(ride), HttpStatus.OK);
        }
    }

    @PutMapping(value = "/{id}/withdraw")
    public ResponseEntity<?> cancelRide(@PathVariable Integer id) {

        Ride ride = rideService.findById(id);

        if (ride == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong format of some field");
        }

        ride.setRideStatus(RideStatus.CANCELED);

        ride = rideService.save(ride);

        return new ResponseEntity<>(new RideFullDTO(ride), HttpStatus.OK);
    }

    @PutMapping(value = "/{id}/panic")
    public ResponseEntity<?> panicRide(@PathVariable Integer id, @RequestBody ReasonDTO reason) {

        Ride ride = rideService.findById(id);

        if (ride == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong format of some field");
        }

        Panic panic = new Panic();
        panic.setCurrentRide(ride);
        panic.setTime(LocalDateTime.now());
        panic.setReason(reason.getReason());

        Driver driver = ride.getDriver();

        panic.setUser(driver);

        panic = panicService.save(panic);
        return new ResponseEntity<>(new PanicDTO(panic), HttpStatus.OK);
    }

    @PutMapping(value = "/{id}/accept")
    public ResponseEntity<?> acceptRide(@PathVariable Integer id) {

        Ride ride = rideService.findById(id);

        if (ride == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong format of some field");
        }

        ride.setRideStatus(RideStatus.ACCEPTED);

        ride = rideService.save(ride);

        return new ResponseEntity<>(new RideFullDTO(ride), HttpStatus.OK);
    }

    @PutMapping(value = "/{id}/end")
    public ResponseEntity<?> endRide(@PathVariable Integer id) {

        Ride ride = rideService.findById(id);

        if (ride == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong format of some field");
        }

        ride.setRideStatus(RideStatus.FINISHED);

        ride = rideService.save(ride);

        return new ResponseEntity<>(new RideFullDTO(ride), HttpStatus.OK);
    }

    @PutMapping(value = "/{id}/cancel")
    public ResponseEntity<?> rejectRide(@PathVariable Integer id, @RequestBody ReasonDTO reason) {

    Ride ride = rideService.findById(id);

        if (ride == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong format of some field");
        }

        if(ride.getRideRejection()==null){
            ride.setRideRejection(new RideRejection());
        }

        ride.getRideRejection().setReason(reason.getReason());
        ride.getRideRejection().setTime(LocalDateTime.now());
        ride.setRideStatus(RideStatus.REJECTED);

        ride = rideService.save(ride);

        return new ResponseEntity<>(new RideFullDTO(ride), HttpStatus.OK);
    }


}
