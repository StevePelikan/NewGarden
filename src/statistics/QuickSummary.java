/*
 * Copyright (C) 2014 pelikan
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package statistics;

import biology.Plant;
import java.util.List;
import parameters.SimData;

/**
 *
 * @author pelikan
 * 
 * This class makes a summary of a List of plants handed
 * to it and returns the fields as an List<String>
 * 
 * It is to be used to accumulate replicates of runs and seed sampling
 * methods independently 
 * 
 * We can dump seeds and maternal parents from different runs
 * and, selecting seeds by different methods, get something that looks like
 * 
 * run,subregion,seed_collection_method,seeds collected,alleles,Ho,He,F
 * 
 * 
 */
public class QuickSummary {
    List<Plant> pop;
    SimData sd;
    static final String[] labels={"seeds_collected","alleles","Ho","He","F"};
    public QuickSummary(List<Plant> population,SimData sd)
    {
        this.pop=population;
        this.sd=sd;
    }
    /**
     * 
     * @return
     * 
     * A locus with $a$ alleles has $a+ a*(a-1)/2$ possible genotypes
     * we can just put this in a $a^2$ array and agree to always use the smaller
     * index first
 
    */
    double [][] count_alleles()
    {
        double[][] ans=new double[sd.number_loci][sd.max_alleles];
        for(Plant p:pop)
        {
            for(int l=0;l<sd.number_loci;l++)
            {
                ans[l][p.allele[l][0]]++;
                ans[l][p.allele[l][1]]++;
            }
        }
        return ans;
       
    }
    
    int  count_homozygous()
    {
        int count=0;
        for(Plant p: pop)
        {
        for(int l=0;l<sd.number_loci;l++)
        {
            if(p.allele[l][0]==p.allele[l][1]) count++;
        }
        }
        
        return count;
    }
    int count_homozygous(int locus)
    {
        int count=0;
        for(Plant p: pop)
        {
       
            if(p.allele[locus][0]==p.allele[locus][1]) count++;
        
        }
        
        return count;
    }
    double [] calculate()
    {
        double [] ans= new double[5];
        ans[0]=pop.size();
        if(pop.size()==0) return ans;
        //number alleles
        double [][] allele_counts=count_alleles();
        int alls=0;
        for(int l=0;l<sd.number_loci;l++)
        {
            for(int a=0;a<sd.max_alleles;a++)
            {
                if(allele_counts[l][a]>0) alls++;
            }
        }
        ans[1]=alls;
        //observed (actual) fraction of the loci that are heterozygous 
        ans[2]= 1.0-(1.0f*count_homozygous())/(pop.size()*sd.number_loci);
        
        //expected H
        //find frequencies
        double twoN=2*pop.size();
        for(int l=0;l<sd.number_loci;l++)
        {
            for(int a=0;a<sd.max_alleles;a++)
            {
                allele_counts[l][a]/=twoN;
            }
        }
        //gene diversity, HW expected He
        double sum=0.0;
        double locusdiversity = 0.0;
        for(int l=0;l<sd.number_loci;l++)
        {
            double sumsq=0.0;
            for(int a=0;a<sd.max_alleles;a++)
            {
                double ss=(allele_counts[l][a]*allele_counts[l][a]);
                sumsq+= ss;
            }
             locusdiversity += (1 - sumsq);
            //he is if fraction of people plants we expect heter0 at this
            //locus assuming HW
        }
        ans[3]=locusdiversity/sd.number_loci;
        ans[4]=0.0f;
        
         double He = 0, Ho = 0;
        for(int l=0;l<sd.number_loci;l++)
        {
            //Ho is fraction heterozygous
            Ho=1.0-(1.0*count_homozygous(l))/pop.size();
            double sumsq=0.0;
            for(int a=0;a<sd.max_alleles;a++)
            {
                double ss=(allele_counts[l][a]*allele_counts[l][a]);
                sumsq+= ss;
            }
            He=1-sumsq;
            double fix = Ho / pop.size();
             //double fix = 0;
             double fis;
                    if(He==0 && Ho==0) {fis=1.0;}
                    else{
                    fis= (He - Ho + fix) / (He + fix);}
             ans[4]+= fis;      
        }
        ans[4]/= sd.number_loci;
        return ans;
    }
}
