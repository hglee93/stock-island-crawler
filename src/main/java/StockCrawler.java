import Entity.Company;
import Entity.StockInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class StockCrawler {

    private final String KRX_URL;

    private final String NAVER_FINANCE_URL;

    private final int THREAD_COUNT;

    private static final Logger log = LoggerFactory.getLogger(StockCrawler.class);

    public StockCrawler() throws IOException {

        String appConfigPath = Thread.currentThread().getContextClassLoader().getResource("app.properties").getPath();
        Properties appProps = new Properties();
        appProps.load(new FileInputStream(appConfigPath));

        KRX_URL = appProps.getProperty("crawling.target.url.krx");
        NAVER_FINANCE_URL = appProps.getProperty("crawling.target.url.naverfinance");
        THREAD_COUNT = Integer.parseInt(appProps.getProperty("crawling.thread.count"));

        String log4jConfPath = "/Users/heegwan/Documents/Workspace/stock-island-crawler/log4j.properties";
        PropertyConfigurator.configure(log4jConfPath);
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

    public List<StockInfo> getStockInfo(List<Company> companyList) {

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        List<CompletableFuture<StockInfo>> collect = companyList.parallelStream()
                .map(company -> CompletableFuture.supplyAsync(() -> {
                    StockInfo stockInfo = new StockInfo();
                    try {
                        stockInfo = crawlStockInfoFromNaver(company);
                    } catch (IOException e) {
                        log.warn(e.getMessage());
                    }
                    return stockInfo;
                }, executor))
                .collect(Collectors.toList());

        List<StockInfo> stockInfoList = collect.stream().map(future -> {
            StockInfo stockInfo = new StockInfo();
            try {
                stockInfo = future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.warn(e.getMessage());
            }
            return stockInfo;
        }).collect(Collectors.toList());

        executor.shutdown();
        return stockInfoList;
    }

    public List<StockInfo> getStockInfo(List<Company> companyList, int threadCount) throws IOException, ExecutionException, InterruptedException {

        int stepSize = companyList.size() / threadCount;

        List<StockInfo> stockInfoList = new ArrayList<>();
        List<CompletableFuture<List<StockInfo>>> futures = new ArrayList<>();

        for(int i = 0; i < threadCount; i++) {

            List<Company> targets = companyList.subList(i * stepSize, (i + 1) * stepSize);

            CompletableFuture<List<StockInfo>> future = CompletableFuture.supplyAsync(() -> {

                List<StockInfo> sublist = new ArrayList<>();

                for(Company target : targets) {
                    StockInfo stockInfo = crawlStockInfoFromNaver(target);
                    sublist.add(stockInfo);
                }

                return sublist;
            });

            futures.add(future);
        }

        for(int i = 0; i < threadCount; i++) {
            List<StockInfo> sublist = futures.get(i).get();
            stockInfoList.addAll(sublist);
        }

        return stockInfoList;
    }

    public StockInfo crawlStockInfoFromNaver(Company company) throws IOException{

        String crawlingUrl = NAVER_FINANCE_URL + "?itemcode=" + company.getCode();

        HttpURLConnection urlConnection = requestHttpRequest(crawlingUrl, "GET");
        String json = convertInputStreamToString(urlConnection.getInputStream(), StandardCharsets.UTF_8.name());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(json);

        return new StockInfo.StockInfoBuilder(company.getCode(), company.getName())
                .marketSum(jsonNode.get("marketSum").asLong())
                .now(jsonNode.get("now").asLong())
                .diff(jsonNode.get("diff").asLong())
                .rate(jsonNode.get("rate").asDouble())
                .quantity(jsonNode.get("quant").asLong())
                .high(jsonNode.get("high").asLong())
                .low(jsonNode.get("low").asLong())
                .tradingTime(LocalDateTime.now())
                .build();
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
            List<StockInfo> stockInfoList = new ArrayList<>();

            long startTime = System.currentTimeMillis();
            stockInfoList = stockCrawler.getStockInfo(companyCodeList);
            long endTime = System.currentTimeMillis();
            log.info("Execution Time: {} sec", (double)((endTime - startTime) / 1000d));

            startTime = System.currentTimeMillis();
            stockInfoList = stockCrawler.getStockInfo(companyCodeList, 16);
            endTime = System.currentTimeMillis();
            log.info("Execution Time: {} sec", (double)((endTime - startTime) / 1000d));

            startTime = System.currentTimeMillis();
            stockInfoList = stockCrawler.getStockInfoExecutionService(companyCodeList, 16);
            endTime = System.currentTimeMillis();
            log.info("Thread Count: {}, Execution Time: {} sec", (double)((endTime - startTime) / 1000d));

        } catch (IOException e) {
            System.out.println("getMessage : " + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }
}
