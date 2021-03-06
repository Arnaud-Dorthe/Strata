/*
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.calc.marketdata;

import java.util.Map;
import java.util.NoSuchElementException;

import org.joda.beans.Bean;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.gen.BeanDefinition;
import org.joda.beans.gen.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.collect.Messages;
import com.opengamma.strata.data.MarketDataId;
import com.opengamma.strata.data.scenario.MarketDataBox;
import com.opengamma.strata.data.scenario.ScenarioPerturbation;

/**
 * Contains a market data perturbation and a filter that decides what market data it applies to.
 * <p>
 * The mapping links the perturbation to be applied to the filter that decides whether it applies.
 * The generic types of the filter can be for a subtype of the type that the perturbation applies to.
 *
 * @param <T>  the type of the market data handled by the mapping
 */
@BeanDefinition
public final class PerturbationMapping<T> implements ImmutableBean {

  /** The type of market data handled by this mapping. */
  @PropertyDefinition(validate = "notNull")
  private final Class<T> marketDataType;

  /** The filter that decides whether the perturbation should be applied to a piece of market data. */
  @PropertyDefinition(validate = "notNull")
  private final MarketDataFilter<? extends T, ?> filter;

  /** Perturbation that should be applied to market data as part of a scenario. */
  @PropertyDefinition(validate = "notNull")
  private final ScenarioPerturbation<T> perturbation;

  //-------------------------------------------------------------------------
  /**
   * Returns a mapping containing a single perturbation.
   * <p>
   * This uses the type from {@link ScenarioPerturbation} as the type of the mapping.
   *
   * @param <T>  the type of the market data handled by the mapping
   * @param filter  the filter used to choose the market data
   * @param perturbation  the perturbation applied to any market data matching the filter
   * @return a mapping containing a single perturbation
   */
  public static <T> PerturbationMapping<T> of(MarketDataFilter<? extends T, ?> filter, ScenarioPerturbation<T> perturbation) {
    return new PerturbationMapping<T>(perturbation.getMarketDataType(), filter, perturbation);
  }

