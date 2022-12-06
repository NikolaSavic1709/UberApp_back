package com.uberTim12.ihor.dto.ride;

import com.uberTim12.ihor.dto.route.PathDTO;
import com.uberTim12.ihor.dto.users.UserRideDTO;
import com.uberTim12.ihor.model.comunication.Review;
import com.uberTim12.ihor.model.ride.Ride;
import com.uberTim12.ihor.model.ride.RideRejection;
import com.uberTim12.ihor.model.ride.RideStatus;
import com.uberTim12.ihor.model.route.Path;
import com.uberTim12.ihor.model.users.Driver;
import com.uberTim12.ihor.model.users.Passenger;
import com.uberTim12.ihor.model.users.User;
import com.uberTim12.ihor.model.vehicle.VehicleCategory;
import com.uberTim12.ihor.model.vehicle.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RideDTO {

    private Integer id;
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Double totalCost;

    private UserRideDTO driver;

    private Set<UserRideDTO> passengers = new HashSet<>();

    private Integer estimatedTimeInMinutes;

    private VehicleCategory vehicleType;
    private boolean babyTransport;

    private boolean petTransport;


    private RideRejectionDTO rejection;

    private Set<PathDTO> locations = new HashSet<>();

    private RideStatus status;

    public RideDTO(Ride ride){
        this(ride.getId(), ride.getStartTime(), ride.getEndTime(), ride.getTotalPrice(), ride.getEstimatedTime(),
                ride.getVehicleType().getVehicleCategory(), ride.isBabiesAllowed(), ride.isPetsAllowed(), ride.getRideStatus());

        this.driver = new UserRideDTO(ride.getDriver());

        Set<UserRideDTO> passengers = new HashSet<>();
        for (User u : ride.getPassengers()){
            passengers.add(new UserRideDTO(u));
        }
        this.passengers = passengers;

        this.rejection = new RideRejectionDTO(ride.getRideRejection());

        Set<PathDTO> locations = new HashSet<>();
        for (Path p : ride.getPaths()){
            locations.add(new PathDTO(p));
        }
        this.locations = locations;
    }

    public RideDTO(Integer id, LocalDateTime startTime, LocalDateTime endTime, Double totalPrice, Integer estimatedTime,
                   VehicleCategory vehicleCategory, boolean babiesAllowed, boolean petsAllowed, RideStatus rideStatus) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalCost = totalPrice;
        this.estimatedTimeInMinutes = estimatedTime;
        this.vehicleType = vehicleCategory;
        this.babyTransport = babiesAllowed;
        this.petTransport = petsAllowed;
        this.status = rideStatus;
    }
}
