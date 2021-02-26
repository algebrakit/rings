package cc.redberry.rings.scaladsl

import cc.redberry.rings.poly.PolynomialMethods.{Factor, PolynomialGCD}
import cc.redberry.rings.scaladsl.util.timing
import org.junit.Test

/**
  *
  */
class Examples3 {

  @Test
  def testNumberField1: Unit = {
    implicit val rationals = Q
    val minimalPolynomial = UnivariatePolynomial(3, 0, 0, 1)
    implicit val numberField = AlgebraicNumberField(minimalPolynomial, "a")

    println(numberField.minimalPolynomial(numberField("1 + a + a^2")))

    assert(numberField.degree() == 3)
    implicit val ring = UnivariateRing(numberField, "x")

    val poly = ring("(x^2 - a*x + a) * (x + a)")
    val factors = ring factor poly
    println(ring stringify factors)
  }

  @Test
  def testNumberField2: Unit = {
    implicit val rationals = Q
    val mPoly1 = UnivariatePolynomial(3, 0, 0, 1)

    implicit var cfField = MultipleFieldExtension(AlgebraicNumberField(mPoly1, "r1"))

    val mPoly2 = UnivariatePolynomial(2, 3, 4, 1)(cfField)
    cfField = cfField.joinAlgebraicElement(mPoly2, "r2")

    val mPoly3 = UnivariatePolynomial(cfField(2), cfField("r1 + r2"), cfField(4), cfField(1))(cfField)
    cfField = cfField.joinAlgebraicElement(mPoly3, "r3")

    println(cfField)
    println(cfField.degree())

    // very fast
    val simpleCfField = cfField.getSimpleExtension("A")
    simpleCfField.coder.bind("r1", cfField.getGeneratorRep(0))
    simpleCfField.coder.bind("r2", cfField.getGeneratorRep(1))
    simpleCfField.coder.bind("r3", cfField.getGeneratorRep(2))

    val psRing = MultivariateRing(simpleCfField, Array("x", "y", "z"))
    val sPoly = psRing("((x - r1 - r2) * (y - r1 - r3) * (z - r2 - r3))^3 - 1")
    println(sPoly.degree())

    //  very slow
    //    val pmRing = MultivariateRing(cfField, Array("x", "y", "z"))
    //    val mPoly = pmRing("((x - r1 - r2) * (y - r1 - r3) * (z - r2 - r3)) - 1")
    //    println(mPoly.degree())
  }

  @Test
  def testComplexNumbers: Unit = {
    implicit val ring = MultivariateRing(GaussianRationals, Array("x", "y", "z"))
    val poly = ring("((x^2 - i)*(y^2 + i)*(z^2 - 2)*(i*x + i*y + i*z - 1))^3 - 1")
    val factors = ring.factor(poly)
    println(ring stringify factors)
    assert(2 == factors.size())
  }


  @Test
  def test4: Unit = {
    import syntax._

    // parse some minimal polynomial from string
    val minimalPoly = UnivariateRing(Q, "x")("x^3 - 5")

    // create algebraic number field generated by specified polynomial
    // variable "r" represents the root of minimal polynomial
    implicit val field = AlgebraicNumberField(minimalPoly, "r")
    // just a shortcut for type of field elements
    type Number = field.ElementType
    val r = field("r")

    // do some arithmetic in number field
    val arith1 = (2 + r.pow(19) / 3).pow(3) - 1
    // parse number elements
    val arith2 = field("1 + r/(3 - r^7)^8 + r")
    // assert that r is the root of X^3 - 5
    assert(r.pow(3) == field(5))

    // compute Norm of some algebraic number
    val norm1 = field.norm(arith1)
    // assert that norm is free of radicals
    assert(field.isInTheBaseField(norm1))

    // compute minimal polynomial of some other algebraic number
    val mp = field.minimalPolynomial(arith2)
    // assert that its degree the same
    assert(mp.degree() == minimalPoly.degree())

    // declare polynomial ring over algebraic numbers
    implicit val ring = MultivariateRing(field, Array("x", "y", "z"))
    val (x, y, z) = ring("x", "y", "z")

    // create some polynomial over algebraic numbers
    val poly: MultivariatePolynomial[Number] = ((x - r) * (y - r) * (z - r)).pow(2) - 1
    // compute norm of poly, its coefficient ring is the base ring of algebraic extension
    type BaseNumber = field.CoefficientType
    val polyNorm: MultivariatePolynomial[BaseNumber] = field.normOfPolynomial(poly)

    // factorize multivariate polynomial over algebraic number field
    val factors = Factor(poly)
    println(ring stringify factors)
  }

