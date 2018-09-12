package fontEditor;

abstract class ChangeEventListener {
    public enum ChangeEventMouseSide {
        LEFT,
        RIGHT
    }

    public abstract void onChange(int color, ChangeEventMouseSide side);
}

