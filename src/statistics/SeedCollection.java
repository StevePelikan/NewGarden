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

import Leslie.Matrix;
import biology.Location;
import biology.Plant;
import biology.Rect;
import biology.Region;
import functions.RandomVariable;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.System.exit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import parameters.MyFormat;
import parameters.SimData;

/**
 *
 * @author pelikan
 *
 *
 *
 *
 * A sampling method made up of three things 1) we select some or all of the
 * maternal plants based on summary region then all, or based on size youngest,
 * oldest (quartile, third?) order: by size, random, nearest neighbor, transect
 * collect --- from up to (as available) N maternal parents, up to (as
 * available) K seeds from each ---sequentially from maternal parents, but to K
 * seeds from each to reach a total of S if possible
 *
 * String[] seedprocedure{SEEDSELECT.ALL,SEEDORDER.RANDOM,SEEDCOLLECT.KTOS}
 */
class collectionstrategy {

    public collectionstrategy() {
    }

    public collectionstrategy(String s) {
        String[] values = s.split(",");
        assert values.length == 12 : "Not enough data in defining strategy " + s;
        N = Integer.parseInt(values[0]);
        K = Integer.parseInt(values[1]);
        S = Integer.parseInt(values[2]);
        replicates = Integer.parseInt(values[3]);
        select = SEEDSELECT.valueOf(values[4]);
        order = SEEDORDER.valueOf(values[5]);
        collect = SEEDCOLLECT.valueOf(values[6]);
        sizefraction = Float.parseFloat(values[7]);
        steplow = Integer.parseInt(values[8]);
        stephigh = Integer.parseInt(values[9]);
        diameter = Integer.parseInt(values[10]);
        label = values[11];
    }
    int N, K, S;
    int replicates;
    SEEDSELECT select;
    SEEDORDER order;
    SEEDCOLLECT collect;
    float sizefraction;
    int steplow, stephigh, diameter;
    //here diameter SHUOLD be radius!!!
    String label;
    static Location[] directions = {new Location(1, 0), new Location(1, 1), new Location(0, 1), new Location(-1, 1), new Location(-1, 0), new Location(-1, -1), new Location(0, -1), new Location(1, -1)};

    String toCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append("" + N + "," + K + "," + S + "," + replicates + ",");
        sb.append(select + "," + order + "," + collect + ",");
        sb.append("" + sizefraction + "," + steplow + "," + stephigh + "," + diameter + "," + label);
        return sb.toString();
    }

    String headers() {
        return "N,K,S,replicates,select,order,collect,sizefraction,steplow,stephigh,diameter,label";
    }
}

public class SeedCollection {
private static boolean DUMPSELECTEDMOMS=true;
private static boolean PRINTDEBUGINFO=true;
Plant lastplant = null;
    String seedfilename;
    String maternalfilename;
    SimData sd;
    int subregion;
    int run;
    int ourstrategy;
    int rep;
    String simdatafilename;
    String outputfilename;
    String strategiesfilename;
    Stats stats;
    private BufferedReader br;
    RandomVariable rv;
    int N, K, S;
    int replicates;
    collectionstrategy[] strategies;
    MyFormat mf;
    Image ourImage;

    public SeedCollection(String[] args) {
        assert args.length == 5 : "not enough arguments to make a seedcollection";
        if (args.length != 5) {
            System.out.println("To make a seed collection:"
                    + "seedfilename, parentalfilename,simdatafilename,outputfilename,strategiesfilename");
        }
        seedfilename = args[0];
        maternalfilename = args[1];
        simdatafilename = args[2];
        outputfilename = args[3];
        strategiesfilename = args[4];
        sd = SimData.readXMLFile(simdatafilename);
        rv = new RandomVariable();
        mf = new MyFormat();
        strategies = readStrategies();
        if (sd.makeMap) {
            ourImage = new Image(sd);
            ourImage.setPixelsPerSite(sd.pixelsPerSite);
            ourImage.drawMap();
        }
        mainLoop();

    }

