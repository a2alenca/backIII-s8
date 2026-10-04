package cl.banco.xyz.batch.processor;

import cl.banco.xyz.batch.domain.InteresCuenta;
import cl.banco.xyz.batch.exception.InvalidDataException;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class InteresesProcessor implements ItemProcessor<InteresCuenta, InteresCuenta> {

    @Override
    public InteresCuenta process(InteresCuenta item) throws Exception {
        if (item.getSaldo() == null || item.getSaldo() < 0) {
            throw new InvalidDataException("Saldo invalido o nulo: " + item.getCuentaId());
        }
        if (item.getEdad() == null || item.getEdad() <= 0 || item.getEdad() > 120) {
            throw new InvalidDataException("Edad fuera rango o nula: " + item.getCuentaId());
        }
        if (item.getTipo() == null || item.getTipo().equals("-1") || item.getTipo().equalsIgnoreCase("Unknown")) {
            throw new InvalidDataException("Tipo invalido en cuenta: " + item.getCuentaId());
        }

        double tasa = 0.0;
        String tipoNorm = item.getTipo().trim().toLowerCase();

        switch (tipoNorm) {
            case "ahorro":
                tasa = (item.getEdad() >= 60) ? 0.05 : 0.03;
                break;
            case "prestamo":
                tasa = 0.08;
                break;
            case "hipoteca":
                tasa = 0.045;
                break;
            default:
                throw new InvalidDataException("Tipo de cuenta no soportado (" + item.getTipo() + ") en cuenta: " + item.getCuentaId());
        }

        item.setTipo(tipoNorm);
        item.setTasaInteres(tasa * 100);
        item.setSaldoFinal(Math.round((item.getSaldo() + (item.getSaldo() * tasa)) * 100.0) / 100.0);

        return item;
    }
}