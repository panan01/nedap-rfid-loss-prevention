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
        //get the file chosen by the user
        Part filePart = request.getPart("fileToUpload");

        //get the InputStream to store the file somewhere
        InputStream fileInputStream = filePart.getInputStream();

        /*
        //for example, you can copy the uploaded file to the server
        //note that you probably don't want to do this in real life!
        //upload it to a file host like S3 or GCS instead
        File fileToSave = new File("WebContent/uploaded-files/" + filePart.getSubmittedFileName());
        Files.copy(fileInputStream, fileToSave.toPath(), StandardCopyOption.REPLACE_EXISTING);

        //get the URL of the uploaded file
        String fileUrl = "http://localhost:8080/uploaded-files/" + filePart.getSubmittedFileName();
        */

        XSSFSheet sheet = excelUtils.read(fileInputStream);
        assert sheet != null;
        String value = excelUtils.getCellData(sheet, 0, 0);

        //create output HTML
        response.getOutputStream().println("<p>Thank you for uploading your file, first value is " + value + "</p>");
    }
}
