package parameters;

/*
 * 29 July 2011 adapted from the Garden program
 * to let user specify genration of random plants in
 * a rectangle instead of listing founders
 * explicitly
 * 
 * There's two different behaviours for which we allow.
 * If numberfemale is specified and >=0 we explicitly
 * generate that number of females.
 * 
 * Otherwise we pick the sex of each plant at random with
 * the specified probfemale probability.
 * 
 * Really yhr only place creating a RandomPlantList is
 * SimDataReader.
 * 
 * 19 Apr 2012 small changes to formatting of output in toXML()
 */


/**
 *
 * @author sep
 */
public class RandomPlantList {
    public int number,numberfemale;
    public int XL,YL,XH,YH;
    public float probfemale;
    public int agelo, agehi;
    public RandomPlantList()
    {
        number=0;
        numberfemale=0;
        probfemale=0.0f;
        
    }
    public boolean sameRect(RandomPlantList other)
    {
        if( XL==other.XL &&XH==other.XH &&YL==other.YL&& YH==other.YH)
        {return (true);}
        else{return(false);}
    }
    public String toXML()
    {
         StringBuilder sb=new StringBuilder();
        if(numberfemale==0)
        {
       
        sb.append("<RandomPlantList number =\" "+number+" \" XL=\""+XL+" \"");
        sb.append("XH=\""+XH+" \" YL=\" "+YL+" \" YH=\""+YH+"\"");
        sb.append("probfemale=\""+probfemale+"\" agelo=\""+agelo+"\" agehi="+agehi+"\"/>");
        }
        else
        {
        sb.append("<RandomPlantList number =\""+number+" \"");
        sb.append("numberfemale=\""+numberfemale+"\" XL=\""+XL+" \"");
        sb.append("XH=\""+XH+"\" YL=\""+YL+"\" YH=\""+YH+" \"");
        sb.append("probfemale=\""+probfemale+"\" agelo=\""+agelo+"\" agehi="+agehi+"\"/>\n");
        }
        return (sb.toString());
    } 
}
