CREATE TABLE IF NOT EXISTS transacciones_procesadas (
                                                        id BIGINT PRIMARY KEY,
                                                        fecha_normalizada VARCHAR(10) NOT NULL,
    monto NUMERIC(12,2) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    estado_alerta VARCHAR(30) NOT NULL
    );

CREATE TABLE IF NOT EXISTS intereses_cuentas (
                                                 cuenta_id BIGINT PRIMARY KEY,
                                                 nombre VARCHAR(100) NOT NULL,
    saldo_original NUMERIC(14,2) NOT NULL,
    edad INTEGER NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    tasa_interes NUMERIC(5,2) NOT NULL,
    saldo_final NUMERIC(14,2) NOT NULL
    );

CREATE TABLE IF NOT EXISTS estados_cuenta_anuales (
                                                      id BIGSERIAL PRIMARY KEY,
                                                      cuenta_id BIGINT NOT NULL,
                                                      fecha_movimiento VARCHAR(10) NOT NULL,
    transaccion VARCHAR(30) NOT NULL,
    monto NUMERIC(12,2) NOT NULL,
    descripcion VARCHAR(150) NOT NULL
    );