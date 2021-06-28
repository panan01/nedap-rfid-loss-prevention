package nl.utwente.m4.lossprevention.utils;

import org.apache.poi.xssf.usermodel.XSSFSheet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class FileDownloadServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        String requestedFile;
        try {
            requestedFile = request.getRequestURI().split("download/", 2)[1].strip().toLowerCase();
            if (requestedFile.endsWith("s")){
                requestedFile = requestedFile.substring(0, requestedFile.length()-1);
            }
            if (requestedFile.contains("/")){
                throw new ArrayIndexOutOfBoundsException();
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            out.write("invalid URL, please try something else");
            response.setContentType("text/html");
            out.close();
            return;
        }

        // FileInputStream fileInputStream = new FileInputStream("C:\\Users\\larsw\\Downloads\\20210503_UTwente_Nedap_Stores.xlsx");
        XSSFSheet sheet;
        switch (requestedFile) {
            case "alarm":
                sheet = excelUtils.exportSheet(0);
                break;
            case "article":
                sheet = excelUtils.exportSheet(1);
                break;
            case "store":
                sheet = excelUtils.exportSheet(2);
                break;
            default:
                out.write("unknown data name '" + requestedFile + "'");
                response.setContentType("text/html");
                out.close();
                return;
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition","attachment; filename=\"" + requestedFile + ".xlsx" + "\"");
        InputStream inputStream = new ByteArrayInputStream(convertSheetToBytes(sheet));
        int i;
        while ((i=inputStream.read()) != -1) {
            out.write(i);
        }
        inputStream.close();
        out.close();
    }

    public byte[] convertSheetToBytes(XSSFSheet sheet) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            sheet.getWorkbook().write(baos);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return baos.toByteArray();
    }
}
