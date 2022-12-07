package com.uberTim12.ihor.controller.users;

import com.uberTim12.ihor.dto.communication.ObjectListResponseDTO;
import com.uberTim12.ihor.model.communication.Message;
import com.uberTim12.ihor.dto.communication.NoteDTO;
import com.uberTim12.ihor.dto.communication.RequestNoteDTO;
import com.uberTim12.ihor.dto.communication.SendingMessageDTO;
import com.uberTim12.ihor.model.ride.Ride;
import com.uberTim12.ihor.dto.ride.RideDTO;
import com.uberTim12.ihor.model.users.User;
import com.uberTim12.ihor.dto.users.UserDTO;
import com.uberTim12.ihor.service.communication.impl.MessageService;
import com.uberTim12.ihor.service.communication.impl.ReviewService;
import com.uberTim12.ihor.service.ride.impl.RideService;
import com.uberTim12.ihor.service.users.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "api/user")
public class UserController {

    @Autowired
    private RideService rideService;
    @Autowired
    private UserService userService;
    @Autowired
    private MessageService messageService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping(value = "/{id}/ride",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserRides(@PathVariable Integer id, @RequestParam(required = false)
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime from,
                                          @RequestParam(required = false)
                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime to, Pageable page)
    {
        Page<Ride> rides = rideService.getRides(id,from,to,page);
        List<RideDTO> ridesDTO=new ArrayList<>();
        if(rides==null) //TODO sve greske
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong format of some field");
        else {
            for (Ride r : rides)
                ridesDTO.add(new RideDTO(r));

            ObjectListResponseDTO<RideDTO> res = new ObjectListResponseDTO<>(ridesDTO.size(),ridesDTO);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUsers(Pageable page)
    {
        Page<User> users = userService.getAll(page);
        List<UserDTO> usersDTO=new ArrayList<>();
        if(users==null) //TODO sve greske
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong format of some field");
        else {
            for (User u : users)
                usersDTO.add(new UserDTO(u));
            ObjectListResponseDTO<UserDTO> res = new ObjectListResponseDTO<>(usersDTO.size(),usersDTO);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
    }


    @GetMapping(value = "/{id}/message",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserMessages(@PathVariable Integer id)
    {
        List<Message> messages = messageService.getMessages(id);
        if(messages==null) //TODO sve greske
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong format of some field");
        else {
            ObjectListResponseDTO<Message> res = new ObjectListResponseDTO<>(messages.size(),messages);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
    }

    @PostMapping(value = "/{id}/message",consumes=MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> sendMessage(@PathVariable("id") Integer senderId, @RequestBody SendingMessageDTO sendingMessageDTO)
    {
        Message message = messageService.sendMessage(senderId, sendingMessageDTO);
        if(message==null) //TODO sve greske
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong format of some field");
        else {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
    }

    @PutMapping(value = "/{id}/block")
    public ResponseEntity<?> blockUser(@PathVariable("id") Integer id)
    {
        if(id==null) //TODO sve greske
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong format of some field");
        else {
            boolean success=userService.blockUser(id);
            if(success)
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            else
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(value = "/{id}/unblock")
    public ResponseEntity<?> unblockUser(@PathVariable("id") Integer id)
    {
        if(id==null) //TODO sve greske
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong format of some field");
        else {
            boolean success=userService.unblockUser(id);
            if(success)
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            else
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/{id}/note",consumes=MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createNote(@PathVariable Integer id, @RequestBody RequestNoteDTO requestNoteDTO)
    {
        if(id==null) //TODO sve greske
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong format of some field");
        else {
            NoteDTO noteDTO = reviewService.createNote(id, requestNoteDTO);
            if(noteDTO==null)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            else
                return new ResponseEntity<>(noteDTO, HttpStatus.OK);
        }
    }
    @GetMapping(value = "/{id}/note",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getNotes(@PathVariable Integer id)
    {
        if(id==null) //TODO sve greske
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong format of some field");
        else {
            Page<NoteDTO> notes = reviewService.getNotes(id);
            if(notes==null)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            else{
                List<NoteDTO> noteDTOS = new ArrayList<>();
                for (NoteDTO n : notes){
                    noteDTOS.add(n);
                }
                ObjectListResponseDTO<NoteDTO> res = new ObjectListResponseDTO<>(noteDTOS.size(),noteDTOS);
                return new ResponseEntity<>(res, HttpStatus.OK);}
        }
    }
}