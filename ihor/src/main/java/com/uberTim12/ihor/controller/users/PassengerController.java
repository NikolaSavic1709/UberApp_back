package com.uberTim12.ihor.controller.users;

import com.uberTim12.ihor.dto.communication.ObjectListResponseDTO;
import com.uberTim12.ihor.dto.ride.RideNoStatusDTO;
import com.uberTim12.ihor.dto.users.PassengerDTO;
import com.uberTim12.ihor.dto.users.PassengerRegistrationDTO;
import com.uberTim12.ihor.exception.EmailAlreadyExistsException;
import com.uberTim12.ihor.model.ride.Ride;
import com.uberTim12.ihor.model.users.Passenger;
import com.uberTim12.ihor.model.users.UserActivation;
import com.uberTim12.ihor.service.users.impl.PassengerService;
import com.uberTim12.ihor.service.users.interfaces.IUserActivationService;
import com.uberTim12.ihor.service.users.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "api/passenger")
public class PassengerController {
    private final PassengerService passengerService;
    private final IUserActivationService userActivationService;
    private final IUserService userService;

    @Autowired
    public PassengerController(PassengerService passengerService, IUserActivationService userActivationService, IUserService userService) {
        this.passengerService = passengerService;
        this.userActivationService = userActivationService;
        this.userService = userService;
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<?> createPassenger(@RequestBody PassengerRegistrationDTO passengerDTO) {
        try {
            userService.emailTaken(passengerDTO.getEmail());
            Passenger passenger = passengerDTO.generatePassenger();
            passenger.setActive(false);
            //DODATI METODU U SERVISU ZA REGISTRACIJU

            passenger = passengerService.save(passenger);
            UserActivation ua = userActivationService.save(passenger);
            return new ResponseEntity<>(new PassengerDTO(passenger), HttpStatus.OK);
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with that email already exist!");
        }
    }

    @GetMapping
    public ResponseEntity<?> getPassengersPage(Pageable page) {
        Page<Passenger> passengers = passengerService.getAll(page);

        List<PassengerDTO> passengersDTO = new ArrayList<>();
        for (Passenger p : passengers) {
            passengersDTO.add(new PassengerDTO(p));
        }

        ObjectListResponseDTO<PassengerDTO> res = new ObjectListResponseDTO<>(passengersDTO.size(),passengersDTO);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping(value = "/activate/{activationId}")
    public ResponseEntity<?> activatePassenger(@PathVariable Integer activationId) {
        UserActivation ua = userActivationService.get(activationId);

        if (ua == null || ua.getExpiryDate().isBefore(LocalDateTime.now())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong format of some field");
        } else {
            Passenger passenger = (Passenger) ua.getUser();

            passenger.setActive(true);
            passengerService.save(passenger);

            userActivationService.delete(activationId);

            return new ResponseEntity<>(HttpStatus.OK);
        }
    }


    @GetMapping(value = "/{id}")
    public ResponseEntity<?> getPassenger(@PathVariable Integer id) {

        Passenger passenger = passengerService.get(id);

        if (passenger == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong format of some field");
        } else {
            return new ResponseEntity<>(new PassengerDTO(passenger), HttpStatus.OK);
        }
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<?> updatePassenger(@PathVariable Integer id, @RequestBody PassengerRegistrationDTO passengerDTO) {

        Passenger passenger = passengerService.findByIdWithRides(id);

        if (passenger == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong format of some field");
        }

        passenger.setName(passengerDTO.getName());
        passenger.setSurname(passengerDTO.getSurname());
        passenger.setProfilePicture(passengerDTO.getProfilePicture());
        passenger.setTelephoneNumber(passengerDTO.getTelephoneNumber());
        passenger.setEmail(passengerDTO.getEmail());
        passenger.setAddress(passengerDTO.getAddress());

        if (!passengerDTO.getPassword().equals("")){
            passenger.setPassword(passengerDTO.getPassword());
        }

        passenger = passengerService.save(passenger);

        return new ResponseEntity<>(new PassengerDTO(passenger), HttpStatus.OK);
    }

    @GetMapping(value = "/{id}/ride")
    public ResponseEntity<?> getPassengerRidesPage(@PathVariable Integer id, Pageable page,
                                                   @RequestParam(required = false) String from,
                                                   @RequestParam(required = false) String to) {

        Passenger passenger = passengerService.findByIdWithRides(id);

        if (passenger == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong format of some field");
        }


        Page<Ride> rides;

        if (from == null || to == null)
            rides = passengerService.findAllById(passenger, page);
        else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime start =  LocalDate.parse(from, formatter).atStartOfDay();
            LocalDateTime end =  LocalDate.parse(to, formatter).atStartOfDay();
            rides = passengerService.findAllById(id, start, end, page);
        }

        List<RideNoStatusDTO> rideDTOs = new ArrayList<>();
        for (Ride r : rides)
            rideDTOs.add(new RideNoStatusDTO(r));

        ObjectListResponseDTO<RideNoStatusDTO> res = new ObjectListResponseDTO<>(rideDTOs.size(),rideDTOs);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
