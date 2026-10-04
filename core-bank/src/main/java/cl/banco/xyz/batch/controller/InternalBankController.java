package cl.banco.xyz.batch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/api")
public class InternalBankController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/accounts/{id}")
    public ResponseEntity<?> getAccountData(@PathVariable Long id) {
        // 1. Consultar la cuenta en la tabla intereses_cuentas
        String sqlCuenta = "SELECT * FROM intereses_cuentas WHERE cuenta_id = ? LIMIT 1";
        List<Map<String, Object>> cuentas = jdbcTemplate.queryForList(sqlCuenta, id);

        if (cuentas.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 2. Consultar los movimientos anuales de esa cuenta en estados_cuenta_anuales
        String sqlMovimientos = "SELECT * FROM estados_cuenta_anuales WHERE cuenta_id = ?";
        List<Map<String, Object>> movimientos = jdbcTemplate.queryForList(sqlMovimientos, id);

        return ResponseEntity.ok(Map.of(
                "cuenta", cuentas.get(0),
                "movimientosAnuales", movimientos
        ));
    }
    @GetMapping("/accounts")
    public ResponseEntity<?> listAvailableAccounts() {
        String sql = "SELECT cuenta_id, nombre, saldo_final FROM intereses_cuentas LIMIT 10";
        return ResponseEntity.ok(jdbcTemplate.queryForList(sql));
    }

    @PostMapping("/accounts/{id}/withdraw")
    public ResponseEntity<?> processWithdraw(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        double monto = Double.parseDouble(body.get("amount").toString());

        // Consultar el saldo disponible en intereses_cuentas
        String sqlSaldo = "SELECT saldo_final FROM intereses_cuentas WHERE cuenta_id = ? LIMIT 1";
        List<Double> saldos = jdbcTemplate.queryForList(sqlSaldo, Double.class, id);

        if (saldos.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cuenta no encontrada"));
        }

        double saldoActual = saldos.get(0);
        if (monto > saldoActual) {
            return ResponseEntity.badRequest().body(Map.of("error", "Saldo insuficiente"));
        }

        double saldoRestante = saldoActual - monto;
        jdbcTemplate.update("UPDATE intereses_cuentas SET saldo_final = ? WHERE cuenta_id = ?", saldoRestante, id);

        Map<String, Object> evento = Map.of(
                "tipo", "RETIRO_PROCESADO",
                "cuentaId", id,
                "montoRetirado", monto,
                "saldoRestante", saldoRestante,
                "status", "APPROVED"
        );
        try {
            kafkaTemplate.send("bank-withdrawals", String.valueOf(id), objectMapper.writeValueAsString(evento));
        } catch (JsonProcessingException ex) {
            // El retiro ya fue procesado
        }

        return ResponseEntity.ok(Map.of(
                "cuentaId", id,
                "montoRetirado", monto,
                "saldoRestante", saldoRestante,
                "status", "APPROVED"
        ));
    }
}