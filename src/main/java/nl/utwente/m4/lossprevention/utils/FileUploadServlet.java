package nl.utwente.m4.lossprevention.utils;

import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

public class FileUploadServlet extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // get the file chosen by the user
        Part filePart = request.getPart("fileToUpload");

        // get the InputStream
        InputStream fileInputStream = filePart.getInputStream();

        XSSFSheet sheet = excelUtils.read(fileInputStream);
        assert sheet != null;
        String value = excelUtils.getCellData(sheet, 0, 0);

        //create output HTML
        response.getWriter().println("<p>Thank you for uploading your file, first value is '" + value + "'</p>");
    }
}
