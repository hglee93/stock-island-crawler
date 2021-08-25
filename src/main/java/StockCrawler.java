import org.apache.poi.ss.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StockCrawler {

    private static final String KRX_URL = "https://kind.krx.co.kr/corpgeneral/corpList.do?method=download&marketType=stockMkt";

    public List<String> getCompanyCodeList() throws IOException {

        URL url = new URL(KRX_URL);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(urlConnection.getInputStream(), "EUC-KR"));
        String inputLine;
        StringBuilder html = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            html.append(inputLine);
        }

        in.close();

        Document document = Jsoup.parse(html.toString());
        Elements elements = document.select("body table tr");
        System.out.println("elements = " + elements.toString());

        return new ArrayList<>();
    }

    private static Object getValueFromCell(Cell cell) {
        switch(cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case NUMERIC:
                if(DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                return cell.getNumericCellValue();
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    public static void main(String[] args) {
        StockCrawler stockCrawler = new StockCrawler();
        try {
            stockCrawler.getCompanyCodeList();
        } catch (IOException e) {
            System.out.println("getMessage : " + e.getMessage());
        }

    }
}
