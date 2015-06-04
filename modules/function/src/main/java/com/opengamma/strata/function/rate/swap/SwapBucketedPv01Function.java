/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.function.rate.swap;

import com.opengamma.strata.finance.rate.swap.ExpandedSwap;
import com.opengamma.strata.market.sensitivity.CurveCurrencyParameterSensitivities;
import com.opengamma.strata.market.sensitivity.PointSensitivities;
import com.opengamma.strata.pricer.rate.RatesProvider;

/**
 * Calculates the bucketed PV01, the present value curve parameter sensitivity of a {@code SwapTrade}.
 * This operates by algorithmic differentiation (AD).
 */
public class SwapBucketedPv01Function
    extends AbstractSwapFunction<CurveCurrencyParameterSensitivities> {

  @Override
  protected CurveCurrencyParameterSensitivities execute(ExpandedSwap product, RatesProvider provider) {
    PointSensitivities pointSensitivity = pricer().presentValueSensitivity(product, provider).build();
    return provider.curveParameterSensitivity(pointSensitivity);
  }

}
