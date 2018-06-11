/**
 * MathTools -- A collection of useful math utility routines.
 * 
 * Copyright (C) 1999-2015, Joseph A. Huwaldt. All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place -
 * Suite 330, Boston, MA 02111-1307, USA. Or visit: http://www.gnu.org/licenses/lgpl.html
 */
package jahuwaldt.tools.math;

import java.util.BitSet;
import java.math.BigInteger;

/**
 * A collection of useful static routines of a general mathematical nature. This file
 * includes functions that accomplish all types of wondrous mathematical stuff.
 * 
 * <p> Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt, Date: September 29, 1997
 * @version September 26, 2015
 */
public final class MathTools {

    /**
     * The natural logarithm of 10.
     */
    public static final double LOG10 = Math.log(10);

    /**
     * The natural logarithm of 2.
     */
    public static final double LOG2 = Math.log(2);

    /**
     * The natural logarithm of the maximum double value: log(MAX_VALUE).
     */
    public static final double MAX_LOG = Math.log(Double.MAX_VALUE);

    /**
     * The natural logarithm of the minimum double value: log(MIN_VALUE).
     */
    public static final double MIN_LOG = Math.log(Double.MIN_VALUE);

    /**
     * The machine epsilon (macheps) or unit roundoff for <code>double</code> in the Java
     * environment. Machine epsilon gives an upper bound on the relative error due to
     * rounding in floating point arithmetic. Machine epsilon is the smallest number such
     * that (1.0 + EPS != 1.0).
     */
    public static final double EPS = epsilon(1.0);

    /**
     * Square-root of the machine epsilon for <code>double</code>.
     */
    public static final double SQRT_EPS = Math.sqrt(EPS);

    /**
     * The machine epsilon (macheps) or unit roundoff for <code>float</code> in the Java
     * environment. Machine epsilon gives an upper bound on the relative error due to
     * rounding in floating point arithmetic. Machine epsilon is the smallest number such
     * that (1F + EPSF != 1F).
     */
    public static final float EPSF = epsilon(1F);

    /**
     * Square-root of the machine epsilon for <code>float</code>.
     */
    public static final float SQRT_EPSF = (float)Math.sqrt(EPSF);

    /**
     * Prevent anyone from instantiating this utility class.
     */
    private MathTools() { }

    //-----------------------------------------------------------------------------------
    /**
     * Test to see if a given long integer is even.
     *
     * @param n Integer number to be tested.
     * @return True if the number is even, false if it is odd.
     */
    public static boolean even(long n) {
        return (n & 1) == 0;
    }

    /**
     * Test to see if a given long integer is odd.
     *
     * @param n Integer number to be tested.
     * @return True if the number is odd, false if it is even.
     */
    public static boolean odd(long n) {
        return (n & 1) != 0;
    }

    /**
     * Calculates the square (x^2) of the argument.
     *
     * @param x Argument to be squared.
     * @return Returns the square (x^2) of the argument.
     */
    public static double sqr(double x) {
        if (x == 0.)
            return 0.;
        else
            return x * x;
    }

    /**
     * Computes the cube root of the specified real number. If the argument is negative,
     * then the cube root is negative.
     *
     * @param x Argument for which the cube root is to be found.
     * @return The cube root of the argument is returned.
     * @see Math#cbrt(double)
     * @deprecated 
     */
    public static double cubeRoot(double x) {
//      double value;
//      
//      if ( x < 0. )
//          value = -Math.exp( Math.log(-x) / 3. );
//      else
//          value = Math.exp( Math.log( x ) / 3. );
//          
//      return value;
        return Math.cbrt(x);    //  Added with Java 1.5.
    }

    /**
     * Returns a number "a" raised to the power "b". A "long" version of Math.pow(). This
     * is much faster than using Math.pow() if the operands are integers.
     *
     * @param a Number to be raised to the power "b".
     * @param b Power to raise number "a" to.
     * @return A long integer "a" raised to the integer power "b".
     * @throws ArithmeticException if "b" is negative.
     */
    public static long pow(long a, long b) throws ArithmeticException {
        if (b < 0)
            throw new ArithmeticException("Exponent must be positive.");

        long r = 1;
        while (b != 0) {
            if (odd(b))
                r *= a;

            b >>>= 1;
            a *= a;
        }
        return r;
    }

