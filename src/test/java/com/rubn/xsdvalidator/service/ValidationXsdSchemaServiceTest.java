package com.rubn.xsdvalidator.service;

import com.rubn.xsdvalidator.providers.FailureValidationXsdSchemaProvider;
import com.rubn.xsdvalidator.providers.SuccessValidationXsdSchemaProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
class ValidationXsdSchemaServiceTest {

    @InjectMocks
    private ValidationXsdSchemaService validationXsdSchemaService;

    @ParameterizedTest
    @ArgumentsSource(SuccessValidationXsdSchemaProvider.class)
    @DisplayName("Valida xml agains xsd schema")
    void case1(byte[] inputXml, byte[] inputXsd, final Map<String, byte[]> mapPrefixFileNameAndContent) {

        Flux<String> listString = validationXsdSchemaService.validateXmlInputWithXsdSchema(inputXml, inputXsd, mapPrefixFileNameAndContent)
                .doOnNext(System.out::println);

        StepVerifier.create(listString)
                .verifyComplete();

    }

    @ParameterizedTest(name = "name: {0}")
    @ArgumentsSource(FailureValidationXsdSchemaProvider.class)
    @DisplayName("Failed Valida xml agains xsd schema")
    void case2(byte[] inputXml, byte[] inputXsd, final Map<String, byte[]> mapPrefixFileNameAndContent) {

        Flux<String> listString = validationXsdSchemaService.validateXmlInputWithXsdSchema(inputXml, inputXsd, mapPrefixFileNameAndContent)
                .doOnNext(System.out::println);

        StepVerifier.create(listString)
                .expectErrorMatches(error -> error.getMessage().contains("The prefix \"prod\" for element \"prod:Sku\" is not bound."))
                .verify();

    }

}