package Leslie;

import java.util.Arrays;
import parameters.MyFormat;

/**
Leslie is a class that contains a Leslie matrix M
giving age-dependent survival and reproduction rates.
We suppose that we have age classes 0,1,2,...n

M[0][j] is the expected number of offspring generated per time period by
someone in age class $j$

M[a][a-1] is the fraction of those in age class $a-1$ that survive to enter age class $a$. Here $1\le a \le n$.

Example: 
\[M = \left\(\begin{array}{ccccc}
0&0.5&2&2&2\\
0.5&0&0&0&0\\
0&1&0&0&0\\
0&0&1&0&0\\
0&0&0&0.5&0
\end{array}\right\)

If $N=(n_0,n_1,\ldots,n_n)^T$ is a vector giving the number in each ageclass
then $M\cdot N$ is the number in each age class during the following time period.
The population size grows as $p(t) = |M^t\cdot N|_1$, so within a multiplicitive constant, the (exponential) growth rate of the population is given as the eigen value of largest modulus of $M$. There's presumably as theorem about matricies of the form of $M$ that ensures this eigenvalue is positive.

The stable age distribution is the corresponding eigenvector.

The calculate() method of this class finds and reports 
 * (to stdout, though we will eventually have it put it in a string)
 * the intrinsic growth rate and the stable ave distribution.

The approximate() method takes an initial distribution and projects it 
 * foreward several years, computing the (exponential) population growth
 * rate in the first few years. Differences between this observed rate and
 * the maximal rate reported by calculate() can be attributed to the fact
 * that the initial age distribution is not that same as the stable age distribution.
 * That is, the maximal rate is only obtained when the age distribution is the stable
 * (limiting) age distribution.

 */
