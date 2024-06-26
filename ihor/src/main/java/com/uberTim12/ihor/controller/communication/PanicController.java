package com.uberTim12.ihor.controller.communication;

import com.uberTim12.ihor.dto.communication.ObjectListResponseDTO;
import com.uberTim12.ihor.dto.communication.PanicDTO;
import com.uberTim12.ihor.model.communication.Panic;
import com.uberTim12.ihor.service.communication.interfaces.IPanicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "api/panic")
public class PanicController {

    private final IPanicService panicService;

    @Autowired
    public PanicController(IPanicService panicService) {
        this.panicService = panicService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPanicNotifications() {
        List<Panic> panics = panicService.findAll();

        List<PanicDTO> panicsDTO = new ArrayList<>();
        for (Panic p : panics) {
            panicsDTO.add(new PanicDTO(p));
        }

        ObjectListResponseDTO<PanicDTO> res = new ObjectListResponseDTO<>(panicsDTO.size(), panicsDTO);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
