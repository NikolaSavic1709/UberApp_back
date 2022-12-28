package com.uberTim12.ihor.service.users.impl;

import com.uberTim12.ihor.model.users.Driver;
import com.uberTim12.ihor.model.vehicle.Vehicle;
import com.uberTim12.ihor.repository.users.IDriverRepository;
import com.uberTim12.ihor.service.users.interfaces.IDriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriverService implements IDriverService {

    private IDriverRepository driverRepository;
    @Autowired
    DriverService(IDriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }
    @Override
    public Driver save(Driver driver) {
        return driverRepository.save(driver);
    }
    @Override
    public Driver findOne(Integer id) {
        return driverRepository.findById(id).orElse(null);
    }

    @Override
    public List<Driver> findAll() { return driverRepository.findAll(); }

    @Override
    public Driver findByEmail(String email) {
        return driverRepository.findByEmail(email);
    }
    @Override
    public Driver findOneWithDocuments(Integer driverId) {
        return driverRepository.findOneWithDocuments(driverId);
    }
    @Override
    public Vehicle getVehicleFor(Integer driverId) {
        Driver driver = findOne(driverId);
        if (driver != null)
            return driver.getVehicle();

        return null;
    }
    @Override
    public Page<Driver> findAll(Pageable page) {
        return driverRepository.findAll(page);
    }

    @Override
    public Driver findById(Integer id){
        return driverRepository.findById(id).orElse(null);
    }

}
