package JavaToSparrow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MethodFieldTable {
    public ObjectTable root;
    public Map<String, String> childToParentMap;
    public ArrayList<ObjectTable> orphans;
    public Map<String, ObjectTable> allObjects;

    public MethodFieldTable() {
        root = new ObjectTable();
        childToParentMap = new HashMap<>();
        orphans = new ArrayList<>();
        allObjects = new HashMap<>();
    }

    public void generateTree() {
        while (!orphans.isEmpty()) {
            ArrayList<ObjectTable> foundParents = new ArrayList<>();
            for (ObjectTable orphan : orphans) {
                String child = orphan.objectName;
                String parent = childToParentMap.get(child);

                ObjectTable parentTable = findParent(parent, root);
                parentTable.children.add(orphan);
                orphan.parent = parentTable;
                foundParents.add(orphan);
            }
            for (ObjectTable child : foundParents){
                orphans.remove(child);
            }
        }
    }

    private ObjectTable findParent(String parent, ObjectTable currentObj) {
        if (currentObj.objectName == parent) {
            return currentObj;
        }
        if (currentObj.children.isEmpty()) {
            return null;
        }
        for (ObjectTable child : currentObj.children) {
            ObjectTable target = findParent(parent, child);
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    public void generateAllObjects(ObjectTable parent) {
        // Merge Fields / methodTables
        for (ObjectTable child : parent.children) {
            child.fields = new ArrayList<>(parent.fields);
            child.methodTable = new ArrayList<>(parent.methodTable);
            for (String field : child.localFields) {
                if (!child.fields.contains(field)) {
                    child.fields.add(field);
                }
            }
            for (String method : child.localMethods) {
                boolean overwritten = false;
                for (String oldMethod : child.methodTable) {
                    if (oldMethod.endsWith(method)) {
                        int index = child.methodTable.indexOf(oldMethod);
                        child.methodTable.set(index, child.objectName + method);
                        overwritten = true;
                        break;
                    }
                }
                if (!overwritten) {
                    child.methodTable.add(child.objectName + method);
                }
            }

            allObjects.put(child.objectName, child);
            generateAllObjects(child);
        }
    }

}

