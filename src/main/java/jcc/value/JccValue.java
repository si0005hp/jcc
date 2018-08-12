package jcc.value;

public interface JccValue {
    IntegerValue integer();
    PointerValue<?> pointer();
}
