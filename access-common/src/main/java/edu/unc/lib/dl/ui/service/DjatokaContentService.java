/**
 * Copyright 2008 The University of North Carolina at Chapel Hill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.unc.lib.dl.ui.service;

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import edu.unc.lib.dl.ui.exception.ClientAbortException;
import edu.unc.lib.dl.ui.util.ApplicationPathSettings;
import edu.unc.lib.dl.ui.util.FileIOUtil;

/**
 * Generates request, connects to, and streams the output from djatoka.  Sets pertinent headers. 
 * @author bbpennel
 */
public class DjatokaContentService {
	private static final Logger LOG = LoggerFactory.getLogger(DjatokaContentService.class);
	
	@Autowired
	private ApplicationPathSettings applicationPathSettings;
	
	public void getMetadata(String simplepid, String datastream, OutputStream outStream, HttpServletResponse response){
		this.getMetadata(simplepid, datastream, outStream, response, 1);
	}
	
	public void getMetadata(String simplepid, String datastream, OutputStream outStream, HttpServletResponse response, int retryServerError){
		HttpClientParams params = new HttpClientParams();
		params.setContentCharset("UTF-8");
		HttpClient client = new HttpClient();
		client.setParams(params);
		
		StringBuilder path = new StringBuilder(applicationPathSettings.getDjatokaPath());
		path.append("resolver?url_ver=Z39.88-2004&rft_id=").append(applicationPathSettings.getFedoraPathWithoutDefaultPort())
			.append("/objects/").append(simplepid).append("/datastreams/").append(datastream).append("/content")
			.append("&svc_id=info:lanl-repo/svc/getMetadata");
		
		GetMethod method = new GetMethod(path.toString());
		try {
			client.executeMethod(method);
			if (method.getStatusCode() == HttpStatus.SC_OK) {
				if (response != null){
					response.setHeader("Content-Type", "application/json");
					response.setHeader("content-disposition", "inline");
					
					FileIOUtil.stream(outStream, method);
				}
			} else {
				if ((method.getStatusCode() == 500 || method.getStatusCode() == 404) && retryServerError > 0){
					this.getMetadata(simplepid, datastream, outStream, response, retryServerError-1);
				} else {
					LOG.error("Unexpected failure: " + method.getStatusLine().toString());
					LOG.error("Path was: " + method.getURI().getURI());
				}
			}
		} catch (ClientAbortException e) {
			if (LOG.isDebugEnabled())
				LOG.debug("User client aborted request to stream jp2 metadata for " + simplepid, e);
		} catch (Exception e){
			LOG.error("Problem retrieving metadata for " + path, e);
		} finally {
			method.releaseConnection();
		}
	}
	
	public void streamJP2(String simplepid, String region, String scale, String rotate, String datastream, 
			OutputStream outStream, HttpServletResponse response){
		this.streamJP2(simplepid, region, scale, rotate, datastream, outStream, response, 1);
	}
	
	public void streamJP2(String simplepid, String region, String scale, String rotate, String datastream, 
			OutputStream outStream, HttpServletResponse response, int retryServerError){
		HttpClient client = new HttpClient();
		
		StringBuilder path = new StringBuilder(applicationPathSettings.getDjatokaPath());
		path.append("resolver?url_ver=Z39.88-2004&rft_id=").append(applicationPathSettings.getFedoraPathWithoutDefaultPort())
			.append("/objects/").append(simplepid).append("/datastreams/").append(datastream).append("/content")
			.append("&svc_id=info:lanl-repo/svc/getRegion&svc_val_fmt=info:ofi/fmt:kev:mtx:jpeg2000&svc.format=image/jpeg")
			.append("&svc.rotate=").append(rotate).append("&svc.level=").append(scale).append("&svc.region=").append(region);
		
		GetMethod method = new GetMethod(path.toString());
		
		try {
			client.executeMethod(method);
			if (method.getStatusCode() == HttpStatus.SC_OK) {
				if (response != null){
					response.setHeader("Content-Type", "image/jpeg");
					response.setHeader("content-disposition", "inline");
					
					FileIOUtil.stream(outStream, method);
				}
			} else {
				if ((method.getStatusCode() == 500 || method.getStatusCode() == 404) && retryServerError > 0){
					this.getMetadata(simplepid, datastream, outStream, response, retryServerError-1);
				} else {
					LOG.error("Unexpected failure: " + method.getStatusLine().toString());
					LOG.error("Path was: " + method.getURI().getURI());
				}
			}
		} catch (ClientAbortException e) {
			if (LOG.isDebugEnabled())
				LOG.debug("User client aborted request to stream jp2 for " + simplepid, e);
		} catch (Exception e){
			LOG.error("Problem retrieving metadata for " + path, e);
		} finally {
			method.releaseConnection();
		}
	}

	public ApplicationPathSettings getApplicationPathSettings() {
		return applicationPathSettings;
	}

	public void setApplicationPathSettings(ApplicationPathSettings applicationPathSettings) {
		this.applicationPathSettings = applicationPathSettings;
	}
}