    private collectionstrategy[] readStrategies() {
        ArrayList<String> inputlines = new ArrayList<String>();
        try {

            br = new BufferedReader(new FileReader(strategiesfilename));
            //read header and discard
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                inputlines.add(line);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        collectionstrategy[] answer = new collectionstrategy[inputlines.size()];
        for (int i = 0; i < answer.length; i++) {
            answer[i] = new collectionstrategy(inputlines.get(i));
        }

        return answer;
    }

    public SeedCollection() {
        seedfilename = "seedcollection.csv";
        //seedfilename=args[0];
        maternalfilename = "parental-seedcollection.csv";
        //simdatafilename="MasterSimData.xml";
        simdatafilename = "SeedCollection.xml";
        // maternalfilename=args[1];
        outputfilename = "seedstats.csv";
        sd = SimData.readXMLFile(simdatafilename);
        // sd=SimData.readXMLFile(args[2]);
        stats = new Statistics(sd);
        rv = new RandomVariable();
        mf = new MyFormat();

        strategies = new collectionstrategy[3];
        strategies[0] = new collectionstrategy();
        strategies[0].K = 1;
        strategies[0].N = 172;
        strategies[0].S = 172;
        strategies[0].select = SEEDSELECT.ALL;
        strategies[0].order = SEEDORDER.RANDOM;
        strategies[0].collect = SEEDCOLLECT.KTOS;
        strategies[0].sizefraction = 1.0f;
        strategies[0].replicates = 3;
        strategies[0].steplow = 1;
        strategies[0].stephigh = 1;
        strategies[0].diameter = 1;

        strategies[0].label = "AllRandK2S";

        strategies[1] = new collectionstrategy();
        strategies[1].K = 1;
        strategies[1].N = 172;
        strategies[1].S = 172;
        strategies[1].select = SEEDSELECT.ALL;
        strategies[1].order = SEEDORDER.TRANSECT;
        strategies[1].collect = SEEDCOLLECT.KTOS;
        strategies[1].sizefraction = 1.0f;
        strategies[1].replicates = 3;
        strategies[1].steplow = 1;
        strategies[1].stephigh = 1;
        strategies[1].diameter = 3;
        strategies[1].label = "AllRandKtS";

        strategies[2] = new collectionstrategy();
        strategies[2].K = 20;
        strategies[2].N = 200;
        strategies[2].S = 4000;
        strategies[2].select = SEEDSELECT.ALL;
        strategies[2].order = SEEDORDER.BIGTOSMALL;
        strategies[2].collect = SEEDCOLLECT.NANDK;
        strategies[2].sizefraction = 1.0f;
        strategies[2].replicates = 3;
        strategies[2].steplow = 1;
        strategies[2].stephigh = 1;
        strategies[2].diameter = 1;
        strategies[2].label = "AllBtSNK";
        if (sd.makeMap) {
            ourImage = new Image(sd);
            ourImage.setPixelsPerSite(sd.pixelsPerSite);
            ourImage.drawMap();
        }
        for (collectionstrategy cs : strategies) {
            System.out.println(cs.toCSV());
        }
        mainLoop();
     //   ArrayList<Plant> temp = readCSV(maternalfilename, 0);

//        System.out.println("Here is the original list " + temp.size() + " plants");
//        for (Plant p : temp) {
//            System.out.print(p.toXML());
//        }
//
//        temp = this.selectMoms(temp, 1);
//        System.out.println("Here is the method 1 selection " + temp.size() + " plants");
//        for (Plant p : temp) {
//            System.out.print(p.toXML());
//        }
//
//        temp = this.selectMoms(temp, 2);
//        System.out.println("Here is the method 2 selection " + temp.size() + " plants");
//        for (Plant p : temp) {
//            System.out.print(p.toXML());
//        }
        //first collection method N=20 plants and K=20 seeds from each
//        int N = 20;
//        int K = 20;
//        int collectionregion = 0;
//        for (Region r : sd.summaryregions.Regions) {
//            System.out.println("Collection region = " + collectionregion);
//            collectionregion++;
//            //make a stats and simdata for tabulating seeds
//            //selected in this subregion over all the runs
//            //simdata should have no summary regions.
//            SimData statssimdata = SimData.readXMLFile(simdatafilename);
//            statssimdata.summaryregions.Regions.clear();
//            statssimdata.current_year = 0;
//            statssimdata.number_generations = 1;
//
//            Statistics stats = new Statistics(statssimdata);
//
//            for (int run = 0; run < sd.number_runs; run++) {
//                statssimdata.run_number = run;
//                ArrayList<Plant> maternalplants = this.readCSV(maternalfilename, run);
//                ArrayList<Plant> seedscollected = new ArrayList<Plant>();
//                System.out.println("run number = " + run + " maternal plants = " + maternalplants.size());
//                ArrayList<Plant> thesemoms = new ArrayList<Plant>();
//                for (Plant p : maternalplants) {
//                    if (r.inRegion(p.location.X, p.location.Y)) {
//                        thesemoms.add(p);
//                    }
//                }
//                System.out.println("subregion has = " + thesemoms.size());
//                //1 for random select
//                //2 for random start them nearest neighbor
//                thesemoms = selectMoms(thesemoms, 4);
//                int howmanymoms = Math.min(thesemoms.size(), N);
//                System.out.println("howmanymoms = " + howmanymoms);
//                ArrayList<Plant> seeds = readCSV(this.seedfilename, run);
//
//                for (int mom = 0; mom < howmanymoms; mom++) {
//                    Plant M = thesemoms.get(mom);
//              //select seeds from this mom and this run
//                    //and put in seedscollected
//
//                    ArrayList<Plant> kids = new ArrayList<Plant>();
//                    for (Plant kid : seeds) {
//                        if (kid.parent1 == M.my_id_no) {
//                            kids.add(kid);
//                        }
//                    }
//
//                    int howmanykids = Math.min(K, kids.size());
//                    System.out.print("\t" + howmanykids + ",");
//                    int[] index = rv.pickKfromM(howmanykids, kids.size());
//                    for (int i = 0; i < index.length; i++) {
//                        seedscollected.add(kids.get(index[i]));
//                    }
//                    System.out.println("");
//                }//end of loop over moms
//                //make a summary for this run and year 0 of the seeds selected
//                stats.makeSummary(seedscollected, statssimdata);
//            }//end loop over runs
//            System.out.println(stats.fullReport());
//        }
    }

    /*
     The main thing we do is
  
     For each method of collecting seeds (sampling maternal parents, selecting seeds)
  
     int N=25; //number maternal plants to consider
     int K=10; //number of seeds to try for from each mom
     int S=250; // number of seeds to aim for
  
     for each run
     read in maternal plants
     for each subregion select maternal plants by some method
     from that list of maternal plants select seeds by some method
     compute summary statistics for that seed collection
     (adjust run_number by hand in sd and remove the summary regions
     since we operate by region of maternal parent not location to which
     seed would disperse.)
    
     */
    ArrayList<Plant> readCSV(String filename, int ourrun) {
        String line;
        String cvsSplitBy = ",";
        ArrayList<Plant> ans = new ArrayList<Plant>();

        try {

            br = new BufferedReader(new FileReader(filename));
            //read header and discard
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                int run = Integer.parseInt(values[1]);
                if (run == ourrun) {
                    ans.add(Plant.fromCSVString(line, sd, 2));
                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ans;
    }

    /*
     * 
     * We will have several different ways of selecting maternal parents
     to sample
     1) at random from a region up to a total of N plants sampled, taking up
     to K seeds from each plant. Optionally continue until S seeds obtained
    
     2) starting at a random plant and proceeding to nearest neighbor
     selecting up to a maximum of K seeds until a total of S seeds is obtained.
    
     3) Look along a transect selecting plants up to N, talking up to K seeds
     from each plant or optionally continue to end of transect or a total
     of S seeds which ever comes first
    
     4) Sort the maternal plants intoorder by age. Then we can go in
     small to large or large to small order.
    
     To do 1 we select all the maternal plants in a subregion into a
     list and then permute the list
    
     To do 2 we generate a list of maternal plants 
     and build a matrix of distances
     To do 3 
    
    
    
     ----SelectMoms gives us a list of maternal parents
     --- select the first N of them
     ---select up to K random seeds from each
    
     ---Obtain S seeds by selecting up to K at random
     from each Mom in turn to get a total of S seeds
     */
    /*
     final ArrayList<Plant> selectMoms(ArrayList<Plant> subregionlist, int method) {
     ArrayList<Plant> ans = new ArrayList<Plant>();

     //optionally sort subregionlist by size
     //IF WE ARE STARTIFIED, BREAK list INTO SIZE CLASSES
     switch (method) {
     case 1://SEEDORDER.RANDOM
     int[] index = new int[subregionlist.size()];
     for (int i = 0; i < index.length; i++) {
     index[i] = i;
     }
     index = rv.permuteList(index);
     for (int i = 0; i < index.length; i++) {
     ans.add(subregionlist.get(index[i]));
     }
     break;
     case 2://SEEDORDER.NEARNEIGHBOR
     Matrix M = new Matrix(subregionlist.size(), subregionlist.size());
     for (int i = 0; i < subregionlist.size(); i++) {
     for (int j = 0; j <= i; j++) {
     M.set(i, j, subregionlist.get(i).location.dist(subregionlist.get(j).location));
     M.set(j, i, M.get(i, j));
     }
     }
     ArrayList<Integer> contenders = new ArrayList<Integer>();
     for (int i = 0; i < subregionlist.size(); i++) {
     contenders.add(i);
     }
     int current = contenders.get(rv.RandomInt(0, contenders.size()));
     ans.add(subregionlist.get(current));
     contenders.remove(current);
     while (contenders.size() > 1) {
     double value = 1e100;
     int where = -1;
     for (int j : contenders) {
     double temp = M.get(current, j);
     if (temp < value) {
     value = temp;
     where = j;
     }
     }
     current = where;
     ans.add(subregionlist.get(current));
     contenders.remove(Integer.valueOf(current));

     }
     ans.add(subregionlist.get(contenders.get(0)));
     //pick a starting point and remove it from list to consider
     //add it to our answer
     //while there's still stuff to consider
     //pick the nearest of them to our current point
     //remove it from consideration, and add it to ans
     break;
     case 3:\\ sorthighest to lowest //SEEDORDER.BIGTOSMALL

     Collections.sort(subregionlist, new Comparator<Plant>() {
     public int compare(Plant s1, Plant s2) {
     return s2.dob - s1.dob;
     }
     });
     ans = subregionlist;
     break;

     case 4: //sort lowest to highest //SEEDORDER.SAMLLTOBIG

     Collections.sort(subregionlist, new Comparator<Plant>() {
     public int compare(Plant s1, Plant s2) {
     return s1.dob - s2.dob;
     }
     });
     ans = subregionlist;
     break;
     }

     return ans;
     }
     */
    public float collectsubregion(Region ourregion,List<Plant> moms, List<Plant> seeds,
            collectionstrategy strategy, ArrayList collected, float pathlength) {
        double[] distances = null;
        int thispathlength;
        //SELECT moms to visit ALL BIG (sort to decreasing age truncate) SMALL (sort to increasing age truncate )
        ArrayList<Plant> temp;
        int[] index = new int[moms.size()];
        for (int i = 0; i < index.length; i++) {
            index[i] = i;
        }
        temp = new ArrayList<Plant>();
        index = rv.permuteList(index);
        for (int i = 0; i < index.length; i++) {
            temp.add(moms.get(index[i]));
        }
        moms = temp;
        switch (strategy.select) {
            case ALL:
                //nothing to do
                break;
            case BIG:
                //sort from big to small
                Collections.sort(moms, new Comparator<Plant>() {
                    public int compare(Plant s1, Plant s2) {
                        return s2.dob - s1.dob;
                    }
                });
                //select required fraction from the list
                if (strategy.sizefraction != 1.0f) {
                    moms = moms.subList(0, (int) Math.floor(strategy.sizefraction * moms.size()));
                }
                break;
            case SMALL:

                Collections.sort(moms, new Comparator<Plant>() {
                    public int compare(Plant s1, Plant s2) {
                        return s1.dob - s2.dob;
                    }
                });
                if (strategy.sizefraction != 1.0f) {
                    moms = moms.subList(0, (int) Math.floor(strategy.sizefraction * moms.size()));
                }
                break;
            case SIZESTRATIFIED:
                //sort by size and break into several groups, each of which will
                //be ordered and collected from.
                //in this case sizefraction tells us how many age classes
                //to use
                break;
        }

        //ORDER visits to moms
        switch (strategy.order) {
            case RANDOM:
                //make a random permutation
                index = new int[moms.size()];
                for (int i = 0; i < index.length; i++) {
                    index[i] = i;
                }
                temp = new ArrayList<Plant>();
                index = rv.permuteList(index);
                for (int i = 0; i < index.length; i++) {
                    temp.add(moms.get(index[i]));
                }
                moms = temp;
                break;
            case BIGTOSMALL:
                //sort big to small
                Collections.sort(moms, new Comparator<Plant>() {
                    public int compare(Plant s1, Plant s2) {
                        return s2.dob - s1.dob;
                    }
                });
                break;
            case SMALLTOBIG:
                //sort small to big
                Collections.sort(moms, new Comparator<Plant>() {
                  
                    public int compare(Plant s1, Plant s2) {
                        return s1.dob - s2.dob;
                    }
                });
                break;

            case TRANSECT2:
                System.out.println("Sorting on a transect2 in rectangle"+ourregion.toXML());
                //make a copy of moms that we can modify
                ArrayList<Plant> ourcandidates = new ArrayList<Plant>();
                ourcandidates.addAll(moms);
                //and a place to store the sorted plants in the order
                //we encounter them on the transect
                temp = new ArrayList<Plant>();
                //and a place to store the cumulative distance to the
                //plants along the transect
                ArrayList<Double> ourdistances = new ArrayList<Double>();
                
                //and the current total path length
                int ourpathlength = 0;
                //Do a loop to walk the transect and add parents to the
                //list in temp.
                //we stop if all the candidates are used or we've walked too
                //far
                //WE assume we are in a single Rectangle, perhaps as a Region

                boolean done = false;
                //HERE WE NEED TO DETERMINE WHICH RECT OF THE SUMMARY REGION
                //WE ARE WORKING IN, OR HAVE IT PASSED TO US
               // Region ourRegion = sd.summaryregions.Regions.get(subregion);
                //Rect ourRect = new Rect(ourRegion.XL, ourRegion.YL, ourRegion.XH, ourRegion.YH);
                //note that Region extends Rect so we could just use ourRegion!
                
               
                
                Location ourcurrent,
                 ourdirection;

                int boundarydirection = 1; //+-1
                 if (rv.nextBoolean()) {
                    boundarydirection = -1;
                }
                int boundarystep;
                boundarystep = 2 * strategy.diameter;
                int thisstep;

                //Start at a random point on the boundary
                //that isn't a corner
                //NEW 10 Oct 2014 Start at lower left +/- 1 if lastplant==null
                //otherwise start at nearest corner to lastplant +/- 1
                /*
                If lastplant==null ourcurrent is a random corner + 1 boundary direction
                else our current is the nearest corner to lastplant.
                */
               // ourdirection = new Location(0, 0);
               // ourcurrent = new Location(0, 0);
               // while (ourdirection.X == 0 && ourdirection.Y == 0) {

                    //ourcurrent = ourRect.randomBoundaryPoint(rv);
                    //ourdirection = ourRect.inwardnormal(ourcurrent);
               // }
                
                //13 October 2014
                if(lastplant == null)
                {
                    ourcurrent=new Location(ourregion.LEFT,ourregion.BOTTOM);
                    ourcurrent=ourregion.boundaryPoint(ourregion.parameterValue(ourcurrent)+boundarystep);
                    ourdirection = new Location(0, 0);
                    ourdirection = ourregion.inwardnormal(ourcurrent);
                    if (ourdirection.X == 0 && ourdirection.Y == 0) {
                        ourcurrent=ourregion.boundaryPoint(ourregion.parameterValue(ourcurrent)+boundarystep);
                        ourdirection = ourregion.inwardnormal(ourcurrent);
                    }
                    ourpathlength=0;
                }
                else//we're moving here from a previous subrectange of the summary region so go to the nearest corner
                {
                    Location closest=new Location(ourregion.LEFT,ourregion.BOTTOM);
                    float distance = closest.dist(lastplant.location);
                    if(lastplant.location.dist(new Location(ourregion.RIGHT,ourregion.BOTTOM)) < distance)
                    {
                        closest=new Location(ourregion.RIGHT,ourregion.BOTTOM);
                        distance=lastplant.location.dist(new Location(ourregion.RIGHT,ourregion.BOTTOM));
                    }
                    if(lastplant.location.dist(new Location(ourregion.RIGHT,ourregion.TOP)) < distance)
                    {
                        closest=new Location(ourregion.RIGHT,ourregion.TOP);
                        distance=lastplant.location.dist(new Location(ourregion.RIGHT,ourregion.TOP));
                    }
                    if(lastplant.location.dist(new Location(ourregion.LEFT,ourregion.TOP)) < distance)
                    {
                        closest=new Location(ourregion.LEFT,ourregion.TOP);
                        distance=lastplant.location.dist(new Location(ourregion.LEFT,ourregion.TOP));
                    }
                  //   ourcurrent=ourRect.boundaryPoint(ourRect.parameterValue(ourcurrent)+boundarystep);
                     ourcurrent=ourregion.boundaryPoint(ourregion.parameterValue(closest)+boundarystep);
                    ourdirection = new Location(0, 0);
                    ourdirection = ourregion.inwardnormal(ourcurrent);
                    if (ourdirection.X == 0 && ourdirection.Y == 0) {
                        ourcurrent=ourregion.boundaryPoint(ourregion.parameterValue(ourcurrent)+boundarystep);
                        ourdirection = ourregion.inwardnormal(ourcurrent);
                    }
                    //add in how far we've moved from lastplant
                    ourpathlength+=lastplant.location.dist(ourcurrent);
                }
                
                
                //System.out.println("ourcurrent = "+ourcurrent+"ourdirection = "+ ourdirection);
                System.out.println("collectsubregion ourregion= "+ourregion.toXML()+"starting at "+ourcurrent.toString()+ "with direction "+ourdirection.toString());
             if(lastplant != null)   System.out.println("lastplant = "+lastplant.toXML()+" ourpathlength = "+ourpathlength);
                while (!done) {
                    //look for someone near ourcurrent to add to the list
                    ArrayList<Plant> shortlist = new ArrayList<Plant>();
                    for (Plant p : ourcandidates) {
                        if (ourcurrent.dist(p.location) <= strategy.diameter) {
                            shortlist.add(p);
                        }
                    }
                    if (shortlist.size() > 0) {
                       // Plant next = shortlist.get(rv.RandomInt(0, shortlist.size() - 1));
                       // ourcandidates.remove(next);
                        //temp.add(next);
                        //ourpathlength+= (2*ourcurrent.dist(next.location));
                        //ourdistances.add((double) ourpathlength);
                        
                        
                        
                        
                        
                         //SHOULD WE PICK THE NEAREST?
                        double ourmin=1e15;
                        Plant closest=null;
                        for(Plant who: shortlist)
                        {
                            double thisone=who.location.dist(ourcurrent);
                            if(thisone <ourmin)
                            {
                                closest=who;
                                ourmin=thisone;
                            }
                        }
                        //
                       // Plant next = shortlist.get(rv.RandomInt(0, shortlist.size() - 1));
                        //ourcandidates.remove(next);
                       // temp.add(next);
                        ourcandidates.remove(closest);
                        temp.add(closest);
                        ourdistances.add((double) ourpathlength+2*ourmin);

                        

                    }
                   //move along transect

                    thisstep = rv.RandomInt(strategy.steplow, strategy.stephigh);
                    if (ourregion.inRect(ourcurrent.X + thisstep * ourdirection.X, ourcurrent.Y + thisstep * ourdirection.Y)) {
                        // System.out.println("Simple step = "+thisstep);
                        ourpathlength += thisstep;
                        ourcurrent = new Location(ourcurrent.X + thisstep * ourdirection.X, ourcurrent.Y + thisstep * ourdirection.Y);
                    } else {
                        //move in ourdirection to the boundary
                        int i = 0;
                        Location test = new Location(ourcurrent.X + i * ourdirection.X, ourcurrent.Y + i * ourdirection.Y);
                        while (ourregion.inRect(test.X, test.Y)) {
                            i++;
                            test = new Location(ourcurrent.X + i * ourdirection.X, ourcurrent.Y + i * ourdirection.Y);
                        }
                        i = i - 1;//how far to the boundary
                        int distanceremaining = thisstep - i;
                        test = new Location(ourcurrent.X + i * ourdirection.X, ourcurrent.Y + i * ourdirection.Y);//on boundary
                        int newk = ourregion.parameterValue(test) + (boundarystep * boundarydirection);
                        test = ourregion.boundaryPoint(newk);
                        if (ourregion.inwardnormal(test).X == 0 && ourregion.inwardnormal(test).Y == 0) {
                            newk = (boundarystep * boundarydirection + 1);
                            test = ourregion.boundaryPoint(newk);
                        }
                        ourdirection = ourregion.inwardnormal(test);
                        boundarydirection *= -1;
                        ourcurrent = new Location(test.X + distanceremaining * ourdirection.X, test.Y + distanceremaining * ourdirection.Y);
                        //ourpathlength += thisstep + newk;
                        ourpathlength+= thisstep;

                    }
                  //  System.out.println("ourcurrent = "+ourcurrent+"ourdirection = "+ ourdirection);
                    //update pathlength

                    if (ourcandidates.size() == 0 || ourpathlength > ourregion.W * ourregion.H) {
                        done = true;
                    }
                    //are we done?
                }
                moms = temp;
                distances = new double[ourdistances.size()];
                for (int ii = 0; ii < ourdistances.size(); ii++) {
                    distances[ii] = ourdistances.get(ii);
                }
                break;
            case TRANSECT:
                //pick a random starting point and direction in the subregion
                System.out.println("Sorting on a transect");
                ArrayList<Plant> candidates = new ArrayList<Plant>();
                candidates.addAll(moms);
                temp = new ArrayList<Plant>();
                while (!candidates.isEmpty()) {
                    //pick a random candidate as starting point an
                    int who = rv.RandomInt(0, candidates.size() - 1, 1)[0];
                    Plant currentplant = candidates.get(who);
                    candidates.remove(who);
                    temp.add(currentplant);

                    //move candidates to temp
                    //pick random direction 
                    Location direction = collectionstrategy.directions[rv.RandomInt(0, 7)];
                    Location currentlocation = currentplant.location;
                    //take random step
                    int howfar = rv.RandomInt(strategy.steplow, strategy.stephigh);
                    Location step = new Location();
                    step.X = howfar * direction.X;
                    step.Y = howfar * direction.Y;

                    currentlocation = currentlocation.add(step);
                    //while( subregion contains currentLocation && not done and !candidates.isEmpty())
                    while (sd.summaryregions.Regions.get(subregion).inRegion(currentlocation.X, currentlocation.Y)
                            && !candidates.isEmpty()) {
                        //generate a list of all candidates within radius of current location
                        ArrayList<Plant> shortlist = new ArrayList<Plant>();
                        for (Plant p : candidates) {
                            if (currentlocation.dist(p.location) <= strategy.diameter) {
                                shortlist.add(p);
                            }
                        }
                        if (shortlist.isEmpty()) {
                            howfar = rv.RandomInt(strategy.steplow, strategy.stephigh);
                            step = new Location();
                            step.X = howfar * direction.X;
                            step.Y = howfar * direction.Y;

                            currentlocation = currentlocation.add(step);
                            continue;
                        }
                        who = rv.RandomInt(0, shortlist.size() - 1);
                        temp.add(shortlist.get(who));
                        candidates.remove(shortlist.get(who));
                        //pick one and move it from candidates to temp
                        //take another random step
                        howfar = rv.RandomInt(strategy.steplow, strategy.stephigh);
                        step = new Location();
                        step.X = howfar * direction.X;
                        step.Y = howfar * direction.Y;

                        currentlocation = currentlocation.add(step);
                    }
                }

                //step off random distances and collect
                //from nearest parent
                //we need to know what subregion we're working in
                //We can use N to determine distances if NANDK
                //otherwise???
                break;
                /*
                NEARNEIGHBOR2 will pick a random starting point
                in the region, move to the nearest maternal plant and then proceed
                */
            case NEARNEIGHBOR:
                //arrange 
                Matrix M = new Matrix(moms.size(), moms.size());
                for (int i = 0; i < moms.size(); i++) {
                    for (int j = 0; j <= i; j++) {
                        M.set(i, j, moms.get(i).location.dist(moms.get(j).location));
                        M.set(j, i, M.get(i, j));
                    }
                }
                ArrayList<Integer> contenders = new ArrayList<Integer>();
                for (int i = 0; i < moms.size(); i++) {
                    contenders.add(i);
                }

                ////
                temp = new ArrayList<Plant>();
                ////
                if (contenders.size() > 0) {
                    int current = contenders.get(rv.RandomInt(0, contenders.size() - 1));
                    temp.add(moms.get(current));
                    contenders.remove(current);
                    while (contenders.size() > 1) {
                        double value = 1e100;
                        int where = -1;
                        for (int j : contenders) {
                            double t = M.get(current, j);
                            if (t < value) {
                                value = t;
                                where = j;
                            }
                        }
                        current = where;
                        temp.add(moms.get(current));
                        contenders.remove(Integer.valueOf(current));

                    }
                    temp.add(moms.get(contenders.get(0)));
                }
                ////
                moms = temp;
                break;

        }

         //COLLECT the seeds
        //double pathlength=0;
        //Plant lastMom=null;
        switch (strategy.collect) {
            case NANDK:
                //howmanmoms=min(N,moms.size())
                thispathlength=0;
                int howmanymoms = Math.min(strategy.N, moms.size());
                System.out.println("NANDK Working with " + howmanymoms + " moms");
                  //pathlength=0;
                //lastMom=null;
                for (int momnumber = 0; momnumber < howmanymoms; momnumber++) {
                    Plant p = moms.get(momnumber);
                    //PATHLENGTH
                    if (lastplant != null) {
                        thispathlength += p.location.dist(lastplant.location);
                        lastplant = p;
                    } else {
                        lastplant = p;
                    }
                    //PATHLENGTH
                    ArrayList<Plant> offspring = new ArrayList<Plant>();
                    for (Plant kid : seeds) {
                        if (kid.parent1 == p.my_id_no) {
                            offspring.add(kid);
                        }
                    }
                    int howmanykids = Math.min(strategy.K, offspring.size());
                    for (int i = 0; i < howmanykids; i++) {
                        collected.add(offspring.get(i));
                    }
                }
                 //if TRANSECT2 pathlength=distances[howmanymoms-1];
                    if (strategy.order == SEEDORDER.TRANSECT2) {
                        pathlength += distances[howmanymoms - 1];
                    }
                    else
                    {
                        pathlength+= thispathlength;
                    }
                break;
            case KTOS:
                //while moms are all used
                //how many kids = min()
                //pathlength=0;
                //lastMom=null;
               thispathlength=0;
                ArrayList<Plant> donors = new ArrayList<Plant>();
                int numbermoms = 0;
                while (collected.size() < strategy.S && !moms.isEmpty()) {
                    Plant m = moms.get(0);
                    numbermoms++;
                    moms.remove(0);
                    if (lastplant != null) {
                        thispathlength += m.location.dist(lastplant.location);
                        lastplant = m;
                    } else {
                        lastplant = m;
                    }

                    if (run == 0 && ourstrategy == 0 && rep == 0 && sd.makeMap) {
                        donors.add(m);
                    }
                    //If run==0 put m in a List of Plants
                    //that we'll plot on a map
                    ArrayList<Plant> offspring = new ArrayList<Plant>();
                    for (Plant kid : seeds) {
                        if (kid.parent1 == m.my_id_no) {
                            offspring.add(kid);
                        }
                    }
                    //randomize the order of offspring

                    index = new int[offspring.size()];
                    for (int i = 0; i < index.length; i++) {
                        index[i] = i;
                    }
                    temp = new ArrayList<Plant>();
                    index = rv.permuteList(index);
                    for (int i = 0; i < index.length; i++) {
                        temp.add(offspring.get(index[i]));
                    }
                    offspring = temp;

                    int howmanykids = Math.min(strategy.K, offspring.size());
                    if (howmanykids + collected.size() > strategy.S) {
                        howmanykids = strategy.S - collected.size();
                    }
                    for (int i = 0; i < howmanykids; i++) {
                        collected.add(offspring.get(i));
                    }
                }
                 //if TRANSECT2 pathlength=distances[howmanymoms-1];
                    if (strategy.order == SEEDORDER.TRANSECT2) {
                        pathlength += distances[numbermoms - 1];
                    }
                     else
                    {
                        pathlength+= thispathlength;
                    }
                System.out.println("KTOS used " + numbermoms + " moms");
                break;
        }

        return pathlength;

    }

    /**
     *
     * @return We had this method a list of possible maternal and seed plants
     * already selected by run number and subregion
     *
     * This probably from a loop that calls for replicate seedcollections and
     * does the statistical analysis and writing of the results
     */
    public ArrayList<Plant> collectseed(List<Plant> moms, List<Plant> seeds,
            collectionstrategy strategy, PrintWriter out) {
        System.out.println("collectseed from " + moms.size() + " moms and " + seeds.size() + " seed pool.");
        //IF WE'RE SIZE STRATIFIED, BREAK moms up INTO AN ARRAY OF LISTS
        ArrayList<Plant> temp;
        ArrayList<Plant> collected = new ArrayList<Plant>();
        double[] distances = null;
        //SELECT moms to visit ALL BIG (sort to decreasing age truncate) SMALL (sort to increasing age truncate )
        int[] index = new int[moms.size()];
        for (int i = 0; i < index.length; i++) {
            index[i] = i;
        }
        temp = new ArrayList<Plant>();
        index = rv.permuteList(index);
        for (int i = 0; i < index.length; i++) {
            temp.add(moms.get(index[i]));
        }
        moms = temp;
        switch (strategy.select) {
            case ALL:
                //nothing to do
                break;
            case BIG:
                //sort from big to small
                Collections.sort(moms, new Comparator<Plant>() {
                    public int compare(Plant s1, Plant s2) {
                        return s2.dob - s1.dob;
                    }
                });
                //select required fraction from the list
                if (strategy.sizefraction != 1.0f) {
                    moms = moms.subList(0, (int) Math.floor(strategy.sizefraction * moms.size()));
                }
                break;
            case SMALL:

                Collections.sort(moms, new Comparator<Plant>() {
                    public int compare(Plant s1, Plant s2) {
                        return s1.dob - s2.dob;
                    }
                });
                if (strategy.sizefraction != 1.0f) {
                    moms = moms.subList(0, (int) Math.floor(strategy.sizefraction * moms.size()));
                }
                break;
            case SIZESTRATIFIED:
                //sort by size and break into several groups, each of which will
                //be ordered and collected from.
                //in this case sizefraction tells us how many age classes
                //to use
                break;
        }
        //ORDER visits to moms
        switch (strategy.order) {
            case RANDOM:
                //make a random permutation
                index = new int[moms.size()];
                for (int i = 0; i < index.length; i++) {
                    index[i] = i;
                }
                temp = new ArrayList<Plant>();
                index = rv.permuteList(index);
                for (int i = 0; i < index.length; i++) {
                    temp.add(moms.get(index[i]));
                }
                moms = temp;
                break;
            case BIGTOSMALL:
                //sort big to small
                Collections.sort(moms, new Comparator<Plant>() {
                    public int compare(Plant s1, Plant s2) {
                        return s2.dob - s1.dob;
                    }
                });
                break;
            case SMALLTOBIG:
                //sort small to big
                Collections.sort(moms, new Comparator<Plant>() {
                    public int compare(Plant s1, Plant s2) {
                        return s1.dob - s2.dob;
                    }
                });
                break;
            case TRANSECT2:
                System.out.println("Sorting on a transect2");
                //make a copy of moms that we can modify
                ArrayList<Plant> ourcandidates = new ArrayList<Plant>();
                ourcandidates.addAll(moms);
                //and a place to store the sorted plants
                temp = new ArrayList<Plant>();
                //and a place to store the cumulative distance to the
                //plants along the transect
                ArrayList<Double> ourdistances = new ArrayList<Double>();
                int ourpathlength = 0;
                //Do a loop to walk the transect and add parents to the
                //list in temp.
                //we stop if all the candidates are used or we've walked too
                //far
                //WE assume we are in a single Rectangle, perhaps as a Region

                boolean done = false;
                Region ourRegion = sd.summaryregions.Regions.get(subregion);
                Rect ourRect = new Rect(ourRegion.XL, ourRegion.YL, ourRegion.XH, ourRegion.YH);
                //note that Region extends Rect so we could just use ourRegion!
                Location ourcurrent,
                 ourdirection;

                int boundarydirection = 1; //+-1
                  if (rv.nextBoolean()) {
                    boundarydirection = -1;
                }
                int boundarystep;
                 boundarystep = 2 * strategy.diameter;
                int thisstep;

                //Start at a random point on the boundary
                //that isn't a corner
                ourdirection = new Location(0, 0);
                ourcurrent = new Location(0, 0);
                while (ourdirection.X == 0 && ourdirection.Y == 0) {

                    ourcurrent = ourRect.randomBoundaryPoint(rv);
                    ourdirection = ourRect.inwardnormal(ourcurrent);
                }
                
                //13 Octo 2014 start in LowerLeft
                  ourcurrent=new Location(ourRect.LEFT,ourRect.BOTTOM);
                    ourcurrent=ourRect.boundaryPoint(ourRect.parameterValue(ourcurrent)+boundarystep);
                    ourdirection = new Location(0, 0);
                    ourdirection = ourRect.inwardnormal(ourcurrent);
                    if (ourdirection.X == 0 && ourdirection.Y == 0) {
                        ourcurrent=ourRect.boundaryPoint(ourRect.parameterValue(ourcurrent)+boundarystep);
                        ourdirection = ourRect.inwardnormal(ourcurrent);
                    }
                //System.out.println("ourcurrent = "+ourcurrent+"ourdirection = "+ ourdirection);
              
               
                while (!done) {
                    //look for someone near ourcurrent to add to the list
                    ArrayList<Plant> shortlist = new ArrayList<Plant>();
                    for (Plant p : ourcandidates) {
                        if (ourcurrent.dist(p.location) <= strategy.diameter) {
                            shortlist.add(p);
                        }
                    }
                    if (shortlist.size() > 0) {
                        //SHOULD WE PICK THE NEAREST?
                        double ourmin=1e15;
                        Plant closest=null;
                        for(Plant who: shortlist)
                        {
                            double thisone=who.location.dist(ourcurrent);
                            if(thisone <ourmin)
                            {
                                closest=who;
                                ourmin=thisone;
                            }
                        }
                        //
                       // Plant next = shortlist.get(rv.RandomInt(0, shortlist.size() - 1));
                        //ourcandidates.remove(next);
                       // temp.add(next);
                        ourcandidates.remove(closest);
                        temp.add(closest);
                        ourdistances.add((double) ourpathlength+2*ourmin);

                    }
                   //move along transect

                    thisstep = rv.RandomInt(strategy.steplow, strategy.stephigh);
                    if (ourRect.inRect(ourcurrent.X + thisstep * ourdirection.X, ourcurrent.Y + thisstep * ourdirection.Y)) {
                        // System.out.println("Simple step = "+thisstep);
                        ourpathlength += thisstep;
                        ourcurrent = new Location(ourcurrent.X + thisstep * ourdirection.X, ourcurrent.Y + thisstep * ourdirection.Y);
                    } else {
                        //move in ourdirection to the boundary
                        int i = 0;
                        Location test = new Location(ourcurrent.X + i * ourdirection.X, ourcurrent.Y + i * ourdirection.Y);
                        while (ourRect.inRect(test.X, test.Y)) {
                            i++;
                            test = new Location(ourcurrent.X + i * ourdirection.X, ourcurrent.Y + i * ourdirection.Y);
                        }
                        i = i - 1;//how far to the boundary
                        int distanceremaining = thisstep - i;
                        test = new Location(ourcurrent.X + i * ourdirection.X, ourcurrent.Y + i * ourdirection.Y);//on boundary
                        int newk = ourRect.parameterValue(test) + (boundarystep * boundarydirection);
                        test = ourRect.boundaryPoint(newk);
                        if (ourRect.inwardnormal(test).X == 0 && ourRect.inwardnormal(test).Y == 0) {
                            newk = (boundarystep * boundarydirection + 1);
                            test = ourRect.boundaryPoint(newk);
                        }
                        ourdirection = ourRect.inwardnormal(test);
                        boundarydirection *= -1;
                        Location ourlast=new Location(ourcurrent.X,ourcurrent.Y);
                        ourcurrent = new Location(test.X + distanceremaining * ourdirection.X, test.Y + distanceremaining * ourdirection.Y);
                        //ourpathlength += thisstep + newk;
                       // ourpathlength+= ourcurrent.dist(ourlast);
                        ourpathlength+= thisstep;
                    }
                  //  System.out.println("ourcurrent = "+ourcurrent+"ourdirection = "+ ourdirection);
                    //update pathlength

                    if (ourcandidates.size() == 0 || ourpathlength > ourRect.W * ourRect.H) {
                        done = true;
                    }
                    //are we done?
                }
                moms = temp;
                distances = new double[ourdistances.size()];
                for (int ii = 0; ii < ourdistances.size(); ii++) {
                    distances[ii] = ourdistances.get(ii);
                }
                //Look at the moms
                //int xxx=1;
                
                //for(Plant p:moms){System.out.println(p.location.toString()+" ");}
                break;
            case TRANSECT:
                //pick a random starting point and direction in the subregion
                System.out.println("Sorting on a transect");
                ArrayList<Plant> candidates = new ArrayList<Plant>();
                candidates.addAll(moms);
                temp = new ArrayList<Plant>();
                while (!candidates.isEmpty()) {
                    //pick a random candidate as starting point an
                    int who = rv.RandomInt(0, candidates.size() - 1, 1)[0];
                    Plant currentplant = candidates.get(who);
                    candidates.remove(who);
                    temp.add(currentplant);

                    //move candidates to temp
                    //pick random direction 
                    Location direction = collectionstrategy.directions[rv.RandomInt(0, 7)];
                    Location currentlocation = currentplant.location;
                    //take random step
                    int howfar = rv.RandomInt(strategy.steplow, strategy.stephigh);
                    Location step = new Location();
                    step.X = howfar * direction.X;
                    step.Y = howfar * direction.Y;

                    currentlocation = currentlocation.add(step);
                    //while( subregion contains currentLocation && not done and !candidates.isEmpty())
                    while (sd.summaryregions.Regions.get(subregion).inRegion(currentlocation.X, currentlocation.Y)
                            && !candidates.isEmpty()) {
                        //generate a list of all candidates within radius of current location
                        ArrayList<Plant> shortlist = new ArrayList<Plant>();
                        for (Plant p : candidates) {
                            if (currentlocation.dist(p.location) <= strategy.diameter) {
                                shortlist.add(p);
                            }
                        }
                        if (shortlist.isEmpty()) {
                            howfar = rv.RandomInt(strategy.steplow, strategy.stephigh);
                            step = new Location();
                            step.X = howfar * direction.X;
                            step.Y = howfar * direction.Y;

                            currentlocation = currentlocation.add(step);
                            continue;
                        }
                        who = rv.RandomInt(0, shortlist.size() - 1);
                        temp.add(shortlist.get(who));
                        candidates.remove(shortlist.get(who));
                        //pick one and move it from candidates to temp
                        //take another random step
                        howfar = rv.RandomInt(strategy.steplow, strategy.stephigh);
                        step = new Location();
                        step.X = howfar * direction.X;
                        step.Y = howfar * direction.Y;

                        currentlocation = currentlocation.add(step);
                    }
                }

                //step off random distances and collect
                //from nearest parent
                //we need to know what subregion we're working in
                //We can use N to determine distances if NANDK
                //otherwise???
                moms = temp; //?????
                break;
            case NEARNEIGHBOR:
                //arrange 
                Matrix M = new Matrix(moms.size(), moms.size());
                for (int i = 0; i < moms.size(); i++) {
                    for (int j = 0; j <= i; j++) {
                        M.set(i, j, moms.get(i).location.dist(moms.get(j).location));
                        M.set(j, i, M.get(i, j));
                    }
                }
                ArrayList<Integer> contenders = new ArrayList<Integer>();
                for (int i = 0; i < moms.size(); i++) {
                    contenders.add(i);
                }
                temp = new ArrayList<Plant>();
                int current = contenders.get(rv.RandomInt(0, contenders.size() - 1));
                temp.add(moms.get(current));
                contenders.remove(current);
                while (contenders.size() > 1) {
                    double value = 1e100;
                    int where = -1;
                    for (int j : contenders) {
                        double t = M.get(current, j);
                        if (t < value) {
                            value = t;
                            where = j;
                        }
                    }
                    current = where;
                    temp.add(moms.get(current));
                    contenders.remove(Integer.valueOf(current));

                }
                temp.add(moms.get(contenders.get(0)));
                moms = temp;
                break;

        }
        System.out.println("Selected " + moms.size() + " moms");
        //COLLECT the seeds
       ArrayList<Plant> selectedmoms=null;
       if(this.DUMPSELECTEDMOMS)
       {
           selectedmoms=new ArrayList<Plant>();
       }
        double pathlength = 0;
        Plant lastMom = null;
        switch (strategy.collect) {
            case NANDK:
                //howmanmoms=min(N,moms.size())
                int howmanymoms = Math.min(strategy.N, moms.size());
                System.out.println("NANDK Working with " + howmanymoms + " moms");
                pathlength = 0;
                lastMom = null;
                for (int momnumber = 0; momnumber < howmanymoms; momnumber++) {
                    Plant p = moms.get(momnumber);
                    if(this.DUMPSELECTEDMOMS)
                    {
                        selectedmoms.add(p);
                    }
                    //PATHLENGTH
                    if (lastMom != null) {
                        pathlength += p.location.dist(lastMom.location);
                        lastMom = p;
                    } else {
                        lastMom = p;
                    }
                    //PATHLENGTH
                    ArrayList<Plant> offspring = new ArrayList<Plant>();
                    for (Plant kid : seeds) {
                        if (kid.parent1 == p.my_id_no) {
                            offspring.add(kid);
                        }
                    }
                    int howmanykids = Math.min(strategy.K, offspring.size());
                    for (int i = 0; i < howmanykids; i++) {
                        collected.add(offspring.get(i));
                    }
                   
                }
                 //if TRANSECT2 pathlength=distances[howmanymoms-1];
                    if (strategy.order == SEEDORDER.TRANSECT2) {
                        pathlength = distances[howmanymoms - 1];
                        System.out.println("Here's the array of distances");
                            System.out.println(Arrays.toString(distances));
                        
                    }
                break;
            case KTOS:
                //while moms are all used
                //how many kids = min()
                pathlength = 0;
                lastMom = null;
                ArrayList<Plant> donors = new ArrayList<Plant>();
                int numbermoms = 0;
                while (collected.size() < strategy.S && !moms.isEmpty()) {
                    Plant m = moms.get(0);
                    numbermoms++;
                    moms.remove(0);
                     if(this.DUMPSELECTEDMOMS)
       {
           selectedmoms=new ArrayList<Plant>();
       }
                    if (lastMom != null) {
                        pathlength += m.location.dist(lastMom.location);
                        lastMom = m;
                    } else {
                        lastMom = m;
                    }

                    if (run == 0 && ourstrategy == 0 && rep == 0 && sd.makeMap) {
                        donors.add(m);
                    }
                    //If run==0 put m in a List of Plants
                    //that we'll plot on a map
                    ArrayList<Plant> offspring = new ArrayList<Plant>();
                    for (Plant kid : seeds) {
                        if (kid.parent1 == m.my_id_no) {
                            offspring.add(kid);
                        }
                    }
                    //randomize the order of offspring

                    index = new int[offspring.size()];
                    for (int i = 0; i < index.length; i++) {
                        index[i] = i;
                    }
                    temp = new ArrayList<Plant>();
                    index = rv.permuteList(index);
                    for (int i = 0; i < index.length; i++) {
                        temp.add(offspring.get(index[i]));
                    }
                    offspring = temp;

                    int howmanykids = Math.min(strategy.K, offspring.size());
                    if (howmanykids + collected.size() > strategy.S) {
                        howmanykids = strategy.S - collected.size();
                    }
                    for (int i = 0; i < howmanykids; i++) {
                        collected.add(offspring.get(i));
                    }
                }
                System.out.println("KTOS used " + numbermoms + " moms");
                if (strategy.order == SEEDORDER.TRANSECT2) {
                    pathlength = distances[numbermoms - 1];
                }
                break;
        }
        if(this.DUMPSELECTEDMOMS)
       {
           StringBuilder filename=new StringBuilder("selectedmoms");
           System.out.println("Selected moms in run "+run);
           for(Plant p:selectedmoms)System.out.println(p.toCSVString());
           
           selectedmoms=new ArrayList<Plant>();
       }
        System.out.println("Collected " + collected.size() + " seeds");
        if (run == 0 && rep == 0 && ourstrategy == 0 && sd.makeMap) {
            ourImage.plotPlants(collected, Color.green);
        }
         //if run==0 plot donors on the Image

        out.print("" + mf.format(pathlength) + ",");
        QuickSummary qs = new QuickSummary(collected, sd);
        double[] stats = qs.calculate();
        out.print("" + subregion + "," + run + "," + strategies[ourstrategy].label + "," + rep);
        for (double x : stats) {
            out.print("," + mf.format(x));
        }
        out.print("\n");

        return collected;
    }

    void mainLoop() {
        System.out.println("mainLoop");
        //open output file and insert labels
        //String label1="subregion,run,strategy,replicate,seeds_collected,alleles,Ho,He,F";
        //NEW AUG14 
        String label1 = "pathlength,subregion,run,strategy,replicate,seeds_collected,alleles,Ho,He,F";
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(this.outputfilename, true)));
            out.println(label1);
        } catch (Exception e) {
        }

        //for each subregion (read moms)
        for (subregion = 0; subregion < sd.summaryregions.Regions.size(); subregion++) {
            //for each run
            for (run = 0; run < sd.number_runs; run++) {

                //for each collection strategy
                //THESE WE CODE BY HAND USING N,K,S, ect
                for (ourstrategy = 0; ourstrategy < strategies.length; ourstrategy++) {
                    //for each requested replicate sampling
                    for (rep = 0; rep < strategies[ourstrategy].replicates; rep++) {
                        System.out.println("mainLoop: subregion = " + subregion + " strategy = " + ourstrategy + "  run = " + run + " rep = " + rep);
                        //MAKE ACTAUL COPIES of MOMS AS WE'll modify them below
                        //get moms for this run
                        ArrayList<Plant> moms = readCSV(this.maternalfilename, run);
                        //restrict to this subregion
                        Region r = sd.summaryregions.Regions.get(subregion);
                        Iterator<Plant> it = moms.iterator();
                        while (it.hasNext()) {
                            Plant p = it.next();
                            if (!r.inRegion(p.location.X, p.location.Y)) {
                                it.remove();
                            }
                        }
                        ArrayList<Plant> seeds = readCSV(seedfilename, run);

                        //get seeds from csv
                        //for each run (read seeds from that run, moms from that run)
                        //List<Plant> collection=
                        //subregion,run,rep are global so we can print them from collectseeds
                        //   collectseed(moms,seeds,strategies[ourstrategy],out);
                        //NEW 1 SEPT 2014 If subregion has subregions we can to
                        //take that into account at least in evaluating paths.
                        //Divide N Equally among the subsubregions  
                        //do the strategy in the subsubregion (calculating the pathlength)
                        //then combined the seeds harvested and pathlengths   
                        if (r.numberRectangles() <= 1) {
                            collectseed(moms, seeds, strategies[ourstrategy], out);
                        } 
                        //If Region r (one of the summary regions has subrectangles we iterate over them)
                        //accumulating the seeds collected and the pathlength across the different
                        //subregions
                        else if (r.numberRectangles() >= 2) {
                            Iterator<Rect> rit = r.getRectangles();

                            ArrayList<Plant> allseedscollected = new ArrayList<Plant>();
                            float totalpathlength = 0.0f;
                            //Plant lastplant = null;
                            while (rit.hasNext()) {
                                System.out.println("Subregions loop");
                                //work in just this Rect
                                Rect or = rit.next();
                                Region ourregion = new Region(or.LEFT, or.BOTTOM, or.RIGHT, or.TOP);
                                System.out.println("subsubregion: " + or.toXML());
                                //System.out.println("subsubregion: " + ourregion.toXML());
                                moms = readCSV(this.maternalfilename, run);
                             if(PRINTDEBUGINFO)   System.out.println("Subregions loop read in moms:" + moms.size());
                                it = moms.iterator();
                                while (it.hasNext()) {
                                    Plant p = it.next();
                                    // if(! ourregion.inRegion(p.location.X, p.location.Y)) it.remove();
                                    if (!or.inRect(p.location.X, p.location.Y)) {
                                        it.remove();
                                    }
                                }
                                      if(PRINTDEBUGINFO)  System.out.println("Subregions loop moms in subregion " + moms.size());
                                seeds = readCSV(seedfilename, run);
                                totalpathlength
                                        = collectsubregion(ourregion,moms,
                                                seeds,
                                                strategies[ourstrategy],
                                                allseedscollected,
                                          
                                                totalpathlength);
                                if(lastplant != null)
                                {       if(PRINTDEBUGINFO)  System.out.println("Pathlength = " + totalpathlength + "seed = " + allseedscollected.size()+" lastplant = "+lastplant.toXML());
                                }else
                                {
                                        if(PRINTDEBUGINFO)  System.out.println("Pathlength = " + totalpathlength + "seed = " + allseedscollected.size()+" lastplant = null"); 
                                }
                            }
                           //write AGGREGATE results with BySubregions added to strategy

                            out.print("" + mf.format(totalpathlength) + ",");
                            QuickSummary qs = new QuickSummary(allseedscollected, sd);
                            double[] stats = qs.calculate();
                            out.print("" + subregion + "," + run + "," + strategies[ourstrategy].label + "BySubRegion" + "," + rep);
                            for (double x : stats) {
                                out.print("," + mf.format(x));
                            }
                            out.print("\n");

                        }

//NEW AUG14 THIS PREVIOUS CALL WILL PRINT PATHLENGTH
                        //OR MOVE THE SUMMARY PROCESS TO collectseed()
                        //AND HAND IT out.
                        //  QuickSummary qs=new QuickSummary(collection,sd);
                        // double[] stats=qs.calculate();
                        // out.print(""+subregion+","+run+","+strategies[ourstrategy].label+","+rep);
                        // for(double x:stats)
                        //{
                        //   out.print(","+mf.format(x));
                        // }
                        //out.print("\n");
                        //collectseeds
                        //QuickSummary
                        //append results to outputfile sr,run,strategy,rep,....
                    }

                }

                //for each replicate
                //collectseeds
                //QuickSummary
                //append results to outputfile sr,run,strategy,rep,....
            }//run
        }//subregion
        try {
            out.close();
        } catch (Exception e) {
        }
        if (sd.makeMap) {
            ourImage.writeImage(this.outputfilename, "png");
        }

    }

