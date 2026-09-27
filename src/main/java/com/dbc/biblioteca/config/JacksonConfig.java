package com.dbc.biblioteca.config;

import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /*Por padrão o Jackson aceita: true, 1, "1", 14, tudo sendo true. Durante os testes no postman inverti colocando o valor 1993 em disponível par testar mensagens de erro
    e ele aceito, por isso adicionei a classe JacksonConfig para bloquear esse comportamento.
     */

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer strictBooleanCoercion() {
        return builder -> builder.postConfigurer(mapper ->
                mapper.coercionConfigFor(LogicalType.Boolean)
                        .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.String, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.EmptyString, CoercionAction.Fail)
        );
    }
}