    /**
     * Raises 2 to the small integer power indicated (eg: 2^3 = 8). This is MUCH faster
     * than calling Math.pow(2, x).
     *
     * @param x Amount to raise 2 to the power of.
     * @return Returns 2 raised to the power indicated.
     */
    public static long pow2(long x) {
        long value = 1;
        for (long i = 0; i < x; ++i)
            value *= 2;
        return value;
    }

    /**
     * Raises 10 to the small integer power indicated (eg: 10^5 = 100000). This is faster
     * than calling Math.pow(10, x).
     *
     * @param x Amount to raise 10 to the power of.
     * @return Returns 10 raised to the power indicated.
     */
    public static double pow10(int x) {
        double pow10 = 10.;

        if (x != 0) {
            boolean neg = false;
            if (x < 0) {
                x *= -1;
                neg = true;
            }

            for (int i = 1; i < x; ++i)
                pow10 *= 10.;

            if (neg)
                pow10 = 1. / pow10;

        } else
            pow10 = 1.;

        return (pow10);
    }

    /**
     * Find the base 10 logarithm of the given double.
     *
     * @param x Value to find the base 10 logarithm of.
     * @return The base 10 logarithm of x.
     * @see Math#log10(double) 
     * @deprecated 
     */
    public static double log10(double x) {
        //return Math.log(x)/LOG10;
        return Math.log10(x);   //  Added with Java 1.5.
    }

    /**
     * Find the base 2 logarithm of the given double.
     *
     * @param x Value to find the base 2 logarithm of.
     * @return The base 2 logarithm of x.
     */
    public static double log2(double x) {
        return Math.log(x) / LOG2;
    }

    /**
     * Rounds a floating point number to the desired decimal place. Example: 1346.4667
     * rounded to the 2nd place = 1300.
     *
     * @param value The value to be rounded.
     * @param place Number of decimal places to round value to. A place of 1 rounds to
     *              10's place, 2 to 100's place, -2 to 1/100th place, et cetera.
     * @return A floating point number rounded to the desired decimal place.
     * @see #roundToSigFig(double, int) 
     * @see #roundDownToPlace(double, int) 
     * @see #roundUpToPlace(double, int) 
     */
    public static double roundToPlace(double value, int place) {

        //  If the value is zero, just pass the number back out.
        if (value != 0.) {

            //  If the place is zero, round to the one's place.
            if (place == 0)
                value = Math.floor(value + 0.5);

            else {
                double pow10 = MathTools.pow10(place);  //  = 10 ^ place
                double holdvalue = value / pow10;

                value = Math.floor(holdvalue + 0.5);        // Round number to nearest integer
                value *= pow10;
            }
        }

        return value;
    }

    /**
     * Rounds a floating point number up to the desired decimal place. Example: 1346.4667
     * rounded up to the 2nd place = 1400.
     *
     * @param value The value to be rounded up.
     * @param place Number of decimal places to round value to. A place of 1 rounds to
     *              10's place, 2 to 100's place, -2 to 1/100th place, et cetera.
     * @return A floating point number rounded up to the desired decimal place.
     * @see #roundToPlace(double, int) 
     * @see #roundDownToPlace(double, int) 
     */
    public static double roundUpToPlace(double value, int place) {

        //  If the value is zero, just pass the number back out.
        if (value != 0.) {

            //  If the place is zero, round to the one's place.
            if (place == 0)
                value = Math.ceil(value);

            else {
                double pow10 = MathTools.pow10(place);  //  = 10 ^ place
                double holdvalue = value / pow10;

                value = Math.ceil(holdvalue);           // Round number up to nearest integer
                value *= pow10;
            }
        }

        return value;
    }

    /**
     * Rounds a floating point number down to the desired decimal place. Example:
     * 1346.4667 rounded down to the 1st place = 1340.
     *
     * @param value The value to be rounded down.
     * @param place Number of decimal places to round value to. A place of 1 rounds to
     *              10's place, 2 to 100's place, -2 to 1/100th place, et cetera.
     * @return A floating point number rounded down to the desired decimal place.
     * @see #roundToPlace(double, int) 
     * @see #roundUpToPlace(double, int) 
     */
    public static double roundDownToPlace(double value, int place) {

        //  If the value is zero, just pass the number back out.
        if (value != 0.) {

            //  If the place is zero, round to the one's place.
            if (place == 0)
                value = Math.floor(value);

            else {
                double pow10 = MathTools.pow10(place);  //  = 10 ^ place
                double holdvalue = value / pow10;

                value = Math.floor(holdvalue);          // Round number down to nearest integer
                value *= pow10;
            }
        }

        return value;
    }

