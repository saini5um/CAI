/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.celcom.kenan.adapter.cai;

import LOG4J.LogException;
import LOG4J.LogObj;
import java.io.PrintStream;

/**
 *
 * @author Dell
 */
public class LogHandler {
    public LogHandler()
    {
    }

    public static void logErr(LogObj logobj, String s, String s1, String s2)
    {
        try
        {
            String as[] = {
                new String()
            };
            as[0] = s2;
            logobj.LogError("NotUsed", s, s1, "Cannot log", as, null);
        }
        catch(LogException logexception)
        {
            System.out.println("Log Exception");
        }
    }

    public static void logInform(LogObj logobj, String s, String s1, String s2)
    {
        try
        {
            String as[] = {
                new String()
            };
            as[0] = s2;
            logobj.LogInfo("NotUsed", s, s1, "Cannot log", as, null);
        }
        catch(LogException logexception)
        {
            System.out.println("Log Exception");
        }
    }

    public static void logInform(LogObj logobj, String s, String s1, String s2, int i)
    {
        String as[] = new String[2];
        try
        {
            String as1[] = {
                new String()
            };
            as[0] = (new Integer(i)).toString();
            as[1] = s2;
            logobj.LogInfo("NotUsed", s, s1, "Cannot log", as, null);
        }
        catch(LogException logexception)
        {
            System.out.println("Log Exception");
        }
    }
}
