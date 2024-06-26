package com.uberTim12.ihor.service.users.impl;

import com.uberTim12.ihor.exception.EmailAlreadyExistsException;
import com.uberTim12.ihor.model.ride.ActiveDriver;
import com.uberTim12.ihor.model.ride.ActiveDriverCriticalRide;
import com.uberTim12.ihor.model.ride.Ride;
import com.uberTim12.ihor.model.ride.RideStatus;
import com.uberTim12.ihor.model.users.Driver;
import com.uberTim12.ihor.model.vehicle.Vehicle;
import com.uberTim12.ihor.repository.ride.IActiveDriverRepository;
import com.uberTim12.ihor.repository.users.IDriverRepository;
import com.uberTim12.ihor.service.base.impl.JPAService;
import com.uberTim12.ihor.service.ride.interfaces.IRideService;
import com.uberTim12.ihor.repository.users.IAuthorityRepository;
import com.uberTim12.ihor.service.users.interfaces.IDriverService;
import com.uberTim12.ihor.util.ImageConverter;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DriverService extends JPAService<Driver> implements IDriverService {
    private final IDriverRepository driverRepository;
    private final IRideService rideService;
    private final PasswordEncoder passwordEncoder;
    private final IAuthorityRepository authorityRepository;
    private final IActiveDriverRepository activeDriverRepository;

    @Autowired
    DriverService(IDriverRepository driverRepository, IRideService rideService, PasswordEncoder passwordEncoder, IAuthorityRepository authorityRepository, IActiveDriverRepository activeDriverRepository) {
        this.driverRepository = driverRepository;
        this.rideService = rideService;
        this.passwordEncoder = passwordEncoder;
        this.authorityRepository = authorityRepository;
        this.activeDriverRepository = activeDriverRepository;
    }

    @Override
    protected JpaRepository<Driver, Integer> getEntityRepository() {
        return driverRepository;
    }

    @Override
    public Driver findByEmail(String email) {
        return driverRepository.findByEmail(email);
    }

    @Override
    public Driver register(Driver driver) throws EmailAlreadyExistsException {
        if (findByEmail(driver.getEmail()) != null)
            throw new EmailAlreadyExistsException("User with that email already exists!");

        driver.setAuthority(authorityRepository.findById(2).get());
        driver.setPassword(passwordEncoder.encode(driver.getPassword()));
        return save(driver);
    }

    @Override
    public Driver update(Integer driverId, String name, String surname, String profilePicture,
                         String telephoneNumber, String email, String address)
            throws EntityNotFoundException {
        Driver driver = get(driverId);
        driver.setName(name);
        driver.setSurname(surname);
        driver.setProfilePicture(ImageConverter.decodeToImage(profilePicture));
        driver.setTelephoneNumber(telephoneNumber);
        driver.setEmail(email);
        driver.setAddress(address);
        return save(driver);
    }

    @Override
    public boolean isDriverFreeForRide(Driver driver, Ride newRide) {
        LocalDateTime newRideStart=newRide.getStartTime();
        LocalDateTime newRideEnd=newRide.getStartTime().plusMinutes(newRide.getEstimatedTime().longValue());
        LocalDateTime rideStart, rideEnd;
        for(Ride ride: driver.getRides())
        {
            if (ride.getRideStatus() == RideStatus.ACCEPTED || ride.getRideStatus() == RideStatus.STARTED) {
                rideStart=ride.getStartTime();
                rideEnd=rideStart.plusMinutes(ride.getEstimatedTime().longValue());
                if(rideService.hasIntersectionBetweenRides(rideStart, rideEnd, newRideStart,newRideEnd))
                    return false;
            }
        }
        return true;
    }
    @Override
    public boolean isDriverFreeForRide(Driver driver) {
        for(Ride ride: driver.getRides())
        {
            if(ride.getRideStatus()==RideStatus.STARTED)
                return false;
        }
        return true;
    }

    @Override
    public List<ActiveDriverCriticalRide> sortPerEndOfCriticalRide(List<ActiveDriver> activeDrivers, Ride newRide)
    {
        Ride criticalRide;
        List<ActiveDriverCriticalRide> activeDriversCriticalRides=new ArrayList<>();
        for(ActiveDriver activeDriver:activeDrivers)
        {
            criticalRide=rideService.findCriticalRide(activeDriver.getDriver().getRides(),newRide);
            if(criticalRide!=null)
                activeDriversCriticalRides.add(new ActiveDriverCriticalRide(activeDriver,criticalRide));

        }
        activeDriversCriticalRides.sort(RideEndComparator);
        return activeDriversCriticalRides;
    }

    @Override
    public List<ActiveDriver> getActiveDrivers() {
        return activeDriverRepository.findAll();
    }

    public static Comparator<ActiveDriverCriticalRide> RideEndComparator=new Comparator<ActiveDriverCriticalRide>() {
        @Override
        public int compare(ActiveDriverCriticalRide o1, ActiveDriverCriticalRide o2) {
            LocalDateTime end1=o1.getCriticalRide().getStartTime().plusMinutes(o1.getCriticalRide().getEstimatedTime().longValue());
            LocalDateTime end2=o2.getCriticalRide().getStartTime().plusMinutes(o2.getCriticalRide().getEstimatedTime().longValue());;
            return end1.compareTo(end2);
        }
    };
}
