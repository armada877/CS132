import Typecheck.Pair;
import Typecheck.TypeContainer;
import cs132.minijava.syntaxtree.*;

import java.util.*;

public class SymbolGenerator {
    public Map<String, Map<String, Map<String, TypeContainer>>> allTables; // Class -> Method -> (Var Name -> Type)
    // every class will have a 'method' MEMBER-VAR-TABLE containing all member vars of each class
    // or i might jut use fields
    public Map<String, TypeContainer> currentTable;

    public Map<String, Map<String, TypeContainer>> fields;

    public List<TypeContainer> classTable;
    public List<Pair<String, String>> linksets;
    public Map<String, String> linksets_map;

    public Map<Pair<String, String>, Pair<List<Pair<String, TypeContainer>>, TypeContainer>> methodTypes;
    //                id  ,  id_M            List of Params               ->   return type
    public String currentClass;
    public String currentMethod;
    public String parent;

    public SymbolGenerator() {
        allTables = new HashMap<>();
        currentTable = new HashMap<>();
        classTable = new ArrayList<>();
        fields = new HashMap<>();
        methodTypes = new HashMap<>();
        linksets = new ArrayList<>();
        linksets_map = new HashMap<>();
    }
    // classname helper function
    public String classname(ClassDeclaration cd) {
        return cd.f1.f0.tokenImage;
    }

    // linkset helper functions
    public Pair<String, String> linkset(ClassDeclaration cd) {
        return null;
    }

    public Pair<String, String> linkset(ClassExtendsDeclaration ced) {
        String id = ced.f1.f0.tokenImage;
        String idP = ced.f3.f0.tokenImage;

        Pair<String, String> lset = new Pair<>(id, idP);
        return lset;
    }

    public String methodName(MethodDeclaration md) {
        return md.f2.f0.tokenImage;
    }

    public void addToFields (ClassDeclaration cd) {
        Map<String, TypeContainer> field = new HashMap<>();
        String id = cd.f1.f0.tokenImage;
        for (Node n : cd.f3.nodes) {
            VarDeclaration vd = (VarDeclaration) n;
            int type = vd.f0.f0.which;
            String typeIdName = "";
            if (type == 3) {
                Node typeId = vd.f0.f0.choice;
                typeIdName = ((Identifier) typeId).f0.tokenImage;
            }
            TypeContainer tc = new TypeContainer(type, typeIdName);
            field.put(vd.f1.f0.tokenImage, tc);
        }
        fields.put(id, field);
    }

    public void addToFields (MainClass mc) {
        Map<String, TypeContainer> field = new HashMap<>();
        String id = mc.f1.f0.tokenImage;

        for (Node n : mc.f14.nodes) {
            VarDeclaration vd = (VarDeclaration) n;
            int type = vd.f0.f0.which;
            String typeIdName = "";
            if (type == 3) {
                Node typeId = vd.f0.f0.choice;
                typeIdName = ((Identifier) typeId).f0.tokenImage;
            }
            TypeContainer tc = new TypeContainer(type, typeIdName);
            field.put(vd.f1.f0.tokenImage, tc);
        }
        fields.put(id, field);
    }

    public void addToFields (ClassExtendsDeclaration ced) {
        Map<String, TypeContainer> field = new HashMap<>();
        String id = ced.f1.f0.tokenImage;
        String parent = ced.f3.f0.tokenImage;

        field = new HashMap<>(fields.get(parent));

        for (Node n : ced.f5.nodes) {
            VarDeclaration vd = (VarDeclaration) n;
            int type = vd.f0.f0.which;
            String typeIdName = "";
            if (type == 3) {
                Node typeId = vd.f0.f0.choice;
                typeIdName = ((Identifier) typeId).f0.tokenImage;
            }
            TypeContainer tc = new TypeContainer(type, typeIdName);
            field.put(vd.f1.f0.tokenImage, tc);
        }
        fields.put(id, field);
    }

    public String getTypeName (Type t) {
        String typeDec = "";
        if (t.f0.choice instanceof Identifier) {
            typeDec = ((Identifier) t.f0.choice).f0.tokenImage;
        }

        return typeDec;
    }
}

