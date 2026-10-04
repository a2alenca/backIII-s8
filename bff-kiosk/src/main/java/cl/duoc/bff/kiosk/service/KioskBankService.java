package cl.duoc.bff.kiosk.service;

import cl.duoc.bff.kiosk.client.CoreBankClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class KioskBankService {

    private final CoreBankClient coreClient;

    public KioskBankService(CoreBankClient coreClient) {
        this.coreClient = coreClient;
    }

    public Map<String, Object> obtenerResumenKiosk(Long cuentaId) {
        Map<String, Object> data = (Map<String, Object>) coreClient.obtenerCuentaPorId(cuentaId);

        if (data == null || data.isEmpty()) {
            return null;
        }

        Map<?, ?> cuenta = (Map<?, ?>) data.get("cuenta");

        return Map.of(
                "canal", "CAJERO_AUTOMATICO",
                "cuentaId", cuentaId,
                "titular", cuenta != null ? cuenta.get("nombre") : "N/A",
                "saldoActual", cuenta != null ? cuenta.get("saldo_final") : 0
        );
    }

    public Object realizarRetiro(Long cuentaId, double monto) {
        Map<String, Object> body = Map.of("amount", monto);
        return coreClient.procesarRetiro(cuentaId, body);
    }
}