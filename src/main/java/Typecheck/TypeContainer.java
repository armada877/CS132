package Typecheck;

import java.util.Objects;

public class TypeContainer {
    public enum Type {
        INTARR,
        BOOL,
        INT,
        OBJ
    }
    public Type type;
    public String typeName;

    public TypeContainer(int typeNum, String typeDec) {
        switch (typeNum) {
            case 0:
                type = Type.INTARR;
                break;
            case 1:
                type = Type.BOOL;
                break;
            case 2:
                type = Type.INT;
                break;
            default:
                type = Type.OBJ;
        }
        typeName = typeDec;
    }

    @Override
    public boolean equals(Object obj) {
        return
                obj instanceof TypeContainer &&
                        Objects.equals(type, ((TypeContainer)obj).type) &&
                        Objects.equals(typeName, ((TypeContainer)obj).typeName);
    }
}
