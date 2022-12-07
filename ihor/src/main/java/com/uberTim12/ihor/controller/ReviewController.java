package com.uberTim12.ihor.controller;

import com.uberTim12.ihor.model.communication.FullReviewDTO;
import com.uberTim12.ihor.model.communication.Review;
import com.uberTim12.ihor.model.communication.ReviewDTO;
import com.uberTim12.ihor.model.communication.ReviewRequestDTO;
import com.uberTim12.ihor.service.communication.impl.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/review")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @PostMapping(value = "/{rideId}/vehicle/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> leaveReviewForVehicle(@PathVariable Integer rideId, @PathVariable("id") Integer vehicleId, @RequestBody ReviewRequestDTO reviewRequestDTO)
    {
        if(rideId==null || vehicleId==null) //TODO sve greske
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong format of some field");
        else {
            ReviewDTO review = reviewService.createVehicleReview(rideId, vehicleId, reviewRequestDTO);
            if(review==null)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            else
                return new ResponseEntity<>(review, HttpStatus.OK);
        }
    }

    @GetMapping(value = "/vehicle/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getReviewsForVehicle(@PathVariable("id") Integer vehicleId)
    {
        List<ReviewDTO> reviews=reviewService.getReviewsForVehicle(vehicleId);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    @PostMapping(value = "/{rideId}/driver/{id}",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> leaveReviewForDriver(@PathVariable Integer rideId, @PathVariable("id") Integer driverId, @RequestBody ReviewRequestDTO reviewRequestDTO)
    {
        if(rideId==null || driverId==null) //TODO sve greske
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong format of some field");
        else {
            ReviewDTO review = reviewService.createDriverReview(rideId, driverId, reviewRequestDTO);
            if(review==null)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            else
                return new ResponseEntity<>(review, HttpStatus.OK);
        }
    }

    @GetMapping(value = "/driver/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getReviewsForDriver(@PathVariable("id") Integer driverId)
    {
        List<ReviewDTO> reviews=reviewService.getReviewsForDriver(driverId);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    @GetMapping(value = "/{rideId}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getReviewsForRide(@PathVariable Integer rideId)
    {
        List<FullReviewDTO> reviews=reviewService.getReviewsForRide(rideId);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

}
