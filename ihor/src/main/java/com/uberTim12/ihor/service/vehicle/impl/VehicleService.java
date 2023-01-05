package com.uberTim12.ihor.service.vehicle.impl;

import com.uberTim12.ihor.model.users.Driver;
import com.uberTim12.ihor.model.vehicle.Vehicle;
import com.uberTim12.ihor.repository.vehicle.IVehicleRepository;
import com.uberTim12.ihor.service.base.impl.JPAService;
import com.uberTim12.ihor.service.route.interfaces.ILocationService;
import com.uberTim12.ihor.service.users.interfaces.IDriverService;
import com.uberTim12.ihor.service.vehicle.interfaces.IVehicleService;
import com.uberTim12.ihor.service.vehicle.interfaces.IVehicleTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class VehicleService extends JPAService<Vehicle> implements IVehicleService {
    private final IVehicleRepository vehicleRepository;
    private final IDriverService driverService;
    private final ILocationService locationService;
    private final IVehicleTypeService vehicleTypeService;

    @Autowired
    VehicleService(IVehicleRepository vehicleRepository, IDriverService driverService, ILocationService locationService, IVehicleTypeService vehicleTypeService) {
        this.vehicleRepository = vehicleRepository;
        this.driverService = driverService;
        this.locationService = locationService;
        this.vehicleTypeService = vehicleTypeService;
    }

    @Override
    protected JpaRepository<Vehicle, Integer> getEntityRepository() {
        return vehicleRepository;
    }

    @Override
    public void addVehicleToDriver(Integer driverId, Vehicle vehicle) {
        Driver driver = driverService.get(driverId);

        locationService.save(vehicle.getCurrentLocation());
        vehicleTypeService.save(vehicle.getVehicleType());
        vehicle.setDriver(driver);
        vehicle = save(vehicle);

        driver.setVehicle(vehicle);
        driverService.save(driver);
    }
}
