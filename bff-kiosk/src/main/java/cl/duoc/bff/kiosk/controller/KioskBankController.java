package cl.duoc.bff.kiosk.controller;

import cl.duoc.bff.kiosk.service.KioskBankService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/kiosk")
public class KioskBankController {

    private final KioskBankService kioskBankService;

    public KioskBankController(KioskBankService kioskBankService) {
        this.kioskBankService = kioskBankService;
    }


    @GetMapping({"/cuentas/{id}", "/cajero/{id}"})
    public ResponseEntity<Map<String, Object>> consultarSaldoKiosk(@PathVariable Long id) {
        Map<String, Object> respuesta = kioskBankService.obtenerResumenKiosk(id);
        if (respuesta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping({"/cuentas/{id}/withdraw", "/cajero/{id}/withdraw"})
    public ResponseEntity<Object> retirarDineroKiosk(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Object rawAmount = body.get("amount");
            if (rawAmount == null) {
                rawAmount = body.get("monto");
            }
            double monto = Double.parseDouble(rawAmount.toString());

            Object resultado = kioskBankService.realizarRetiro(id, monto);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "No se pudo procesar el retiro: " + e.getMessage()));
        }
    }
}