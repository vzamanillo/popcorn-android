package butter.droid.base.providers.media.filters;

public class Filters {
    private String keywords = null;
    private String genre = null;
    private Order order = Order.DESC;
    private Sort sort = Sort.POPULARITY;
    private Integer page = null;
    private String langCode = "en";

    public Filters() {
    }

    public Filters(String keywords, String genre, Order order, Sort sort, Integer page, String langCode) {
        this.keywords = keywords;
        this.genre = genre;
        this.order = order;
        this.sort = sort;
        this.page = page;
        this.langCode = langCode;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }
}