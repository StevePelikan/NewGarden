package parameters;

/*
 * 
 * 29 July 2011 adapted the Founders program to generate a list
 * of plants with random locations.
 */

import biology.Location;
import biology.Plant;
import functions.RandomVariable;
import java.util.ArrayList;



/**
 *
 * Founders 
 * generates a list of plants as requested by a RandomPlantList
 * writes XML statements to describe founders
 * N is the number of founders to generate
 * xl,ylxh,yl are the limits of the grid on which toplace the founders
 * number, probfemale,agelow,agehigh
 * @author sep
 */

public class Founders {

    SimData sd;
    RandomVariable rv;
    RandomPlantList rpl;
  

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        //Founders founders=new Founders(args);

    }
    
    public ArrayList<Plant> calculate()
    {
         ArrayList<Plant> answer=new ArrayList<Plant>();
        if(!sd.CreateAllFounders)
        {
       
        int number_gridpoints=(rpl.YH-rpl.YL+1)*(rpl.XH-rpl.XL+1);
        int [] selected_points=rv.selectnfromN(rpl.number,number_gridpoints);
        selected_points=rv.permuteList(selected_points);
         int xwidth=rpl.XH-rpl.XL+1;
        //loop on requests
        int pointindex=0;
        if(rpl.numberfemale >=0)
        {//generate females
            for(int i=0;i<rpl.numberfemale;i++)
            {
                int thispoint=selected_points[pointindex]-1;
                pointindex++;
                int thisx= rpl.XL+(thispoint % xwidth);
                int thisy= rpl.YL+(thispoint-(thispoint%xwidth))/xwidth;
               // thisx=rv.Uniform(rpl.XL, rpl.XH);
                //thisy=rv.Uniform(rpl.YL, rpl.YH);
                int ourage=-1*rv.RandomInt(rpl.agelo, rpl.agehi);
                Plant p=new Plant(sd.number_loci,ourage,
                        new Location(thisx,thisy),true,true);
                if(sd.region.inRegion(p.location.X, p.location.Y))
                {
                answer.add(p);
            }

            }
            for(int i=rpl.numberfemale;i<rpl.number;i++)
            {//generate the males
                int thispoint=selected_points[pointindex]-1;
                pointindex++;
                int thisx= rpl.XL+(thispoint % xwidth);
                int thisy= rpl.YL+(thispoint-(thispoint%xwidth))/xwidth;
                int ourage=-1*rv.RandomInt(rpl.agelo, rpl.agehi);
                Plant p=new Plant(sd.number_loci,ourage,
                        new Location(thisx,thisy),false,true);
                
                 if(sd.region.inRegion(p.location.X, p.location.Y)) answer.add(p);
            }
        }
        else
        {//we don't don't care about absolute sex and pick at random
            for(int i=0;i<rpl.number;i++)
            {
                int thispoint=selected_points[pointindex]-1;
                pointindex++;
                int thisx= rpl.XL+(thispoint % xwidth);
                int thisy= rpl.YL+(thispoint-(thispoint%xwidth))/xwidth;
                int ourage=-1*rv.RandomInt(rpl.agelo, rpl.agehi);
                boolean oursex=false;
                if(rv.Uniform(0, 1)<rpl.probfemale) oursex=true;
                Plant p=new Plant(sd.number_loci,ourage,
                        new Location(thisx,thisy),oursex,true);
                if(sd.region.inRegion(p.location.X, p.location.Y))  answer.add(p);
            }
        }
      

        }
        else //Keep trying to create all the requested founders
        { //WE CAN NOW USE Region.pickNPoints() to get a list.
            //two things to eliminate unbounded runtime
        
            //loop to select a list of length rpl.number points
            //from the rectangle as follows
            //ask for the right number, eliminate those that aren't
            //in the region
            //then, if necessary as for 20 more than we need to make op the difference
            //randomize their order
            //and take them one at a time testing to see if they're already in  out list
            
            //DEC 2013 THIS ISN'T IMPLEMENTED YET!!!
        int number_gridpoints=(rpl.YH-rpl.YL+1)*(rpl.XH-rpl.XL+1);
        int [] selected_points=rv.selectnfromN(rpl.number,number_gridpoints);
        selected_points=rv.permuteList(selected_points);
         int xwidth=rpl.XH-rpl.XL+1;
        //loop on requests
        int pointindex=0;
        if(rpl.numberfemale >=0)
        {//generate females
            for(int i=0;i<rpl.numberfemale;i++)
            {
                int thispoint=selected_points[pointindex]-1;
                pointindex++;
                int thisx= rpl.XL+(thispoint % xwidth);
                int thisy= rpl.YL+(thispoint-(thispoint%xwidth))/xwidth;
               // thisx=rv.Uniform(rpl.XL, rpl.XH);
                //thisy=rv.Uniform(rpl.YL, rpl.YH);
                int ourage=-1*rv.RandomInt(rpl.agelo, rpl.agehi);
                Plant p=new Plant(sd.number_loci,ourage,
                        new Location(thisx,thisy),true,true);
                if(sd.region.inRegion(p.location.X, p.location.Y))
                {
                answer.add(p);
            }

            }
            for(int i=rpl.numberfemale;i<rpl.number;i++)
            {//generate the males
                int thispoint=selected_points[pointindex]-1;
                pointindex++;
                int thisx= rpl.XL+(thispoint % xwidth);
                int thisy= rpl.YL+(thispoint-(thispoint%xwidth))/xwidth;
                int ourage=-1*rv.RandomInt(rpl.agelo, rpl.agehi);
                Plant p=new Plant(sd.number_loci,ourage,
                        new Location(thisx,thisy),false,true);
                
                 if(sd.region.inRegion(p.location.X, p.location.Y)) answer.add(p);
            }
        }
        else
        {//we don't don't care about absolute sex and pick at random
            for(int i=0;i<rpl.number;i++)
            {
                int thispoint=selected_points[pointindex]-1;
                pointindex++;
                int thisx= rpl.XL+(thispoint % xwidth);
                int thisy= rpl.YL+(thispoint-(thispoint%xwidth))/xwidth;
                int ourage=-1*rv.RandomInt(rpl.agelo, rpl.agehi);
                boolean oursex=false;
                if(rv.Uniform(0, 1)<rpl.probfemale) oursex=true;
                Plant p=new Plant(sd.number_loci,ourage,
                        new Location(thisx,thisy),oursex,true);
                if(sd.region.inRegion(p.location.X, p.location.Y))  answer.add(p);
            }
        }
        }
         
  return (answer);
    }
 public Founders(SimData s,RandomPlantList r) {
        rv = new RandomVariable();
        sd=s;
        rpl=r;
       
    }
   
}
