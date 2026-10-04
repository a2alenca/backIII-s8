package cl.duoc.bff.mobile.controller;

import cl.duoc.bff.mobile.service.MobileBankService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/mobile")
public class MobileBankController {

    private final MobileBankService mobileMovieService;

    public MobileBankController(MobileBankService mobileMovieService) {
        this.mobileMovieService = mobileMovieService;
    }

    @GetMapping("/cuentas/{id}")
    public ResponseEntity<Map<String, Object>> obtenerCuentaMobile(@PathVariable Long id) {
        Map<String, Object> respuesta = mobileMovieService.obtenerResumenMobile(id);
        if (respuesta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(respuesta);
    }
}