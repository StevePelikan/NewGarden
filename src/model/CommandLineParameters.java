/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.PrintWriter;
import java.util.Calendar;

/**
 *
 * @author sep
 */
class CommandLineParameters {

    String ProgramName = "NewGarden version 2.77 (9 Dec  2014)";
    String AuthorInfo = "info/bugs/questions: Steve Pelikan pelikan@math.uc.edu";
    String RunDateTime;
    String XMLfilename;
    String DUMPfilename;
    String RESULTSfilename;
    boolean ShowProgress;
    PrintWriter dumpfilewriter;
    PrintWriter lifetalefilewriter;
    String LIFETABLEfilename;
    boolean ShowVersion;
    boolean ShowDump;
    boolean ShowXML;
    boolean ShowLifetable;
    boolean ShowNe;
    boolean SkipStats;
    boolean SmallStats;

    public CommandLineParameters() {
        Calendar rightNow = Calendar.getInstance();
        RunDateTime = rightNow.getTime().toString();
        ShowVersion = false;
        ShowDump = false;
        ShowXML = false;
        ShowNe = false;
        ShowLifetable = false;
        ShowProgress = false;
        XMLfilename = null;
        DUMPfilename = null;
        LIFETABLEfilename = null;
        RESULTSfilename = null;
        SkipStats=false;
        SmallStats=false;
    }
    //if s (stands for "short") is true, we only
    //put a little stuff in the String
    //otherwise, everything goes in.

    public String toString(boolean s) {
        StringBuilder sb = new StringBuilder();
        sb.append(ProgramName + "\n" + AuthorInfo + "\n");
        sb.append("Run on " + RunDateTime + "\n");
        if (!s) {
            sb.append("XMLfilename = " + XMLfilename + "\n");
            sb.append("Dump = " + ShowDump + " / DUMPfilename = " + DUMPfilename + "\n");
            sb.append("SkipStats = "+ SkipStats+"\n");
            sb.append("smallstats = "+SmallStats+"\n");
            sb.append("Lifetable = " + ShowLifetable + " / LIFETABLEfilename = " + LIFETABLEfilename + "\n");
            sb.append("ShowVersion = " + ShowVersion + "\n");
        }
        return sb.toString();
    }
}
