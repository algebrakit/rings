package cc.redberry.rings.poly;

import cc.redberry.rings.poly.univar.IUnivariatePolynomial;

/**
 * Algebraic number field.
 *
 * @see FiniteField
 */
public class AlgebraicNumberField<Poly extends IUnivariatePolynomial<Poly>>
        extends UnivariateQuotientRing<Poly> {
    /**
     * Constructs algebraic extension of field generated by the root of given irreducible polynomial.
     */
    public AlgebraicNumberField(Poly minimalPoly) {
        super(minimalPoly);
        if (minimalPoly.isOverFiniteField())
            throw new IllegalArgumentException("Use FiniteField for constructing extensions of finite fields.");
    }
}
