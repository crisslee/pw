package com.coomix.security;

import com.app.gmstatisticslib.util.IStaSecurityInterface;
import com.coomix.app.framework.app.BaseApiClient;
import net.goome.im.secret.Secret;

public class Security implements IStaSecurityInterface {
    private static boolean isFirst = true;

    public String getSecInfo(String src, String key, String ver) {
        ver = BaseApiClient.COMMUNITY_COOMIX_VERSION;
        return Secret.getSecInfo(src, key, ver);
    }

    public String decodeProcess(String src, String key, String ver) {
        ver = BaseApiClient.COMMUNITY_COOMIX_VERSION;
        if (isFirst) {
            isFirst = false;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return Secret.decodeProcess(src, key, ver);
    }

    public String hashProcess(String src, String ver, Object ctx) {
        ver = BaseApiClient.COMMUNITY_COOMIX_VERSION;
        return Secret.hashProcess(src, ver);
    }
}
