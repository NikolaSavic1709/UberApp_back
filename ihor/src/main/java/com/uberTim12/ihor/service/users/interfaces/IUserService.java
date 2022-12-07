package com.uberTim12.ihor.service.users.interfaces;

import com.uberTim12.ihor.model.ride.Ride;
import com.uberTim12.ihor.model.users.User;
import com.uberTim12.ihor.model.users.UserCredentialsDTO;
import com.uberTim12.ihor.model.users.UserTokensDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface IUserService {
    UserTokensDTO getUserTokens(UserCredentialsDTO userCredentialDTO);

    Page<User> getAll(Pageable page);

    boolean blockUser(Integer id);

    boolean unblockUser(Integer id);
}