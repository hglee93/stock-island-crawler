package Entity;

import java.time.LocalDateTime;

public class StockInfo {

    // 회사 코드
    private String code;

    // 회사명
    private String companyName;

    // 시가 총액
    private Long marketSum;

    // 현재가
    private Long now;

    // 전일대비
    private Long diff;

    // 등락률
    private Double rate;

    // 거래량
    private Long quantity;

    // 고가
    private Long high;

    // 저가
    private Long low;

    // 거래일
    private LocalDateTime tradingTime;

    public StockInfo() { }

    public StockInfo(String code, String companyName, Long marketSum, Long now, Long diff, Double rate, Long quantity, Long high, Long low, LocalDateTime tradingTime) {
        this.code = code;
        this.companyName = companyName;
        this.marketSum = marketSum;
        this.now = now;
        this.diff = diff;
        this.rate = rate;
        this.quantity = quantity;
        this.high = high;
        this.low = low;
        this.tradingTime = tradingTime;
    }

    public String getCode() {
        return code;
    }

    public String getCompanyName() {
        return companyName;
    }

    public Long getMarketSum() {
        return marketSum;
    }

    public Long getNow() {
        return now;
    }

    public Long getDiff() {
        return diff;
    }

    public Double getRate() {
        return rate;
    }

    public Long getQuantity() {
        return quantity;
    }

    public Long getHigh() {
        return high;
    }

    public Long getLow() {
        return low;
    }

    public LocalDateTime getTradingTime() {
        return tradingTime;
    }

    @Override
    public String toString() {
        return "StockInfo{" +
                "code='" + code + '\'' +
                ", companyName='" + companyName + '\'' +
                ", marketSum=" + marketSum +
                ", now=" + now +
                ", diff=" + diff +
                ", rate=" + rate +
                ", quantity=" + quantity +
                ", high=" + high +
                ", low=" + low +
                ", tradingTime=" + tradingTime +
                '}';
    }

    public static class StockInfoBuilder {

        // 회사 코드
        private String code;

        // 회사명
        private String companyName;

        // 시가 총액
        private Long marketSum;

        // 현재가
        private Long now;

        // 전일대비
        private Long diff;

        // 등락률
        private Double rate;

        // 거래량
        private Long quantity;

        // 고가
        private Long high;

        // 저가
        private Long low;

        // 거래일
        private LocalDateTime tradingTime;

        public StockInfoBuilder(String code, String companyName) {
            this.code = code;
            this.companyName = companyName;
        }

        public StockInfoBuilder marketSum(Long marketSum) {
            this.marketSum = marketSum;
            return this;
        }

        public StockInfoBuilder now(Long now) {
            this.now = now;
            return this;
        }

        public StockInfoBuilder diff(Long diff) {
            this.diff = diff;
            return this;
        }

        public StockInfoBuilder rate(Double rate) {
            this.rate = rate;
            return this;
        }

        public StockInfoBuilder quantity(Long quantity) {
            this.quantity = quantity;
            return this;
        }

        public StockInfoBuilder high(Long high) {
            this.high = high;
            return this;
        }

        public StockInfoBuilder low(Long low) {
            this.low = low;
            return this;
        }

        public StockInfoBuilder tradingTime(LocalDateTime tradingTime) {
            this.tradingTime = tradingTime;
            return this;
        }

        public StockInfo build() {
            return new StockInfo(
                    code, companyName, marketSum, now,
                    diff, rate, quantity, high, low, tradingTime);
        }

    }
}