    public static void main(String[] args) {
        if (args.length != 5) {
            System.out.println("args.length = " + args.length);
            System.out.println("You need to provide some file names for the program to run");
            System.out.println("seedfilename parentalfilename simdatafilename outputfilename strategiesfilename\n");
            System.out.println("There are no other commanline options used by this program.");

        }
        if (args.length == 5) {
            SeedCollection sc = new SeedCollection(args);
        } else {
            exit(0);
            SeedCollection sc = new SeedCollection();
        }
    }

}
/*


New TRANSECT in a Rect

 ArrayList<Plant> candidates=new ArrayList<Plant>();
 candidates.addAll(moms);

Get random Boundary Point and direction to move in.
Pick a Clock

while(notdone)
{
    Make a step from current point
    Search withing Diamter and make a list of all the candidates
    pick one at random, remove from candidate, add to orderlist on moms
    if candidates are used or we've taken maximum steps, we are done
}

We order the candidates along a transect and simultaneously make a list
of the cumulative distance to the kth parent along the transect.
MakeStep(last, direction,howfar,clock)
{
    Location current,direction;
    int boundarydirection; //+-1
    int boundarystep;
    int thisstep;
    if (current+thisstep*direction is in rect) return current+thisstep*direction;
    else
    {
        next=current;
        while(next=next+direction in rect);
        boundary=next-direction;
        remainingstep=thisstep-(distance current to boundary)
        boundaryk=rect.parameterValue(boundary);
        boundaryk=boundaryk+boundarydirection*boundarystep %rect.BoundaryMax
        Location newboundary=rect.boundaryPoint(boundaryk);
        //find new direction
        if(newboundry.X==boundary.X || newboundary.Y==boundary.Y)
        {
            //still on same side
            direction= -1* direction
        }
        else
        {
            direction = rect.inwardnormal(newboundary);
        }
        //assert direction != (0,0)
        //take remainder of step
        if(remainingstep !=0) return newboundary+remainingstep*direction
    }

    pick stepsize
    move in direction by stepsize to next
    if next is inRect, return next
    else
    {
        what moves us to boundry?
        moce that far
        k=parameter for Location
        newparamter=k+clock*boundary step
        Location from parameter
        new direction
    }

}
*/
