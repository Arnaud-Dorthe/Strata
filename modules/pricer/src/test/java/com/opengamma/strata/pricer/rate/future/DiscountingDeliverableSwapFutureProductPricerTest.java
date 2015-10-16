/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.pricer.rate.future;

import static com.opengamma.strata.basics.PayReceive.PAY;
import static com.opengamma.strata.basics.PayReceive.RECEIVE;
import static com.opengamma.strata.basics.currency.Currency.USD;
import static com.opengamma.strata.basics.date.BusinessDayConventions.MODIFIED_FOLLOWING;
import static com.opengamma.strata.basics.date.BusinessDayConventions.PRECEDING;
import static com.opengamma.strata.basics.date.DayCounts.ACT_ACT_ISDA;
import static com.opengamma.strata.basics.date.DayCounts.THIRTY_U_360;
import static com.opengamma.strata.basics.index.IborIndices.USD_LIBOR_3M;
import static com.opengamma.strata.basics.schedule.Frequency.P3M;
import static com.opengamma.strata.basics.schedule.Frequency.P6M;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.time.LocalDate;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.basics.currency.FxMatrix;
import com.opengamma.strata.basics.date.BusinessDayAdjustment;
import com.opengamma.strata.basics.date.DaysAdjustment;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;
import com.opengamma.strata.basics.interpolator.CurveInterpolator;
import com.opengamma.strata.basics.schedule.PeriodicSchedule;
import com.opengamma.strata.basics.schedule.StubConvention;
import com.opengamma.strata.basics.value.ValueSchedule;
import com.opengamma.strata.finance.rate.future.DeliverableSwapFuture;
import com.opengamma.strata.finance.rate.swap.FixedRateCalculation;
import com.opengamma.strata.finance.rate.swap.IborRateCalculation;
import com.opengamma.strata.finance.rate.swap.NotionalSchedule;
import com.opengamma.strata.finance.rate.swap.PaymentSchedule;
import com.opengamma.strata.finance.rate.swap.RateCalculationSwapLeg;
import com.opengamma.strata.finance.rate.swap.Swap;
import com.opengamma.strata.finance.rate.swap.SwapLeg;
import com.opengamma.strata.market.curve.CurveMetadata;
import com.opengamma.strata.market.curve.CurveName;
import com.opengamma.strata.market.curve.Curves;
import com.opengamma.strata.market.curve.InterpolatedNodalCurve;
import com.opengamma.strata.market.sensitivity.CurveCurrencyParameterSensitivities;
import com.opengamma.strata.market.sensitivity.PointSensitivities;
import com.opengamma.strata.math.impl.interpolation.Interpolator1DFactory;
import com.opengamma.strata.math.impl.matrix.DoubleArray;
import com.opengamma.strata.pricer.rate.ImmutableRatesProvider;
import com.opengamma.strata.pricer.sensitivity.RatesFiniteDifferenceSensitivityCalculator;

/**
 * Test {@link DiscountingDeliverableSwapFutureProductPricer}.
 */
