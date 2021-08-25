import Entity.Company;
import Entity.StockInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class StockCrawler {

    private final String KRX_URL;

    private final String NAVER_FINANCE_URL;

    public StockCrawler() throws IOException {

        String appConfigPath = Thread.currentThread().getContextClassLoader().getResource("app.properties").getPath();
        Properties appProps = new Properties();
        appProps.load(new FileInputStream(appConfigPath));

        KRX_URL = appProps.getProperty("crawling.target.url.krx");
        NAVER_FINANCE_URL = appProps.getProperty("crawling.target.url.naverfinance");
    }

    public List<Company> getCompanyCodeList() throws IOException {

        List<Company> companyList = new ArrayList<>();

        HttpURLConnection urlConnection = requestHttpRequest(KRX_URL, "GET");

        if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

            String html = convertInputStreamToString(urlConnection.getInputStream(), "EUC-KR");
            Document document = Jsoup.parse(html);
            Elements rows = document.select("body table tr");
            if(rows.size() > 0) {
                for(int rowNum = 1; rowNum < rows.size(); rowNum++) {
                    Elements cells = rows.eq(rowNum).select("td");
                    String name = cells.eq(0).text();
                    String code = cells.eq(1).text();
                    companyList.add(new Company(name, code));
                }
            } else {
                throw new IOException("row가 0 이하 입니다.");
            }
        }

        return companyList;
    }

    public List<StockInfo> getStockInfo(List<Company> companyList) throws IOException {

        List<StockInfo> stockInfoList = new ArrayList<>();

        for(Company company : companyList){

            String crawlingUrl = NAVER_FINANCE_URL + "?itemcode=" + company.getCode();

            HttpURLConnection urlConnection = requestHttpRequest(crawlingUrl, "GET");
            String json = convertInputStreamToString(urlConnection.getInputStream(), StandardCharsets.UTF_8.name());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(json);

            StockInfo stockInfo = new StockInfo.StockInfoBuilder(company.getCode(), company.getName())
                    .marketSum(jsonNode.get("marketSum").asLong())
                    .now(jsonNode.get("now").asLong())
                    .diff(jsonNode.get("diff").asLong())
                    .rate(jsonNode.get("rate").asDouble())
                    .quantity(jsonNode.get("quant").asLong())
                    .high(jsonNode.get("high").asLong())
                    .low(jsonNode.get("low").asLong())
                    .tradingTime(LocalDateTime.now())
                    .build();

            stockInfoList.add(stockInfo);
        }

        return stockInfoList;
    }

    public HttpURLConnection requestHttpRequest(String requestUrl, String requestMethod) throws IOException {
        URL url = new URL(requestUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod(requestMethod);
        urlConnection.connect();
        return urlConnection;
    }

    public String convertInputStreamToString(InputStream inputStream, String charset) throws IOException {
        return new BufferedReader(
                new InputStreamReader(inputStream, charset))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    public static void main(String[] args) {

        try {
            StockCrawler stockCrawler = new StockCrawler();

            List<Company> companyCodeList = stockCrawler.getCompanyCodeList();
            List<StockInfo> stockInfoList = stockCrawler.getStockInfo(companyCodeList);

            for(StockInfo stockInfo : stockInfoList) {
                System.out.println(stockInfo.toString());
            }

        } catch (IOException e) {
            System.out.println("getMessage : " + e.getMessage());
        }

    }
}
