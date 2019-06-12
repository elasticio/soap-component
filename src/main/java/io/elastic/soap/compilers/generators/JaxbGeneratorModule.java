package io.elastic.soap.compilers.generators;

import com.google.inject.AbstractModule;
import io.elastic.soap.compilers.generators.impl.Axis2GeneratorImpl;

/**
 * Google Guice configuration. Here you should specify which of the available JAXB generator
 * implementations you use. {@link Axis2GeneratorImpl} is preferred in most cases
 */
public class JaxbGeneratorModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IJaxbGenerator.class).to(Axis2GeneratorImpl.class);
    }
}
