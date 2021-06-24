package nl.utwente.m4.lossprevention.utils;

import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
        InputStream inputStream = filePart.getInputStream();

        // create Workbook and Sheet instances to validate user's file
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet uploadedSheet = workbook.getSheetAt(0);

        // check whether the file has at least 10 rows AND exactly 6 OR minimum 3 rows (standard NEDAP format)
        boolean correctFormat = uploadedSheet.getLastRowNum() > 10 && uploadedSheet.getRow(1).getLastCellNum() == 6 && uploadedSheet.getRow(1).getLastCellNum() == 3;

        // check once more in back-end whether the file type is .xlsx (=BIFF2/3/4)
        try {
            if (correctFormat && (FileMagic.valueOf(inputStream).equals(FileMagic.BIFF2) || FileMagic.valueOf(inputStream).equals(FileMagic.BIFF3) || FileMagic.valueOf(inputStream).equals(FileMagic.BIFF4))) {
                XSSFSheet sheet = excelUtils.read(inputStream);
                try {
                    if (sheet == null) {
                        response.getWriter().println("Error: Could not read excel file.");
                    } else {
                        String result = sqlUtils.XSSFSheet_to_DB(sheet);
                        response.getWriter().println("File upload was a "
                                + (result.equals("Status-0") ? "success." : ("failure! status=" + result)));
                    }
                } catch (Exception e) {
                    response.getWriter().println("Error: Could not connect to database!\n");
                    e.printStackTrace(response.getWriter());
                    response.getWriter().println("connection = " + sqlUtils.getConnection());
                }
            }
        } catch (IOException e) {
            response.getWriter().println("Error: Could not determine file type");
        }
    }
}
