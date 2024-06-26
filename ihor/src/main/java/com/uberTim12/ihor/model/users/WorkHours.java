package com.uberTim12.ihor.model.users;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class WorkHours {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @OneToOne
    private Driver driver;

    public WorkHours(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public WorkHours(LocalDateTime startTime, LocalDateTime endTime, Driver driver) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.driver = driver;
    }
}
