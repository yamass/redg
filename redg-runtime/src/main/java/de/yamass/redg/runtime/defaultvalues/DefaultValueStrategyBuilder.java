package de.yamass.redg.runtime.defaultvalues;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import de.yamass.redg.models.ColumnModel;
import de.yamass.redg.runtime.defaultvalues.pluggable.DefaultDefaultValueProvider;
import de.yamass.redg.runtime.defaultvalues.pluggable.PluggableDefaultValueProvider;
import de.yamass.redg.runtime.defaultvalues.pluggable.PluggableDefaultValueStrategy;

public class DefaultValueStrategyBuilder {

    private PluggableDefaultValueStrategy pluggableDefaultValueStrategy = new PluggableDefaultValueStrategy();
    private DefaultValueStrategy fallbackStrategy = new DefaultDefaultValueProvider();

    public Condition when(final Predicate<ColumnModel> predicate) {
        return new PredicateCondition(predicate);
    }

    public Condition whenTableNameMatches(final String regex) {
        return new TableNameRegexCondition(regex);
    }

    public Condition whenColumnNameMatches(final String regex) {
        return new ColumnNameRegexCondition(regex);
    }

    public void setFallbackStrategy(DefaultValueStrategy fallbackStrategy) {
        this.fallbackStrategy = fallbackStrategy;
    }

    private void addProvider(Predicate<ColumnModel> conditionPredicate, BiFunction<ColumnModel, Class, Object> valueProducer) {
        pluggableDefaultValueStrategy.addProvider(new PluggableDefaultValueProvider() {
            @Override
            public boolean willProvide(ColumnModel columnModel) {
                return conditionPredicate.test(columnModel);
            }

            @Override
            public <T> T getDefaultValue(ColumnModel columnModel, Class<T> type) {
                return (T) valueProducer.apply(columnModel, type);
            }
        });
    }

    public DefaultValueStrategy build() {
        pluggableDefaultValueStrategy.addProvider(new PluggableDefaultValueProvider() {
            @Override
            public boolean willProvide(ColumnModel columnModel) {
                return true;
            }

            @Override
            public <T> T getDefaultValue(ColumnModel columnModel, Class<T> type) {
                return fallbackStrategy.getDefaultValue(columnModel, type);
            }
        });
        return pluggableDefaultValueStrategy;
    }

    public abstract class Condition {

        public abstract boolean evaluate(ColumnModel columnModel);

        public void thenUse(Object staticValue) {
            addProvider(this::evaluate, (columnModel, aClass) -> staticValue);
        }

        public void thenCompute(BiFunction<ColumnModel, Class, Object> computationFunction) {
            addProvider(this::evaluate, computationFunction);
        }

        public void thenUseProvider(PluggableDefaultValueProvider provider) {
            addProvider(columnModel -> evaluate(columnModel) && provider.willProvide(columnModel), provider::getDefaultValue);
        }

        public Condition and(final Predicate<ColumnModel> predicate) {
            return new AndCondition(this, new PredicateCondition(predicate));
        }

        public Condition andTableNameMatches(final String regex) {
            return new AndCondition(this, new TableNameRegexCondition(regex));
        }

        public Condition andColumnNameMatches(final String regex) {
            return new AndCondition(this, new ColumnNameRegexCondition(regex));
        }
    }

    class AndCondition extends Condition {

        private final Condition condition1;
        private final Condition condition2;

        public AndCondition(Condition condition1, Condition condition2) {
            this.condition1 = condition1;
            this.condition2 = condition2;
        }

        @Override
        public boolean evaluate(ColumnModel columnModel) {
            return condition1.evaluate(columnModel) && condition2.evaluate(columnModel);
        }
    }

    class PredicateCondition extends Condition {

        private final Predicate<ColumnModel> predicate;

        PredicateCondition(Predicate<ColumnModel> predicate) {
            this.predicate = predicate;
        }

        @Override
        public boolean evaluate(ColumnModel columnModel) {
            return predicate.test(columnModel);
        }
    }

    class TableNameRegexCondition extends Condition {

        private final Pattern pattern;

        TableNameRegexCondition(String regex) {
            pattern = Pattern.compile(regex);
        }

        @Override
        public boolean evaluate(ColumnModel columnModel) {
            return pattern.matcher(columnModel.getDbTableName()).matches();
        }
    }

    class ColumnNameRegexCondition extends Condition {

        private final Pattern pattern;

        ColumnNameRegexCondition(String regex) {
            pattern = Pattern.compile(regex);
        }

        @Override
        public boolean evaluate(ColumnModel columnModel) {
            return pattern.matcher(columnModel.getDbName()).matches();
        }
    }
}
