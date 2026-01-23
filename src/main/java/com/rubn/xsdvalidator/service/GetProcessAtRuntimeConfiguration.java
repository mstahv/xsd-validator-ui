package com.rubn.xsdvalidator.service;


import com.rubn.xsdvalidator.enums.GetOperatingSystem;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Here the logic applies depending on the operating system.
 */
@Log4j2
@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration
public class GetProcessAtRuntimeConfiguration {

    public static final String NOT_IMPLEMENTED_YET = "Not implemented yet";

    private static final String[] SEARCH_JAVAW_PROCESS_NAME_USING_WMIC = {"cmd", "/c", "wmic", "Path", "win32_process", "Where", "\"CommandLine Like '%xsd-validator-ui.jar%'\""};
    private static final String[] SEARCH_JAVAW_PROCESS_NAME_USING_PS_ELF = {"/bin/sh", "-c", "ps -elf | grep -v grep | grep 'test.jar'"};

    @Bean
    public CommandLineRunner runner() {
        return (String[] args) -> {
            final GetOperatingSystem osInfo = GetOperatingSystem.getOsInfo();
            //TODO cambiar por switch
            if (osInfo == GetOperatingSystem.WINDOWS) {
                final ProcessContext processContext = new ProcessContext(new WindowsProcessStrategyImpl());
                processContext.test(SEARCH_JAVAW_PROCESS_NAME_USING_WMIC);
            } else if (osInfo == GetOperatingSystem.LINUX) {
                final ProcessContext processContext = new ProcessContext(new LinuxProcessStrategyImpl());
                processContext.test(SEARCH_JAVAW_PROCESS_NAME_USING_PS_ELF);
            } else if (osInfo == GetOperatingSystem.MAC) {
                info();
            } else if (osInfo == GetOperatingSystem.FREEBSD) {
                info();
            } else {
                info();
            }
        };
    }

    private static void info() {
        log.info(NOT_IMPLEMENTED_YET);
    }

}
