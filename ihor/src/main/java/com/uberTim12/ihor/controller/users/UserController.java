package com.uberTim12.ihor.controller.users;

import com.uberTim12.ihor.dto.ResponseMessageDTO;
import com.uberTim12.ihor.dto.communication.*;
import com.uberTim12.ihor.dto.ride.RideFullDTO;
import com.uberTim12.ihor.dto.users.*;
import com.uberTim12.ihor.dto.users.NoteDTO;
import com.uberTim12.ihor.exception.*;
import com.uberTim12.ihor.model.communication.Message;
import com.uberTim12.ihor.model.ride.Ride;
import com.uberTim12.ihor.model.users.Note;
import com.uberTim12.ihor.model.users.User;
import com.uberTim12.ihor.security.AuthUtil;
import com.uberTim12.ihor.security.JwtUtil;
import com.uberTim12.ihor.service.communication.impl.MessageService;
import com.uberTim12.ihor.service.communication.interfaces.IMessageService;
import com.uberTim12.ihor.service.ride.impl.RideService;
import com.uberTim12.ihor.service.ride.interfaces.IRideService;
import com.uberTim12.ihor.service.users.impl.UserService;
import com.uberTim12.ihor.service.users.interfaces.INoteService;
import com.uberTim12.ihor.service.users.interfaces.IUserService;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "api/user")
public class UserController {
    private final IRideService rideService;
    private final IUserService userService;
    private final IMessageService messageService;
    private final INoteService noteService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuthUtil authUtil;
    private PasswordEncoder passwordEncoder;

