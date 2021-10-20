/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.celcom.kenan.adapter.cai;

import LOG4J.LogException;
import LOG4J.LogObj;
import java.util.*;
import yagg.adapter.adkgeneric.GenericAdapter;
import yagg.adapter.adkgeneric.GwyAdapterException;
import yagg.adapter.adksess.Sess;

/**
 *
 * @author Dell
 */
public class CAICommon {
    public CAICommon(GenericAdapter genericadapter, CAIMsgCount caimsgcount, Properties properties)
    {
        integName = null;
        intfName = null;
        v_sess = null;
        debugArgs = new String[2];
        v_LogObj = null;
        qAttemptCnt = 0;
        qAttemptWait = 0;
        baseObj = genericadapter;
        v_LogObj = baseObj.getLogObj("yagg.cai");
        integName = baseObj.getIntegrationName();
        intfName = baseObj.getInterfaceName();
        qAttemptCnt = (new Integer(properties.getProperty("yagg.adapter.attemptOnFullQ", "3"))).intValue();
        qAttemptWait = (new Integer(properties.getProperty("yagg.adapter.attemptOnFullQWait", "5000"))).intValue();
        try
        {
            v_sess = baseObj.getSess();
        }
        catch(GwyAdapterException gwyadapterexception)
        {
            LogHandler.logErr(v_LogObj, "CAICommon", "14", gwyadapterexception.getMessage());
        }
        cmcount = caimsgcount;
    }

    public boolean validateMsgLen(String s)
    {
        if(s == null)
            return false;
        
        return true;
    }

    public boolean validateCommand(String s, String s1)
    {
/*        String s2;
        byte byte0 = 21;
        s2 = s.substring(21, 40);
        String s3 = s2.trim().toUpperCase();
        if(!s3.equals(s1))
            break MISSING_BLOCK_LABEL_64;
        debugArg[0] = s2;
        v_LogObj.LogInfo("NotUsed", "KSICommon", "6", "Could not log", debugArg, null);
        return true;
        debugArg[0] = s2;
        v_LogObj.LogInfo("NotUsed", "KSICommon", "13", "Could not log", debugArg, null);
        return false;
        LogException logexception;
        logexception;*/
        return true;
    }

    public boolean buildGWmsg(Vector vector, String s)
    {
        String s1 = baseObj.genInputOPT_MSG_TYPE(s);
        LogHandler.logInform(v_LogObj, "CAICommon", "7", s1);
        Properties properties = new Properties();
        properties.put("OPT_APP_GRP", integName);
        properties.put("OPT_MSG_TYPE", s1);
        properties.put("SRC_ID", getSRCID());
        properties.put("DEST_ID", "");
        StringBuffer stringbuffer = new StringBuffer();
        for(Enumeration enumeration = vector.elements(); enumeration.hasMoreElements(); stringbuffer.append("\n"))
            stringbuffer.append((String)enumeration.nextElement());

        LogHandler.logInform(v_LogObj, "CAICommon", "8", stringbuffer.toString());
        if(!putMsgOnQueue(properties, stringbuffer.toString()))
        {
            for(int i = 0; i < qAttemptCnt; i++)
            {
                LogHandler.logErr(v_LogObj, "CAICommon", "16", "");
                try
                {
                    Thread.sleep(qAttemptWait);
                }
                catch(InterruptedException interruptedexception) { }
                if(putMsgOnQueue(properties, stringbuffer.toString()))
                    return true;
            }

        } else
        {
            return true;
        }
        return false;
    }

    public String getMainResponse(String s)
    {
        int index = s.indexOf(";");
        if ( index == -1 ) return s;
        return s.substring(0, index);
    }
    
    public String getErrorDescription(String s)
    {
        int index = s.indexOf(";");
        int length = s.length();
        String errDesc = null;
        
        if ( index < length ) { 
            int index2 = s.indexOf(";", index+1);
            if ( index2 != -1 )
                errDesc = s.substring(index+1, index2);
        }
        
        return errDesc;
    }
    
    public StringTokenizer getMsgHdrFlds(String s)
    {
//        String s1 = s.substring(0, 95);
        StringTokenizer stringtokenizer = new StringTokenizer(s, "\n");
        return stringtokenizer;
    }

    public StringTokenizer getMsgFlds(String s)
    {
        StringTokenizer stringtokenizer = new StringTokenizer(s, ":");
        return stringtokenizer;
    }

    public StringTokenizer getMsgBodyFlds(String s)
    {
//        String s1 = s.substring(95, s.length());
        StringTokenizer stringtokenizer = new StringTokenizer(s, "\n");
        return stringtokenizer;
    }

    public String pad(String s, int i)
    {
        StringBuffer stringbuffer = new StringBuffer();
        stringbuffer.append(s);
        for(int j = s.length(); j < i; j++)
            stringbuffer.append(" ");

        return stringbuffer.toString();
    }

    public String getSRCID()
    {
        return intfName;
    }

    public String getVersion()
    {
        String s = null;
        String s1 = baseObj.getAdapterAttribute("CAI_CLIENT_VERSION");
        try
        {
            if(s1.length() > 20)
            {
                debugArg[0] = (new Integer(21)).toString();
                v_LogObj.LogError("NotUsed", "CAICommon", "10", "Could not log", debugArg, null);
            } else
            {
                s = pad(s1, 20);
            }
        }
        catch(LogException logexception) { }
        return s;
    }

    public String getReplyToGrp()
    {
        String s = null;
        String s1 = baseObj.getAdapterAttribute("CAI_REPLY_GROUPNO");
        try
        {
            if(s1.length() > 5)
            {
                debugArg[0] = (new Integer(6)).toString();
                v_LogObj.LogError("NotUsed", "CAICommon", "11", "Could not log", debugArg, null);
            } else
            {
                s = pad(s1, 5);
            }
        }
        catch(LogException logexception) { }
        return s;
    }

    public Sess getSess()
    {
        return v_sess;
    }

    public boolean putMsgOnQueue(Properties properties, String s)
    {
        try
        {
            baseObj.sendTextMessage(v_sess, properties, s);
        }
        catch(GwyAdapterException gwyadapterexception)
        {
            LogHandler.logErr(v_LogObj, "CAICommon", "9", "");
            return false;
        }
        try
        {
            baseObj.commit(v_sess);
        }
        catch(GwyAdapterException gwyadapterexception1)
        {
            LogHandler.logErr(v_LogObj, "CAICommon", "12", "");
            return false;
        }
        cmcount.gwMsgCount(properties, s.length());
        return true;
    }

    public boolean commitMsg() {
        try
        {
            baseObj.commit(v_sess);
        }
        catch(GwyAdapterException gwyadapterexception1)
        {
            LogHandler.logErr(v_LogObj, "CAICommon", "12", "");
            return false;
        }
        return true;
    }
    
    private String integName;
    private String intfName;
    private Sess v_sess;
    private GenericAdapter baseObj;
    private CAIMsgCount cmcount;
    private String debugArg[] = {
        new String()
    };
    private String debugArgs[];
    private LogObj v_LogObj;
    private int qAttemptCnt;
    private int qAttemptWait;
}
