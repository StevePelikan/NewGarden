package functions;

/*
Copyright � 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.

*/




/**
 * Gamma and Beta functions.
 * <p>
 * <b>Implementation:</b>
 * <dt>
 * Some code taken and adapted from the <A HREF="http://www.sci.usq.edu.au/staff/leighb/graph/Top.html">Java 2D Graph Package 2.4</A>,
 * which in turn is a port from the <A HREF="http://people.ne.mediaone.net/moshier/index.html#Cephes">Cephes 2.2</A> Math Library (C).
 * Most Cephes code (missing from the 2D Graph Package) directly ported.
 *
 * @author wolfgang.hoschek@cern.ch
 * @version 0.9, 22-Jun-99
 */

public class Gamma extends Constants {
/**
 * Makes this class non instantiable, but still let's others inherit from it.
 */
protected Gamma() {}
/**
 * Returns the beta function of the arguments.
 * \[\frac{\Gamma(a)\Gamma(b)}{\Gamma(a+b)}\]
 * <pre>
 *                   -     -
 *                  | (a) | (b)
 * beta( a, b )  =  -----------.
 *                     -
 *                    | (a+b)
 * </pre>
 */
static public double beta(double a, double b) throws ArithmeticException {
	double y;
	
	y = a + b;
	y = gamma(y);
	if( y == 0.0 ) return 1.0;

	if( a > b ) {
		y = gamma(a)/y;
		y *= gamma(b);
	}
	else {
		y = gamma(b)/y;
		y *= gamma(a);
	}

	return(y);
}
static public double cumulativeZ(double x)
    {
	if(x <0) return 0.5*(1-incompleteGamma(0.5,x*x/2));
	else  return 0.5*(1+incompleteGamma(0.5,x*x/2));
    }
    /*
      \sum_{j=k}^n C_^n_jp^j(1-p)^{n-j} = iBeta(k,n-k+1,p)
     */
    static public double cumulativeBinomial(double n,double p,double k)
    {
	return incompleteBeta(k,n-k+1,p);
    }
    static public double cumulativeChisquare(double X, double df)
    {
	return incompleteGamma(df/2,X/2);
    }
    static public double cumulativeT(double t,double df)
    {
	if(t >0) return (1-0.5*incompleteBeta(df/2,0.5,df/(df+t*t)));
	else return 0.5*incompleteBeta(df/2,0.5,df/(df+t*t));
    }
/**
 * Returns the Gamma function of the argument.
 */
static public double gamma(double x) throws ArithmeticException {

double P[] = {
			   1.60119522476751861407E-4,
			   1.19135147006586384913E-3,
			   1.04213797561761569935E-2,
			   4.76367800457137231464E-2,
			   2.07448227648435975150E-1,
			   4.94214826801497100753E-1,
			   9.99999999999999996796E-1
			  };
double Q[] = {
			   -2.31581873324120129819E-5,
				5.39605580493303397842E-4,
			   -4.45641913851797240494E-3,
				1.18139785222060435552E-2,
				3.58236398605498653373E-2,
			   -2.34591795718243348568E-1,
				7.14304917030273074085E-2,
				1.00000000000000000320E0
			   };
//double MAXGAM = 171.624376956302725;
//double LOGPI  = 1.14472988584940017414;

double p, z;
int i;

double q = Math.abs(x);

if( q > 33.0 ) {
   if( x < 0.0 ) {
		p = Math.floor(q);
	if( p == q ) throw new ArithmeticException("gamma: overflow");
	i = (int)p;
	z = q - p;
	if( z > 0.5 ) {
		p += 1.0;
		z = q - p;
	}
	z = q * Math.sin( Math.PI * z );
	if( z == 0.0 ) throw new ArithmeticException("gamma: overflow");
	z = Math.abs(z);
	z = Math.PI/(z * stirlingFormula(q) );

		return -z;
   } else {
	return stirlingFormula(x);
   }
 }

 z = 1.0;
   while( x >= 3.0 ) {
  	     x -= 1.0;
	 z *= x;
   }

   while( x < 0.0 ) {
	 if( x == 0.0 ) {
			throw new ArithmeticException("gamma: singular");
		 } else
	 if( x > -1.E-9 ) {
			 return( z/((1.0 + 0.5772156649015329 * x) * x) );
		 }
	 z /= x;
	 x += 1.0;
   }

   while( x < 2.0 ) {
	 if( x == 0.0 ) {
			throw new ArithmeticException("gamma: singular");
		 } else
	 if( x < 1.e-9 ) {
  	        return( z/((1.0 + 0.5772156649015329 * x) * x) );
		 }
	 z /= x;
	 x += 1.0;
}

	if( (x == 2.0) || (x == 3.0) ) 	return z;

	x -= 2.0;
	p = Polynomial.polevl( x, P, 6 );
	q = Polynomial.polevl( x, Q, 7 );
	return  z * p / q;

}
/**
 * Returns the Incomplete Beta Function evaluated from zero to <tt>xx</tt>; formerly named <tt>ibeta</tt>.
 *
 * @param aa the alpha parameter of the beta distribution.
 * @param bb the beta parameter of the beta distribution.
 * @param xx the integration end point.
 */
public static double incompleteBeta( double aa, double bb, double xx ) throws ArithmeticException {
		double a, b, t, x, xc, w, y;
		boolean flag;

		if( aa <= 0.0 || bb <= 0.0 ) throw new 
						  ArithmeticException("ibeta: Domain error!");

		if( (xx <= 0.0) || ( xx >= 1.0) ) {
  	       if( xx == 0.0 ) return 0.0;
   	       if( xx == 1.0 ) return 1.0;
		   throw new ArithmeticException("ibeta: Domain error!");
	    }

		flag = false;
		if( (bb * xx) <= 1.0 && xx <= 0.95) {
	        t = powerSeries(aa, bb, xx);
		    return t;
	    }

		w = 1.0 - xx;

		/* Reverse a and b if x is greater than the mean. */
		if( xx > (aa/(aa+bb)) ) {
	       flag = true;
	       a = bb;
	       b = aa;
	       xc = xx;
	       x = w;
	    } else {
  	       a = aa;
	       b = bb;
	       xc = w;
	       x = xx;
	    }

		if( flag  && (b * x) <= 1.0 && x <= 0.95) {
 	       t = powerSeries(a, b, x);
	       if( t <= MACHEP ) 	t = 1.0 - MACHEP;
	       else  		        t = 1.0 - t;
		   return t;
	    }

		/* Choose expansion for better convergence. */
		y = x * (a+b-2.0) - (a-1.0);
		if( y < 0.0 )
	                  w = incompleteBetaFraction1( a, b, x );
		else
	                  w = incompleteBetaFraction2( a, b, x ) / xc;

		/* Multiply w by the factor
		   a      b   _             _     _
		  x  (1-x)   | (a+b) / ( a | (a) | (b) ) .   */

		y = a * Math.log(x);
		t = b * Math.log(xc);
		if( (a+b) < MAXGAM && Math.abs(y) < MAXLOG && Math.abs(t) < MAXLOG ) {
	        t = Math.pow(xc,b);
	        t *= Math.pow(x,a);
	        t /= a;
	        t *= w;
	        t *= gamma(a+b) / (gamma(a) * gamma(b));
			if( flag ) {
 	           if( t <= MACHEP ) 	t = 1.0 - MACHEP;
	           else  		        t = 1.0 - t;
	        }
			return t;
	    }
		/* Resort to logarithms.  */
		y += t + logGamma(a+b) - logGamma(a) - logGamma(b);
		y += Math.log(w/a);
		if( y < MINLOG )
	                    t = 0.0;
		else
	                    t = Math.exp(y);

		if( flag ) {
 	           if( t <= MACHEP ) 	t = 1.0 - MACHEP;
	           else  		        t = 1.0 - t;
	    }
		return t;
   }   
/**
 * Continued fraction expansion #1 for incomplete beta integral; formerly named <tt>incbcf</tt>.
 */
static double incompleteBetaFraction1( double a, double b, double x ) throws ArithmeticException {
	   double xk, pk, pkm1, pkm2, qk, qkm1, qkm2;
	   double k1, k2, k3, k4, k5, k6, k7, k8;
	   double r, t, ans, thresh;
	   int n;

	   k1 = a;
	   k2 = a + b;
	   k3 = a;
	   k4 = a + 1.0;
	   k5 = 1.0;
	   k6 = b - 1.0;
	   k7 = k4;
	   k8 = a + 2.0;

	   pkm2 = 0.0;
	   qkm2 = 1.0;
	   pkm1 = 1.0;
	   qkm1 = 1.0;
	   ans = 1.0;
	   r = 1.0;
	   n = 0;
	   thresh = 3.0 * MACHEP;
	   do {
	      xk = -( x * k1 * k2 )/( k3 * k4 );
	      pk = pkm1 +  pkm2 * xk;
	      qk = qkm1 +  qkm2 * xk;
	      pkm2 = pkm1;
	      pkm1 = pk;
	      qkm2 = qkm1;
	      qkm1 = qk;

	      xk = ( x * k5 * k6 )/( k7 * k8 );
	      pk = pkm1 +  pkm2 * xk;
	      qk = qkm1 +  qkm2 * xk;
	      pkm2 = pkm1;
	      pkm1 = pk;
	      qkm2 = qkm1;
	      qkm1 = qk;

	      if( qk != 0 )		r = pk/qk;
	      if( r != 0 ) {
		       t = Math.abs( (ans - r)/r );
		       ans = r;
		  }	else
		       t = 1.0;

	      if( t < thresh ) return ans;

	      k1 += 1.0;
		  k2 += 1.0;
	  	  k3 += 2.0;
	  	  k4 += 2.0;
	  	  k5 += 1.0;
	  	  k6 -= 1.0;
	  	  k7 += 2.0;
	  	  k8 += 2.0;

	  	  if( (Math.abs(qk) + Math.abs(pk)) > big ) {
	  		pkm2 *= biginv;
	  		pkm1 *= biginv;
	  		qkm2 *= biginv;
	  		qkm1 *= biginv;
		  }
	  	  if( (Math.abs(qk) < biginv) || (Math.abs(pk) < biginv) ) {
	  		pkm2 *= big;
	  		pkm1 *= big;
	  		qkm2 *= big;
	  		qkm1 *= big;
		  }
	   } while( ++n < 300 );

	return ans;
   }   
/**
 * Continued fraction expansion #2 for incomplete beta integral; formerly named <tt>incbd</tt>.
 */
static double incompleteBetaFraction2( double a, double b, double x ) throws ArithmeticException {
		 double xk, pk, pkm1, pkm2, qk, qkm1, qkm2;
		 double k1, k2, k3, k4, k5, k6, k7, k8;
		 double r, t, ans, z, thresh;
		 int n;

		 k1 = a;
		 k2 = b - 1.0;
		 k3 = a;
		 k4 = a + 1.0;
		 k5 = 1.0;
		 k6 = a + b;
		 k7 = a + 1.0;;
		 k8 = a + 2.0;

		 pkm2 = 0.0;
		 qkm2 = 1.0;
		 pkm1 = 1.0;
		 qkm1 = 1.0;
		 z = x / (1.0-x);
		 ans = 1.0;
		 r = 1.0;
		 n = 0;
		 thresh = 3.0 * MACHEP;
		 do {
	         xk = -( z * k1 * k2 )/( k3 * k4 );
	         pk = pkm1 +  pkm2 * xk;
	         qk = qkm1 +  qkm2 * xk;
	         pkm2 = pkm1;
	         pkm1 = pk;
	         qkm2 = qkm1;
	         qkm1 = qk;

	         xk = ( z * k5 * k6 )/( k7 * k8 );
	         pk = pkm1 +  pkm2 * xk;
	         qk = qkm1 +  qkm2 * xk;
	         pkm2 = pkm1;
	         pkm1 = pk;
	         qkm2 = qkm1;
	         qkm1 = qk;

	         if( qk != 0 )  r = pk/qk;
	         if( r != 0 ) {
		         t = Math.abs( (ans - r)/r );
		         ans = r;
		     } else
		         t = 1.0;

	         if( t < thresh ) return ans;

	         k1 += 1.0;
	         k2 -= 1.0;
	         k3 += 2.0;
	         k4 += 2.0;
	         k5 += 1.0;
	         k6 += 1.0;
	         k7 += 2.0;
	         k8 += 2.0;

	         if( (Math.abs(qk) + Math.abs(pk)) > big ) {
		        pkm2 *= biginv;
		        pkm1 *= biginv;
		        qkm2 *= biginv;
		        qkm1 *= biginv;
		     }
	         if( (Math.abs(qk) < biginv) || (Math.abs(pk) < biginv) ) {
		        pkm2 *= big;
		        pkm1 *= big;
		        qkm2 *= big;
		        qkm1 *= big;
		     }
	    } while( ++n < 300 );

		return ans;
	 }
/**
 * Returns the Incomplete Gamma function; formerly named <tt>igamma</tt>.
 * @param a the parameter of the gamma distribution.
 * @param x the integration end point.
 */
static public double incompleteGamma(double a, double x) 
						 throws ArithmeticException {


		double ans, ax, c, r;

		if( x <= 0 || a <= 0 ) return 0.0;

		if( x > 1.0 && x > a ) return 1.0 - incompleteGammaComplement(a,x);

	   /* Compute  x**a * exp(-x) / gamma(a)  */
		ax = a * Math.log(x) - x - logGamma(a);
		if( ax < -MAXLOG ) return( 0.0 );

		ax = Math.exp(ax);

		/* power series */
		r = a;
		c = 1.0;
		ans = 1.0;

		do {
  	    r += 1.0;
	    c *= x/r;
	    ans += c;
	}
		while( c/ans > MACHEP );

		return( ans * ax/a );

	 }
/**
 * Returns the Complemented Incomplete Gamma function; formerly named <tt>igamc</tt>.
 * @param a the parameter of the gamma distribution.
 * @param x the integration start point.
 */
static public double incompleteGammaComplement( double a, double x ) throws ArithmeticException {
		double ans, ax, c, yc, r, t, y, z;
		double pk, pkm1, pkm2, qk, qkm1, qkm2;

		if( x <= 0 || a <= 0 ) return 1.0;

		if( x < 1.0 || x < a ) return 1.0 - incompleteGamma(a,x);

		ax = a * Math.log(x) - x - logGamma(a);
		if( ax < -MAXLOG ) return 0.0;

		ax = Math.exp(ax);

		/* continued fraction */
		y = 1.0 - a;
		z = x + y + 1.0;
		c = 0.0;
		pkm2 = 1.0;
		qkm2 = x;
		pkm1 = x + 1.0;
		qkm1 = z * x;
		ans = pkm1/qkm1;

		do {
  	    c += 1.0;
	    y += 1.0;
	    z += 2.0;
	    yc = y * c;
	    pk = pkm1 * z  -  pkm2 * yc;
	    qk = qkm1 * z  -  qkm2 * yc;
	    if( qk != 0 ) {
		r = pk/qk;
		t = Math.abs( (ans - r)/r );
		ans = r;
	    } else
		t = 1.0;

	    pkm2 = pkm1;
	    pkm1 = pk;
	    qkm2 = qkm1;
	    qkm1 = qk;
	    if( Math.abs(pk) > big ) {
		pkm2 *= biginv;
		pkm1 *= biginv;
		qkm2 *= biginv;
		qkm1 *= biginv;
	    }
	} while( t > MACHEP );

		return ans * ax;
	 }
/**
 * Returns the natural logarithm of the gamma function; formerly named <tt>lgamma</tt>.
 */
public static double logGamma(double x) throws ArithmeticException {
	double p, q, w, z;

		 double A[] = {
					   8.11614167470508450300E-4,
					   -5.95061904284301438324E-4,
						7.93650340457716943945E-4,
					   -2.77777777730099687205E-3,
						8.33333333333331927722E-2
					   };
		 double B[] = {
					   -1.37825152569120859100E3,
					   -3.88016315134637840924E4,
					   -3.31612992738871184744E5,
					   -1.16237097492762307383E6,
					   -1.72173700820839662146E6,
					   -8.53555664245765465627E5
					   };
		 double C[] = {
					   /* 1.00000000000000000000E0, */
					   -3.51815701436523470549E2,
					   -1.70642106651881159223E4,
					   -2.20528590553854454839E5,
					   -1.13933444367982507207E6,
					   -2.53252307177582951285E6,
					   -2.01889141433532773231E6
					  };

		 if( x < -34.0 ) {
  	   q = -x;
	   w = logGamma(q);
	   p = Math.floor(q);
	   if( p == q ) throw new ArithmeticException("lgam: Overflow");
	   z = q - p;
	   if( z > 0.5 ) {
		p += 1.0;
		z = p - q;
 	   }
	   z = q * Math.sin( Math.PI * z );
	   if( z == 0.0 ) throw new ArithmeticException("lgamma: Overflow");
	   z = LOGPI - Math.log( z ) - w;
	   return z;
	 }

		 if( x < 13.0 ) {
  	   z = 1.0;
	   while( x >= 3.0 ) {
		x -= 1.0;
		z *= x;
	   }
	   while( x < 2.0 ) {
		if( x == 0.0 ) throw new ArithmeticException("lgamma: Overflow");
		z /= x;
		x += 1.0;
	   }
	   if( z < 0.0 ) z = -z;
	   if( x == 2.0 ) return Math.log(z);
	   x -= 2.0;
	   p = x * Polynomial.polevl( x, B, 5 ) / Polynomial.p1evl( x, C, 6);
 	   return( Math.log(z) + p );
	 }

		 if( x > 2.556348e305 ) throw new 
						  ArithmeticException("lgamma: Overflow");

		 q = ( x - 0.5 ) * Math.log(x) - x + 0.91893853320467274178;
		 //if( x > 1.0e8 ) return( q );
		 if( x > 1.0e8 ) return( q );

		 p = 1.0/(x*x);
		 if( x >= 1000.0 )
	     q += ((   7.9365079365079365079365e-4 * p
		      - 2.7777777777777777777778e-3) *p
		     + 0.0833333333333333333333) / x;
		 else
	     q += Polynomial.polevl( p, A, 4 ) / x;
		 return q;
	 }
/**
 * Power series for incomplete beta integral; formerly named <tt>pseries</tt>.
 * Use when b*x is small and x not too close to 1.  
 */
static double powerSeries( double a, double b, double x ) throws ArithmeticException {
	double s, t, u, v, n, t1, z, ai;

	ai = 1.0 / a;
	u = (1.0 - b) * x;
	v = u / (a + 1.0);
	t1 = v;
	t = u;
	n = 2.0;
	s = 0.0;
	z = MACHEP * ai;
	while( Math.abs(v) > z ) {
	       u = (n - b) * x / n;
	       t *= u;
	       v = t / (a + n);
	       s += v; 
	       n += 1.0;
	    }
	s += t1;
	s += ai;

	u = a * Math.log(x);
	if( (a+b) < MAXGAM && Math.abs(u) < MAXLOG ) {
	        t = Gamma.gamma(a+b)/(Gamma.gamma(a)*Gamma.gamma(b));
	        s = s * t * Math.pow(x,a);
	    } else {
	       t = Gamma.logGamma(a+b) - Gamma.logGamma(a) - Gamma.logGamma(b) + u + Math.log(s);
	       if( t < MINLOG ) 	s = 0.0;
	       else  	            s = Math.exp(t);
	    }
	return s;
}
/**
 * Returns the Gamma function computed by Stirling's formula; formerly named <tt>stirf</tt>.
 * The polynomial STIR is valid for 33 <= x <= 172.
 */
static double stirlingFormula(double x) throws ArithmeticException {
		double STIR[] = {
					 7.87311395793093628397E-4,
					-2.29549961613378126380E-4,
					-2.68132617805781232825E-3,
					 3.47222221605458667310E-3,
					 8.33333333333482257126E-2,
					};
		double MAXSTIR = 143.01608;

		double w = 1.0/x;
		double  y = Math.exp(x);

		w = 1.0 + w * Polynomial.polevl( w, STIR, 4 );

		if( x > MAXSTIR ) {
	       /* Avoid overflow in Math.pow() */
	       double v = Math.pow( x, 0.5 * x - 0.25 );
	       y = v * (v / y);
	} else {
			   y = Math.pow( x, x - 0.5 ) / y;
	}
		y = SQTPI * y * w;
		return y;
	 }
    
}


