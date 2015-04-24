package functions;



/*
 * RandomVariable.java
 *
 * Created on December 20, 2005, 7:56 PM
 *
 */

/**
 *
 * @author sep
 */
import functions.Gamma;
import java.util.Arrays;
import java.util.Random;
/**
 * RandomVariable is a class with methods for producing random deviates
 * with a variety of useful distributions
 * 
 * Some of the methods here use things from Gamma.java
 * @author s.pelikan
 */
public class RandomVariable {
    Random rnum;
    /** Creates a new instance of RandomVariable */
    public RandomVariable() {
        rnum=new Random();
        rnum.setSeed(System.nanoTime());
    }
    
    /**
     * Creates a new instance of RandomVariable
     * @param seed A long value to use as the seed for the (java supplied)
     * linear congruence pseudorandom generator.
     */
    public RandomVariable(long seed) {
        rnum=new Random();
        rnum.setSeed(seed);
    }
    /**
     * Returns an arrary of $B(n=flips,prob=p)$ values
     * @param flips number of flips
     * @param p chance of a head on a sinlge flip
     * @param n number of values to return
     * @return an array of the values generated
     */
    public double [] Binomial(int flips, double p, int n)
    {
        double []ans=new double[n];
        int heads;
        for(int i=0;i<n;i++)
        {
            heads=0;
            for(int f=0;f<flips;f++)
            {
                if(rnum.nextDouble()<p) {
                    heads++;
                }
            }
            ans[i]=(double)heads;
        }
        return ans;
    }
    /**
      * RandomInt(a,b,n) returns a random integer between a and b INCLUSIVE
      * or an array of n such
      * @param a lower limit
      * @param b upper limit
      * @return the random integer generated
      */
     public int RandomInt(int a,int b)
    {
        
        return(a+rnum.nextInt((b-a)+1)); //(0,....,b-a)
        //return(this.RandomInt(a,b,1)[0]);
    }
    /**
     * Returns an array of random integers selected
     * from the range a to b INCLUSIVE
     * @param a lower limit
     * @param b upper limit
     * @param n number of variates to generate
     * @return an array containing the random values
     */
    public int[]  RandomInt(int a,int b,int n)
    {
        int [] ans=new int[n];
        for(int i=0;i<n;i++) {
            ans[i] = a + rnum.nextInt((b - a) + 1);
        } //(0,....,b-a)
        return ans;
    }
    /**
     * Generates and returns an array of $n$ Poisson variates from
     * a distribution with mean mean
     * @param mean The mean of the Poisson distribution
     * @param n The number of values to generate
     * @return The values generated. These are integer values (Poissons are discrete, integer
     * valued  variables) but returned as an array of doubles.
     */
    public double [] Poisson(double mean,int n)
    {
        double PI=3.141592653;
        double x;
        double cut=0.5,temp;
        double [] ans=new double[n];
        if(mean<15.0)
        {
            cut=Math.exp(-mean);
            for(int i=0;i<n;i++)
            {
                x=-1.0;
                temp=1.0;
                do{
                    x++;
                    temp *= rnum.nextDouble();
                }while(temp>cut);
                ans[i]=x;
            }
            
        }
        else{
            double s,lm,y;
             s=Math.sqrt(2.0*mean);
                lm=Math.log(mean);
                cut=mean*lm-Gamma.logGamma(1.0+mean);
            for(int i=0;i<n;i++){
              do{
                  do{
                      y=Math.tan(PI*rnum.nextDouble());
                      x=s*y+mean;
                  }while(x<0.0);
                  x=Math.floor(x);
                  temp=0.9*(1.0+y*y)*Math.exp(x*lm-Gamma.logGamma(x+1.0)-cut);
              }  while(rnum.nextDouble()>temp);
               
                ans[i]=x;
                
            }
                
        }
        return ans;
    }
    /**
     * Generates an array of $N(\mu,\sigma)$ random variates
     * @param mean The mean $\mu$
     * @param sd The standard deviation $\sigma$
     * @param n The number of values to generate
     * @return An array with the values generated
     *
     *Not very profound, this just wraps Java's built in function
     */
    public double [] Gaussian(double mean, double sd, int n) {
        double [] ans=new double[n];
        for(int i=0;i<n;i++) {
            ans[i] = mean + sd * rnum.nextGaussian();
        }
        return ans;
    }
    /**
     * This method returns an array of n Uniform[a,b] deviates.
     * @param a $a$ and $b$ are the endpoints of the interval
     * @param b $a$ and $b$ are the endpoints of the interval
     * @param n The number of values to generate
     * @return and array of values
     */
    public double [] Uniform(double a,double b,int n) {
        double [] ans=new double[n];
        for(int i=0;i<n;i++) {
            ans[i] = a + (b - a) * rnum.nextDouble();
        }
        return ans;
    }
    public float nextDouble(){return (float)rnum.nextDouble();}
    public boolean nextBoolean(){
      
        return rnum.nextBoolean();}
    
