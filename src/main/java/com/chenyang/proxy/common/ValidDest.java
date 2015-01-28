package com.chenyang.proxy.common;

import java.util.List;

/**
 * Created by chenyang on 15-1-28.
 */
public class ValidDest {

    public List<AgentAddress> agentAddresses;
    public List<String> agentDomains;

    public ValidDest(List<AgentAddress> agentAddresses, List<String> agentDomains) {
        this.agentAddresses = agentAddresses;
        this.agentDomains = agentDomains;
    }

    public List<AgentAddress> getAgentAddresses() {
        return agentAddresses;
    }

    public List<String> getAgentDomains() {
        return agentDomains;
    }

    public void setAgentAddresses(List<AgentAddress> agentAddresses) {
        this.agentAddresses = agentAddresses;
    }

    public void setAgentDomains(List<String> agentDomains) {
        this.agentDomains = agentDomains;
    }
}
