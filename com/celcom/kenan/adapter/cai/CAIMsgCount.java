/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.celcom.kenan.adapter.cai;

import java.util.Properties;
import yagg.adapter.adkgeneric.GenericAdapter;

/**
 *
 * @author Dell
 */
public class CAIMsgCount {
    public CAIMsgCount(GenericAdapter genericadapter)
    {
        TOTALBYTECOUNT = "totalbytecount";
        DOT = ".";
        genAdapObj = null;
        genAdapObj = genericadapter;
    }

    public void gwMsgCount(Properties properties, int i)
    {
        for(int j = 0; j < countlist.length; j++)
        {
            String s = properties.getProperty(countlist[j]);
            if(s == null)
                s = new String();
            String s1 = countlist[j] + DOT + s + DOT + "msgcounts";
            String s2 = countlist[j] + DOT + s + DOT + "bytecounts";
            if(genAdapObj.getcount(s1) == null)
                genAdapObj.resetcount(s1);
            genAdapObj.incrcount(s1);
            if(genAdapObj.getcount(s2) == null)
                genAdapObj.resetcount(s2);
            genAdapObj.incrcount(s2, i);
        }

    }

    private String TOTALBYTECOUNT;
    private String countlist[] = {
        "OPT_MSG_TYPE"
    };
    private String DOT;
    private GenericAdapter genAdapObj;
}
