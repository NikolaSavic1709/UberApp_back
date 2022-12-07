package com.uberTim12.ihor.dto.communication;

import com.uberTim12.ihor.dto.users.PassengerIdentificatorsDTO;
import com.uberTim12.ihor.model.communication.Review;
import com.uberTim12.ihor.model.ride.Ride;
import com.uberTim12.ihor.model.users.Passenger;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FullReviewDTO {
    private ReviewDTO vehicleReviewDTO;
    private ReviewDTO driverReviewDTO;

    public FullReviewDTO(Review review)
    {
        this(new ReviewDTO(review.getId(),review.getVehicleRate(),review.getVehicleComment(),new PassengerIdentificatorsDTO(review.getPassenger())),
                new ReviewDTO(review.getId(),review.getDriverRate(),review.getDriverComment(),new PassengerIdentificatorsDTO(review.getPassenger())));
    }
}