@Test
public class DiscountingDeliverableSwapFutureProductPricerTest {
  // curves
  private static final CurveInterpolator INTERPOLATOR = Interpolator1DFactory.LINEAR_INSTANCE;
  private static final LocalDate VALUATION = LocalDate.of(2012, 9, 20);
  private static final DoubleArray USD_DSC_TIME = DoubleArray.of(0.0, 0.5, 1.0, 2.0, 5.0, 10.0);
  private static final DoubleArray USD_DSC_RATE = DoubleArray.of(0.0100, 0.0120, 0.0120, 0.0140, 0.0140, 0.0140);
  private static final CurveName USD_DSC_NAME = CurveName.of("USD Dsc");
  private static final CurveMetadata USD_DSC_METADATA = Curves.zeroRates(USD_DSC_NAME, ACT_ACT_ISDA);
  private static final InterpolatedNodalCurve USD_DSC =
      InterpolatedNodalCurve.of(USD_DSC_METADATA, USD_DSC_TIME, USD_DSC_RATE, INTERPOLATOR);
  private static final DoubleArray USD_FWD3_TIME = DoubleArray.of(0.0, 0.5, 1.0, 2.0, 5.0, 10.0);
  private static final DoubleArray USD_FWD3_RATE = DoubleArray.of(0.0150, 0.0125, 0.0150, 0.0175, 0.0150, 0.0150);
  private static final CurveName USD_FWD3_NAME = CurveName.of("USD LIBOR 3M");
  private static final CurveMetadata USD_FWD3_METADATA = Curves.zeroRates(USD_FWD3_NAME, ACT_ACT_ISDA);
  private static final InterpolatedNodalCurve USD_FWD3 =
      InterpolatedNodalCurve.of(USD_FWD3_METADATA, USD_FWD3_TIME, USD_FWD3_RATE, INTERPOLATOR);
  private static final ImmutableRatesProvider PROVIDER = ImmutableRatesProvider.builder()
      .valuationDate(VALUATION)
      .fxMatrix(FxMatrix.empty())
      .discountCurves(ImmutableMap.of(USD, USD_DSC))
      .indexCurves(ImmutableMap.of(USD_LIBOR_3M, USD_FWD3))
      .build();
  // underlying swap
  private static final NotionalSchedule UNIT_NOTIONAL = NotionalSchedule.of(USD, 1d);
  private static final HolidayCalendar CALENDAR = HolidayCalendars.SAT_SUN;
  private static final BusinessDayAdjustment BDA_MF = BusinessDayAdjustment.of(MODIFIED_FOLLOWING, CALENDAR);
  private static final BusinessDayAdjustment BDA_P = BusinessDayAdjustment.of(PRECEDING, CALENDAR);
  private static final LocalDate START = LocalDate.of(2012, 12, 19);
  private static final LocalDate END = START.plusYears(10);
  private static final double RATE = 0.0175;
  private static final SwapLeg FIXED_LEG = RateCalculationSwapLeg.builder()
      .payReceive(RECEIVE)
      .accrualSchedule(PeriodicSchedule.builder()
          .startDate(START)
          .endDate(END)
          .frequency(P6M)
          .businessDayAdjustment(BDA_MF)
          .stubConvention(StubConvention.SHORT_FINAL)
          .build())
      .paymentSchedule(PaymentSchedule.builder()
          .paymentFrequency(P6M)
          .paymentDateOffset(DaysAdjustment.NONE)
          .build())
      .notionalSchedule(UNIT_NOTIONAL)
      .calculation(FixedRateCalculation.builder()
          .dayCount(THIRTY_U_360)
          .rate(ValueSchedule.of(RATE))
          .build())
      .build();
  private static final SwapLeg IBOR_LEG = RateCalculationSwapLeg.builder()
      .payReceive(PAY)
      .accrualSchedule(PeriodicSchedule.builder()
          .startDate(START)
          .endDate(END)
          .frequency(P3M)
          .businessDayAdjustment(BDA_MF)
          .stubConvention(StubConvention.SHORT_FINAL)
          .build())
      .paymentSchedule(PaymentSchedule.builder()
          .paymentFrequency(P3M)
          .paymentDateOffset(DaysAdjustment.NONE)
          .build())
      .notionalSchedule(UNIT_NOTIONAL)
      .calculation(IborRateCalculation.builder()
          .index(USD_LIBOR_3M)
          .fixingDateOffset(DaysAdjustment.ofBusinessDays(-2, CALENDAR, BDA_P))
          .build())
      .build();
  private static final Swap SWAP = Swap.of(FIXED_LEG, IBOR_LEG);
  // deliverable swap future
  private static final LocalDate LAST_TRADE = LocalDate.of(2012, 12, 17);
  private static final LocalDate DELIVERY = LocalDate.of(2012, 12, 19);
  private static final double NOTIONAL = 100000;
  private static final DeliverableSwapFuture FUTURE = DeliverableSwapFuture.builder()
      .deliveryDate(DELIVERY)
      .lastTradeDate(LAST_TRADE)
      .notional(NOTIONAL)
      .underlyingSwap(SWAP)
      .build();
  // calculators
  private static final double TOL = 1.0e-13;
  private static final double EPS = 1.0e-6;
  private static final DiscountingDeliverableSwapFutureProductPricer PRICER =
      DiscountingDeliverableSwapFutureProductPricer.DEFAULT;
  private static final RatesFiniteDifferenceSensitivityCalculator FD_CAL =
      new RatesFiniteDifferenceSensitivityCalculator(EPS);

  //-------------------------------------------------------------------------
  public void test_price() {
    double computed = PRICER.price(FUTURE, PROVIDER);
    double pvSwap = PRICER.getSwapPricer().presentValue(SWAP, PROVIDER).getAmount(USD).getAmount();
    double yc = ACT_ACT_ISDA.relativeYearFraction(VALUATION, DELIVERY);
    double df = Math.exp(-USD_DSC.yValue(yc) * yc);
    double expected = 1d + pvSwap / df;
    assertEquals(computed, expected, TOL);
  }

  public void test_priceSensitivity() {
    PointSensitivities point = PRICER.priceSensitivity(FUTURE, PROVIDER);
    CurveCurrencyParameterSensitivities computed = PROVIDER.curveParameterSensitivity(point);
    CurveCurrencyParameterSensitivities expected =
        FD_CAL.sensitivity(PROVIDER, (p) -> CurrencyAmount.of(USD, PRICER.price(FUTURE, (p))));
    assertTrue(computed.equalWithTolerance(expected, 10d * EPS));
  }

  //-------------------------------------------------------------------------
  public void regression() {
    double price = PRICER.price(FUTURE, PROVIDER);
    assertEquals(price, 1.022245377054993, TOL); // 2.x
  }

}
