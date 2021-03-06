/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.hl7query.web.filter;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.dv.util.Base64;
import org.openmrs.api.context.Context;

/**
 * Filter intended for all /module/hl7query calls that allows the user to authenticate via Basic
 * authentication. (It will not fail on invalid or missing credentials. We count on the API to throw
 * exceptions if an unauthenticated user tries to do something they are not allowed to do.) <br/>
 * <br/>
 */
public class AuthorizationFilter implements Filter {

	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		log.debug("Initializing HL7Query Authorization filter");
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		log.debug("Destroying HL7Query Authorization filter");
	}

	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	        ServletException {

		// skip if we're already authenticated, or it's not an HTTP request
		if (!Context.isAuthenticated() && request instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			String basicAuth = httpRequest.getHeader("Authorization");
			if (basicAuth != null) {
				// this is "Basic ${base64encode(username + ":" + password)}"
				try {
					basicAuth = basicAuth.substring(6); // remove the leading "Basic "
					String decoded = new String(Base64.decode(basicAuth), Charset.forName("UTF-8"));
					String[] userAndPass = decoded.split(":");
					Context.authenticate(userAndPass[0], userAndPass[1]);
					if (log.isDebugEnabled())
						log.debug("authenticated " + userAndPass[0]);
				}
				catch (Exception ex) {
					// This filter never stops execution. If the user failed to
					// authenticate, that will be caught later.
				}
			}
		}
		// continue with the filter chain in all circumstances
		chain.doFilter(request, response);
	}
}