package cl.banco.xyz.batch.processor;

import cl.banco.xyz.batch.domain.Transaccion;
import cl.banco.xyz.batch.exception.InvalidDataException;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

@Component
public class TransaccionProcessor implements ItemProcessor<Transaccion, Transaccion> {

    private final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            .appendOptional(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
            .toFormatter(Locale.ENGLISH);

    @Override
    public Transaccion process(Transaccion item) throws Exception {
        if (item.getTipo() == null || (!item.getTipo().equalsIgnoreCase("credito") && !item.getTipo().equalsIgnoreCase("debito"))) {
            throw new InvalidDataException("Tipo no valido: " + item.getTipo() + " en ID: " + item.getId());
        }

        if (item.getMonto() == null || item.getMonto() <= 0) {
            throw new InvalidDataException("Monto nulo en ID: " + item.getId());
        }

        try {
            LocalDate parsed = LocalDate.parse(item.getFecha(), formatter);
            item.setFecha(parsed.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } catch (Exception e) {
            throw new InvalidDataException("Fecha  invalida (" + item.getFecha() + ") en ID: " + item.getId());
        }

        if (item.getMonto() >= 2500) {
            item.setEstadoAlerta("AUDITORIA_REQUERIDA");
        } else {
            item.setEstadoAlerta("NORMAL");
        }

        item.setTipo(item.getTipo().toLowerCase());
        return item;
    }
}