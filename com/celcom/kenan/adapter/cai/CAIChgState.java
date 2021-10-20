/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.celcom.kenan.adapter.cai;

import LOG4J.LogException;
import LOG4J.LogObj;
import yagg.adapter.adkgeneric.GenericAdapter;

/**
 *
 * @author Dell
 */
public class CAIChgState implements Runnable {

    public CAIChgState(GenericAdapter genericadapter, int i)
    {
        gaObj = null;
        v_LogObj = null;
        adapter_state = -1;
        gaObj = genericadapter;
        v_LogObj = gaObj.getLogObj("yagg.cai");
        adapter_state = i;
    }

    public void run()
    {
        try
        {
            v_LogObj.LogInfo("NotUsed", "CAIChgState", "1", "Could not log", null, null);
            gaObj.changeState(0);
            if(adapter_state == 4)
                gaObj.changeState(4);
        }
        catch(LogException logexception) { }
    }

    private GenericAdapter gaObj;
    private LogObj v_LogObj;
    private int adapter_state;
}
