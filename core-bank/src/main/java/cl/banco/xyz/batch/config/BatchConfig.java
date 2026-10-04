package cl.banco.xyz.batch.config;

import cl.banco.xyz.batch.domain.EstadoCuenta;
import cl.banco.xyz.batch.domain.InteresCuenta;
import cl.banco.xyz.batch.domain.Transaccion;
import cl.banco.xyz.batch.exception.TemporaryBatchException;
import cl.banco.xyz.batch.listener.CustomSkipListener;
import cl.banco.xyz.batch.listener.JobCompletionListener;
import cl.banco.xyz.batch.partition.RangePartitioner;
import cl.banco.xyz.batch.policy.BankSkipPolicy;
import cl.banco.xyz.batch.processor.EstadosCuentaProcessor;
import cl.banco.xyz.batch.processor.InteresesProcessor;
import cl.banco.xyz.batch.processor.TransaccionProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class BatchConfig {


    // job1: transacciones diarias

    @Bean
    @StepScope
    public FlatFileItemReader<Transaccion> transaccionesReader(
            @Value("#{stepExecutionContext['startItem'] ?: 0}") int startItem,
            @Value("#{stepExecutionContext['maxItems'] ?: 500}") int maxItems) {

        FlatFileItemReader<Transaccion> reader = new FlatFileItemReaderBuilder<Transaccion>()
                .name("transaccionesReader")
                .resource(new ClassPathResource("input/transacciones.csv"))
                .linesToSkip(1)
                .delimited()
                .names("id", "fecha", "monto", "tipo")
                .targetType(Transaccion.class)
                .build();

        reader.setCurrentItemCount(startItem);
        reader.setMaxItemCount(startItem + maxItems);
        return reader;
    }

    @Bean
    public JdbcBatchItemWriter<Transaccion> transaccionesWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaccion>()
                .sql("INSERT INTO transacciones_procesadas (id, fecha_normalizada, monto, tipo, estado_alerta) " +
                        "VALUES (:id, :fecha, :monto, :tipo, :estadoAlerta)")
                .dataSource(dataSource)
                .beanMapped()
                .build();
    }

    @Bean
    public Step transaccionWorkerStep(JobRepository jobRepository,
                                      PlatformTransactionManager txManager,
                                      FlatFileItemReader<Transaccion> reader,
                                      TransaccionProcessor processor,
                                      JdbcBatchItemWriter<Transaccion> writer,
                                      BankSkipPolicy skipPolicy,
                                      CustomSkipListener skipListener) {
        return new StepBuilder("transaccionWorkerStep", jobRepository)
                .<Transaccion, Transaccion>chunk(5, txManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipPolicy(skipPolicy)
                .retry(TemporaryBatchException.class)
                .retryLimit(3)
                .listener(skipListener)
                .build();
    }

    @Bean
    public TaskExecutorPartitionHandler transaccionPartitionHandler(Step transaccionWorkerStep,
                                                                    @Qualifier("taskExecutor") TaskExecutor taskExecutor,
                                                                    @Value("${app.grid-size:3}") int gridSize) {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setStep(transaccionWorkerStep);
        handler.setTaskExecutor(taskExecutor);
        handler.setGridSize(gridSize);
        return handler;
    }

    @Bean
    public Step transaccionesPartitionStep(JobRepository jobRepository,
                                           TaskExecutorPartitionHandler transaccionPartitionHandler,
                                           @Value("${app.transacciones-total:100}") int total) {
        return new StepBuilder("transaccionesPartitionStep", jobRepository)
                .partitioner("transaccionWorkerStep", new RangePartitioner(total))
                .partitionHandler(transaccionPartitionHandler)
                .build();
    }

    @Bean
    public Job reporteTransaccionesJob(JobRepository jobRepository,
                                       Step transaccionesPartitionStep,
                                       JobCompletionListener completionListener) {
        return new JobBuilder("reporteTransaccionesJob", jobRepository)
                .listener(completionListener)
                .start(transaccionesPartitionStep)
                .build();
    }

    // Job2: interes mensual

    @Bean
    @StepScope
    public FlatFileItemReader<InteresCuenta> interesesReader(
            @Value("#{stepExecutionContext['startItem'] ?: 0}") int startItem,
            @Value("#{stepExecutionContext['maxItems'] ?: 500}") int maxItems) {

        FlatFileItemReader<InteresCuenta> reader = new FlatFileItemReaderBuilder<InteresCuenta>()
                .name("interesesReader")
                .resource(new ClassPathResource("input/intereses.csv"))
                .linesToSkip(1)
                .delimited()
                .names("cuentaId", "nombre", "saldo", "edad", "tipo")
                .targetType(InteresCuenta.class)
                .build();

        reader.setCurrentItemCount(startItem);
        reader.setMaxItemCount(startItem + maxItems);
        return reader;
    }

    @Bean
    public JdbcBatchItemWriter<InteresCuenta> interesesWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<InteresCuenta>()
                .sql("INSERT INTO intereses_cuentas (cuenta_id, nombre, saldo_original, edad, tipo, tasa_interes, saldo_final) " +
                        "VALUES (:cuentaId, :nombre, :saldo, :edad, :tipo, :tasaInteres, :saldoFinal) " +
                        "ON CONFLICT (cuenta_id) DO UPDATE SET " +
                        "nombre = EXCLUDED.nombre, " +
                        "saldo_original = EXCLUDED.saldo_original, " +
                        "edad = EXCLUDED.edad, " +
                        "tipo = EXCLUDED.tipo, " +
                        "tasa_interes = EXCLUDED.tasa_interes, " +
                        "saldo_final = EXCLUDED.saldo_final")
                .dataSource(dataSource)
                .beanMapped()
                .build();
    }

    @Bean
    public Step interesesWorkerStep(JobRepository jobRepository,
                                    PlatformTransactionManager txManager,
                                    FlatFileItemReader<InteresCuenta> reader,
                                    InteresesProcessor processor,
                                    JdbcBatchItemWriter<InteresCuenta> writer,
                                    BankSkipPolicy skipPolicy,
                                    CustomSkipListener skipListener) {
        return new StepBuilder("interesesWorkerStep", jobRepository)
                .<InteresCuenta, InteresCuenta>chunk(5, txManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipPolicy(skipPolicy)
                .retry(TemporaryBatchException.class)
                .retryLimit(3)
                .listener(skipListener)
                .build();
    }

    @Bean
    public TaskExecutorPartitionHandler interesesPartitionHandler(Step interesesWorkerStep,
                                                                  @Qualifier("taskExecutor") TaskExecutor taskExecutor,
                                                                  @Value("${app.grid-size:3}") int gridSize) {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setStep(interesesWorkerStep);
        handler.setTaskExecutor(taskExecutor);
        handler.setGridSize(gridSize);
        return handler;
    }

    @Bean
    public Step interesesPartitionStep(JobRepository jobRepository,
                                       TaskExecutorPartitionHandler interesesPartitionHandler,
                                       @Value("${app.intereses-total:100}") int total) {
        return new StepBuilder("interesesPartitionStep", jobRepository)
                .partitioner("interesesWorkerStep", new RangePartitioner(total))
                .partitionHandler(interesesPartitionHandler)
                .build();
    }

    @Bean
    public Job calculoInteresesJob(JobRepository jobRepository,
                                   Step interesesPartitionStep,
                                   JobCompletionListener completionListener) {
        return new JobBuilder("calculoInteresesJob", jobRepository)
                .listener(completionListener)
                .start(interesesPartitionStep)
                .build();
    }


    // Job3: generacion estado cuenta


    @Bean
    @StepScope
    public FlatFileItemReader<EstadoCuenta> estadosCuentaReader(
            @Value("#{stepExecutionContext['startItem'] ?: 0}") int startItem,
            @Value("#{stepExecutionContext['maxItems'] ?: 500}") int maxItems) {

        FlatFileItemReader<EstadoCuenta> reader = new FlatFileItemReaderBuilder<EstadoCuenta>()
                .name("estadosCuentaReader")
                .resource(new ClassPathResource("input/cuentas_anuales.csv"))
                .linesToSkip(1)
                .delimited()
                .names("cuentaId", "fecha", "transaccion", "monto", "descripcion")
                .targetType(EstadoCuenta.class)
                .build();

        reader.setCurrentItemCount(startItem);
        reader.setMaxItemCount(startItem + maxItems);
        return reader;
    }

    @Bean
    public JdbcBatchItemWriter<EstadoCuenta> estadosCuentaWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<EstadoCuenta>()
                .sql("INSERT INTO estados_cuenta_anuales (cuenta_id, fecha_movimiento, transaccion, monto, descripcion) " +
                        "VALUES (:cuentaId, :fecha, :transaccion, :monto, :descripcion)")
                .dataSource(dataSource)
                .beanMapped()
                .build();
    }

    @Bean
    public Step estadosCuentaWorkerStep(JobRepository jobRepository,
                                        PlatformTransactionManager txManager,
                                        FlatFileItemReader<EstadoCuenta> reader,
                                        EstadosCuentaProcessor processor,
                                        JdbcBatchItemWriter<EstadoCuenta> writer,
                                        BankSkipPolicy skipPolicy,
                                        CustomSkipListener skipListener) {
        return new StepBuilder("estadosCuentaWorkerStep", jobRepository)
                .<EstadoCuenta, EstadoCuenta>chunk(5, txManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipPolicy(skipPolicy)
                .retry(TemporaryBatchException.class)
                .retryLimit(3)
                .listener(skipListener)
                .build();
    }

    @Bean
    public TaskExecutorPartitionHandler estadosCuentaPartitionHandler(Step estadosCuentaWorkerStep,
                                                                      @Qualifier("taskExecutor") TaskExecutor taskExecutor,
                                                                      @Value("${app.grid-size:3}") int gridSize) {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setStep(estadosCuentaWorkerStep);
        handler.setTaskExecutor(taskExecutor);
        handler.setGridSize(gridSize);
        return handler;
    }

    @Bean
    public Step estadosCuentaPartitionStep(JobRepository jobRepository,
                                           TaskExecutorPartitionHandler estadosCuentaPartitionHandler,
                                           @Value("${app.estados-total:100}") int total) {
        return new StepBuilder("estadosCuentaPartitionStep", jobRepository)
                .partitioner("estadosCuentaWorkerStep", new RangePartitioner(total))
                .partitionHandler(estadosCuentaPartitionHandler)
                .build();
    }

    @Bean
    public Job estadosCuentaJob(JobRepository jobRepository,
                                Step estadosCuentaPartitionStep,
                                JobCompletionListener completionListener) {
        return new JobBuilder("estadosCuentaJob", jobRepository)
                .listener(completionListener)
                .start(estadosCuentaPartitionStep)
                .build();
    }
}