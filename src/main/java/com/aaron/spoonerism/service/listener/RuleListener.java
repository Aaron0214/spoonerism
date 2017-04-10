package com.aaron.spoonerism.service.listener;

import com.aaron.spoonerism.service.RuleHelper;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by Aaron on 17/4/10.
 */
@Component
public class RuleListener implements InitializingBean {

    private final static String RULE_PATH = "/rules";

    @Override
    public void afterPropertiesSet() throws Exception {
        this.buildRule();
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 1000)
    public void buildRule() throws Exception {
        InputStream inputStream = RuleListener.class.getResourceAsStream(RULE_PATH);
        String rules = IOUtils.toString(inputStream);
        String[] ruleList = rules.split("\\n");
        for (String item : ruleList) {
            InputStream rule = RuleListener.class.getResourceAsStream(RULE_PATH + "/" + item);
            String ruleStr = IOUtils.toString(rule);
            String[] temp = ruleStr.split("\\n");
            for (String value : temp) {
                RuleHelper.registerRule(value.split("->")[0].trim(), value.split("->")[1].trim());
            }
        }
    }
}
