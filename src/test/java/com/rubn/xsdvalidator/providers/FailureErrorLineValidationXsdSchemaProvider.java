package com.rubn.xsdvalidator.providers;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

public class FailureErrorLineValidationXsdSchemaProvider implements ArgumentsProvider {

    private static final Path XML_ORDER_INSTANCE_MOCK_PATH = Path.of("src/test/resources/documents/validation-failure/order-instance.without-namespace.xml");
    private static final Path XSD_MAIN_ORDER_MOCK_PATH = Path.of("src/test/resources/documents/validation-failure/main-order.xsd");
    private static final Path XSD_DATATYPES_MOCK_PATH = Path.of("src/test/resources/documents/validation-failure/datatypes.xsd");
    private static final Path XSD_PRODUCT_MOCK_PATH = Path.of("src/test/resources/documents/validation-failure/product.xsd");

    @Override
    public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) throws Exception {

        InputStream inputXml = new BufferedInputStream(Files.newInputStream(XML_ORDER_INSTANCE_MOCK_PATH));
        InputStream inputMainOrderXsd = new BufferedInputStream(Files.newInputStream(XSD_MAIN_ORDER_MOCK_PATH));
        InputStream inputDataTypesXsd = new BufferedInputStream(Files.newInputStream(XSD_DATATYPES_MOCK_PATH));
        InputStream inputProducXsd = new BufferedInputStream(Files.newInputStream(XSD_PRODUCT_MOCK_PATH));

        byte[] array1 = inputXml.readAllBytes();
        byte[] array2 = inputMainOrderXsd.readAllBytes();
        byte[] array3 = inputDataTypesXsd.readAllBytes();
        byte[] array4 = inputProducXsd.readAllBytes();

        final Map<String, byte[]> mapPrefixFileNameAndContent = Map.of(
                XML_ORDER_INSTANCE_MOCK_PATH.getFileName().toString(), array1,
                XSD_MAIN_ORDER_MOCK_PATH.getFileName().toString(), array2,
                XSD_DATATYPES_MOCK_PATH.getFileName().toString(), array3,
                XSD_PRODUCT_MOCK_PATH.getFileName().toString(), array4);

        return Stream.of(Arguments.of("without-name-space", array1, array2, mapPrefixFileNameAndContent));
    }
}