public class Leslie
{
    public Matrix M;
    private int dimension;
    private MyFormat mf;
    public Leslie(Matrix lt)
    {
	//if lt isn't square, we have a problem
	M=lt;
        dimension=M.rows();
        mf=new MyFormat();
    }
    /**
       rrate[0..n] is the age-dependent reproduction rate
       mrate[0..n-1] is the age-dependent mortality rate
       The matrix M gets first row equal to rrate
       and subdiagonal equal to mrate.
       
     */
    public Leslie(double[] rrate,double [] mrate)
    {
	//if rrate isn't (n+1) x 1 and mrate isn't n x 1
	//we have a problem
	//M is new Matrix(n+1,n+1)
        mf=new MyFormat();
	if(mrate.length != rrate.length-1)
	    {
		System.out.println("Bad dimensions in Leslie(double [],double)");
	    }
	M=new Matrix(rrate.length,rrate.length);
        dimension=rrate.length;
	for(int i=0;i<rrate.length;i++) M.set(0,i,rrate[i]);//i=0..n
	for(int i=1;i<rrate.length;i++)M.set(i,i-1,1.0-mrate[i-1]); //i=1..n
    }
    /**Power method for largest eigenvalue, vector
     * 
     * @return 
     */
    public String quickCalculate()
    {
        StringBuilder ans=new StringBuilder();
        Matrix V=new Matrix(M.rows(),1);
        for(int i=0;i<V.rows();i++) V.set(i, 0, 1);
        boolean done=false;
        while(! done)
        {
            Matrix W= M.times(V);
            double cosangle=(W.transpose().times(V)).get(0,0)/(W.norm2()*V.norm2());
            //System.out.println(cosangle);
            V=W.timesEquals(1.0/W.norm2());
            if(cosangle >0.99999) done=true;
            
        }
        Matrix W= M.times(V);
        double lambda = W.norm2()/V.norm2();
        ans.append("lambda= "+lambda+"\n");
        ans.append("Equilibrium distribution: [");
        V=V.times(100);
        for(int i=0;i<V.rows()-1;i++) ans.append(mf.format(V.get(i, 0))+",");
        ans.append(mf.format(V.get(V.rows()-1, 0))+"]");
        return ans.toString();
    }
    public String calculate()
    {
        StringBuffer sb=new StringBuffer();
        sb.append("Leslie matrix is:\n");
        sb.append(printMatrix(M,5,2));
        //M.print(5,2);
	//EigenvalueDecomposition EVD=new EigenvalueDecomposition(M);
        EigenvalueDecomposition EVD=M.eig();
	double [] ev=EVD.getRealEigenvalues();
        //find the biggest
        int ind=0;
        double max=-10000;
        for(int i=0;i<ev.length;i++)
        {
            if(ev[i]>max){max=ev[i]; ind=i;}
        }
        sb.append("\nGrowth factor = "+max+ "\ngrowth rate (i.e. the log())="+Math.log(max)+"\n");
        //System.out.println("Growth factor = "+max+ "\ngrowth rate (i.e. the log())="+Math.log(max));
	//for(int i=0;i<ev.length;i++) System.out.println(ev[i]);
       // System.out.println("Stable age distribtuion is in col = "+ind);
       // EVD.getV().print(5,2);
        Matrix V=EVD.getV();
        Matrix Stable=V.getMatrix(0,V.getRowDimension()-1,ind,ind);
        if(Stable.get(0,0)<0) Stable.timesEquals(-1);
        sb.append("Stable age distribution is:\n"+printMatrix(Stable, 5,2));
        //System.out.println("Stable age distribution is: ");
        //Stable.print(5,2);
        return sb.toString();
    }
    public String approximate(double [] initial_age_dist,int years)
    {
	if(initial_age_dist.length!= M.getRowDimension())
	    {
		//we have a problem
            System.out.println("Problem with dimensions inapproximate()");
            System.exit(0);
	    }
        StringBuffer sb=new StringBuffer();
	Matrix N0=new Matrix(initial_age_dist.length,1);
	for(int i=0;i<initial_age_dist.length;i++) N0.set(i,0,initial_age_dist[i]);
	double []population=new double[years];
	population[0]=N0.norm1();
	Matrix N1=  N0.copy();
	for(int i=1;i<years;i++)
	    {
		N1=M.times(N1);
		population[i]=N1.norm1();
	    }
	//find initial growth rates
	double [] rates=new double[years];
	for(int i=1;i<years;i++)
	    {
		rates[i]=(1.0/i)*Math.log(population[i]/(population[0]));
	    }
        sb.append("Actual population and growth rates:\n");
        sb.append("Initial population:"+printMatrix(N0,5,2));
	//System.out.println("Actual population and growth rates:");
	//System.out.println("Initial population:");
	//N0.print(5,2);
        //System.out.println(printMatrix(M,5,2));
        sb.append("Year\tpopulation\trate\n");
	//System.out.println("Year population rate");
        java.text.NumberFormat nf=java.text.NumberFormat.getInstance();
        nf.setMaximumFractionDigits(4);
	for(int i=0;i<years;i++)
	    {
            sb.append(""+i+"\t"+population[i]+"\t"+nf.format(rates[i])+"\n");
		//System.out.println(""+i+" "+population[i]+" "+rates[i]);
	    }
        return sb.toString();
    }
    private String printMatrix(Matrix M, int w, int d)
    {
      java.text.NumberFormat nf=java.text.NumberFormat.getInstance();
      nf.setMaximumFractionDigits(d);
      nf.setMinimumFractionDigits(d);
      nf.setMaximumIntegerDigits(w-d);
      nf.setMinimumIntegerDigits(1);
      StringBuffer sb=new StringBuffer();
      for(int r=0;r<M.getRowDimension();r++)
      {
          sb.append("\n");
          for(int c=0;c<M.getColumnDimension();c++)
          {
              String t=nf.format(M.get(r,c));
              if(t.length()<1+w+d)
              {
                  for(int a=0;a<1+w+d-t.length();a++)
                      t = " ".concat(t);
              }
              sb.append(" "+t);
          }
      }
      sb.append("\n");
      return sb.toString();
    }
    public static void main(String [] args)
    {
	double []rrate={0,0.5,2,2,2};
	double []mrate={0,0,0.5,1};
        Leslie les=new Leslie(rrate,mrate);
        System.out.println(les.calculate());
        System.out.println(les.quickCalculate());
        double []init={5,5,3,12,0};
        System.out.println(les.approximate(init, 8));
      
        /*double []n0={1,0,0,0,0};
        Matrix N0=new Matrix(5,1);
        for(int i=0;i<5;i++) N0.set(i,0,n0[i]);
        for(int count=0;count<30;count++)
        {
            N0=les.M.times(N0);
            double norm=N0.norm1();
            N0.timesEquals(1.0/norm);
        }
        Matrix N1= les.M.times(N0);
        double r=N1.norm1()/N0.norm1();
        System.out.println("R= "+r);
         **/
    }
    //project vector V ahead k years
    //find stable age distribution
    //find population growth rate
}