/**
 * Polynomial functions.
 */
class Polynomial extends Constants {
/**
 * Makes this class non instantiable, but still let's others inherit from it.
 */
protected Polynomial() {}
/**
 * Evaluates the given polynomial of degree <tt>N</tt> at <tt>x</tt>, assuming coefficient of N is 1.0.
 * Otherwise same as <tt>polevl()</tt>.
 * <pre>
 *                     2          N
 * y  =  C  + C x + C x  +...+ C x
 *        0    1     2          N
 *
 * where C  = 1 and hence is omitted from the array.
 *        N
 *
 * Coefficients are stored in reverse order:
 *
 * coef[0] = C  , ..., coef[N-1] = C  .
 *            N-1                   0
 *
 * Calling arguments are otherwise the same as polevl().
 * </pre>
 * In the interest of speed, there are no checks for out of bounds arithmetic.
 *
 * @param x argument to the polynomial.
 * @param coef the coefficients of the polynomial.
 * @param N the degree of the polynomial.
 */
public static double p1evl( double x, double coef[], int N ) throws ArithmeticException {
	double ans;

	ans = x + coef[0];

	for(int i=1; i<N; i++) { ans = ans*x+coef[i]; }

	return ans;
}
/**
 * Evaluates the given polynomial of degree <tt>N</tt> at <tt>x</tt>.
 * <pre>
 *                     2          N
 * y  =  C  + C x + C x  +...+ C x
 *        0    1     2          N
 *
 * Coefficients are stored in reverse order:
 *
 * coef[0] = C  , ..., coef[N] = C  .
 *            N                   0
 * </pre>
 * In the interest of speed, there are no checks for out of bounds arithmetic.
 *
 * @param x argument to the polynomial.
 * @param coef the coefficients of the polynomial.
 * @param N the degree of the polynomial.
 */
public static double polevl( double x, double coef[], int N ) throws ArithmeticException {
	double ans;
	ans = coef[0];

	for(int i=1; i<=N; i++) ans = ans*x+coef[i];

	return ans;
}
}

