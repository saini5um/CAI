/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.celcom.kenan.adapter.cai;
import LOG4J.LogException;
import LOG4J.LogObj;
import java.io.*;
import java.util.Properties;
import yagg.adapter.adkbasic.BooleanLock;
import yagg.adapter.adkgeneric.GenericAdapter;
import yagg.adapter.adkgeneric.GwyAdapterException;

/**
 *
 * @author Dell
 */
public class CAIAdapter extends GenericAdapter {

    public CAIAdapter(String s, String propertiesFile)
        throws GwyAdapterException
    {
        super(true, s);
        clientLock = new BooleanLock(false);
        serverLock = new BooleanLock(false);
//        altServerLock = new BooleanLock(false);
        caiclient_v = null;
        caiserver_v = null;
//        caialtserver_v = null;
        v_LogObj = null;
        clientT = null;
        serverT = null;
//        altServerT = null;
        v_LogObj = getLogObj("yagg.cai");
        try
        {
            v_LogObj.LogInfo("NotUsed", "CAIAdapter", "5", "Could not log", null, null);
            Properties properties = null;
            FileInputStream fileinputstream = new FileInputStream(propertiesFile);
            properties = new Properties();
            properties.load(fileinputstream);
            caiserver_v = new CAIServer(this, serverLock, properties, true);
            caiclient_v = new CAIClient(this, clientLock, properties);
//            caialtserver_v = new CAIServer(this, altServerLock, properties, false);
        }
        catch(IOException ioexception)
        {
            LogHandler.logErr(v_LogObj, "CAIAdapter", "6", ioexception.getMessage());
        }
        catch(LogException logexception) { }
    }

    public boolean stop()
    {
        LogHandler.logInform(v_LogObj, "CAIAdapter", "3", "");
        try
        {
            LogHandler.logInform(v_LogObj, "CAIAdapter", "12", "");
            clientT.interrupt();
            clientLock.waitUntilFalse(0L);
        } catch(InterruptedException interruptedexception) {
            LogHandler.logErr(v_LogObj, "CAIAdapter", "10", interruptedexception.getMessage());
            return false;
        }
        try {
            LogHandler.logInform(v_LogObj, "CAIAdapter", "13", "");
            serverT.interrupt();
            serverLock.waitUntilFalse(0L);
        } catch(InterruptedException interruptedexception) {
            LogHandler.logErr(v_LogObj, "CAIAdapter", "11", interruptedexception.getMessage());
            return false;
        }
        return true;
    }

    public void run()
    {
        try
        {
            v_LogObj.LogInfo("NotUsed", "CAIAdapter", "4", "Could not log", null, null);
            clientT = new Thread(caiclient_v);
            clientT.start();
            serverT = new Thread(caiserver_v);
            serverT.start();
//            altServerT = new Thread(caialtserver_v);
//            altServerT.start();
        }
        catch(LogException logexception) { }
    }

    public boolean sessrun()
    {
        (new Thread(this)).start();
        return true;
    }

    public static void main(String args[])
    {
        LogObj logobj = null;
        String s = null;
        String s1 = null;
        if(args.length == 1)
            s = args[0];
        else
        if(args.length == 2)
        {
            s1 = args[0];
            s = args[1];
        }
        try
        {
            CAIAdapter caiadapter = new CAIAdapter(s1, s);
            logobj = caiadapter.getLogObj("yagg.cai");
            caiadapter.is_Ready(4);
        }
        catch(GwyAdapterException gwyadapterexception)
        {
            if(logobj != null)
                LogHandler.logErr(logobj, "CAIAdapter", "8", gwyadapterexception.getMessage());
            else
                System.out.println("Exception:  " + gwyadapterexception.getMessage());
            System.exit(1);
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            System.exit(1);
        }
    }

    private BooleanLock clientLock;
    private BooleanLock serverLock;
//    private BooleanLock altServerLock;
    private CAIClient caiclient_v;
    private CAIServer caiserver_v;
//    private CAIServer caialtserver_v;
    private LogObj v_LogObj;
    private String debugArg[] = {
        new String()
    };
    private Thread clientT;
    private Thread serverT;
//    private Thread altServerT;
}
