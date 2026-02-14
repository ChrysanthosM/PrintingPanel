package org.masouras.app.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@Slf4j
public class OpenTelemetryConfig {
    @Value("${otel.exporter.enabled:true}") private boolean exporterEnabled;
    @Value("${otel.exporter.type:grpc}") private String exporterType;
    @Value("${otel.exporter.grpc.endpoint:localhost:4317}") private String grpcEndpoint;
    @Value("${otel.exporter.http.endpoint:http://localhost:4318/v1/traces}") private String httpEndpoint;

    @Bean
    public OpenTelemetry openTelemetry(Environment environment) {
        String appName = environment.getProperty("spring.application.name", "default-app");

        AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(SERVICE_NAME, appName)));

        SdkTracerProvider tracerProvider = createTracerProvider(resource);

        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(ContextPropagators.create(TextMapPropagator.composite(
                        TextMapPropagator.noop(), // or W3CTraceContextPropagator for propagation
                        TextMapPropagator.noop()
                )))
                .buildAndRegisterGlobal();
    }

    @Bean
    public Tracer openTelemetryTracer(OpenTelemetry openTelemetry, Environment environment) {
        String appName = environment.getProperty("spring.application.name", "default-app");
        return openTelemetry.getTracer(appName + "-tracer");
    }

    private SdkTracerProvider createTracerProvider(Resource resource) {
        SpanExporter spanExporter;

        if (!exporterEnabled) {
            log.info("⚠️ Exporter disabled via configuration, spans will not be exported");
            return SdkTracerProvider.builder()
                    .setResource(resource)
                    .build();
        }

        if ("grpc".equalsIgnoreCase(exporterType)) {
            log.info("✅ Using gRPC exporter at {}", grpcEndpoint);
            spanExporter = OtlpGrpcSpanExporter.builder()
                    .setEndpoint(grpcEndpoint)
                    .build();
        } else {
            log.info("✅ Using HTTP exporter at {}", httpEndpoint);
            spanExporter = OtlpHttpSpanExporter.builder()
                    .setEndpoint(httpEndpoint)
                    .build();
        }

        return SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
                .build();
    }

}
