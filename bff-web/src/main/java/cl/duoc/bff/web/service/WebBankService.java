package cl.duoc.bff.web.service;

import cl.duoc.bff.web.client.CoreBankClient;
import org.springframework.stereotype.Service;

@Service
public class WebBankService {

    private final CoreBankClient coreClient;

    public WebBankService(CoreBankClient coreClient) {
        this.coreClient = coreClient;
    }

    public Object[] obtenerDatosCompletosWeb() {
        // Retorna la información obtenida del core del banco
        return coreClient.obtenerCuentasWeb();
    }
}