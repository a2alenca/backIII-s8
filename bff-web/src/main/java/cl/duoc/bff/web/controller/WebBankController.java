package cl.duoc.bff.web.controller;

import cl.duoc.bff.web.client.CoreBankClient;
import cl.duoc.bff.web.service.KafkaEventConsumer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/web")
public class WebBankController {

    private final CoreBankClient client;
    private final KafkaEventConsumer kafkaEventConsumer;

    public WebBankController(CoreBankClient client, KafkaEventConsumer kafkaEventConsumer) {
        this.client = client;
        this.kafkaEventConsumer = kafkaEventConsumer;
    }

    // Endpoint para buscar una cuenta específica por ID con la estructura exacta del canal Web
    @GetMapping("/cuentas/{id}")
    public ResponseEntity<Map<String, Object>> obtenerCuentaWeb(@PathVariable Long id) {
        // Pide los datos al Core del Banco
        Map<String, Object> dataCore = (Map<String, Object>) client.obtenerCuentaPorId(id);

        if (dataCore == null || dataCore.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Estructura la respuesta enriquecida para el canal Web
        Map<String, Object> respuestaWeb = Map.of(
                "cuenta", dataCore.get("cuenta"),
                "canal", "PORTAL_WEB",
                "movimientosAnuales", dataCore.get("movimientosAnuales")
        );

        return ResponseEntity.ok(respuestaWeb);
    }
    @GetMapping("/eventos")
    public ResponseEntity<List<String>> obtenerEventosKafka() {
        return ResponseEntity.ok(kafkaEventConsumer.obtenerEventos());
    }

}