    /**
     * Rounds a floating point number to the desired number of significant digits.
     * For example: 1346.4667 rounded to 3 significant digits is 1350.0.
     * 
     * @param value The value to be rounded.
     * @param sigFig The number of significant digits/figures to retain.
     * @return A floating point number rounded to the specified number of significant digits.
     * @see #roundToPlace(double, int) 
     */
    public static double roundToSigFig(double value, int sigFig) {
        int p = (int)Math.round(Math.log10(value) + 0.5);
        p -= sigFig;
        return MathTools.roundToPlace(value, p);
    }
    
    /**
     * Returns the factorial of n (usually written as n!) for any value of n. The
     * factorial of n is the product of all integers up to and including n.
     *
     * @param n The number to calculate the factorial for.
     * @return The factorial of n.
     * @see #intFactorial(int)
     */
    public static BigInteger factorial(int n) {
        if (n < 0)
            throw new IllegalArgumentException("n must be positive");
        if (n <= 12)
            return BigInteger.valueOf(intFactorial(n));

        BigInteger fact = BigInteger.ONE;
        for (int i = 2; i <= n; ++i)
            fact = fact.multiply(BigInteger.valueOf(i));
        return fact;
    }

    /**
     * Returns the factorial of n (usually written as n!) for values of n up to 12. The
     * factorial of n is the product of all integers up to and including n. This method is
     * more efficient than "factorial()" for values of n &le; 12. Values of n > 12 cause a
     * numerical overflow with this method. Use "factorial()" for larger values of n (or
     * any value of n where performance is not critical).
     *
     * @param n The number to calculate the factorial for (0 &le; n &le; 12).
     * @return The factorial of n.
     * @see #factorial(int) 
    *
     */
    public static int intFactorial(int n) {
        if (n < 0)
            throw new IllegalArgumentException("n must be positive");
        if (n > 12)
            throw new IllegalArgumentException("Integer overflow in factorial()!");
        int fact = 1;
        for (int i = 2; i <= n; ++i)
            fact *= i;
        return fact;
    }

    /**
     * Calculates the greatest common divisor between two input integers. The GCD is the
     * largest number that can be divided into both input numbers.
     *
     * @param xval First integer
     * @param yval Second integer
     * @return The largest number that can be divided into both input values.
     */
    public static long greatestCommonDivisor(long xval, long yval) {
        // Uses Euler's method.
        long value = 0;
        while (value != xval) {
            if (xval < yval)
                yval = yval - xval;

            else {
                if (xval > yval)
                    xval = xval - yval;
                else
                    value = xval;
            }
        }
        return (value);
    }

    /**
     * Returns the fractional part of a floating point number (removes the integer part).
     *
     * @param x Argument for which the fractional part is to be returned.
     * @return The fractional part of the argument is returned.
     */
    public static double frac(double x) {
        x = x - (long)x;
        if (x < 0.)
            ++x;

        return x;
    }

    /**
     * Straight linear 1D interpolation between two points.
     *
     * @param x1 X-coordinate of the 1st point (the high point).
     * @param y1 Y-coordinate of the 1st point (the high point).
     * @param x2 X-coordinate of the 2nd point (the low point).
     * @param y2 Y-coordinate of the 2nd point (the low point).
     * @param x  The X coordinate of the point for which we want to interpolate to
     *           determine a Y coordinate. Will extrapolate if X is outside of the bounds
     *           of the point arguments.
     * @return The linearly interpolated Y value corresponding to the input X value is
     *         returned.
     */
    public static double lineInterp(double x1, double y1, double x2, double y2, double x) {
        return ((y2 - y1) / (x2 - x1) * (x - x1) + y1);
    }

    /**
     * Converts a positive decimal number to it's binary equivalent.
     *
     * @param decimal The positive decimal number to be encoded in binary.
     * @param bits    The bitset to encode the number in.
     */
    public static void dec2bin(int decimal, BitSet bits) {
        if (decimal < 0)
            throw new IllegalArgumentException("Cannot convert a negative number to binary.");

        int i = 0;
        int value = decimal;
        while (value > 0) {

            if (value % 2 > 0)
                bits.set(i);
            else
                bits.clear(i);

            value /= 2;
            ++i;
        }

        for (; i < bits.size(); ++i)
            bits.clear(i);
    }