    @Autowired
    UserController(RideService rideService,
                   UserService userService,
                   MessageService messageService,
                   INoteService noteService, AuthenticationManager authenticationManager,
                   JwtUtil jwtUtil, AuthUtil authUtil, PasswordEncoder passwordEncoder) {
        this.rideService = rideService;
        this.userService = userService;
        this.messageService = messageService;
        this.noteService = noteService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.authUtil = authUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping(value = "/{id}/ride",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserRides(@Min(value = 1) @PathVariable Integer id,
                                          Pageable page,
                                          @RequestParam(required = false) String fromStr,
                                          @RequestParam(required = false) String toStr,
                                          @RequestHeader("Authorization") String authHeader
                                          )
    {
        if(Integer.parseInt(jwtUtil.extractId(authHeader.substring(7)))!=id &&
                (jwtUtil.extractRole(authHeader.substring(7)).equals("ROLE_PASSENGER") ||
                        jwtUtil.extractRole(authHeader.substring(7)).equals("ROLE_DRIVER")))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist!");

        try {
            userService.get(id);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist!");
        }

        Page<Ride> rides;
        if (fromStr == null || toStr == null)
            rides = rideService.findFilteredRidesForUser(id, page);
        else {
            LocalDateTime from = LocalDateTime.parse(fromStr);
            LocalDateTime to = LocalDateTime.parse(toStr);
            rides = rideService.findFilteredRidesForUser(id, from, to, page);
        }

        List<RideFullDTO> rideDTOs = new ArrayList<>();
        for (Ride r : rides)
            rideDTOs.add(new RideFullDTO(r));

        ObjectListResponseDTO<RideFullDTO> objectListResponse = new ObjectListResponseDTO<>((int) rides.getTotalElements(), rideDTOs);
        return new ResponseEntity<>(objectListResponse, HttpStatus.OK);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ObjectListResponseDTO<UserDTO>> getUsers(Pageable page)
    {
        Page<User> users = userService.getAll(page);

        List<UserDTO> usersDTO = new ArrayList<>();
        for (User u : users)
            usersDTO.add(new UserDTO(u));

        ObjectListResponseDTO<UserDTO> res = new ObjectListResponseDTO<>(userService.getAll().size(), usersDTO);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping(value = "/login",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserCredentialsDTO userCredentialDTO)
    {
        try {
            userService.checkIntegrity(userCredentialDTO.getEmail());
            var authentication = authenticationManager.authenticate (
                    new UsernamePasswordAuthenticationToken(userCredentialDTO.getEmail(),
                             userCredentialDTO.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtUtil.generateToken(authentication);
            AuthTokenDTO tokenDTO = new AuthTokenDTO(token, token);
            return new ResponseEntity<>(tokenDTO, HttpStatus.OK);
        } catch (AuthenticationException | AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO( "Wrong username or password!"));
        }
    }

    @GetMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> logoutUser () {
        SecurityContextHolder.getContext().setAuthentication(null);
        return ResponseEntity.status(HttpStatus.OK).body("Successful logout.");
    }

    @GetMapping(value = "/{id}/message",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserMessages(@Min(value = 1) @PathVariable Integer id, @RequestHeader("Authorization") String authHeader)
    {
        if(Integer.parseInt(jwtUtil.extractId(authHeader.substring(7)))!=id)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist!");
        try {
            User user=userService.get(id);
            List<MessageDTO> messages;
            if(user.getAuthority().getName().equals("ROLE_ADMIN"))
                messages=messageService.getMessagesForAdmin();
            else
                messages = messageService.getMessages(id);
            ObjectListResponseDTO<MessageDTO> res = new ObjectListResponseDTO<>(messages.size(), messages);
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body( "User does not exist!");
        }
    }

    @GetMapping(value = "/{id}/message/{ride_id}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserMessagesFromSpecificRide(@Min(value = 1) @PathVariable Integer id, @PathVariable Integer ride_id, @RequestHeader("Authorization") String authHeader)
    {
        if(Integer.parseInt(jwtUtil.extractId(authHeader.substring(7)))!=id)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist!");
        try {
            userService.get(id);
            List<MessageDTO> messages = messageService.getMessagesOfSpecificRide(id, ride_id);
            ObjectListResponseDTO<MessageDTO> res = new ObjectListResponseDTO<>(messageService.getAll().size(), messages);
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body( "User does not exist!");
        }
    }

    @PostMapping(value = "/{id}/message",consumes=MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> sendMessage(@Min(value = 1) @PathVariable("id") Integer receiverId, @Valid @RequestBody SendingMessageDTO sendingMessageDTO, @RequestHeader("Authorization") String authHeader)
    {
        try {
            Message message = messageService.sendMessage(Integer.parseInt(jwtUtil.extractId(authHeader.substring(7))), receiverId,
                    sendingMessageDTO.getRideId(), sendingMessageDTO.getMessage(), sendingMessageDTO.getType());
            return new ResponseEntity<>(new MessageDTO(message), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body( e.getMessage());
        }
    }
    @PostMapping(value = "/send-messages",consumes=MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> sendMessageToPassengers(@Valid @RequestBody MultipleSendingMessageDTO multipleSendingMessageDTO, @RequestHeader("Authorization") String authHeader)
    {
        try {
            for(int receiverId:multipleSendingMessageDTO.getUserIds())
            {
                messageService.sendMessage(Integer.parseInt(jwtUtil.extractId(authHeader.substring(7))), receiverId,
                        multipleSendingMessageDTO.getMessage().getRideId(), multipleSendingMessageDTO.getMessage().getMessage(), multipleSendingMessageDTO.getMessage().getType());
            }

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body( e.getMessage());
        }
    }

    @PutMapping(value = "/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> blockUser(@Min(value = 1) @PathVariable("id") Integer id)
    {
        try {
            userService.blockUser(id);
            return ResponseEntity.status(HttpStatus.OK).body("User is successfully blocked");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist!");
        } catch (UserAlreadyBlockedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO("User already blocked!"));
        }
    }

    @PutMapping(value = "/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> unblockUser(@Min(value = 1) @PathVariable("id") Integer id)
    {
        try {
            userService.unblockUser(id);
            return ResponseEntity.status(HttpStatus.OK).body("User is successfully unblocked");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist!");
        } catch (UserNotBlockedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO( "User is not blocked!"));
        }
    }

    @PostMapping(value = "/{id}/note",consumes=MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createNote(@Min(value = 1) @PathVariable Integer id, @Valid @RequestBody RequestNoteDTO requestNoteDTO)
    {
        try {
            Note note = noteService.create(id, requestNoteDTO.getMessage());
            return new ResponseEntity<>(new NoteDTO(note), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body( "User does not exist!");
        }
    }
    @GetMapping(value = "/{id}/note",produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getNotes(@Min(value = 1) @PathVariable Integer id, Pageable page)
    {
        try {
            userService.get(id);
            Page<Note> notes = noteService.getFor(id, page);

            List<NoteDTO> noteDTOs = new ArrayList<>();
            for (Note n : notes) {
                noteDTOs.add(new NoteDTO(n));
            }

            ObjectListResponseDTO<NoteDTO> objectListResponse = new ObjectListResponseDTO<>((int) notes.getTotalElements(), noteDTOs);
            return new ResponseEntity<>(objectListResponse, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body( "User does not exist!");
        }
    }

    @PutMapping(value="/{id}/changePassword", consumes = "application/json")
    public ResponseEntity<?> changePassword(@Min(value = 1) @PathVariable Integer id, @Valid @RequestBody NewPasswordDTO newPasswordDTO, @RequestHeader("Authorization") String authHeader)
    {
        if(Integer.parseInt(jwtUtil.extractId(authHeader.substring(7)))!=id && (jwtUtil.extractRole(authHeader.substring(7)).equals("ROLE_PASSENGER")|| jwtUtil.extractRole(authHeader.substring(7)).equals("ROLE_DRIVER")))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist!");
        try {
            userService.changePassword(id, newPasswordDTO.getOldPassword(), newPasswordDTO.getNewPassword());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Password successfully changed!");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body( "User does not exist!");
        } catch (PasswordDoesNotMatchException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO("Current password is not matching!"));
        }
    }

    @GetMapping(value="/{id}/resetPassword")
    public ResponseEntity<?> sendResetCodeToEmail(@Min(value = 1) @PathVariable Integer id)
    {
        try {
            userService.forgotPassword(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Email with reset code has been sent!");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist!");
        } catch (MessagingException | UnsupportedEncodingException e) {
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body("Error while sending mail!");
        }

    }

    @PutMapping(value="/{id}/resetPassword", consumes = "application/json")
    public ResponseEntity<?> changePasswordWithResetCode(@Min(value = 1) @PathVariable Integer id, @Valid @RequestBody ResetPasswordDTO resetPasswordDTO)
    {
        try {
            userService.resetPassword(id, resetPasswordDTO.getCode(), resetPasswordDTO.getNewPassword());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Password successfully changed!");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body( "User does not exist!");
        } catch (IncorrectCodeException | CodeExpiredException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO("Code is expired or not correct!"));
        }
    }
}
