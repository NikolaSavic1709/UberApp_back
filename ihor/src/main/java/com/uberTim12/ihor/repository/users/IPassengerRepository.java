package com.uberTim12.ihor.repository.users;

import com.uberTim12.ihor.model.users.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface IPassengerRepository extends JpaRepository<Passenger, Integer> {

    Passenger findByEmail(String email);

    @Query("select p from Passenger p join fetch p.rides e where p.id =?1")
    Passenger findByIdWithRides(Integer id);

    @Query("select p from Passenger p left join fetch p.favoriteRoutes f where p.id =?1")
    Passenger findByIdWithFavorites(Integer id);

    @Query("select p from Passenger p join p.favoriteRoutes f where p.email =?1")
    Passenger findByEmailWithFavorites(String email);
}
