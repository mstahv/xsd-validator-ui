package com.rubn.xsdvalidator.service;

import java.util.function.Predicate;

/**
 * @author rubn
 */
public class ProcessContext {

    private final Predicate<String[]> processStrategy;

    public ProcessContext(Predicate<String[]> processStrategy) {
        this.processStrategy = processStrategy;
    }

    public boolean test(String... commands) {
        return this.processStrategy.test(commands);
    }
}
