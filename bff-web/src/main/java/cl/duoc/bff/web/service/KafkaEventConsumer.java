package cl.duoc.bff.web.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class KafkaEventConsumer {

    private final List<String> eventos = Collections.synchronizedList(new ArrayList<>());

    @KafkaListener(topics = "bank-withdrawals", groupId = "bff-web")
    public void consumir(String mensaje) {
        synchronized (eventos) {
            eventos.add(mensaje);
            if (eventos.size() > 20) {
                eventos.remove(0);
            }
        }
    }

    public List<String> obtenerEventos() {
        synchronized (eventos) {
            return List.copyOf(eventos);
        }
    }
}
