
package com.chenyang.proxy;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.http.HttpMethod;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebUtil {

	public static Logger logger = LoggerFactory.getLogger(WebUtil.class);

	public static String parseRequestMethod(String url, HttpMethod methodd) {
		String method = "";
		int lastIndexOf = url.lastIndexOf("?");
		url = url.substring(url.indexOf("/") + 1, lastIndexOf > 0 ? lastIndexOf : url.length());
		if (StringUtils.isBlank(url) || url.startsWith("/?")) {
			url += "index";
		}
		url += "/" + methodd.name().toLowerCase();
		int count = 0;
		for (String path : url.split("/")) {
			path = path.trim();
			if (count == 0) {
				method += path;
			} else if (StringUtils.isNotBlank(path)) {
				method += path.substring(0, 1).toUpperCase() + path.substring(1);
			}
			count++;
		}
		return method;
	}

	public static Map<String, String> parseParams(String params, HttpMethod method) {
		Map<String, String> paramMap = new HashMap<String, String>();
		if (method == HttpMethod.GET && StringUtils.isNotBlank(params) && !params.equals("/") && params.contains("?")) {
			String par = params.substring(params.indexOf("?") + 1);
			if (StringUtils.isNotBlank(par)) {
				for (String st : par.split("&")) {
					String[] parm = st.split("=");
					paramMap.put(parm[0], parm[1]);
				}
			}
		} else if (method == HttpMethod.POST) {
			String spaceEx = "[\\s*|\t|\r|\n]*";
			String regEx = "name=\".*\"" + spaceEx + "\n" + spaceEx + "\n.*" + spaceEx;
			Pattern p = Pattern.compile(regEx);
			Matcher m = p.matcher(params);
			while (m.find()) {
				String group = m.group();
				String str[] = group.split(spaceEx + "\n" + spaceEx + "\n");
				if (str == null || str.length < 2) {
					continue;
				}
				paramMap.put(str[0].substring("name=\"".length(), str[0].lastIndexOf("\"")), str[1].trim());
			}
		}
		return paramMap;
	}

	public static void sendResponse(OutputStream output, String responseBody) throws IOException {
		StringBuffer response = bulidResponse(responseBody);

		output.write(response.toString().getBytes());
		output.flush();
	}

	public static StringBuffer bulidResponse(String responseBody) {
		StringBuffer response = new StringBuffer("HTTP/1.1 200 OK\n");
		response.append("Content-Length: " + responseBody.getBytes().length + "\n");
		response.append("Server: Sunpache 1.0\n");
		String date = new Date().toString();
		response.append("Date : " + date + "\n");
		response.append("Expires : " + date + "\n");
		response.append("Content-Type: text/html;charset=utf8\n");
		response.append("Server:BWS/1.1\n");
		response.append("Content-Type: text/html;charset=utf8\n");
		response.append("Last-Modified: " + date + "\n");
		response.append("Accept-ranges: bytes\n");
		response.append("\r\n");
		response.append(responseBody);
		return response;
	}

	public static void sendNotFoundResponse(OutputStream output) throws IOException {
		String responseBody = "<html><head><title>404 Not Found</title></head><body><h1>404 Not Found</h1>url was not found on this server.<p /><hr /></body></html>";
		StringBuffer response = new StringBuffer("HTTP/1.1 404 Not Found\n");
		response.append("Content-Length: " + responseBody.getBytes().length + "\n");
		response.append("Server: Sunpache 1.0\n");
		String date = new Date().toString();
		response.append("Date : " + date + "\n");
		response.append("Expires : " + date + "\n");
		response.append("Content-Type: text/html;charset=utf8\n");
		response.append("Server:BWS/1.1\n");
		response.append("Content-Type: text/html;charset=utf8\n");
		response.append("Last-Modified: " + date + "\n");
		response.append("Accept-ranges: bytes\n");
		response.append("\r\n");
		response.append(responseBody);

		output.write(response.toString().getBytes());
		output.flush();
	}
}
