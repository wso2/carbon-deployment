/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.protobuf.listener.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class ProtoBufServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		response.setContentType("text/plain");
		String filename = "/WEB-INF/";
		String search = null;
		ServletContext context = getServletContext();

		String realPath = context.getRealPath("/WEB-INF");

		File folder = new File(realPath);
		File[] listOfFiles = folder.listFiles();

		if (listOfFiles == null)
			return;
		for (File file : listOfFiles) {
			String path = file.getPath();
			if (path.contains(".proto")) {

				String[] t = path.split(File.separator);

				search = t[t.length - 1];
			}

		}

		InputStream inputStream = context.getResourceAsStream(filename + search);
		if (inputStream != null) {
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader reader = new BufferedReader(inputStreamReader);
			PrintWriter out = response.getWriter();
			String text = "";
			while ((text = reader.readLine()) != null) {

				out.println(text);
			}
			out.flush();
			out.close();
		}

	}
}
