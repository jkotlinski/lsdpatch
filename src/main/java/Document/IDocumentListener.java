package Document;

public interface IDocumentListener {
    void onRomDirty(boolean dirty);
    void onSavDirty(boolean dirty);
}
