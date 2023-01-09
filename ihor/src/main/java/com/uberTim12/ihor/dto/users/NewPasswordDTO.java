package com.uberTim12.ihor.dto.users;

import com.uberTim12.ihor.model.users.Passenger;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NewPasswordDTO {

    private String old_password;
    private String new_password;

}
