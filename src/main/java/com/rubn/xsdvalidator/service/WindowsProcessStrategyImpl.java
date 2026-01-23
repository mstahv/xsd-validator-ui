package com.rubn.xsdvalidator.service;

import com.rubn.xsdvalidator.util.XsdValidatorConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Predicate;

/**
 * @author rubn
 */
public class WindowsProcessStrategyImpl implements Predicate<String[]> {

    @Override
    public boolean test(String... input) {
        return this.detectTheJavaProcessByName(line ->
                line.startsWith("javaw.exe") && line.contains(XsdValidatorConstants.XSD_VALIDATOR_UI_JAR), input);
    }

    private boolean detectTheJavaProcessByName(Predicate<String> predicate, String... command) {

        try (final InputStream inputStream = Runtime.getRuntime().exec(command).getInputStream();
             final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {

            boolean isTestJarExists = bufferedReader.lines()
                    .filter(predicate)
                    .peek(System.out::println)
                    .count() > 1;

            return !isTestJarExists
                    ? true //deploy
                    : false; // delete process, only ?

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
