/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.celcom.kenan.adapter.cai;

import LOG4J.LogException;
import LOG4J.LogObj;
import java.util.Properties;
import yagg.adapter.adkbasic.BooleanLock;
import yagg.adapter.adkgeneric.GenericAdapter;
import yagg.adapter.adksess.Sess;

/**
 *
 * @author Dell
 */
public class CAIServer implements Runnable 
{
    public CAIServer(GenericAdapter genericadapter, BooleanLock booleanlock, Properties properties, boolean isPrimary)
    {
        tcpPort = 0;
        rspMsg = null;
        baseObj = null;
        chgState = null;
        cmsgCount = null;
        writeQsess = null;
        cmsgObj = null;
        v_LogObj = null;
        re_attemptCnt = 0;
        maxReattempt = 3;
        baseObj = genericadapter;
        v_LogObj = baseObj.getLogObj("yagg.cai");
        sLock = booleanlock;
        cmsgCount = new CAIMsgCount(baseObj);
        rspMsg = new CAIResponse(baseObj, cmsgCount, properties, isPrimary);
        cmsgObj = rspMsg.getCAICommonObj();
        writeQsess = cmsgObj.getSess();
    }

    private boolean init()
    {
        if(!rspMsg.init()) {
            return false;
        }
        LogHandler.logInform(v_LogObj, "CAIServer", "2", "");
        return true;
    }

    private boolean cleanup()
    {
        LogHandler.logInform(v_LogObj, "CAIServer", "3", "");
        return rspMsg.cleanup();
    }

    public void run()
    {
        try
        {
label0:
            {
                v_LogObj.LogInfo("NotUsed", "CAIServer", "4", "Could not log", null, null);
                sLock.waitToSetTrue(0L);
                Object obj = null;
                if(init())
                {
                    int i;
                    do
                        do
                        {
                            do
                            {
                                if(baseObj.getTargetState() != 4)
                                {
                                    if(baseObj.getTargetState() != 5)
                                        break label0;
                                    sLock.setValue(false);
                                    sLock.waitUntilTrue(0L);
                                }
                                i = rspMsg.receive();
                                if(baseObj.getTargetState() == 0)
                                {
                                    LogHandler.logInform(v_LogObj, "CAIServer", "9", "");
                                    cleanup();
                                    break label0;
                                }
                                if(i != 0)
                                    break;
                                re_attemptCnt = 0;
                            } while(true);
                            if(i != 2)
                                break;
                            v_LogObj.LogInfo("NotUsed", "CAIServer", "6", "Could not log", null, null);
                            if(re_attemptCnt < maxReattempt)
                            {
                                v_LogObj.LogInfo("NotUsed", "CAIServer", "11", "Could not log", null, null);
                                re_attemptCnt++;
                                cleanup();
                                if(!init())
                                {
                                    (new Thread(new CAIChgState(baseObj, 0))).start();
                                    break label0;
                                }
                            } else
                            {
                                (new Thread(new CAIChgState(baseObj, 0))).start();
                                break label0;
                            }
                        } while(true);
                    while(i != 1);
                    v_LogObj.LogError("NotUsed", "CAIServer", "10", "Could not log", null, null);
                    (new Thread(new CAIChgState(baseObj, 0))).start();
                } else
                {
                    (new Thread(new CAIChgState(baseObj, 0))).start();
                }
            }
        }
        catch(Exception exception)
        {
            LogHandler.logErr(v_LogObj, "CAIServer", "7", exception.getMessage());
//            cleanup();
//            sLock.setValue(false);
        }
        finally
        {
            try
            {
                cleanup();
            }
            catch(Exception exception2)
            {
                exception2.printStackTrace();
            }
            sLock.setValue(false);
        }

    }

    private int tcpPort;
    private CAIResponse rspMsg;
    private GenericAdapter baseObj;
    private CAIChgState chgState;
    private CAIMsgCount cmsgCount;
    private BooleanLock sLock;
    private Sess writeQsess;
    private CAICommon cmsgObj;
    private String debugArg[] = {
        new String()
    };
    private LogObj v_LogObj;
    private int re_attemptCnt;
    private int maxReattempt;
}
