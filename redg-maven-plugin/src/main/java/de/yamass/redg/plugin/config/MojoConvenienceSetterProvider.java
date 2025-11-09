package de.yamass.redg.plugin.config;

import de.yamass.redg.generator.extractor.conveniencesetterprovider.ConvenienceSetterProvider;
import de.yamass.redg.models.ConvenienceSetterModel;
import de.yamass.redg.schema.model.Column;
import de.yamass.redg.schema.model.Table;

import java.util.List;
import java.util.stream.Collectors;

public class MojoConvenienceSetterProvider implements ConvenienceSetterProvider {

    private final List<ConvenienceSetterConfig> convenienceSetterConfig;

    public MojoConvenienceSetterProvider(List<ConvenienceSetterConfig> convenienceSetterConfig) {
        this.convenienceSetterConfig = convenienceSetterConfig;
    }

    @Override
    public List<ConvenienceSetterModel> getConvenienceSetters(Column column, Table table, String javaDataTypeName) {
        return this.convenienceSetterConfig.stream()
                .filter(csc -> csc.getOriginalType().equals(javaDataTypeName))
                .map(csc -> new ConvenienceSetterModel(csc.getConvenienceType(), csc.getConverterMethod()))
                .collect(Collectors.toList());
    }
}