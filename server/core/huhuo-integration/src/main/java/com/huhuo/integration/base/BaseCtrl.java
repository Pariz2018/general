package com.huhuo.integration.base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.huhuo.integration.algorithm.GzipUtils;
import com.huhuo.integration.algorithm.HuhuoEncryptUtils;
import com.huhuo.integration.config.GlobalConstant;
import com.huhuo.integration.exception.CtrlException;


public class BaseCtrl {
	protected Logger logger = LoggerFactory.getLogger(getClass());
	private String encoding = GlobalConstant.Encoding.UTF8;
	private String dateFormat = GlobalConstant.DateFormat.LONG_FORMAT;
	
	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String parseReqParam(InputStream in, boolean isDecrypt) throws IOException{
		ByteArrayOutputStream param = new ByteArrayOutputStream();
		byte[] b = new byte[1024];
		int len = 0;
		while ((len = in.read(b)) != -1) {
			param.write(b, 0, len);
		}
		if (isDecrypt) {
			b = HuhuoEncryptUtils.decrypt(param.toByteArray());
		} else {
			b = param.toByteArray();
		}
		String paramPost = new String(b, encoding);
		logger.info("==> received param --> {}", paramPost);
		return paramPost;
	}
	
	public String parseReqParam(InputStream in) throws IOException{
		return parseReqParam(in, false);
	}
	
	protected void write(InputStream data, HttpServletResponse resp){
		try {
			byte[] b = new byte[1024];
			int len = 0;
			while ((len = data.read(b)) != -1) {
				resp.getOutputStream().write(b, 0, len);
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getFullStackTrace(e));
			throw new CtrlException(e);
		}
	}
	
	protected void write(Object obj, HttpServletResponse resp) {
		write(obj, resp, false);
	}
	
	protected void write(Object obj, HttpServletResponse resp, boolean encrypted) {
		write(obj, resp, encrypted, false);
	}
	
	/**
	 * ?????????obj?????????json??????????????????????????????
	 * @param obj ????????????
	 * @param resp
	 * @param encrypted ??????????????????????????????
	 * @param compressed ??????????????????????????????
	 * @throws IOException
	 */
	protected void write(Object obj, HttpServletResponse resp, boolean encrypted,
			boolean compressed) {
		try {
			resp.addHeader("Content-Type", "application/json;charset=UTF-8");
			String content = null;
			if (obj instanceof String) {
				content = String.valueOf(obj);
			} else {
				content = JSONObject.toJSONStringWithDateFormat(obj, dateFormat);
			}
			logger.info("==> write out result --> {}", content);
			byte[] response;
			response = content.getBytes(encoding);
			if (compressed) {
				response = GzipUtils.compress(response);
			}
			if (encrypted) {
				response = HuhuoEncryptUtils.decrypt(response);
			}
			resp.getOutputStream().write(response);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getFullStackTrace(e));
			throw new CtrlException(e);
		}
	}

}
