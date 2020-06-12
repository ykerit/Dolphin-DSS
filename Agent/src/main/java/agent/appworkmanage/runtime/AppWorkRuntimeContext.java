package agent.appworkmanage.runtime;

import agent.appworkmanage.appwork.AppWork;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AppWorkRuntimeContext {
    private final AppWork appWork;
    private final Map<Attribute<?>, Object> executionAttributes;

    public static final class Attribute<T> {
        private final Class<T> valueClass;
        private final String id;

        private Attribute(Class<T> valueClass, String id) {
            this.valueClass = valueClass;
            this.id = id;
        }

        @Override
        public int hashCode() {
            return valueClass.hashCode() + 31 * id.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof Attribute)){
                return false;
            }

            Attribute<?> attribute = (Attribute<?>) obj;

            return valueClass.equals(attribute.valueClass) && id.equals(attribute.id);
        }
        public static <T> Attribute<T> attribute(Class<T> valueClass, String id) {
            return new Attribute<T>(valueClass, id);
        }
    }

    public static final class Builder {
        private final AppWork appWork;
        private Map<Attribute<?>, Object> executionAttributes;

        public Builder(AppWork appWork) {
            this.appWork = appWork;
            executionAttributes = new HashMap<>();
        }

        public <E> Builder setExecutionAttribute(Attribute<E> attribute, E val) {
            this.executionAttributes.put(attribute, attribute.valueClass.cast(val));
            return this;
        }

        public AppWorkRuntimeContext build() {
            return new AppWorkRuntimeContext(this);
        }
    }

    private AppWorkRuntimeContext(Builder builder) {
        this.appWork = builder.appWork;
        this.executionAttributes = builder.executionAttributes;
    }

    public AppWork getAppWork() {
        return appWork;
    }

    public Map<Attribute<?>, Object> getExecutionAttributes() {
        return Collections.unmodifiableMap(this.executionAttributes);
    }

    public <E> E getExecutionAttribute(Attribute<E> attribute) {
        return attribute.valueClass.cast(executionAttributes.get(attribute));
    }
}
