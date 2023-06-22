package com.gxdcnjq.sharedbikesmis.constant;

import java.util.HashMap;
import java.util.Map;

public final class MacConstants {

    private static final Map<String, String> macNameMap = new HashMap<>();

    static {
        macNameMap.put("FF:FF:10:40:8F:7C", "北交小蓝车06号");
        macNameMap.put("FF:FF:10:44:01:B0", "北交小蓝车08号");
    }

    public static String getNameByMacAddress(String macAddress) {
        return macNameMap.get(macAddress);
    }

    public static final String MacAddress1 = "FF:FF:10:40:8F:7C";
    public static final String MacAddress2 = "FF:FF:10:44:01:B0";

    public static final String Name1 = "北交小蓝车06号";
    public static final String Name2 = "北交小蓝车08号";
}