    /**
     * Converts binary number to it's base 10 decimal equivalent.
     *
     * @param bits The bitset that encodes the number to be converted.
     * @return Returns the decimal equivalent of the given binary number.
     */
    public static long bin2dec(BitSet bits) {
        long value = 0;
        int length = bits.size();

        for (int i = 0; i < length; ++i) {
            if (bits.get(i))
                value += pow2(i);
        }
        return value;
    }

    /**
     * Return the hyperbolic cosine of the specified argument.
     *
     * @param x The number whose hyperbolic cosine is to be returned.
     * @return The hyperbolic cosine of x.
     * @see Math#cosh(double) 
     * @deprecated 
     */
    public static double cosh(double x) {
        return Math.cosh(x);
    }

    /**
     * Return the hyperbolic sine of the specified argument.
     *
     * @param x The number whose hyperbolic sine is to be returned.
     * @return The hyperbolic sine of x.
     * @see Math#sinh(double) 
     * @deprecated 
     */
    public static double sinh(double x) {
        return Math.sinh(x);
    }

    /**
     * Returns the hyperbolic tangent of the specified argument.
     *
     * @param x The number whose hyperbolic tangent is to be returned.
     * @return The hyperbolic tangent of x.
     * @see Math#tanh(double) 
     * @deprecated 
     */
    public static double tanh(double x) {
        return Math.tanh(x);
    }

    /**
     * Returns the inverse hyperbolic cosine of the specified argument. The inverse
     * hyperbolic cosine is defined as: <code>acosh(x) = log(x + sqrt((x-1)*(x+1)))</code>
     *
     * @param x Value to return inverse hyperbolic cosine of.
     * @return The inverse hyperbolic cosine of x.
     * @throws IllegalArgumentException if x is less than 1.0.
     */
    public static double acosh(double x) {
        if (Double.isNaN(x))
            return Double.NaN;
        if (Double.isInfinite(x))
            return x;
        if (x < 1.0)
            throw new IllegalArgumentException("x may not be less than 1.0");

        double y;
        if (x > 1.0E8) {
            y = Math.log(x) + LOG2;

        } else {
            double a = Math.sqrt((x - 1.0) * (x + 1.0));
            y = Math.log(x + a);
        }

        return y;
    }

    /**
     * Returns the inverse hyperbolic sine of the specified argument. The inverse
     * hyperbolic sine is defined as: <code>asinh(x) = log(x + sqrt(1 + x*x))</code>
     *
     * @param xx Value to return inverse hyperbolic cosine of.
     * @return The inverse hyperbolic sine of x.
     */
    public static double asinh(double xx) {
        if (Double.isNaN(xx))
            return Double.NaN;
        if (Double.isInfinite(xx))
            return xx;
        if (xx == 0)
            return 0;

        int sign = 1;
        double x = xx;
        if (xx < 0) {
            sign = -1;
            x = -xx;
        }

        double y;
        if (x > 1.0E8) {
            y = sign * (Math.log(x) + LOG2);

        } else {
            double a = Math.sqrt(x * x + 1.0);
            y = sign * Math.log(x + a);
        }

        return y;
    }

    /**
     * Returns the inverse hyperbolic tangent of the specified argument. The inverse
     * hyperbolic tangent is defined as: <code>atanh(x) = 0.5*log((1 + x)/(1 - x))</code>
     *
     * @param x Value to return inverse hyperbolic cosine of.
     * @return The inverse hyperbolic tangent of x.
     * @throws IllegalArgumentException if x is outside the range -1, to +1.
     */
    public static double atanh(double x) {
        if (Double.isNaN(x))
            return Double.NaN;
        if (x == 0)
            return 0;

        double z = Math.abs(x);
        if (z >= 1.0) {
            if (x == 1.0)
                return Double.POSITIVE_INFINITY;
            if (x == -1.0)
                return Double.NEGATIVE_INFINITY;

            throw new IllegalArgumentException("x outside of range -1 to +1");
        }

        if (z < 1.0E-7)
            return x;

        double y = 0.5 * Math.log((1.0 + x) / (1.0 - x));

        return y;
    }

    /**
     * Returns the absolute value of "a" times the sign of "b".
     *
     * @param a The value for which the magnitude is returned.
     * @param b The value for which the sign is returned.
     * @return The absolute value of "a" times the sign of "b".
     */
    public static double sign(double a, double b) {
        return b >= 0 ? (a >= 0 ? a : -a) : (a >= 0 ? -a : a);
    }

