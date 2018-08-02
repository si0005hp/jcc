package jcc;

import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum CType {
    INT("int"),
    VOID("void");
    
    @Getter
    private final String name;
    
    public static CType of(String name) {
        return Stream.of(CType.values())
                .filter(c -> name.equalsIgnoreCase(c.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Illegal c type: " + name));
    }
}
