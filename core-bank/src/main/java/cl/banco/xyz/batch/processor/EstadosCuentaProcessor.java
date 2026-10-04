package cl.banco.xyz.batch.processor;

import cl.banco.xyz.batch.domain.EstadoCuenta;
import cl.banco.xyz.batch.exception.InvalidDataException;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

@Component
public class EstadosCuentaProcessor implements ItemProcessor<EstadoCuenta, EstadoCuenta> {

    private final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            .appendOptional(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
            .toFormatter(Locale.ENGLISH);

    @Override
    public EstadoCuenta process(EstadoCuenta item) throws Exception {
        if (item.getCuentaId() == null) {
            throw new InvalidDataException("Cuenta ID nulo");
        }
        if (item.getMonto() == null || item.getMonto() <= 0) {
            throw new InvalidDataException("Monto nulo o no positivo en cuenta: " + item.getCuentaId());
        }
        if (item.getTransaccion() == null || item.getTransaccion().trim().isEmpty()) {
            throw new InvalidDataException("Tipo de transaccion vacio en cuenta: " + item.getCuentaId());
        }

        try {
            LocalDate parsed = LocalDate.parse(item.getFecha(), formatter);
            item.setFecha(parsed.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } catch (Exception e) {
            throw new InvalidDataException("Fecha invalida (" + item.getFecha() + ") en cuenta: " + item.getCuentaId());
        }

        if (item.getDescripcion() == null || item.getDescripcion().trim().isEmpty()) {
            item.setDescripcion("Movimiento regular consolidado");
        }

        item.setTransaccion(item.getTransaccion().trim().toLowerCase());
        return item;
    }
}