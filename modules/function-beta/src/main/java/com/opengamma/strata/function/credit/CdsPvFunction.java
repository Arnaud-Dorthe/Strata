package com.opengamma.strata.function.credit;

import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.engine.calculations.DefaultSingleCalculationMarketData;
import com.opengamma.strata.finance.credit.ExpandedCdsTrade;

public class CdsPvFunction extends AbstractCdsFunction<MultiCurrencyAmount> {

  @Override
  protected MultiCurrencyAmount execute(ExpandedCdsTrade trade, DefaultSingleCalculationMarketData provider) {
    return pricer().presentValue(trade, provider);
  }


}