  @Test
  def test5: Unit = {

    // Gaussian integers (not a field)
    val integers = GaussianIntegers

    // Gaussian rationals
    val rationals = GaussianRationals

    // by default "i" is used for imaginary unit
    // another symbol may be used as well
    val otherSymbols: AlgebraicNumberField[Rational[IntZ]] = GaussianRationals("ImaginaryUnit")
  }


  @Test
  def test6: Unit = {
    import syntax._
    // the first algebraic element is given by its minimal polynomial in Q[x]
    val minPoly1 = UnivariatePolynomial(3, 0, 0, 1)(Q)
    // create initial field extension Q(alpha1)
    implicit var field = MultipleFieldExtension(minPoly1, "alpha1")
    var alpha1 = field("alpha1")

    // create minimal polynomial for second algebraic number
    // it may have coefficients from algebraic number field Q(alpha1)
    val minPoly2 = UnivariatePolynomial(alpha1, field(3), alpha1.pow(2))
    // assert that minimal polynomial is irreducible
    assert(Factor(minPoly2).isTrivial)

    // join alpha2 to field extension
    // that is field is now Q(alpha1, alpha2)
    field = field.joinAlgebraicElement(minPoly2, "alpha2")
    alpha1 = field("alpha1") // cast alpha1 to updated field
    var alpha2 = field("alpha2")

    // create minimal polynomial for third algebraic number
    // it may have coefficients from algebraic number field Q(alpha1, alpha2)
    val minPoly3 = UnivariatePolynomial(field(2), alpha1 + alpha2, field(4), field(1))
    // assert that minimal polynomial is irreducible
    assert(Factor(minPoly3).isTrivial)

    // join alpha3 to field extension
    // that is field is now Q(alpha1, alpha2, alpha3)
    field = field.joinAlgebraicElement(minPoly3, "alpha3")
    alpha1 = field("alpha1") // cast alpha1 to updated field
    alpha2 = field("alpha2") // cast alpha2 to updated field
    var alpha3 = field("alpha3")

    // field has three "variables": alpha1, alpha2, alpha3
    assert(field.nVariables() == 3)

    // check the degree of obtained field extension:
    println(field.degree())

    // do some arithmetic in multiple extension (this is typically
    // quite slow and expressions are quire large)
    val el1 = (alpha1 + alpha2 - alpha3 / 17).pow(2) - 1 / alpha2
    // parse from string
    val el2 = field("(-alpha1 - alpha2 + alpha3/17)^2 - 1/alpha2")
    assert(el1 - el2 == field(0))

    // create multivariate polynomial ring over multiple field extension
    // Q(alpha1, alpha2, alpha3)[x,y,z] and perform some arithmetic
    // this will will be typically quite slow
    val pmRing = MultivariateRing(field, Array("x", "y", "z"))
    val (t1, thePoly1) = timing { pmRing("((x - alpha1 - alpha2) * (y - alpha1 - alpha3) * (z - alpha2 - alpha3))^2 - 1") }


    // create the same multivariate ring, but using the isomorphic
    // simple field extension Q(gamma) = Q(alpha1, alpha2, alpha3)
    val simpleCfField = field.getSimpleExtension("gamma")
    //  Q(gamma)[x,y,z]
    val psRing = MultivariateRing(simpleCfField, Array("x", "y", "z"))
    val (t2, thePoly2_) = timing { psRing("((x - alpha1 - alpha2) * (y - alpha1 - alpha3) * (z - alpha2 - alpha3))^2 - 1") }
    // convert polynomial Q(gamma)[x,y,z] to Q(alpha1, alpha2, alpha3)[x,y,z]
    // by substituting gamma = primitive_element (combination of alpha's)
    val thePoly2 = thePoly2_.mapCoefficients(field, p => field.valueOf(p.composition(field.getPrimitiveElement)))

    // polynomials are equal, however arithmetic in simple
    // extension is orders of magnitude faster
    assert(thePoly2 == thePoly1)
    println(s"Arithmetic in multiple extension: $t1")
    println(s"Arithmetic in simple extension: $t2")
  }

