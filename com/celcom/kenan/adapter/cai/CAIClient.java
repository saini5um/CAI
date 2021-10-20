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
import yagg.adapter.adksess.YaggMessage;
import yagg.adapter.adkgeneric.GwyAdapterException;

/**
 *
 * @author Dell
 */
public class CAIClient implements Runnable {

    public CAIClient(GenericAdapter genericadapter, BooleanLock booleanlock, Properties properties)
    {
        serverAddr = null;
        tcpPort = 0;
        crqmsg = null;
        baseObj = null;
        chgState = null;
        cmsgCount = null;
        readQsess = null;
        cmsgObj = null;
        v_LogObj = null;
        debugArgs = new String[2];
        re_attemptCnt = 0;
        maxReattempt = 3;
        lazyConnFlag = false;
        String s = null;
        baseObj = genericadapter;
        cLock = booleanlock;
        props = properties;
        v_LogObj = baseObj.getLogObj("yagg.cai");
        cmsgCount = new CAIMsgCount(baseObj);
        crqmsg = new CAIRequest(baseObj, cmsgCount, props);
        cmsgObj = crqmsg.getCAICommonObj();
        readQsess = cmsgObj.getSess();
        maxReattempt = (new Integer(props.getProperty("yagg.adapter.numOfReattempt", "3"))).intValue();
        if((s = baseObj.getAdapterAttribute("LAZY_CONNECT")) == null)
            lazyConnFlag = false;
        else
        if(s.equals("true"))
            lazyConnFlag = true;
        else
        if(s.equals("false"))
            lazyConnFlag = false;
        else
            lazyConnFlag = false;
    }

    private boolean init()
    {
        LogHandler.logInform(v_LogObj, "PKDebug", "2", null);
        if(crqmsg.getCAIAttr() == 1) {
            LogHandler.logInform(v_LogObj, "PKDebug", "3", null);
            return false;
        }
        int i;
        int j;
        int k;
        i = (new Integer(props.getProperty("yagg.adapter.numOfRetry", "999999"))).intValue();
        j = (new Integer(props.getProperty("yagg.adapter.retryTimeout", "30000"))).intValue();
        k = 0;
        while ( k < i ) {
            if(crqmsg.init())
                return true;
            try
            {
                Thread.sleep(j);
                LogHandler.logInform(v_LogObj, "CAIClient", "2", null);
            }
            catch(InterruptedException interruptedexception)
            {
                LogHandler.logErr(v_LogObj, "CAIClient", "8", interruptedexception.getMessage());
                return false;
            }
            k++;
        }
        return false;
    }

    private boolean cleanup()
    {
        return crqmsg.cleanup();
    }

    public void run()
    {
label0:
        {
            Object obj = null;
            try
            {
                try
                {
                    cLock.waitToSetTrue(0L);
                    v_LogObj.LogInfo("NotUsed", "CAIClient", "4", "Could not log", null, null);
                    if(!lazyConnFlag && !init()) {
                        v_LogObj.LogInfo("NotUsed", "CAIClient", "12", "Could not log", null, null);
                        (new Thread(new CAIChgState(baseObj, 0))).start();
                    }
                    do
                    {
                        int targetState = baseObj.getTargetState();
                        if(targetState != 4)
                        {
                            if(targetState != 5)
                                break;
                            cLock.setValue(false);
                            cLock.waitUntilTrue(0L);
                        }
                        YaggMessage yaggmessage = baseObj.readTextMessage(readQsess, null, 5000);
                        if(yaggmessage == null) {
                            continue;
                        }
                        if(lazyConnFlag && !baseObj.getsregister("RequestChannel").equals("CONNECTED") && !init())
                        {
                            crqmsg.rollbackMsg();
                            break;
                        }
                        String s = yaggmessage.getBodyString();
                        int userDataIndex = s.indexOf("USER_DATA=");
                        if ( userDataIndex != -1 )
                            s = s.substring(userDataIndex);                        
//                        debugArg[0] = s;
//                        v_LogObj.LogInfo("NotUsed", "CAIClient", "5", "Could not log", debugArg, null);
                        s = s.replaceFirst("USER_DATA=", "");
//                        debugArg[0] = s;
//                        v_LogObj.LogInfo("NotUsed", "CAIClient", "5", "Could not log", debugArg, null);                        
//                        int length = s.length();
//                        int index = s.indexOf("\n");
                        s.replaceAll("\n", ";");
                        debugArg[0] = s;
                        v_LogObj.LogInfo("NotUsed", "CAIClient", "5", "Could not log", debugArg, null);
                        Properties properties = yaggmessage.getYaggProperties();
                        cmsgCount.gwMsgCount(properties, s.length());
                        int i = crqmsg.send(s);
                        if(baseObj.getTargetState() == 0)
                        {
                            LogHandler.logInform(v_LogObj, "CAIClient", "11", "");
                            break;
                        }
                        if(i == 0)
                        {
                            re_attemptCnt = 0;
                            continue;
                        }
                        if(i == 2)
                        {
                            v_LogObj.LogInfo("NotUsed", "CAIClient", "6", "Could not log", null, null);
                            if(lazyConnFlag)
                            {
                                cleanup();
                                continue;
                            }
                            if(re_attemptCnt < maxReattempt)
                            {
                                re_attemptCnt++;
                                cleanup();
                                if(init())
                                    continue;
                                (new Thread(new CAIChgState(baseObj, 0))).start();
                            } else
                            {
                                (new Thread(new CAIChgState(baseObj, 0))).start();
                            }
                            break;
                        }
                        if(i != 1)
                            continue;
                        v_LogObj.LogError("NotUsed", "CAIClient", "7", "Could not log", null, null);
                        (new Thread(new CAIChgState(baseObj, 0))).start();
                        break;
                    } while(true);
                }
                catch(GwyAdapterException gwexception)
                {
                    debugArg[0] = new Integer(gwexception.code).toString();
                    LogHandler.logErr(v_LogObj, "CAIClient", "9", debugArg[0]);
                    break label0;
                }
                catch(Exception exception)
                {
                    LogHandler.logErr(v_LogObj, "CAIClient", "9", exception.getMessage());
                    break label0;
                }
                break label0;
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
                cLock.setValue(false);
            }
        }
    }

    private String serverAddr;
    private int tcpPort;
    private CAIRequest crqmsg;
    private GenericAdapter baseObj;
    private BooleanLock cLock;
    private CAIChgState chgState;
    private CAIMsgCount cmsgCount;
    private Sess readQsess;
    private CAICommon cmsgObj;
    private LogObj v_LogObj;
    private String debugArgs[];
    private String debugArg[] = {
        new String()
    };
    private int re_attemptCnt;
    private int maxReattempt;
    private Properties props;
    private boolean lazyConnFlag;
}
