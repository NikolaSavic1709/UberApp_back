package com.uberTim12.ihor.service.stats.impl;

import com.uberTim12.ihor.model.ride.Ride;
import com.uberTim12.ihor.model.ride.RideStatus;
import com.uberTim12.ihor.model.route.Path;
import com.uberTim12.ihor.model.stats.RideCountStatistics;
import com.uberTim12.ihor.model.stats.RideDistanceStatistics;
import com.uberTim12.ihor.service.ride.interfaces.IRideService;
import com.uberTim12.ihor.service.route.interfaces.ILocationService;
import com.uberTim12.ihor.service.stats.interfaces.IGlobalStatisticsService;
import net.minidev.json.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TreeMap;

@Service
public class GlobalStatisticsService implements IGlobalStatisticsService {

    private final IRideService rideService;
    private final ILocationService locationService;

    @Autowired
    public GlobalStatisticsService(IRideService rideService, ILocationService locationService) {
        this.rideService = rideService;
        this.locationService = locationService;
    }

    @Override
    public RideCountStatistics numberOfRidesStatistics(LocalDateTime from, LocalDateTime to) {
        TreeMap<LocalDate, Integer> ridesPerDay = initializeDayMap(from, to);
        int totalCount = 0;
        double avgCount = 0d;

        List<Ride> rides = rideService.findAllRidesWithStatusInTimeRange(RideStatus.FINISHED, from, to);
        for (Ride r : rides) {
            LocalDate startDate = r.getStartTime().toLocalDate();
            ridesPerDay.put(startDate, ridesPerDay.get(startDate) + 1);
            totalCount += 1;
            avgCount += 1;
        }
        avgCount = avgCount / ridesPerDay.size();
        DecimalFormat df = new DecimalFormat("#.##");

        return new RideCountStatistics(ridesPerDay, totalCount, Double.parseDouble(df.format(avgCount)));
    }

    @Override
    public RideDistanceStatistics distancePerDayStatistics(LocalDateTime from, LocalDateTime to) {
        TreeMap<LocalDate, Integer> distancePerDay = initializeDayMap(from, to);
        int totalDistance = 0;
        double avgDistance = 0d;

        List<Ride> rides = rideService.findAllRidesWithStatusInTimeRange(RideStatus.FINISHED, from, to);
        for (Ride r : rides) {
            LocalDate startDate = r.getStartTime().toLocalDate();
            Integer distance = calculateDistance(r).intValue();
            distancePerDay.put(startDate, distancePerDay.get(startDate) + distance);
            totalDistance += distance;
            avgDistance += distance;
        }
        avgDistance = avgDistance / distancePerDay.size();

        return new RideDistanceStatistics(distancePerDay, totalDistance, (int) avgDistance);
    }

    private Double calculateDistance(Ride r) {
        Double distance = 0d;
        for (Path p : r.getPaths()) {
            try {
                distance += locationService.calculateDistance(p.getStartPoint(), p.getEndPoint());
            } catch (IOException | ParseException e) {
                return 0d;
            }
        }

        return distance;
    }

    private TreeMap<LocalDate, Integer> initializeDayMap(LocalDateTime from, LocalDateTime to) {
        TreeMap<LocalDate, Integer> dayMap = new TreeMap<>();
        for (LocalDate date = from.toLocalDate(); date.isBefore(to.toLocalDate().plusDays(1)); date = date.plusDays(1))
            dayMap.put(date, 0);

        return dayMap;
    }
}