  @Test
  def testSplittingField(): Unit = {
    import syntax._
    // some irreducible polynomial
    val poly = UnivariateRing(Q, "x")("17*x^3 - 14*x^2 + 25*x +  15")
    // create splitting field as multiple field extension
    // s1,s2,s3 are roots of specified poly
    implicit val field = SplittingField(poly, Array("s1", "s2", "s3"))
    // check the degree of this extension (6 = 3!)
    assert(6 == field.getSimpleExtension().degree())

    // assert Vieta's identities
    val (s1, s2, s3) = field("s1", "s2", "s3")
    assert(s1 * s2 * s3 == field("-15/17"))
    assert(s1 * s2 + s1 * s3 + s2 * s3 == field("25/17"))
    assert(s1 + s2 + s3 == field("14/17"))
  }

  @Test
  def testSplittingField1(): Unit = {
    // some irreducible polynomial
    val poly = UnivariateRing(Q, "x")("17*x^3 - 14*x^2 + 25*x +  15")
    // create splitting field as multiple field extension
    // s1,s2,s3 are roots of specified poly
    implicit val field = SplittingField(poly, Array("s1", "s2", "s3"))
    println(field)
  }

  @Test
  def testParametricNumberField1(): Unit = {
    import syntax._
    // Frac(Q[c, d])
    val params = Frac(MultivariateRing(Q, Array("c", "d")))
    // A minimal polynomial X^2 + c = 0
    val generator = UnivariatePolynomial(params("c"), params(0), params(1))(params)
    // Algebraic number field Q(sqrt(c)), here "s" denotes square root of c
    implicit val cfRing = AlgebraicNumberField(generator, "s")
    // ring of polynomials  Q(sqrt(c))(x, y, z)
    implicit val ring = MultivariateRing(cfRing, Array("x", "y", "z"))
    // bring variables
    val (x, y, z, s) = ring("x", "y", "z", "s")
    // some polynomials
    val poly1 = (x + y + s).pow(3) * (x - y - z).pow(2)
    val poly2 = (x + y + s).pow(3) * (x + y + z).pow(4)

    // compute gcd
    val gcd = PolynomialGCD(poly1, poly2)
    println(ring stringify gcd)
  }

  @Test
  def testParametricNumberField2(): Unit = {
    import syntax._
    // Frac(Q[c, d])
    val params = Frac(MultivariateRing(Q, Array("c", "d")))
    // A minimal polynomial X^2 + c = 0
    val gen1 = UnivariatePolynomial(params("c"), params(0), params(1))(params)
    // A minimal polynomial X^2 + d = 0
    val gen2 = UnivariatePolynomial(params("d"), params(0), params(1))(params)

    val ext = MultipleFieldExtension(Array(gen1, gen2), Array("s1", "s2"))
    implicit val cfRing = ext.getSimpleExtension("s")

    // ring of polynomials  Q(sqrt(c), sqrt(d))(x, y, z)
    implicit val ring = MultivariateRing(cfRing, Array("x", "y", "z"))
    // bring variables
    val (x, y, z, s1, s2) = ring("x", "y", "z", "s1", "s2")
    // some polynomials
    val poly1 = (x + s2 * y + s1).pow(2) * (x - y - z + s2).pow(2)
    val poly2 = (x + s2 * y + s1).pow(1) * (x + y + z - s1).pow(3)

    println(MultivariateRing(ext, ring.variables) stringify poly1.mapCoefficients(ext, p => ext.valueOf(p.composition(ext.getPrimitiveElement))))
    //    // compute gcd
    //    val gcd = PolynomialGCD(poly1, poly2)
    //    println(ring stringify gcd)
  }
}
