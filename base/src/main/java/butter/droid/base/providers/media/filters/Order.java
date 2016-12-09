package butter.droid.base.providers.media.filters;

public enum Order {
    ASC("1"), DESC("-1");

    private String value;

    Order(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}