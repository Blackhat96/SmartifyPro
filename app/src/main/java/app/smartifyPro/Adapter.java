package app.smartifyPro;

class Adapter {
    private final String title;
    private final String hint;

    Adapter(String title, String hint) {
        this.title = title;
        this.hint = hint;
     }

    public String getTitle() {
        return title;
    }

    String getHint() {
        return hint;
    }
}