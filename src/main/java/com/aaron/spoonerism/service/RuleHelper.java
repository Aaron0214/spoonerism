package com.aaron.spoonerism.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Aaron on 17/4/10.
 */
public final class RuleHelper {

    private static Map<String, String> ruleContainer = new ConcurrentHashMap<>();
    private static Map<String, String> hostContainer = new ConcurrentHashMap<>();

    public static void registerRule(String org, String desc) {
        ruleContainer.put(org, desc);
    }

    public static String getRuleUrl(String org) {
        return ruleContainer.get(org);
    }

    public static void registerHostRule(String org, String desc) {
        hostContainer.put(org, desc);
    }

    public static String getHostRuleUrl(String org) {
        return hostContainer.get(org);
    }

}
