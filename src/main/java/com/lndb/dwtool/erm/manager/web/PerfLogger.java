package com.lndb.dwtool.erm.manager.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.perf4j.log4j.Log4JStopWatch;

public class PerfLogger implements Filter {
	public PerfLogger() {
	}

	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) arg0;
		String requestURI = request.getRequestURI();
		if (requestURI != null && requestURI.contains(".action")) {
			Log4JStopWatch watch = new Log4JStopWatch();
			arg2.doFilter(arg0, arg1);
			watch.stop(requestURI);
		} else {
			arg2.doFilter(arg0, arg1);
		}
	}

	public void destroy() {
	}

	public void init(FilterConfig arg0) throws ServletException {
	}
}
