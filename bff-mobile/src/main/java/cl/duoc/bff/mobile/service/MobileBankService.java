package cl.duoc.bff.mobile.service;

import cl.duoc.bff.mobile.client.CoreBankClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MobileBankService {

    private final CoreBankClient coreClient;

    public MobileBankService(CoreBankClient coreClient) {
        this.coreClient = coreClient;
    }

    public Map<String, Object> obtenerResumenMobile(Long cuentaId) {
        Map<String, Object> data = (Map<String, Object>) coreClient.obtenerCuentaPorId(cuentaId);

        if (data == null || data.isEmpty()) {
            return null;
        }

        Map<?, ?> cuenta = (Map<?, ?>) data.get("cuenta");
        List<?> movimientos = (List<?>) data.get("movimientosAnuales");

        List<?> ultimosMovimientos = List.of();
        if (movimientos != null && !movimientos.isEmpty()) {
            int start = Math.max(0, movimientos.size() - 2);
            ultimosMovimientos = movimientos.subList(start, movimientos.size());
        }

        return Map.of(
                "canal", "APP_MOVIL",
                "titular", cuenta != null ? cuenta.get("nombre") : "N/A",
                "cuentaEnmascarada", "****" + cuentaId,
                "saldoDisponible", cuenta != null ? cuenta.get("saldo_final") : 0,
                "ultimosMovimientos", ultimosMovimientos
        );
    }
}