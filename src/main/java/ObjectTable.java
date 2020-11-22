import Typecheck.Pair;

import java.util.ArrayList;

public class ObjectTable {
    public String objectName;

    public ObjectTable parent;
    public ArrayList<ObjectTable> children;

    public ArrayList<String> localFields;
    public ArrayList<String> localMethods;

    public ArrayList<String> fields;
    public ArrayList<String> methodTable;

    public ObjectTable(){
        parent = null;
        children = new ArrayList<>();
        localFields = new ArrayList<>();
        localMethods = new ArrayList<>();
        fields = new ArrayList<>();
        methodTable = new ArrayList<>();
    }
}