     public double  Uniform(double a,double b) {
        double  ans  = a + (b - a) * rnum.nextDouble();
        return ans;
    }
    /**
     * Generates an array of values sampled at random from a Gamma distribution.
     * 
     * Recall that Gamma_a is a waiting time for the $a$th arrival in a unit Poisson
     * process.
     * @param a The parameter $a\in\Integers$, $a\ge 1$ determining
     * the distribution.
     * @param n The number of values to generate
     * @return The array of values generated
     */
    public double [] Gamma(int a, int n) {
        double [] ans=new double[n];
        double x,y,s,u,v,t;
        int aa;
        if(a<1){System.out.println("RandomValue.Gamma(): bad value of a  = "+a); return null;} else if(a<10) {
            for(int i=0;i<n;i++) {
                x=1.0;
                for(int j=1;j<=a;j++) {
                    x *= rnum.nextDouble();
                }
                ans[i]= - Math.log(x);
            }
        } else{
            for(int i=0;i<n;i++) {
                do{
                    do{
                        do{
                            u=rnum.nextDouble();
                            v=2.0*rnum.nextDouble()-1.0;
                        }while (v*v+u*u> 1.0);
                        y= v/u;
                        aa=a-1;
                        s= Math.sqrt(2.0*aa+1);
                        x=s+y*aa;
                    } while(x<=0.0);
                    t=(1.0+y*y)*Math.exp(aa*Math.log(x/aa)-s*y);
                }while(rnum.nextDouble()>t);
                ans[i]=x;
                
            }
        }
        return ans;
    }
  