class Constants {
  /*
   * machine constants
   */
	protected static final double MACHEP =  1.11022302462515654042E-16;
	protected static final double MAXLOG =  7.09782712893383996732E2;
	protected static final double MINLOG = -7.451332191019412076235E2;
	protected static final double MAXGAM = 171.624376956302725;
	protected static final double SQTPI  =  2.50662827463100050242E0;
	protected static final double SQRTH  =  7.07106781186547524401E-1;
	protected static final double LOGPI  =  1.14472988584940017414;

	protected static final double big = 4.503599627370496e15;
	protected static final double biginv =  2.22044604925031308085e-16;


	/*
 * MACHEP =  1.38777878078144567553E-17       2**-56
 * MAXLOG =  8.8029691931113054295988E1       log(2**127)
 * MINLOG = -8.872283911167299960540E1        log(2**-128)
 * MAXNUM =  1.701411834604692317316873e38    2**127
 *
 * For IEEE arithmetic (IBMPC):
 * MACHEP =  1.11022302462515654042E-16       2**-53
 * MAXLOG =  7.09782712893383996843E2         log(2**1024)
 * MINLOG = -7.08396418532264106224E2         log(2**-1022)
 * MAXNUM =  1.7976931348623158E308           2**1024
 *
 * The global symbols for mathematical constants are
 * PI     =  3.14159265358979323846           pi
 * PIO2   =  1.57079632679489661923           pi/2
 * PIO4   =  7.85398163397448309616E-1        pi/4
 * SQRT2  =  1.41421356237309504880           sqrt(2)
 * SQRTH  =  7.07106781186547524401E-1        sqrt(2)/2
 * LOG2E  =  1.4426950408889634073599         1/log(2)
 * SQ2OPI =  7.9788456080286535587989E-1      sqrt( 2/pi )
 * LOGE2  =  6.93147180559945309417E-1        log(2)
 * LOGSQ2 =  3.46573590279972654709E-1        log(2)/2
 * THPIO4 =  2.35619449019234492885           3*pi/4
 * TWOOPI =  6.36619772367581343075535E-1     2/pi
 */
 
/**
 * Makes this class non instantiable, but still let's others inherit from it.
 */
protected Constants() {}
}