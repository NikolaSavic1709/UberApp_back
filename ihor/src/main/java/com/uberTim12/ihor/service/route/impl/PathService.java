package com.uberTim12.ihor.service.route.impl;

import com.uberTim12.ihor.model.route.Path;
import com.uberTim12.ihor.repository.ride.IRideRepository;
import com.uberTim12.ihor.repository.route.IPathRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PathService {
    @Autowired
    private IPathRepository pathRepository;

    public Path save(Path path){
        return pathRepository.save(path);
    }
}