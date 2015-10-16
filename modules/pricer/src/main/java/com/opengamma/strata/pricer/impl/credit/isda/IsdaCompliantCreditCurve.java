/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.pricer.impl.credit.isda;

import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.math.impl.matrix.DoubleArray;

/**
 * An ISDA compliant credit curve.
 */
@BeanDefinition
public class IsdaCompliantCreditCurve extends IsdaCompliantCurve {

  public static IsdaCompliantCreditCurve makeFromForwardRates(double[] t, double[] fwd) {
    return new IsdaCompliantCreditCurve(IsdaCompliantCurve.makeFromForwardRates(t, fwd));
  }

  public static IsdaCompliantCreditCurve makeFromForwardRates(DoubleArray t, DoubleArray fwd) {
    return makeFromForwardRates(t.toArray(), fwd.toArray());
  }

  public static IsdaCompliantCreditCurve makeFromRT(double[] t, double[] ht) {
    ArgChecker.notEmpty(t, "t");
    ArgChecker.notEmpty(ht, "ht");
    ArgChecker.isTrue(t.length == ht.length, "length of t not equal to length of rt");
    return new IsdaCompliantCreditCurve(new IsdaCompliantCurve(new double[][] {t, ht}));
  }

  public static IsdaCompliantCreditCurve makeFromRT(DoubleArray t, DoubleArray rt) {
    return makeFromRT(t.toArray(), rt.toArray());
  }

  /**
   * Constructor for Joda-Beans.
   */
  protected IsdaCompliantCreditCurve() {
  }

  /**
   * Creates a flat credit (hazard) curve at hazard rate h.
   * 
   * @param t  the (arbitrary) single knot point (t > 0)
   * @param h  the level
   */
  public IsdaCompliantCreditCurve(double t, double h) {
    super(t, h);
  }

  /**
   * Creates a credit (hazard) curve with knots at times, t, zero hazard rates, h,
   * at the knots and piecewise constant forward hazard rates between knots
   * (i.e. linear interpolation of h*t or the -log(survival-probability).
   * 
   * @param t  the knot (node) times, not null
   * @param h  the zero hazard rates, not null
   */
  public IsdaCompliantCreditCurve(double[] t, double[] h) {
    super(t, h);
  }

  /**
   * Creates a shallow copy of the specified curve, used to down cast from ISDACompliantCurve.
   * 
   * @param from  the curve to clone from, not null
   */
  public IsdaCompliantCreditCurve(IsdaCompliantCurve from) {
    super(from);
  }

  //-------------------------------------------------------------------------
  /**
   * Get the zero hazard rate at time t (note: this simply a pseudonym for getZeroRate).
   * 
   * @param t  the time
   * @return zero hazard rate at time t
   */
  public double getHazardRate(double t) {
    return getZeroRate(t);
  }

  /**
   * Get the survival probability at time t (note: this simply a pseudonym for getDiscountFactor).
   * 
   * @param t  the time
   * @return survival probability at time t
   */
  public double getSurvivalProbability(double t) {
    return getDiscountFactor(t);
  }

  @Override
  public IsdaCompliantCreditCurve withRates(double[] r) {
    return new IsdaCompliantCreditCurve(super.withRates(r));
  }

  @Override
  public IsdaCompliantCreditCurve withRate(double rate, int index) {
    return new IsdaCompliantCreditCurve(super.withRate(rate, index));
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code IsdaCompliantCreditCurve}.
   * @return the meta-bean, not null
   */
  public static IsdaCompliantCreditCurve.Meta meta() {
    return IsdaCompliantCreditCurve.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(IsdaCompliantCreditCurve.Meta.INSTANCE);
  }

  @Override
  public IsdaCompliantCreditCurve.Meta metaBean() {
    return IsdaCompliantCreditCurve.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  @Override
  public IsdaCompliantCreditCurve clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      return super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(32);
    buf.append("IsdaCompliantCreditCurve{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  @Override
  protected void toString(StringBuilder buf) {
    super.toString(buf);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code IsdaCompliantCreditCurve}.
   */
  public static class Meta extends IsdaCompliantCurve.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap());

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    public BeanBuilder<? extends IsdaCompliantCreditCurve> builder() {
      return new DirectBeanBuilder<IsdaCompliantCreditCurve>(new IsdaCompliantCreditCurve());
    }

    @Override
    public Class<? extends IsdaCompliantCreditCurve> beanType() {
      return IsdaCompliantCreditCurve.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
