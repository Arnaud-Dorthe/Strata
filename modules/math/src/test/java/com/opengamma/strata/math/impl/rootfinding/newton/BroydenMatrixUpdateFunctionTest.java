/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.strata.math.impl.rootfinding.newton;

import org.testng.annotations.Test;

import com.opengamma.strata.math.impl.function.Function1D;
import com.opengamma.strata.math.impl.matrix.DoubleArray;
import com.opengamma.strata.math.impl.matrix.DoubleMatrix;

/**
 * Test.
 */
@Test
public class BroydenMatrixUpdateFunctionTest {
  private static final BroydenMatrixUpdateFunction UPDATE = new BroydenMatrixUpdateFunction();
  private static final DoubleArray V = DoubleArray.of(1, 2);
  private static final DoubleMatrix M = DoubleMatrix.copyOf(new double[][] { {3, 4}, {5, 6}});
  private static final Function1D<DoubleArray, DoubleMatrix> J = new Function1D<DoubleArray, DoubleMatrix>() {
    @Override
    public DoubleMatrix evaluate(final DoubleArray x) {
      return M;
    }
  };

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDeltaX() {
    UPDATE.getUpdatedMatrix(J, V, null, V, M);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDeltaY() {
    UPDATE.getUpdatedMatrix(J, V, V, null, M);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMatrix() {
    UPDATE.getUpdatedMatrix(J, V, V, V, null);
  }
}