    /**
     * Returns the absolute value of "a" times the sign of "b".
     *
     * @param a The value for which the magnitude is returned.
     * @param b The value for which the sign is returned.
     * @return The absolute value of "a" times the sign of "b".
     */
    public static float sign(float a, double b) {
        return b >= 0 ? (a >= 0 ? a : -a) : (a >= 0 ? -a : a);
    }

    /**
     * Returns the absolute value of "a" times the sign of "b".
     *
     * @param a The value for which the magnitude is returned.
     * @param b The value for which the sign is returned.
     * @return The absolute value of "a" times the sign of "b".
     */
    public static long sign(long a, double b) {
        return b >= 0 ? (a >= 0 ? a : -a) : (a >= 0 ? -a : a);
    }

    /**
     * Returns the absolute value of "a" times the sign of "b".
     *
     * @param a The value for which the magnitude is returned.
     * @param b The value for which the sign is returned.
     * @return The absolute value of "a" times the sign of "b".
     */
    public static int sign(int a, double b) {
        return b >= 0 ? (a >= 0 ? a : -a) : (a >= 0 ? -a : a);
    }

    //  Used by "lnGamma()".
    private static final double[] gamCoef = {76.18009172947146, -86.50532032941677, 24.01409824083091,
        -1.231739572450155, 0.1208650973866179e-2, -0.5395239384953e-5};

    /**
     * Returns the natural log of the Gamma Function defined by the integral:
     * <code>Gamma(z) = integral from 0 to infinity of t^(z-1)*e^-t dt</code>.
     * <p>
     * It is better to implement ln(Gamma(x)) rather than Gamma(x) since the latter will
     * overflow most computer floating point representations at quite modest values of x.
     * </p>
     *
     * @param value The value to evaluate the log of the gamma function for.
     * @return The natural log of the Gamma Function.
     */
    public static double lnGamma(double value) {
        double x = value, y = value;
        double tmp = x + 5.5;
        tmp -= (x + 0.5) * Math.log(tmp);
        double ser = 1.000000000190015;
        for (int j = 0; j <= 5; ++j)
            ser += gamCoef[j] / ++y;
        return -tmp + Math.log(2.5066282746310005 * ser / x);
    }

    /**
     * Returns the smallest roundoff in quantities of size x, EPSILON, such that
     * <code>x + EPSILON > x</code>. This is the Units in the Last Place (ULP) and is
     * different than the machine roundoff EPS (macheps).
     *
     * @param x The value to return the roundoff quantity for.
     * @return The smallest roundoff in quantities of size x, EPSILON, such that x +
     *         EPSILON > x
     * @see #EPS
     * @see Math#ulp(double) 
     */
    public static double epsilon(double x) {
        return Math.ulp(x);
    }

    /**
     * Returns the smallest roundoff in quantities of size x, EPSILON, such that
     * <code>x + EPSILON > x</code>. This is the Units in the Last Place (ULP) and is
     * different than the machine roundoff EPS (macheps).
     *
     * @param x The value to return the roundoff quantity for.
     * @return The smallest roundoff in quantities of size x, EPSILON, such that x +
     *         EPSILON > x
     * @see #EPSF
     * @see Math#ulp(float) 
     */
    public static float epsilon(float x) {
        return Math.ulp(x);
    }

    /**
     * Returns true if the two supplied numbers are approximately equal to within machine
     * precision.
     *
     * @param a The 1st value to compare for approximate equality.
     * @param b The 2nd value to compare for approximate equality.
     * @return true if the two supplied numbers are approximately equal to within machine
     *         precision
     */
    public static boolean isApproxEqual(double a, double b) {
        double eps2 = epsilon(a);
        double eps = (eps2 > EPS ? eps2 : EPS);
        return Math.abs(a - b) <= eps;
    }

    /**
     * Returns true if the two supplied numbers are approximately equal to within the
     * specified tolerance.
     *
     * @param a   The 1st value to compare for approximate equality.
     * @param b   The 2nd value to compare for approximate equality.
     * @param tol The tolerance for equality.
     * @return true if the two supplied numbers are approximately equal to within the
     *         specified tolerance
     */
    public static boolean isApproxEqual(double a, double b, double tol) {
        return Math.abs(a - b) <= tol;
    }

