package com.uberTim12.ihor.model.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DriverStatistics {
    Integer numberOfRejectedRides;
    Integer numberOfAcceptedRides;
    Integer totalWorkHours;
    Integer totalIncome;
}

