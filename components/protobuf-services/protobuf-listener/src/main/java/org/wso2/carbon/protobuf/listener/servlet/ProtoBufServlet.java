package org.wso2.carbon.protobuf.listener.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class ProtoBufServlet extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws IOException {


        response.setContentType("text/plain");
      String filename = "/WEB-INF/";
        String search = null;
        ServletContext context = getServletContext();

        String realPath=context.getRealPath("/WEB-INF");

        File folder = new File(realPath);
        File[] listOfFiles = folder.listFiles();

        if(listOfFiles == null) return;
        for (File file : listOfFiles) {
            String path=file.getPath();
            if(path.contains(".proto")){

                String[] t =path.split(File.separator);

                search=t[t.length-1];
            }

        }

       InputStream inputStream = context.getResourceAsStream(filename+search);
        if(inputStream!=null){
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
