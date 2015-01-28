package com.chenyang.proxy.util;

import com.chenyang.proxy.common.AgentAddress;
import com.chenyang.proxy.common.AgentErrorCode;
import com.chenyang.proxy.common.AgentException;
import com.chenyang.proxy.common.ValidDest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;

/**
 * 判断过来的请求的目的地址是否有效
 */
public class HostAuthenticationUtil {

    private static final Logger logger = LoggerFactory.getLogger(HostAuthenticationUtil.class);

    private static Map<Long, Integer> agentHostMap0 = new HashMap<Long, Integer>();
    private static Set<String> domainMap0 = new HashSet<String>();

    private static Map<Long, Integer> agentHostMap1 = new HashMap<Long, Integer>();
    private static Set<String> domainMap1 = new HashSet<String>();

    private static boolean use0 = false;

    public static boolean syncDest(ValidDest validDest) throws AgentException {
        long beginTime = System.currentTimeMillis();
        if (HostAuthenticationUtil.addValidDest(validDest) ) {
            HostAuthenticationUtil.reset();
            logger.info("AgentServiceImpl syncDesc  validDest : {} true ", validDest);
            return true;
        }
        return false;
    }

    private static boolean addAgentAddress(AgentAddress address) throws AgentException {
        long ipv4ToLong = NetworkUtils.ipv4ToLong(address.getIp());
        if (ipv4ToLong <= 0 || address.getPort() <= 0) {
            logger.error("add agent address error address : {}", address);
            throw new AgentException(AgentErrorCode.PARAMETER_ERROR.hashCode(), "ip : " + address.getIp() + " port : " + address.getPort());
        }
        if (use0) {
            agentHostMap1.put(ipv4ToLong, address.getPort());
        } else {
            agentHostMap0.put(ipv4ToLong, address.getPort());
        }
        return true;
    }

    private static boolean addAgentDomain(String agentdDemain) throws AgentException {
        agentdDemain = standardDomain(agentdDemain);
        if (StringUtils.isBlank(agentdDemain)) {
            throw new AgentException(AgentErrorCode.PARAMETER_ERROR.hashCode(), "demain : " + agentdDemain);
        }
        if (use0) {
            domainMap1.add(agentdDemain);
        } else {
            domainMap0.add(agentdDemain);
        }
        return true;
    }

    public static boolean addValidDest(ValidDest validDest) throws AgentException {
        if (validDest == null) {
            new AgentException(AgentErrorCode.PARAMETER_ERROR.getValue(), "参数为空");
        }
        addInit();
        List<AgentAddress> agentAddresses = validDest.getAgentAddresses();
        List<String> domains = validDest.getAgentDomains();
        if (CollectionUtils.isEmpty(agentAddresses)) {
            new AgentException(AgentErrorCode.PARAMETER_ERROR.getValue(), "参数为空");
        }
        for (AgentAddress address : agentAddresses) {
            if (!addAgentAddress(address)) {
                return false;
            }
        }
        for (String demain : domains) {
            if (!addAgentDomain(demain)) {
                return false;
            }
        }
        return true;
    }

    private static void addInit() {
        if (use0) {
            agentHostMap1 = new HashMap<Long, Integer>();
            domainMap1 = new HashSet<String>();
        } else {
            agentHostMap0 = new HashMap<Long, Integer>();
            domainMap0 = new HashSet<String>();
        }
    }

    public static void reset() {
        use0 = !use0;
    }

    public static boolean isValidAddress(String ip, int port) {
        boolean result = false;
        Integer validPort;
        if (use0) {
            validPort = agentHostMap0.get(NetworkUtils.ipv4ToLong(ip));
        } else {
            validPort = agentHostMap1.get(NetworkUtils.ipv4ToLong(ip));
        }
        if (validPort != null && port == validPort) {
            result = true;
        }
        logger.info("ip : {} ,port : {} is valid = {}", ip, port, result);
        return result;
    }

    public static boolean isValidDomain(String demain) {
//        demain = standardDomain(demain);
//        boolean result;
//        if (use0) {
//            result = domainMap0.contains(demain);
//        } else {
//            result = domainMap1.contains(demain);
//        }
//        logger.info("demain : {} is valid = : {}", demain, result);
//        return result;
        return true;
    }

    public static boolean isValidAddress(InetSocketAddress address) {
        //return isValidAddress(address.getAddress().getHostAddress(), address.getPort());
        return true;
    }

    private static String standardDomain(String demain) {
        demain = demain.trim();
        if (demain.startsWith("http://")) {
            demain = demain.substring("http://".length() - 1);
        }
        if (demain.startsWith("https://")) {
            demain = demain.substring("https://".length() - 1);
        }
        return demain;
    }

}
