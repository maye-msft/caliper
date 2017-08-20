/*
MIT License

Copyright (c) 2016 Kent Randall

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package org.point85.uom.test;

import static org.hamcrest.number.BigDecimalCloseTo.closeTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;
import org.point85.uom.Constant;
import org.point85.uom.Prefix;
import org.point85.uom.Quantity;
import org.point85.uom.Unit;
import org.point85.uom.UnitOfMeasure;
import org.point85.uom.UnitType;

public class TestQuantity extends BaseTest {

	@Test
	public void testNamedQuantity() throws Exception {

		Quantity q = new Quantity(BigDecimal.TEN, Unit.CELSIUS);
		assertTrue(q.toString() != null);

		// faraday
		Quantity f = sys.getQuantity(Constant.FARADAY_CONSTANT);
		Quantity qe = sys.getQuantity(Constant.ELEMENTARY_CHARGE);
		Quantity na = sys.getQuantity(Constant.AVAGADRO_CONSTANT);
		Quantity eNA = qe.multiply(na);
		assertThat(f.getAmount(), closeTo(eNA.getAmount(), DELTA6));
		assertThat(f.getAmount(), closeTo(Quantity.createAmount("96485.3328959"), DELTA5));

		// epsilon 0
		UnitOfMeasure fm = sys.createQuotientUOM(UnitType.UNCLASSIFIED, "Farad per metre", "F/m", "Farad per metre",
				sys.getUOM(Unit.FARAD), sys.getUOM(Unit.METRE));
		Quantity eps0 = sys.getQuantity(Constant.ELECTRIC_PERMITTIVITY);
		assertThat(eps0.getAmount(), closeTo(Quantity.createAmount("8.854187817E-12"), DELTA6));
		assertThat(eps0.convert(fm).getAmount(), closeTo(Quantity.createAmount("8.854187817E-12"), DELTA6));

		// atomic masses
		Quantity u = new Quantity(Quantity.createAmount("1.66053904020E-24"), sys.getUOM(Unit.GRAM));
		Quantity me = sys.getQuantity(Constant.ELECTRON_MASS);
		BigDecimal bd = me.divide(u).getAmount();
		assertThat(bd, closeTo(Quantity.createAmount("5.48579909016E-04"), DELTA6));

		Quantity mp = sys.getQuantity(Constant.PROTON_MASS);
		bd = mp.divide(u).getAmount();
		assertThat(bd, closeTo(Quantity.createAmount("1.00727646687991"), DELTA6));
	}

	@Test
	public void testAllUnits() throws Exception {

		for (Unit u : Unit.values()) {
			UnitOfMeasure uom1 = sys.getUOM(u);
			UnitOfMeasure uom2 = sys.getUOM(u);
			assertTrue(uom1.equals(uom2));

			Quantity q1 = new Quantity(BigDecimal.TEN, uom1);
			Quantity q2 = q1.convert(uom2);
			assertTrue(q1.equals(q2));
		}
	}

	@Test
	public void testTime() throws Exception {

		UnitOfMeasure second = sys.getSecond();
		UnitOfMeasure minute = sys.getMinute();

		Quantity oneMin = new Quantity(BigDecimal.ONE, minute);
		Quantity oneSec = new Quantity(BigDecimal.ONE, second);
		Quantity converted = oneMin.convert(second);
		BigDecimal bd60 = Quantity.createAmount("60");

		assertThat(converted.getAmount(), closeTo(bd60, DELTA6));
		assertTrue(converted.getUOM().equals(second));

		Quantity sixty = oneMin.divide(oneSec);
		assertThat(sixty.getAmount(), closeTo(BigDecimal.ONE, DELTA6));
		assertThat(sixty.getUOM().getScalingFactor(), closeTo(bd60, DELTA6));

		Quantity q1 = sixty.convert(sys.getOne());
		assertTrue(q1.getUOM().equals(sys.getOne()));
		assertThat(q1.getAmount(), closeTo(bd60, DELTA6));

		q1 = q1.multiply(oneSec);
		assertTrue(q1.convert(second).getUOM().equals(second));
		assertThat(q1.getAmount(), closeTo(bd60, DELTA6));

		q1 = q1.convert(minute);
		assertTrue(q1.getUOM().equals(minute));
		assertThat(q1.getAmount(), closeTo(BigDecimal.ONE, DELTA6));

		assertTrue(q1.hashCode() != 0);

	}

	@Test
	public void testTemperature() throws Exception {

		UnitOfMeasure K = sys.getUOM(Unit.KELVIN);
		UnitOfMeasure C = sys.getUOM(Unit.CELSIUS);
		UnitOfMeasure R = sys.getUOM(Unit.RANKINE);
		UnitOfMeasure F = sys.getUOM(Unit.FAHRENHEIT);

		BigDecimal bd212 = Quantity.createAmount("212");
		BigDecimal oneHundred = Quantity.createAmount("100");

		Quantity q1 = new Quantity(bd212, F);
		Quantity q2 = q1.convert(C);
		assertThat(q2.getAmount(), closeTo(oneHundred, DELTA6));
		assertThat(q2.convert(F).getAmount(), closeTo(bd212, DELTA6));

		BigDecimal bd32 = Quantity.createAmount("32");
		q1 = new Quantity(bd32, F);
		q2 = q1.convert(C);
		assertThat(q2.getAmount(), closeTo(BigDecimal.ZERO, DELTA6));
		assertThat(q2.convert(F).getAmount(), closeTo(bd32, DELTA6));

		q1 = new Quantity(BigDecimal.ZERO, F);
		q2 = q1.convert(C);
		assertThat(q2.getAmount(), closeTo(Quantity.createAmount("-17.7777777777778"), DELTA6));
		assertThat(q2.convert(F).getAmount(), closeTo(BigDecimal.ZERO, DELTA6));

		BigDecimal bd459 = Quantity.createAmount("459.67");
		q1 = new Quantity(bd459, R);
		q2 = q1.convert(F);
		assertThat(q2.getAmount(), closeTo(BigDecimal.ZERO, DELTA6));
		assertThat(q2.convert(R).getAmount(), closeTo(bd459, DELTA6));

		BigDecimal bd255 = Quantity.createAmount("255.3722222222222");
		q2 = q1.convert(K);
		assertThat(q2.getAmount(), closeTo(bd255, DELTA6));
		assertThat(q2.convert(R).getAmount(), closeTo(bd459, DELTA6));

		BigDecimal bd17 = Quantity.createAmount("-17.7777777777778");
		q2 = q1.convert(C);
		assertThat(q2.getAmount(), closeTo(bd17, DELTA6));
		assertThat(q2.convert(R).getAmount(), closeTo(bd459, DELTA6));

		BigDecimal bd273 = Quantity.createAmount("273.15");
		q1 = new Quantity(bd273, K);
		q2 = q1.convert(C);
		assertThat(q2.getAmount(), closeTo(BigDecimal.ZERO, DELTA6));
		assertThat(q2.convert(K).getAmount(), closeTo(bd273, DELTA6));

		q1 = new Quantity(BigDecimal.ZERO, K);
		q2 = q1.convert(R);
		assertThat(q2.getAmount(), closeTo(BigDecimal.ZERO, DELTA6));
		assertThat(q2.convert(K).getAmount(), closeTo(BigDecimal.ZERO, DELTA6));
	}

	@Test
	public void testLength() throws Exception {

		UnitOfMeasure m = sys.getUOM(Unit.METRE);
		UnitOfMeasure cm = sys.getUOM(Prefix.CENTI, m);
		UnitOfMeasure m2 = sys.getUOM(Unit.SQUARE_METRE);

		final char squared = 0x00B2;
		String cmsym = "cm" + squared;
		UnitOfMeasure cm2 = sys.getUOM(cmsym);

		if (cm2 == null) {
			cm2 = sys.createPowerUOM(UnitType.AREA, "square centimetres", cmsym, "centimetres squared", cm, 2);
		}

		UnitOfMeasure ft = sys.getUOM(Unit.FOOT);
		UnitOfMeasure yd = sys.getUOM(Unit.YARD);
		UnitOfMeasure ft2 = sys.getUOM(Unit.SQUARE_FOOT);
		UnitOfMeasure in2 = sys.getUOM(Unit.SQUARE_INCH);

		BigDecimal oneHundred = Quantity.createAmount("100");

		Quantity q1 = new Quantity(BigDecimal.ONE, ft2);
		Quantity q2 = q1.convert(in2);
		assertThat(q2.getAmount(), closeTo(Quantity.createAmount("144"), DELTA6));
		assertThat(q2.convert(ft2).getAmount(), closeTo(BigDecimal.ONE, DELTA6));

		q1 = new Quantity(BigDecimal.ONE, sys.getUOM(Unit.SQUARE_METRE));
		q2 = q1.convert(ft2);
		assertThat(q2.getAmount(), closeTo(Quantity.createAmount("10.76391041670972"), DELTA6));
		assertThat(q2.convert(m2).getAmount(), closeTo(BigDecimal.ONE, DELTA6));

		BigDecimal bd = Quantity.createAmount("3");
		q1 = new Quantity(bd, ft);
		q2 = q1.convert(yd);
		assertThat(q2.getAmount(), closeTo(BigDecimal.ONE, DELTA6));
		assertThat(q2.convert(ft).getAmount(), closeTo(bd, DELTA6));

		bd = Quantity.createAmount("0.3048");
		q1 = new Quantity(BigDecimal.ONE, ft);
		q2 = q1.convert(m);
		assertThat(q2.getAmount(), closeTo(bd, DELTA6));
		assertThat(q2.convert(ft).getAmount(), closeTo(BigDecimal.ONE, DELTA6));

		bd = oneHundred;
		q1 = new Quantity(bd, cm);
		q2 = q1.convert(m);
		assertThat(q2.getAmount(), closeTo(BigDecimal.ONE, DELTA6));
		assertThat(q2.convert(cm).getAmount(), closeTo(bd, DELTA6));

		// add
		bd = Quantity.createAmount("50");
		q1 = new Quantity(bd, cm);
		q2 = new Quantity(bd, cm);
		Quantity q3 = q1.add(q2);
		assertThat(q3.getAmount(), closeTo(bd.add(bd), DELTA6));
		assertThat(q3.convert(m).getAmount(), closeTo(BigDecimal.ONE, DELTA6));

		Quantity q4 = q2.add(q1);
		assertThat(q4.getAmount(), closeTo(bd.add(bd), DELTA6));
		assertThat(q4.convert(m).getAmount(), closeTo(BigDecimal.ONE, DELTA6));
		assertTrue(q3.equals(q4));

		// subtract
		q3 = q1.subtract(q2);
		assertThat(q3.getAmount(), closeTo(BigDecimal.ZERO, DELTA6));
		assertThat(q3.convert(m).getAmount(), closeTo(BigDecimal.ZERO, DELTA6));

		q4 = q2.subtract(q1);
		assertThat(q4.getAmount(), closeTo(BigDecimal.ZERO, DELTA6));
		assertThat(q4.convert(m).getAmount(), closeTo(BigDecimal.ZERO, DELTA6));
		assertTrue(q3.equals(q4));

		// multiply
		q3 = q1.multiply(q2);
		assertThat(q3.getAmount(), closeTo(Quantity.createAmount("2500"), DELTA6));

		q4 = q3.convert(cm2);
		assertThat(q4.getAmount(), closeTo(Quantity.createAmount("2500"), DELTA6));

		q4 = q3.convert(m2);
		assertThat(q4.getAmount(), closeTo(Quantity.createAmount("0.25"), DELTA6));

		// divide
		q4 = q3.divide(q1);
		assertTrue(q4.equals(q2));

	}

	@Test
	public void testUSQuantity() throws Exception {

		UnitOfMeasure gal = sys.getUOM(Unit.US_GALLON);
		UnitOfMeasure in3 = sys.getUOM(Unit.CUBIC_INCH);
		UnitOfMeasure floz = sys.getUOM(Unit.US_FLUID_OUNCE);
		UnitOfMeasure qt = sys.getUOM(Unit.US_QUART);

		Quantity q1 = new Quantity(BigDecimal.TEN, gal);
		Quantity q2 = q1.convert(in3);
		assertThat(q2.getAmount(), closeTo(Quantity.createAmount("2310"), DELTA6));
		assertTrue(q2.getUOM().equals(in3));

		q1 = new Quantity("128", floz);
		q2 = q1.convert(qt);
		assertThat(q2.getAmount(), closeTo(Quantity.createAmount("4"), DELTA6));
		assertTrue(q2.getUOM().equals(qt));

		UnitOfMeasure ft = sys.getUOM(Unit.FOOT);
		UnitOfMeasure in = sys.getUOM(Unit.INCH);
		UnitOfMeasure mi = sys.getUOM(Unit.MILE);

		q1 = new Quantity(BigDecimal.TEN, ft);
		q2 = q1.convert(in);

		q1 = new Quantity(BigDecimal.ONE, mi);

		// British cup to US gallon
		q1 = new Quantity(BigDecimal.TEN, sys.getUOM(Unit.BR_CUP));
		q2 = q1.convert(sys.getUOM(Unit.US_GALLON));
		assertThat(q2.getAmount(), closeTo(Quantity.createAmount("0.6"), DELTA3));

		// US ton to British ton
		q1 = new Quantity(BigDecimal.TEN, sys.getUOM(Unit.US_TON));
		q2 = q1.convert(sys.getUOM(Unit.BR_TON));
		assertThat(q2.getAmount(), closeTo(Quantity.createAmount("8.928571428"), DELTA6));

		// troy ounce to ounce
		q1 = new Quantity(BigDecimal.TEN, Unit.TROY_OUNCE);
		assertThat(q1.convert(Unit.OUNCE).getAmount(), closeTo(Quantity.createAmount("10.971"), DELTA3));

		// deci-litre to quart
		q1 = new Quantity(BigDecimal.TEN, Prefix.DECI, Unit.LITRE);
		q2 = q1.convert(Unit.US_QUART);
		assertThat(q2.getAmount(), closeTo(Quantity.createAmount("1.0566882"), DELTA6));
	}

	@Test
	public void testSIQuantity() throws Exception {

		BigDecimal ten = Quantity.createAmount("10");

		UnitOfMeasure litre = sys.getUOM(Unit.LITRE);
		UnitOfMeasure m3 = sys.getUOM(Unit.CUBIC_METRE);
		UnitOfMeasure m2 = sys.getUOM(Unit.SQUARE_METRE);
		UnitOfMeasure m = sys.getUOM(Unit.METRE);
		UnitOfMeasure cm = sys.getUOM(Prefix.CENTI, m);
		UnitOfMeasure mps = sys.getUOM(Unit.METRE_PER_SEC);
		UnitOfMeasure secPerM = sys.createQuotientUOM(UnitType.UNCLASSIFIED, null, "s/m", null, sys.getSecond(), m);
		UnitOfMeasure oneOverM = sys.getUOM(Unit.DIOPTER);
		UnitOfMeasure fperm = sys.getUOM(Unit.FARAD_PER_METRE);

		UnitOfMeasure oneOverCm = sys.createScalarUOM(UnitType.UNCLASSIFIED, null, "1/cm", null);
		oneOverCm.setConversion(Quantity.createAmount("100"), oneOverM);

		Quantity q1 = new Quantity(ten, litre);
		Quantity q2 = q1.convert(m3);
		assertThat(q2.getAmount(), closeTo(Quantity.createAmount("0.01"), DELTA6));
		assertTrue(q2.getUOM().equals(m3));

		q2 = q1.convert(litre);
		assertThat(q2.getAmount(), closeTo(ten, DELTA6));
		assertTrue(q2.getUOM().equals(litre));

		// add
		q1 = new Quantity("2", m);
		q2 = new Quantity("2", cm);
		Quantity q3 = q1.add(q2);

		assertThat(q3.getUOM().getScalingFactor(), closeTo(BigDecimal.ONE, DELTA6));
		assertTrue(q3.getUOM().getAbscissaUnit().equals(m));
		assertThat(q3.getUOM().getOffset(), closeTo(BigDecimal.ZERO, DELTA6));
		assertThat(q3.getAmount(), closeTo(Quantity.createAmount("2.02"), DELTA6));

		Quantity q4 = q3.convert(cm);
		assertThat(q4.getAmount(), closeTo(Quantity.createAmount("202"), DELTA6));
		assertTrue(q4.getUOM().equals(cm));

		// subtract
		q3 = q3.subtract(q1);
		assertThat(q3.getUOM().getScalingFactor(), closeTo(BigDecimal.ONE, DELTA6));
		assertTrue(q3.getUOM().getAbscissaUnit().equals(m));
		assertThat(q3.getAmount(), closeTo(Quantity.createAmount("0.02"), DELTA6));

		q4 = q3.convert(cm);
		assertThat(q4.getAmount(), closeTo(Quantity.createAmount("2"), DELTA6));
		assertTrue(q4.getUOM().equals(cm));

		// multiply
		q3 = q1.multiply(q2);
		assertThat(q3.getAmount(), closeTo(Quantity.createAmount("4"), DELTA6));
		UnitOfMeasure u = q3.getUOM();
		assertThat(u.getScalingFactor(), closeTo(Quantity.createAmount("0.01"), DELTA6));
		assertTrue(u.getBaseSymbol().equals(m2.getBaseSymbol()));

		q4 = q3.divide(q3);
		assertThat(q4.getAmount(), closeTo(BigDecimal.ONE, DELTA6));
		assertTrue(q4.getUOM().equals(sys.getOne()));

		q4 = q3.divide(q1);
		assertTrue(q4.equals(q2));

		q4 = q3.convert(m2);
		assertThat(q4.getAmount(), closeTo(Quantity.createAmount("0.04"), DELTA6));
		assertTrue(q4.getUOM().equals(m2));

		// divide
		q3 = q3.divide(q2);
		assertThat(q3.getAmount(), closeTo(Quantity.createAmount("2.0"), DELTA6));
		assertTrue(q3.getUOM().equals(m));
		assertTrue(q3.equals(q1));

		q3 = q3.convert(m);
		assertThat(q3.getAmount(), closeTo(Quantity.createAmount("2.0"), DELTA6));

		q1 = new Quantity(BigDecimal.ZERO, litre);

		try {
			q2 = q1.divide(q1);
			fail("divide by zero)");
		} catch (Exception e) {
		}

		q1 = q3.convert(cm).divide(ten);
		assertThat(q1.getAmount(), closeTo(Quantity.createAmount("20"), DELTA6));

		// invert
		q1 = new Quantity(BigDecimal.TEN, mps);
		q2 = q1.invert();
		assertThat(q2.getAmount(), closeTo(Quantity.createAmount("0.1"), DELTA6));
		assertTrue(q2.getUOM().equals(secPerM));

		q2 = q2.invert();
		assertThat(q2.getAmount(), closeTo(BigDecimal.TEN, DELTA6));
		assertTrue(q2.getUOM().equals(mps));

		q1 = new Quantity(BigDecimal.TEN, cm);
		q2 = q1.invert();
		assertThat(q2.getAmount(), closeTo(Quantity.createAmount("0.1"), DELTA6));
		u = q2.getUOM();
		assertTrue(u.equals(oneOverCm));

		q2 = q2.convert(m.invert());
		assertThat(q2.getAmount(), closeTo(BigDecimal.TEN, DELTA6));
		assertTrue(q2.getUOM().equals(oneOverM));

		assertNotNull(q2.toString());

		// Newton-metres divided by metres
		q1 = new Quantity(BigDecimal.TEN, sys.getUOM(Unit.NEWTON_METRE));
		q2 = new Quantity(BigDecimal.ONE, sys.getUOM(Unit.METRE));
		q3 = q1.divide(q2);
		assertThat(q3.getAmount(), closeTo(BigDecimal.TEN, DELTA6));
		assertTrue(q3.getUOM().equals(sys.getUOM(Unit.NEWTON)));

		// length multiplied by force
		q1 = new Quantity(BigDecimal.TEN, sys.getUOM(Unit.NEWTON));
		q2 = new Quantity(BigDecimal.ONE, sys.getUOM(Unit.METRE));
		q3 = q1.multiply(q2);
		assertThat(q3.getAmount(), closeTo(BigDecimal.TEN, DELTA6));
		UnitOfMeasure nm1 = q3.getUOM();
		UnitOfMeasure nm2 = sys.getUOM(Unit.NEWTON_METRE);
		assertTrue(nm1.getBaseSymbol().equals(nm2.getBaseSymbol()));
		q4 = q3.convert(sys.getUOM(Unit.JOULE));
		assertTrue(q4.getUOM().equals(sys.getUOM(Unit.JOULE)));

		// farads
		q1 = new Quantity(BigDecimal.TEN, fperm);
		q2 = new Quantity(BigDecimal.ONE, m);
		q3 = q1.multiply(q2);
		assertThat(q3.getAmount(), closeTo(BigDecimal.TEN, DELTA6));
		assertTrue(q3.getUOM().equals(sys.getUOM(Unit.FARAD)));

		// amps
		q1 = new Quantity(BigDecimal.TEN, sys.getUOM(Unit.AMPERE_PER_METRE));
		q2 = new Quantity(BigDecimal.ONE, m);
		q3 = q1.multiply(q2);
		assertThat(q3.getAmount(), closeTo(BigDecimal.TEN, DELTA6));
		assertTrue(q3.getUOM().equals(sys.getUOM(Unit.AMPERE)));

		// Boltzmann and Avogadro
		Quantity boltzmann = sys.getQuantity(Constant.BOLTZMANN_CONSTANT);
		Quantity avogadro = sys.getQuantity(Constant.AVAGADRO_CONSTANT);
		Quantity gas = sys.getQuantity(Constant.GAS_CONSTANT);
		Quantity qR = boltzmann.multiply(avogadro);
		assertThat(qR.getUOM().getScalingFactor(), closeTo(gas.getUOM().getScalingFactor(), DELTA6));

		// Sieverts
		q1 = new Quantity("20", sys.getUOM(Prefix.MILLI, Unit.SIEVERTS_PER_HOUR));
		q2 = new Quantity("24", sys.getHour());
		q3 = q1.multiply(q2);
		assertThat(q3.getAmount(), closeTo(Quantity.createAmount("480"), DELTA6));

	}

	@Test
	public void testPowers() throws Exception {

		UnitOfMeasure m2 = sys.getUOM(Unit.SQUARE_METRE);
		UnitOfMeasure p2 = sys.createPowerUOM(UnitType.AREA, "m2^1", "m2^1", "square metres raised to power 1", m2, 1);
		UnitOfMeasure p4 = sys.createPowerUOM(UnitType.UNCLASSIFIED, "m2^2", "m2^2", "square metres raised to power 2",
				m2, 2);

		BigDecimal amount = Quantity.createAmount("10");

		Quantity q1 = new Quantity(amount, m2);
		Quantity q3 = new Quantity(amount, p4);

		Quantity q4 = q3.divide(q1);
		assertThat(q4.getAmount(), closeTo(BigDecimal.ONE, DELTA6));
		assertTrue(q4.getUOM().getBaseUOM().equals(m2));

		Quantity q2 = q1.convert(p2);
		assertThat(q2.getAmount(), closeTo(amount, DELTA6));
		assertTrue(q2.getUOM().getBaseUOM().equals(m2));

		// power method
		UnitOfMeasure ft = sys.getUOM(Unit.FOOT);
		UnitOfMeasure ft2 = sys.getUOM(Unit.SQUARE_FOOT);
		q1 = new Quantity(BigDecimal.TEN, ft);

		q3 = q1.power(2);
		assertThat(q3.getAmount(), closeTo(Quantity.createAmount("100"), DELTA6));
		assertTrue(q3.getUOM().getBaseSymbol().equals(ft2.getBaseSymbol()));

		q4 = q3.convert(sys.getUOM(Unit.SQUARE_METRE));
		assertThat(q4.getAmount(), closeTo(Quantity.createAmount("9.290304"), DELTA6));

		q3 = q1.power(1);
		assertTrue(q3.getAmount().equals(q1.getAmount()));
		assertTrue(q3.getUOM().getBaseSymbol().equals(q1.getUOM().getBaseSymbol()));

		q3 = q1.power(0);
		assertTrue(q3.getAmount().equals(BigDecimal.ONE));
		assertTrue(q3.getUOM().getBaseSymbol().equals(sys.getOne().getBaseSymbol()));

		q3 = q1.power(-1);
		assertTrue(q3.getAmount().equals(Quantity.createAmount("0.1")));
		assertTrue(q3.getUOM().equals(ft.invert()));

		q3 = q1.power(-2);
		assertTrue(q3.getAmount().equals(Quantity.createAmount("0.01")));
		assertTrue(q3.getUOM().equals(ft2.invert()));
	}

	@Test
	public void testSIUnits() throws Exception {

		UnitOfMeasure newton = sys.getUOM(Unit.NEWTON);
		UnitOfMeasure metre = sys.getUOM(Unit.METRE);
		UnitOfMeasure m2 = sys.getUOM(Unit.SQUARE_METRE);
		UnitOfMeasure cm = sys.getUOM(Prefix.CENTI, metre);
		UnitOfMeasure mps = sys.getUOM(Unit.METRE_PER_SEC);
		UnitOfMeasure joule = sys.getUOM(Unit.JOULE);
		UnitOfMeasure m3 = sys.getUOM(Unit.CUBIC_METRE);
		UnitOfMeasure farad = sys.getUOM(Unit.FARAD);
		UnitOfMeasure nm = sys.getUOM(Unit.NEWTON_METRE);
		UnitOfMeasure coulomb = sys.getUOM(Unit.COULOMB);
		UnitOfMeasure volt = sys.getUOM(Unit.VOLT);
		UnitOfMeasure watt = sys.getUOM(Unit.WATT);
		UnitOfMeasure cm2 = sys.createProductUOM(UnitType.AREA, "square centimetres", "cm" + (char) 0xB2, "", cm, cm);
		UnitOfMeasure cv = sys.createProductUOM(UnitType.ENERGY, "CxV", "C·V", "Coulomb times Volt", coulomb, volt);
		UnitOfMeasure ws = sys.createProductUOM(UnitType.ENERGY, "Wxs", "W·s", "Watt times second", watt,
				sys.getSecond());
		UnitOfMeasure ft3 = sys.getUOM(Unit.CUBIC_FOOT);
		UnitOfMeasure hz = sys.getUOM(Unit.HERTZ);

		BigDecimal oneHundred = Quantity.createAmount("100");

		assertTrue(nm.getBaseSymbol().equals(joule.getBaseSymbol()));
		assertTrue(cv.getBaseSymbol().equals(joule.getBaseSymbol()));
		assertTrue(ws.getBaseSymbol().equals(joule.getBaseSymbol()));

		Quantity q1 = new Quantity(BigDecimal.TEN, newton);
		Quantity q2 = new Quantity(BigDecimal.TEN, metre);
		Quantity q3 = q1.multiply(q2);
		assertThat(q3.getAmount(), closeTo(oneHundred, DELTA6));
		assertTrue(q3.getUOM().getBaseSymbol().equals(nm.getBaseSymbol()));
		assertThat(q3.getUOM().getScalingFactor(), closeTo(BigDecimal.ONE, DELTA6));
		assertThat(q3.getUOM().getOffset(), closeTo(BigDecimal.ZERO, DELTA6));

		q3 = q3.convert(joule);
		assertThat(q3.getAmount(), closeTo(oneHundred, DELTA6));
		assertTrue(q3.getUOM().equals(joule));
		assertThat(q3.getUOM().getScalingFactor(), closeTo(BigDecimal.ONE, DELTA6));

		q3 = q3.convert(nm);
		assertThat(q3.getAmount(), closeTo(oneHundred, DELTA6));
		assertTrue(q3.getUOM().equals(nm));
		assertThat(q3.getUOM().getScalingFactor(), closeTo(BigDecimal.ONE, DELTA6));

		BigDecimal bd1 = Quantity.createAmount("10000");

		q1 = new Quantity(oneHundred, cm);
		q2 = q1.convert(metre);
		assertThat(q2.getAmount(), closeTo(BigDecimal.ONE, DELTA6));
		assertTrue(q2.getUOM().getEnumeration().equals(Unit.METRE));
		assertThat(q2.getUOM().getScalingFactor(), closeTo(BigDecimal.ONE, DELTA6));

		q2 = q2.convert(cm);
		assertThat(q2.getAmount(), closeTo(oneHundred, DELTA6));
		assertThat(q2.getUOM().getScalingFactor(), closeTo(Quantity.createAmount("0.01"), DELTA6));

		q2 = q1;
		q3 = q1.multiply(q2);
		assertThat(q3.getAmount(), closeTo(Quantity.createAmount("10000"), DELTA6));
		assertThat(q3.getUOM().getScalingFactor(), closeTo(Quantity.createAmount("0.0001"), DELTA6));
		assertThat(q3.getUOM().getOffset(), closeTo(BigDecimal.ZERO, DELTA6));

		Quantity q4 = q3.convert(m2);
		assertTrue(q4.getUOM().equals(m2));
		assertThat(q4.getAmount(), closeTo(BigDecimal.ONE, DELTA6));

		q3 = q3.convert(m2);
		assertThat(q3.getAmount(), closeTo(BigDecimal.ONE, DELTA6));
		assertTrue(q3.getUOM().equals(m2));
		assertThat(q3.getUOM().getScalingFactor(), closeTo(BigDecimal.ONE, DELTA6));

		q3 = q3.convert(cm2);
		assertThat(q3.getAmount(), closeTo(bd1, DELTA6));
		assertTrue(q3.getUOM().equals(cm2));
		assertThat(q3.getUOM().getScalingFactor(), closeTo(BigDecimal.ONE, DELTA6));

		// power
		Quantity onem3 = new Quantity(BigDecimal.ONE, m3);
		String cm3sym = "cm" + (char) 0xB3;
		UnitOfMeasure cm3 = sys.createPowerUOM(UnitType.VOLUME, cm3sym, cm3sym, null, cm, 3);
		Quantity megcm3 = new Quantity("1E+06", cm3);

		Quantity qft3 = onem3.convert(ft3);
		assertThat(qft3.getAmount(), closeTo(Quantity.createAmount("35.31466672148859"), DELTA6));

		Quantity qtym3 = qft3.convert(m3);
		assertThat(qtym3.getAmount(), closeTo(BigDecimal.ONE, DELTA6));

		Quantity qm3 = megcm3.convert(m3);
		assertThat(qm3.getAmount(), closeTo(BigDecimal.ONE, DELTA6));
		qm3 = qm3.convert(cm3);
		assertThat(qm3.getAmount(), closeTo(Quantity.createAmount("1E+06"), DELTA6));

		Quantity qcm3 = onem3.convert(cm3);
		assertThat(qcm3.getAmount(), closeTo(Quantity.createAmount("1E+06"), DELTA6));

		// inversions
		UnitOfMeasure u = metre.invert();
		String sym = u.getAbscissaUnit().getSymbol();
		assertTrue(sym.equals(sys.getUOM(Unit.DIOPTER).getSymbol()));

		u = mps.invert();
		assertTrue(u.getSymbol().equals("s/m"));

		UnitOfMeasure uom = sys.createQuotientUOM(UnitType.UNCLASSIFIED, "1/F", "1/F", "one over farad", sys.getOne(),
				farad);
		assertTrue(uom.getSymbol().equals("1/F"));

		// hz to radians per sec
		q1 = new Quantity(BigDecimal.TEN, sys.getUOM(Unit.HERTZ));
		q2 = q1.convert(sys.getUOM(Unit.RAD_PER_SEC));
		BigDecimal twentyPi = new BigDecimal("20").multiply(new BigDecimal(Math.PI), UnitOfMeasure.MATH_CONTEXT);
		assertThat(q2.getAmount(), closeTo(twentyPi, DELTA6));

		q3 = q2.convert(sys.getUOM(Unit.HERTZ));
		assertThat(q3.getAmount(), closeTo(BigDecimal.TEN, DELTA6));

		// rpm to radians per second
		q1 = new Quantity(BigDecimal.TEN, sys.getUOM(Unit.REV_PER_MIN));
		q2 = q1.convert(sys.getUOM(Unit.RAD_PER_SEC));
		assertThat(q2.getAmount(), closeTo(Quantity.createAmount("1.04719755119"), DELTA6));

		q3 = q2.convert(sys.getUOM(Unit.REV_PER_MIN));
		assertThat(q3.getAmount(), closeTo(BigDecimal.TEN, DELTA6));

		q1 = new Quantity(BigDecimal.TEN, hz);
		q2 = new Quantity(BigDecimal.ONE, sys.getMinute());
		q3 = q1.multiply(q2).convert(sys.getOne());
		assertThat(q3.getAmount(), closeTo(Quantity.createAmount("600"), DELTA6));

		q1 = new Quantity(BigDecimal.ONE, sys.getUOM(Unit.ELECTRON_VOLT));
		q2 = q1.convert(sys.getUOM(Unit.JOULE));
		assertThat(q2.getAmount(), closeTo(Quantity.createAmount("1.60217656535E-19"), DELTA6));

	}

	@Test
	public void testEquations() throws Exception {

		// body mass index
		Quantity height = new Quantity("2", sys.getUOM(Unit.METRE));
		Quantity mass = new Quantity("100", sys.getUOM(Unit.KILOGRAM));
		Quantity bmi = mass.divide(height.multiply(height));
		assertThat(bmi.getAmount(), closeTo(Quantity.createAmount("25"), DELTA6));

		// E = mc^2
		Quantity c = sys.getQuantity(Constant.LIGHT_VELOCITY);
		Quantity m = new Quantity(BigDecimal.ONE, sys.getUOM(Unit.KILOGRAM));
		Quantity e = m.multiply(c).multiply(c);
		assertThat(e.getAmount(),
				closeTo(new BigDecimal("8.987551787368176E+16", UnitOfMeasure.MATH_CONTEXT), BigDecimal.ONE));

		// Ideal Gas Law, PV = nRT
		// A cylinder of argon gas contains 50.0 L of Ar at 18.4 atm and 127 °C.
		// How many moles of argon are in the cylinder?
		Quantity p = new Quantity("18.4", sys.getUOM(Unit.ATMOSPHERE)).convert(Unit.PASCAL);
		Quantity v = new Quantity("50", Unit.LITRE).convert(Unit.CUBIC_METRE);
		Quantity t = new Quantity("127", Unit.CELSIUS).convert(Unit.KELVIN);
		Quantity n = p.multiply(v).divide(sys.getQuantity(Constant.GAS_CONSTANT).multiply(t));
		assertThat(n.getAmount(), closeTo(Quantity.createAmount("28.018673"), DELTA6));

		// energy of red light photon = Planck's constant times the frequency
		Quantity frequency = new Quantity("400", sys.getUOM(Prefix.TERA, Unit.HERTZ));
		Quantity ev = sys.getQuantity(Constant.PLANCK_CONSTANT).multiply(frequency).convert(Unit.ELECTRON_VOLT);
		assertThat(ev.getAmount(), closeTo(Quantity.createAmount("1.65"), DELTA2));

		// wavelength of red light in nanometres
		Quantity wavelength = sys.getQuantity(Constant.LIGHT_VELOCITY).divide(frequency)
				.convert(sys.getUOM(Prefix.NANO, Unit.METRE));
		assertThat(wavelength.getAmount(), closeTo(Quantity.createAmount("749.48"), DELTA2));

		// Newton's second law of motion (F = ma). Weight of 1 kg in lbf
		Quantity mkg = new Quantity(BigDecimal.ONE, Unit.KILOGRAM);
		Quantity f = mkg.multiply(sys.getQuantity(Constant.GRAVITY)).convert(Unit.POUND_FORCE);
		assertThat(f.getAmount(), closeTo(Quantity.createAmount("2.20462"), DELTA5));

		// units per volume of solution, C = A x (m/V)
		// create the "A" unit of measure
		UnitOfMeasure activityUnit = sys.createQuotientUOM(UnitType.UNCLASSIFIED, "activity", "act",
				"activity of material", sys.getUOM(Unit.UNIT), sys.getUOM(Prefix.MILLI, Unit.GRAM));

		// calculate concentration
		Quantity activity = new Quantity(BigDecimal.ONE, activityUnit);
		Quantity grams = new Quantity(BigDecimal.ONE, Unit.GRAM).convert(Prefix.MILLI, Unit.GRAM);
		Quantity volume = new Quantity(BigDecimal.ONE, sys.getUOM(Prefix.MILLI, Unit.LITRE));
		Quantity concentration = activity.multiply(grams.divide(volume));
		assertThat(concentration.getAmount(), closeTo(Quantity.createAmount("1000"), DELTA6));

		Quantity katals = concentration.multiply(new Quantity(BigDecimal.ONE, Unit.LITRE)).convert(Unit.KATAL);
		assertThat(katals.getAmount(), closeTo(Quantity.createAmount("0.01666667"), DELTA6));

		// The Stefan–Boltzmann law states that the power emitted per unit area
		// of the surface of a black body is directly proportional to the fourth
		// power of its absolute temperature: sigma * T^4

		// calculate at 1000 Kelvin
		Quantity temp = new Quantity("1000", Unit.KELVIN);
		Quantity intensity = sys.getQuantity(Constant.STEFAN_BOLTZMANN).multiply(temp.power(4));
		assertThat(intensity.getAmount(), closeTo(Quantity.createAmount("56703.67"), DELTA2));

		// Hubble's law, v = H0 x D. Let D = 10 Mpc
		Quantity d = new Quantity(BigDecimal.TEN, sys.getUOM(Prefix.MEGA, sys.getUOM(Unit.PARSEC)));
		Quantity h0 = sys.getQuantity(Constant.HUBBLE_CONSTANT);
		Quantity velocity = h0.multiply(d);
		assertThat(velocity.getAmount(), closeTo(Quantity.createAmount("719"), DELTA3));

		// Arrhenius equation
		// A device has an activation energy of 0.5 and a characteristic life of
		// 2,750 hours at an accelerated temperature of 150 degrees Celsius.
		// Calculate the characteristic life at an expected use temperature of
		// 85 degrees Celsius.

		// Convert the Boltzman constant from J/K to eV/K for the Arrhenius
		// equation
		// eV per Joule
		Quantity j = new Quantity(BigDecimal.ONE, Unit.JOULE);
		Quantity eV = j.convert(Unit.ELECTRON_VOLT);
		// Boltzmann constant
		Quantity Kb = sys.getQuantity(Constant.BOLTZMANN_CONSTANT).multiply(eV.getAmount());
		// accelerated temperature
		Quantity Ta = new Quantity("150", Unit.CELSIUS);
		// expected use temperature
		Quantity Tu = new Quantity("85", Unit.CELSIUS);
		// calculate the acceleration factor
		Quantity factor1 = Tu.convert(Unit.KELVIN).invert().subtract(Ta.convert(Unit.KELVIN).invert());
		Quantity factor2 = Kb.invert().multiply(Quantity.createAmount("0.5"));
		Quantity factor3 = factor1.multiply(factor2);
		double AF = Math.exp(factor3.getAmount().doubleValue());
		// calculate longer life at expected use temperature
		Quantity life85 = new Quantity("2750", Unit.HOUR);
		Quantity life150 = life85.multiply(new BigDecimal(AF));
		assertThat(life150.getAmount(), closeTo(Quantity.createAmount("33121.4"), DELTA1));
	}

	@Test
	public void testPackaging() throws Exception {

		BigDecimal one = Quantity.createAmount("1");
		BigDecimal four = Quantity.createAmount("4");
		BigDecimal six = Quantity.createAmount("6");
		BigDecimal ten = Quantity.createAmount("10");
		BigDecimal forty = Quantity.createAmount("40");

		UnitOfMeasure one16ozCan = sys.createScalarUOM(UnitType.VOLUME, "16 oz can", "16ozCan", "16 oz can");
		one16ozCan.setConversion(Quantity.createAmount("16"), sys.getUOM(Unit.US_FLUID_OUNCE));

		Quantity q400 = new Quantity("400", one16ozCan);
		Quantity q50 = q400.convert(sys.getUOM(Unit.US_GALLON));
		assertThat(q50.getAmount(), closeTo(Quantity.createAmount("50"), DELTA6));

		// 1 12 oz can = 12 fl.oz.
		UnitOfMeasure one12ozCan = sys.createScalarUOM(UnitType.VOLUME, "12 oz can", "12ozCan", "12 oz can");
		one12ozCan.setConversion(Quantity.createAmount("12"), sys.getUOM(Unit.US_FLUID_OUNCE));

		Quantity q48 = new Quantity("48", one12ozCan);
		Quantity q36 = q48.convert(one16ozCan);
		assertThat(q36.getAmount(), closeTo(Quantity.createAmount("36"), DELTA6));

		// 6 12 oz cans = 1 6-pack of 12 oz cans
		UnitOfMeasure sixPackCan = sys.createScalarUOM(UnitType.VOLUME, "6-pack", "6PCan", "6-pack of 12 oz cans");
		sixPackCan.setConversion(six, one12ozCan);

		UnitOfMeasure fourPackCase = sys.createScalarUOM(UnitType.VOLUME, "4 pack case", "4PCase", "case of 4 6-packs");
		fourPackCase.setConversion(four, sixPackCan);

		BigDecimal bd = fourPackCase.getConversionFactor(one12ozCan);
		assertThat(bd, closeTo(Quantity.createAmount("24"), DELTA6));

		bd = one12ozCan.getConversionFactor(fourPackCase);

		bd = fourPackCase.getConversionFactor(sixPackCan);
		bd = sixPackCan.getConversionFactor(fourPackCase);

		bd = sixPackCan.getConversionFactor(one12ozCan);
		bd = one12ozCan.getConversionFactor(sixPackCan);

		Quantity tenCases = new Quantity(ten, fourPackCase);

		Quantity q1 = tenCases.convert(one12ozCan);
		assertThat(q1.getAmount(), closeTo(Quantity.createAmount("240"), DELTA6));

		Quantity q2 = q1.convert(fourPackCase);
		assertThat(q2.getAmount(), closeTo(Quantity.createAmount("10"), DELTA6));

		Quantity fortyPacks = new Quantity(forty, sixPackCan);
		q2 = fortyPacks.convert(one12ozCan);
		assertThat(q2.getAmount(), closeTo(Quantity.createAmount("240"), DELTA6));

		Quantity oneCan = new Quantity(one, one12ozCan);
		q2 = oneCan.convert(sixPackCan);
		assertThat(q2.getAmount(), closeTo(Quantity.createAmount("0.1666666666666667"), DELTA6));

		// A beer bottling line is rated at 2000 12 ounce cans/hour (US) at the
		// filler. The case packer packs four 6-packs of cans into a case.
		// Assuming no losses, what should be the rating of the case packer in
		// cases per hour? And, what is the draw-down rate on the holding tank
		// in gallons/minute?
		UnitOfMeasure canph = sys.createQuotientUOM(one12ozCan, sys.getHour());
		UnitOfMeasure caseph = sys.createQuotientUOM(fourPackCase, sys.getHour());
		UnitOfMeasure gpm = sys.createQuotientUOM(sys.getUOM(Unit.US_GALLON), sys.getMinute());
		Quantity filler = new Quantity("2000", canph);

		// draw-down
		Quantity draw = filler.convert(gpm);
		assertThat(draw.getAmount(), closeTo(Quantity.createAmount("3.125"), DELTA6));

		// case production
		Quantity packer = filler.convert(caseph);
		assertThat(packer.getAmount(), closeTo(Quantity.createAmount("83.333333"), DELTA6));
	}

	@Test
	public void testGenericQuantity() throws Exception {

		UnitOfMeasure a = sys.createScalarUOM(UnitType.UNCLASSIFIED, "a", "aUOM", "A");

		UnitOfMeasure b = sys.createScalarUOM(UnitType.UNCLASSIFIED, "b", "b", "B");
		b.setConversion(BigDecimal.TEN, a);

		BigDecimal four = Quantity.multiplyAmounts("2", "2");

		BigDecimal bd = Quantity.createAmount(new BigDecimal("4"));
		assertTrue(bd.equals(four));

		bd = Quantity.createAmount(new BigInteger("4"));
		assertTrue(bd.equals(four));

		bd = Quantity.createAmount(new Double(4.0d));
		assertTrue(bd.equals(four));

		bd = Quantity.createAmount(new Float(4.0f));
		assertTrue(bd.equals(four));

		bd = Quantity.createAmount(new Long(4l));
		assertTrue(bd.equals(four));

		bd = Quantity.createAmount(new Integer(4));
		assertTrue(bd.equals(four));

		bd = Quantity.createAmount(new Short((short) 4));
		assertTrue(bd.equals(four));

		// add
		Quantity q1 = new Quantity(four, a);

		assertFalse(q1.equals(null));

		Quantity q2 = new Quantity(four, b);
		Quantity q3 = q1.add(q2);

		assertThat(q3.getUOM().getScalingFactor(), closeTo(BigDecimal.ONE, DELTA6));
		assertTrue(q3.getUOM().getAbscissaUnit().equals(a));
		assertThat(q3.getUOM().getOffset(), closeTo(BigDecimal.ZERO, DELTA6));
		assertThat(q3.getAmount(), closeTo(Quantity.createAmount("44"), DELTA6));

		// subtract
		q3 = q1.subtract(q2);
		assertThat(q3.getUOM().getScalingFactor(), closeTo(BigDecimal.ONE, DELTA6));
		assertTrue(q3.getUOM().getAbscissaUnit().equals(a));
		assertThat(q3.getUOM().getOffset(), closeTo(BigDecimal.ZERO, DELTA6));
		assertThat(q3.getAmount(), closeTo(Quantity.createAmount("-36"), DELTA6));

		// multiply
		q3 = q1.multiply(q2);
		assertThat(q3.getAmount(), closeTo(Quantity.createAmount("16"), DELTA6));
		assertThat(q3.getUOM().getScalingFactor(), closeTo(BigDecimal.ONE, DELTA6));
		assertThat(q3.getUOM().getOffset(), closeTo(BigDecimal.ZERO, DELTA6));

		UnitOfMeasure a2 = sys.createPowerUOM(UnitType.UNCLASSIFIED, "a*2", "a*2", "A squared", a, 2);
		Quantity q4 = q3.convert(a2);
		assertThat(q4.getAmount(), closeTo(Quantity.createAmount("160"), DELTA6));
		assertTrue(q4.getUOM().equals(a2));

		q4 = q3.divide(q2);
		assertTrue(q4.equals(q1));
		assertThat(q4.getAmount(), closeTo(Quantity.createAmount("4"), DELTA6));

		// divide
		q3 = q1.divide(q2);
		assertThat(q3.getAmount(), closeTo(BigDecimal.ONE, DELTA6));
		assertThat(q3.getUOM().getScalingFactor(), closeTo(Quantity.createAmount("0.1"), DELTA6));

		q4 = q3.multiply(q2);
		assertTrue(q4.equals(q1));
	}

	@Test
	public void testExceptions() throws Exception {

		UnitOfMeasure floz = sys.getUOM(Unit.BR_FLUID_OUNCE);

		Quantity q1 = new Quantity(BigDecimal.TEN, sys.getDay());
		Quantity q2 = new Quantity(BigDecimal.TEN, sys.getUOM(Unit.BR_FLUID_OUNCE));

		try {
			String amount = null;
			Quantity.createAmount(amount);
			fail("create");
		} catch (Exception e) {
		}

		try {
			q1.convert(floz);
			fail("convert");
		} catch (Exception e) {
		}

		try {
			q1.add(q2);
			fail("add");
		} catch (Exception e) {
		}

		try {
			q1.subtract(q2);
			fail("subtract");
		} catch (Exception e) {
		}

		// OK
		q1.multiply(q2);

		// OK
		q1.divide(q2);
	}

	@Test
	public void testEquality() throws Exception {

		UnitOfMeasure newton = sys.getUOM(Unit.NEWTON);
		UnitOfMeasure metre = sys.getUOM(Unit.METRE);
		UnitOfMeasure nm = sys.getUOM(Unit.NEWTON_METRE);
		UnitOfMeasure m2 = sys.getUOM(Unit.SQUARE_METRE);
		UnitOfMeasure J = sys.getUOM(Unit.JOULE);
		BigDecimal amount = Quantity.createAmount("10");

		final Quantity q1 = new Quantity(amount, newton);
		final Quantity q2 = new Quantity(amount, metre);
		final Quantity q3 = new Quantity(amount, nm);
		Quantity q5 = new Quantity(Quantity.createAmount("100"), nm);

		// unity
		Quantity q4 = q5.divide(q3);
		assertTrue(q4.getUOM().getBaseSymbol().equals(sys.getOne().getSymbol()));
		assertTrue(q4.getAmount().equals(amount));

		// Newton-metre (Joules)
		q4 = q1.multiply(q2);
		assertTrue(q5.getUOM().getBaseSymbol().equals(q4.getUOM().getBaseSymbol()));
		Quantity q6 = q5.convert(J);
		assertTrue(q6.getAmount().equals(q4.getAmount()));

		// Newton
		q5 = q4.divide(q2);
		assertTrue(q5.getUOM().getBaseSymbol().equals(q1.getUOM().getBaseSymbol()));
		assertTrue(q5.equals(q1));

		// metre
		q5 = q4.divide(q1);
		assertTrue(q5.getUOM().getBaseSymbol().equals(q2.getUOM().getBaseSymbol()));
		assertTrue(q5.equals(q2));

		// square metre
		q4 = q2.multiply(q2);
		q5 = new Quantity(Quantity.createAmount("100"), m2);
		assertTrue(q4.getUOM().getSymbol().equals(q5.getUOM().getSymbol()));
		assertTrue(q5.equals(q4));

		// metre
		q4 = q5.divide(q2);
		assertTrue(q4.getUOM().getBaseSymbol().equals(q2.getUOM().getBaseSymbol()));
		assertTrue(q4.equals(q2));

	}

	@Test
	public void testComparison() throws Exception {

		UnitOfMeasure newton = sys.getUOM(Unit.NEWTON);
		UnitOfMeasure metre = sys.getUOM(Unit.METRE);
		UnitOfMeasure cm = sys.getUOM(Prefix.CENTI, metre);

		BigDecimal amount = Quantity.createAmount("10");

		final Quantity qN = new Quantity(amount, newton);
		final Quantity qm10 = new Quantity(amount, metre);
		final Quantity qm1 = new Quantity(BigDecimal.ONE, metre);
		final Quantity qcm = new Quantity(amount, cm);

		assertTrue(qN.compare(qN) == 0);
		assertTrue(qm10.compare(qm1) == 1);
		assertTrue(qm1.compare(qm10) == -1);
		assertTrue(qcm.compare(qm1) == -1);
		assertTrue(qm1.compare(qcm) == 1);

		try {
			qN.compare(qm10);
			fail("not comparable)");
		} catch (Exception e) {
		}

		Quantity acidpH = new Quantity("4.5", sys.getUOM(Unit.PH));
		Quantity neutralpH = new Quantity("7.0", sys.getUOM(Unit.PH));
		assertTrue(acidpH.compare(neutralpH) == -1);
	}

	@Test
	public void testArithmetic() throws Exception {

		UnitOfMeasure in = sys.getUOM(Unit.INCH);
		UnitOfMeasure cm = sys.getUOM(Prefix.CENTI, sys.getUOM(Unit.METRE));
		Quantity qcm = new Quantity("1", cm);
		Quantity qin = new Quantity("1", in);
		BigDecimal bd = Quantity.createAmount("2.54");
		Quantity q1 = qcm.multiply(bd).convert(in);
		assertTrue(q1.equals(qin));
		Quantity q2 = q1.convert(cm);
		assertThat(q2.getAmount(), closeTo(bd, DELTA6));

	}

	@Test
	public void testFinancial() throws Exception {
		Quantity q1 = new Quantity("10", Unit.US_DOLLAR);
		Quantity q2 = new Quantity("12", Unit.US_DOLLAR);
		Quantity q3 = q2.subtract(q1).divide(q1).convert(Unit.PERCENT);
		assertThat(q3.getAmount(), closeTo(Quantity.createAmount("20"), DELTA6));
	}

	@Test
	public void testPowerProductConversions() throws Exception {

		UnitOfMeasure one = sys.getOne();
		UnitOfMeasure mps = sys.getUOM(Unit.METRE_PER_SEC);
		UnitOfMeasure fps = sys.getUOM(Unit.FEET_PER_SEC);
		UnitOfMeasure nm = sys.getUOM(Unit.NEWTON_METRE);
		UnitOfMeasure ft = sys.getUOM(Unit.FOOT);
		UnitOfMeasure in = sys.getUOM(Unit.INCH);
		UnitOfMeasure mi = sys.getUOM(Unit.MILE);
		UnitOfMeasure hr = sys.getUOM(Unit.HOUR);
		UnitOfMeasure m = sys.getUOM(Unit.METRE);
		UnitOfMeasure s = sys.getUOM(Unit.SECOND);
		UnitOfMeasure n = sys.getUOM(Unit.NEWTON);
		UnitOfMeasure lbf = sys.getUOM(Unit.POUND_FORCE);
		UnitOfMeasure m2 = sys.getUOM(Unit.SQUARE_METRE);
		UnitOfMeasure m3 = sys.getUOM(Unit.CUBIC_METRE);
		UnitOfMeasure ft2 = sys.getUOM(Unit.SQUARE_FOOT);

		// test products and quotients
		sys.unregisterUnit(sys.getUOM(Unit.FOOT_POUND_FORCE));
		Quantity nmQ = new Quantity("1", nm);
		Quantity lbfinQ = nmQ.convertToPowerProduct(lbf, in);
		assertThat(lbfinQ.getAmount(), closeTo(Quantity.createAmount("8.850745791327183"), DELTA6));
		Quantity mpsQ = new Quantity("1", mps);

		Quantity fphQ = mpsQ.convertToPowerProduct(ft, hr);
		assertThat(fphQ.getAmount(), closeTo(Quantity.createAmount("11811.02362204724"), DELTA6));

		Quantity mps2Q = fphQ.convertToPowerProduct(m, s);
		assertThat(mps2Q.getAmount(), closeTo(BigDecimal.ONE, DELTA6));

		Quantity mps3Q = mpsQ.convertToPowerProduct(m, s);
		assertThat(mps3Q.getAmount(), closeTo(BigDecimal.ONE, DELTA6));

		Quantity inlbfQ = nmQ.convertToPowerProduct(in, lbf);
		assertThat(inlbfQ.getAmount(), closeTo(Quantity.createAmount("8.850745791327183"), DELTA6));

		Quantity nm2Q = lbfinQ.convertToPowerProduct(n, m);
		assertThat(nm2Q.getAmount(), closeTo(BigDecimal.ONE, DELTA6));

		nm2Q = lbfinQ.convertToPowerProduct(m, n);
		assertThat(nm2Q.getAmount(), closeTo(BigDecimal.ONE, DELTA6));

		Quantity mQ = new Quantity("1", m);
		try {
			mQ.convertToPowerProduct(ft, hr);
			fail();
		} catch (Exception e) {
		}

		sys.unregisterUnit(sys.getUOM(Unit.SQUARE_FOOT));
		Quantity m2Q = new Quantity("1", m2);
		Quantity ft2Q = m2Q.convertToPowerProduct(ft, ft);
		assertThat(ft2Q.getAmount(), closeTo(Quantity.createAmount("10.76391041670972"), DELTA6));

		Quantity mmQ = ft2Q.convertToPowerProduct(m, m);
		assertThat(mmQ.getAmount(), closeTo(BigDecimal.ONE, DELTA6));

		try {
			m2Q.convertToPowerProduct(m, one);
			fail();
		} catch (Exception e) {
		}

		sys.unregisterUnit(sys.getUOM(Unit.CUBIC_FOOT));
		Quantity m3Q = new Quantity("1", m3);
		Quantity ft3Q = m3Q.convertToPowerProduct(ft2, ft);
		assertThat(ft3Q.getAmount(), closeTo(Quantity.createAmount("35.31466672148858"), DELTA6));

		Quantity m3Q2 = m3Q.convertToPowerProduct(m2, m);
		assertThat(m3Q2.getAmount(), closeTo(BigDecimal.ONE, DELTA6));

		ft3Q = m3Q.convertToPowerProduct(ft, ft2);
		assertThat(ft3Q.getAmount(), closeTo(Quantity.createAmount("35.31466672148858"), DELTA6));

		UnitOfMeasure perM = sys.getUOM(Unit.DIOPTER);
		Quantity perMQ = new Quantity(BigDecimal.ONE, perM);
		Quantity perInQ = perMQ.convertToPowerProduct(one, in);
		assertThat(perInQ.getAmount(), closeTo(Quantity.createAmount("0.0254"), DELTA6));

		try {
			perMQ.convertToPowerProduct(in, one);
			fail();
		} catch (Exception e) {
		}

		try {
			perMQ.convertToPowerProduct(in, in);
			fail();
		} catch (Exception e) {
		}

		Quantity fpsQ = new Quantity(BigDecimal.ONE, fps);
		Quantity mphQ = fpsQ.convertToPowerProduct(mi, hr);
		assertThat(mphQ.getAmount(), closeTo(Quantity.createAmount("0.6818181818181818"), DELTA6));

		// test powers
		Quantity in2Q = m2Q.convertToPower(in);
		assertThat(in2Q.getAmount(), closeTo(Quantity.createAmount("1550.003100006200"), DELTA6));

		Quantity m2Q2 = in2Q.convertToPower(m);
		assertThat(m2Q2.getAmount(), closeTo(BigDecimal.ONE, DELTA6));

		Quantity perInQ2 = perMQ.convertToPower(in);
		assertThat(perInQ2.getAmount(), closeTo(Quantity.createAmount("0.0254"), DELTA6));

		Quantity q1 = perInQ2.convertToPower(m);
		assertThat(q1.getAmount(), closeTo(BigDecimal.ONE, DELTA6));

		Quantity inQ2 = mQ.convertToPower(in);
		assertThat(inQ2.getAmount(), closeTo(Quantity.createAmount("39.37007874015748"), DELTA6));

		q1 = inQ2.convertToPower(m);
		assertThat(q1.getAmount(), closeTo(BigDecimal.ONE, DELTA6));

		Quantity one1 = new Quantity(BigDecimal.ONE, sys.getOne());
		q1 = one1.convertToPower(sys.getOne());
		assertThat(q1.getAmount(), closeTo(BigDecimal.ONE, DELTA6));
	}
}