      public static double [] DoStats(int []x) {
        double s=0,ss=0;
        double [] results=new double[4];
        //if we have no or small samples
        if(x== null || x.length==0) {
            results[0]=results[1]=0.0;
        }
        if(x.length==1) {
            results[0]=x[0];
            results[1]=0.0;
        }
        //otherwise
        for(int i=0;i<x.length;i++) {
            s += x[i];
            ss += (x[i]*x[i]);
        }
        results[0]= s/x.length;
         results[1] = Math.sqrt((ss - x.length * results[0] * results[0]) / (x.length - 1));
        //results[1] =Math.sqrt(ss/x.length-results[0]*results[0]);
        double min,max;
        min=x[0];
        max=x[0];
        for(int i=0;i<x.length;i++)
        {
            if(x[i]<min) {
                min = x[i];
            }
            else if(x[i]>max) {
                max = x[i];
            }
        }
        results[2]=min;
        results[3]=max;
        return results;
    }
     
     
    public static double [] DoStats(double []x) {
        double s=0,ss=0;
        double [] results=new double[4];
        //if we have no or small samples
        if(x== null || x.length==0) {
            results[0]=results[1]=0.0;
        }
        if(x.length==1) {
            results[0]=x[0];
            results[1]=0.0;
        }
        //otherwise
        for(int i=0;i<x.length;i++) {
            s += x[i];
            ss += (x[i]*x[i]);
        }
        results[0]= s/x.length;
        results[1] =Math.sqrt((ss-x.length*results[0]*results[0])/(x.length-1));
        double min,max;
        min=x[0];
        max=x[0];
        for(int i=0;i<x.length;i++)
        {
            if(x[i]<min) {
                min = x[i];
            }
            else if(x[i]>max) {
                max = x[i];
            }
        }
        results[2]=min;
        results[3]=max;
        return results;
    }
    /**
     * Selects K values from the set ${0,1,2,\ldots,m-1}$
     * at random (equally likely) and returns them (unsorted)
     * in an array.
     * The resulting permutation is random.
     * @param K pick K distinct (without replacement) items
     * @param m Pick from the m values 0,1,...,m-1
     * @return The list of values selected
     */
    public int [] pickKfromM(int K,int m)
    {
        int [] X=new int[m];
        for(int i=0;i<m;i++) {
            X[i] = i;
        }
        for(int k=0;k<K;k++)
        {
            int t= rnum.nextInt(m-k);
            int temp=X[t];
            X[t]=X[m-k-1];
            X[m-k-1]=temp;
        }
        int []ans=new int[K];
        for(int j=0;j<K;j++) {
            ans[j] = X[m - 1 - j];
        }
        return ans;
                
    }
    /**
     * This function uses Knuth's "selection" algorithm
     * from volume 2
     * @param n the number to select
     * @param N we draw from 1,2,....,N
     * @return list of n selected at random, but in increasing order
     */
    public int[] selectnfromN(int n, int N)
    {
        int [] selected=new int[n];
        boolean done=false;
        int t,m;
        t=m=0;
        while(!done)
        {
            double u=rnum.nextDouble();
            if((N-t)*u>= n-m)
            {
                t++;
                continue;
            }
            selected[m]=t+1;
            m++;
            t++;
            if(m>=n) {
                done = true;
            }
        }
        return selected;
    }
    /**
     * a simple sorting routine
     * @param a the array of values to sort
     */
    public void shellsort(double [] a)
    {
	int i,j,k;
	double temp;
	k=1;
	do{
	    k *=3;
	    k++;
	}while(k<=a.length);
	do{
	    k /=3;
	    for(i=k;i<a.length;i++){
		temp=a[i];
		j=i;
		while(a[j-k]>temp){
		    a[j]=a[j-k];
		    j-= k;
		    if(j<k) break;
		}
		a[j]=temp;
	    }
	}while(k>1);
    }
   
 
    /**
     * Permutes the integers in an array
     * @param d An array of integers
     * @return The permuted list
     */
    public int [] permuteList(int [] d)
    {
        int [] index=pickKfromM(d.length,d.length);
        int []ans=new int[d.length];
        for(int i=0;i<d.length;i++)ans[i]=d[index[i]];
        return ans;
    }
    /**
     * Just for testing purposes
     * @param args unused
     */
    public static void main(String [] args) {
        RandomVariable rv=new RandomVariable();
        double []x=rv.Uniform(0.0, 2.0, 100);
        double [] results=rv.DoStats(x);
         System.out.println("Uniform(0,2) n=100 : mean="+results[0]+" sd="+results[1]+" Min/max ="+results[2]+"/"+results[3]);
        //System.out.println("Uniform(0,2): mean="+results[0]+" sd="+results[1]+" Min/max ="+results[2]+"/"+results[3]);
        //x=rv.Gaussian(0,1,100);
        //results=rv.DoStats(x);
        //System.out.println("Gaussian(0,1): mean="+results[0]+" sd="+results[1]+" Min/max ="+results[2]+"/"+results[3]);
         //x=rv.Gamma(2,100);
        //results=rv.DoStats(x);
        //System.out.println("Gamma(2): mean="+results[0]+" sd="+results[1]+" Min/max ="+results[2]+"/"+results[3]);
          x=rv.Poisson(1,100);
        results=rv.DoStats(x);
        System.out.println("Poisson(1): mean="+results[0]+" sd="+results[1]+" Min/max ="+results[2]+"/"+results[3]);
        //x=rv.Binomial(12,0.25,10);
        //results=rv.DoStats(x);
        //System.out.println("Binomial(12,0.25): mean="+results[0]+" sd="+results[1]+" Min/max ="+results[2]+"/"+results[3]);
        //int []a=rv.pickKfromM(4,7);
        //System.out.println(" "+a[0]+","+a[1]+","+a[2]+","+a[3]);
        //a=rv.permuteList(a);
        //System.out.println(" "+a[0]+","+a[1]+","+a[2]+","+a[3]);
        int [] ans=rv.selectnfromN(4,1000);
        System.out.println(" "+ans[0]+","+ans[1]+","+ans[2]+","+ans[3]);
        ans=rv.RandomInt(1, 5, 20);
        System.out.println(Arrays.toString(ans));

    }
}
