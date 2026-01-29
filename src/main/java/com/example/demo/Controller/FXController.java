package com.example.demo.Controller;

import com.example.demo.Dto.ConversionRequest;
import com.example.demo.Dto.ConversionResponse;
import com.example.demo.Service.FXService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/fx")
public class FXController {

    private final FXService fxService;

    public FXController(FXService fxService) {
        this.fxService = fxService;
    }

    @PostMapping("/calculate")
    public ConversionResponse calculate(@Valid @RequestBody ConversionRequest request) {
        return fxService.convertCurrency(request);
    }
}