    /**
     * Returns true if the two supplied numbers are approximately equal to within machine
     * precision.
     *
     * @param a The 1st value to compare for approximate equality.
     * @param b The 2nd value to compare for approximate equality.
     * @return true if the two supplied numbers are approximately equal to within machine
     *         precision
     */
    public static boolean isApproxEqual(float a, float b) {
        float eps2 = epsilon(a);
        float eps = (eps2 > EPSF ? eps2 : EPSF);
        return Math.abs(a - b) <= eps;
    }

    /**
     * Returns true if the two supplied numbers are approximately equal to within the
     * specified tolerance.
     *
     * @param a   The 1st value to compare for approximate equality.
     * @param b   The 2nd value to compare for approximate equality.
     * @param tol The tolerance for equality.
     * @return true if the two supplied numbers are approximately equal to within the
     *         specified tolerance
     */
    public static boolean isApproxEqual(float a, float b, float tol) {
        return Math.abs(a - b) <= tol;
    }

    /**
     * Returns true if the supplied number is approximately zero to within machine
     * precision.
     *
     * @param a The value to be compared with zero.
     * @return true if the supplied number is approximately zero to within machine
     *         precision
     */
    public static boolean isApproxZero(double a) {
        return Math.abs(a) <= EPS;
    }

    /**
     * Returns true if the supplied number is approximately zero to within the specified
     * tolerance.
     *
     * @param a   The value to be compared with zero.
     * @param tol The tolerance for equality.
     * @return true if the supplied number is approximately zero to within the specified
     *         tolerance
     */
    public static boolean isApproxZero(double a, double tol) {
        return Math.abs(a) <= tol;
    }

    /**
     * Returns true if the supplied number is approximately zero to within machine
     * precision.
     *
     * @param a The value to be compared with zero.
     * @return true if the supplied number is approximately zero to within machine
     *         precision
     */
    public static boolean isApproxZero(float a) {
        return Math.abs(a) <= EPSF;
    }

    /**
     * Returns true if the supplied number is approximately zero to within the specified
     * tolerance.
     *
     * @param a   The value to be compared with zero.
     * @param tol The tolerance for equality.
     * @return true if the supplied number is approximately zero to within the specified
     *         tolerance
     */
    public static boolean isApproxZero(float a, float tol) {
        return Math.abs(a) <= tol;
    }

    /**
     * Used to test out the methods in this class.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String args[]) {

        System.out.println();
        System.out.println("Testing MathTools...");

        System.out.println("  2 is an " + (even(2) ? "even" : "odd") + " number.");
        System.out.println("  3 is an " + (odd(3) ? "odd" : "even") + " number.");
        System.out.println("  The square of 3.8 is " + sqr(3.8) + ".");
        System.out.println("  The integer 3^7 is " + pow(3, 7) + ".");
        System.out.println("  The integer 2^8 is " + pow2(8) + ".");
        System.out.println("  The double 10^-3 is " + pow10(-3) + ".");
        System.out.println("  The base 2 logarithm of 8 is " + log2(8) + ".");
        System.out.println("  1346.4667 rounded to the nearest 100 is "
                + roundToPlace(1346.4667, 2) + ".");
        System.out.println("  1346.4667 rounded up to the nearest 100 is "
                + roundUpToPlace(1346.4667, 2) + ".");
        System.out.println("  1346.4667 rounded down to the nearest 10 is "
                + roundDownToPlace(1346.4667, 1) + ".");
        System.out.println("  1346.4667 rounded to 3 significant digits is "
                + roundToSigFig(1346.4667, 3) + ".");
        System.out.println("  The GCD of 125 and 45 is " + greatestCommonDivisor(125, 45) + ".");
        System.out.println("  The fractional part of 3.141593 is " + frac(3.141593) + ".");
        double x = Math.cosh(5);
        System.out.println("  The inv. hyperbolic cosine of " + (float)x + " = " + (float)acosh(x) + ".");
        System.out.println("  The inv. hyperbolic sine of " + (float)x + " = " + (float)asinh(x) + ".");
        x = Math.tanh(-0.25);
        System.out.println("  The inv. hyperbolic tangent of " + (float)x + " = " + (float)atanh(x) + ".");

        System.out.println("  4.56 with the sign of -6.33 is " + sign(4.56F, -6.33));
        System.out.println("  epsilon(0) is " + epsilon(0));
        System.out.println("  epsilon(2387.8) is " + epsilon(2387.8));

    }

}
