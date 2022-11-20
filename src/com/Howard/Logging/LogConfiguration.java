package com.Howard.Logging;

import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.*;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.Level;

public class LogConfiguration {
	public static void Configure() {
		ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();

		builder.setStatusLevel(Level.ERROR);
		builder.setConfigurationName("RollingBuilder");
		// create a console appender
		AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE").addAttribute("target",
				ConsoleAppender.Target.SYSTEM_OUT);
		appenderBuilder
				.add(builder.newLayout("PatternLayout").addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable"));
		builder.add(appenderBuilder);
		// create a rolling file appender
		LayoutComponentBuilder layoutBuilder = builder.newLayout("PatternLayout").addAttribute("pattern",
				"%d [%t] %-5level: %msg%n");
		ComponentBuilder triggeringPolicy = builder.newComponent("Policies")
				.addComponent(builder.newComponent("CronTriggeringPolicy").addAttribute("schedule", "0 0 0 * * ?"))
				.addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", "100M"));
		appenderBuilder = builder.newAppender("rolling", "RollingFile").addAttribute("fileName", "target/rolling.log")
				.addAttribute("filePattern", "target/archive/rolling-%d{MM-dd-yy}.log.gz").add(layoutBuilder)
				.addComponent(triggeringPolicy);
		builder.add(appenderBuilder);

		// create the new logger
		builder.add(builder.newLogger("TestLogger", Level.INFO).add(builder.newAppenderRef("rolling"))
				.addAttribute("additivity", false));

		builder.add(builder.newRootLogger(Level.INFO).add(builder.newAppenderRef("rolling")));
		LoggerContext ctx = Configurator.initialize(builder.build());
	}
}
