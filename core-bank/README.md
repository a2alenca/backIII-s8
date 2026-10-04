Actividad Sumativa 1 -  Banco XYZ

Este trabajo procesa 3 archivos CSV (transacciones, intereses y cuentas anuales)

 y guarda los datos limpios en la base de datos usando Spring Batch

1. Que hace el sistema
- Transacciones: Lee el CSV, valida los tipos de operacion y formatea las fechas
- Intereses: Calcula los saldos finales aplicando las tasas de interes
- Estados de cuenta: Procesa los movimientos descartando montos en cero, nulos o negativos
2. Manejo de fallos
- Si ocurre una falla temporal de conexion (TemporaryBatchException), se reintenta 3 veces antes de fallar
- Si viene un registro con datos corruptos o invalidos (InvalidDataException), el sistema lo salta usando BankSkipPolicy, avisa en consola con CustomSkipListener y sigue con el resto del archivo sin caerse

3. Rendimiento y paralelismo
Para que no procese fila por fila de forma lenta, se uso particionado por rangos (RangePartitioner) y 4 hilos de ejecucion con chunks de a 5 registros

Tiempos reales de prueba:
- reporteTransaccionesJob: 4 hilos, 34 filas saltadas, 125 ms (COMPLETED)
- calculoInteresesJob: 4 hilos, 38 filas saltadas, 38 ms (COMPLETED)
- estadosCuentaJob: 4 hilos, 22 filas saltadas, 38 ms (COMPLETED)

El procesamiento paralelo permitio terminar todo el lote en solo 200 ms y soportar los datos malos sin detener los jobs.

4. Como correrlo
En la terminal ejecutar
mvnw.cmd clean spring-boot:run