  /**
   * Returns a mapping containing a single perturbation.
   *
   * @param <T>  the type of the market data handled by the mapping
   * @param marketDataType the type of market data handled by the mapping
   * @param filter  the filter used to choose the market data
   * @param perturbation  the perturbation applied to any market data matching the filter
   * @return a mapping containing a single perturbation
   * @deprecated Use the two-argument version, now that {@link ScenarioPerturbation} knows its type
   */
  @Deprecated
  public static <T> PerturbationMapping<T> of(
      Class<T> marketDataType,
      MarketDataFilter<? extends T, ?> filter,
      ScenarioPerturbation<T> perturbation) {

    return new PerturbationMapping<>(marketDataType, filter, perturbation);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns true if the filter matches the market data ID and value.
   *
   * @param marketDataId  the ID of a piece of market data
   * @param marketData  the market data value
   * @param refData  the reference data
   * @return true if the filter matches
   */
  @SuppressWarnings("unchecked")
  public boolean matches(MarketDataId<?> marketDataId, MarketDataBox<?> marketData, ReferenceData refData) {
    // The raw type is necessary to keep the compiler happy, the call is definitely safe because the
    // type of the ID is checked against the ID type handled by the filter
    @SuppressWarnings("rawtypes")
    MarketDataFilter rawFilter = filter;

    return marketDataType.isAssignableFrom(marketData.getMarketDataType()) &&
        filter.getMarketDataIdType().isInstance(marketDataId) &&
        rawFilter.matches(marketDataId, marketData, refData);
  }

  /**
   * Applies the perturbations in this mapping to an item of market data and returns the results.
   * <p>
   * This method should only be called after calling {@code #matches} and receiving a result of {@code true}.
   *
   * @param marketData  the market data value
   * @param refData  the reference data
   * @return a list of market data values derived from the input value by applying the perturbations
   */
  @SuppressWarnings("unchecked")
  public MarketDataBox<T> applyPerturbation(MarketDataBox<T> marketData, ReferenceData refData) {
    if (!marketDataType.isAssignableFrom(marketData.getMarketDataType())) {
      throw new IllegalArgumentException(
          Messages.format(
              "Market data {} is not an instance of the required type {}",
              marketData,
              marketDataType.getName()));
    }
    return perturbation.applyTo(marketData, refData);
  }

  /**
   * Returns the number of scenarios for which this mapping can generate data.
   *
   * @return the number of scenarios for which this mapping can generate data
   */
  public int getScenarioCount() {
    return perturbation.getScenarioCount();
  }

  //------------------------- AUTOGENERATED START -------------------------
  /**
   * The meta-bean for {@code PerturbationMapping}.
   * @return the meta-bean, not null
   */
  @SuppressWarnings("rawtypes")
  public static PerturbationMapping.Meta meta() {
    return PerturbationMapping.Meta.INSTANCE;
  }

  /**
   * The meta-bean for {@code PerturbationMapping}.
   * @param <R>  the bean's generic type
   * @param cls  the bean's generic type
   * @return the meta-bean, not null
   */
  @SuppressWarnings("unchecked")
  public static <R> PerturbationMapping.Meta<R> metaPerturbationMapping(Class<R> cls) {
    return PerturbationMapping.Meta.INSTANCE;
  }

  static {
    MetaBean.register(PerturbationMapping.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @param <T>  the type
   * @return the builder, not null
   */
  public static <T> PerturbationMapping.Builder<T> builder() {
    return new PerturbationMapping.Builder<>();
  }

  private PerturbationMapping(
      Class<T> marketDataType,
      MarketDataFilter<? extends T, ?> filter,
      ScenarioPerturbation<T> perturbation) {
    JodaBeanUtils.notNull(marketDataType, "marketDataType");
    JodaBeanUtils.notNull(filter, "filter");
    JodaBeanUtils.notNull(perturbation, "perturbation");
    this.marketDataType = marketDataType;
    this.filter = filter;
    this.perturbation = perturbation;
  }

  @SuppressWarnings("unchecked")
  @Override
  public PerturbationMapping.Meta<T> metaBean() {
    return PerturbationMapping.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the type of market data handled by this mapping.
   * @return the value of the property, not null
   */
  public Class<T> getMarketDataType() {
    return marketDataType;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the filter that decides whether the perturbation should be applied to a piece of market data.
   * @return the value of the property, not null
   */
  public MarketDataFilter<? extends T, ?> getFilter() {
    return filter;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets perturbation that should be applied to market data as part of a scenario.
   * @return the value of the property, not null
   */
  public ScenarioPerturbation<T> getPerturbation() {
    return perturbation;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder<T> toBuilder() {
    return new Builder<>(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      PerturbationMapping<?> other = (PerturbationMapping<?>) obj;
      return JodaBeanUtils.equal(marketDataType, other.marketDataType) &&
          JodaBeanUtils.equal(filter, other.filter) &&
          JodaBeanUtils.equal(perturbation, other.perturbation);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(marketDataType);
    hash = hash * 31 + JodaBeanUtils.hashCode(filter);
    hash = hash * 31 + JodaBeanUtils.hashCode(perturbation);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("PerturbationMapping{");
    buf.append("marketDataType").append('=').append(marketDataType).append(',').append(' ');
    buf.append("filter").append('=').append(filter).append(',').append(' ');
    buf.append("perturbation").append('=').append(JodaBeanUtils.toString(perturbation));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code PerturbationMapping}.
   * @param <T>  the type
   */
  public static final class Meta<T> extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    @SuppressWarnings("rawtypes")
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code marketDataType} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Class<T>> marketDataType = DirectMetaProperty.ofImmutable(
        this, "marketDataType", PerturbationMapping.class, (Class) Class.class);
    /**
     * The meta-property for the {@code filter} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<MarketDataFilter<? extends T, ?>> filter = DirectMetaProperty.ofImmutable(
        this, "filter", PerturbationMapping.class, (Class) MarketDataFilter.class);
    /**
     * The meta-property for the {@code perturbation} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<ScenarioPerturbation<T>> perturbation = DirectMetaProperty.ofImmutable(
        this, "perturbation", PerturbationMapping.class, (Class) ScenarioPerturbation.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "marketDataType",
        "filter",
        "perturbation");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 843057760:  // marketDataType
          return marketDataType;
        case -1274492040:  // filter
          return filter;
        case -924739417:  // perturbation
          return perturbation;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public PerturbationMapping.Builder<T> builder() {
      return new PerturbationMapping.Builder<>();
    }

    @SuppressWarnings({"unchecked", "rawtypes" })
    @Override
    public Class<? extends PerturbationMapping<T>> beanType() {
      return (Class) PerturbationMapping.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code marketDataType} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Class<T>> marketDataType() {
      return marketDataType;
    }

    /**
     * The meta-property for the {@code filter} property.
     * @return the meta-property, not null
     */
    public MetaProperty<MarketDataFilter<? extends T, ?>> filter() {
      return filter;
    }

    /**
     * The meta-property for the {@code perturbation} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ScenarioPerturbation<T>> perturbation() {
      return perturbation;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 843057760:  // marketDataType
          return ((PerturbationMapping<?>) bean).getMarketDataType();
        case -1274492040:  // filter
          return ((PerturbationMapping<?>) bean).getFilter();
        case -924739417:  // perturbation
          return ((PerturbationMapping<?>) bean).getPerturbation();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code PerturbationMapping}.
   * @param <T>  the type
   */
  public static final class Builder<T> extends DirectFieldsBeanBuilder<PerturbationMapping<T>> {

    private Class<T> marketDataType;
    private MarketDataFilter<? extends T, ?> filter;
    private ScenarioPerturbation<T> perturbation;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(PerturbationMapping<T> beanToCopy) {
      this.marketDataType = beanToCopy.getMarketDataType();
      this.filter = beanToCopy.getFilter();
      this.perturbation = beanToCopy.getPerturbation();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 843057760:  // marketDataType
          return marketDataType;
        case -1274492040:  // filter
          return filter;
        case -924739417:  // perturbation
          return perturbation;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder<T> set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 843057760:  // marketDataType
          this.marketDataType = (Class<T>) newValue;
          break;
        case -1274492040:  // filter
          this.filter = (MarketDataFilter<? extends T, ?>) newValue;
          break;
        case -924739417:  // perturbation
          this.perturbation = (ScenarioPerturbation<T>) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder<T> set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public PerturbationMapping<T> build() {
      return new PerturbationMapping<>(
          marketDataType,
          filter,
          perturbation);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the type of market data handled by this mapping.
     * @param marketDataType  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder<T> marketDataType(Class<T> marketDataType) {
      JodaBeanUtils.notNull(marketDataType, "marketDataType");
      this.marketDataType = marketDataType;
      return this;
    }

    /**
     * Sets the filter that decides whether the perturbation should be applied to a piece of market data.
     * @param filter  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder<T> filter(MarketDataFilter<? extends T, ?> filter) {
      JodaBeanUtils.notNull(filter, "filter");
      this.filter = filter;
      return this;
    }

    /**
     * Sets perturbation that should be applied to market data as part of a scenario.
     * @param perturbation  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder<T> perturbation(ScenarioPerturbation<T> perturbation) {
      JodaBeanUtils.notNull(perturbation, "perturbation");
      this.perturbation = perturbation;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(128);
      buf.append("PerturbationMapping.Builder{");
      buf.append("marketDataType").append('=').append(JodaBeanUtils.toString(marketDataType)).append(',').append(' ');
      buf.append("filter").append('=').append(JodaBeanUtils.toString(filter)).append(',').append(' ');
      buf.append("perturbation").append('=').append(JodaBeanUtils.toString(perturbation));
      buf.append('}');
      return buf.toString();
    }

  }

  //-------------------------- AUTOGENERATED END --------------------------
}
