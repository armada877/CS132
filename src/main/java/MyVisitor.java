
import cs132.minijava.syntaxtree.*;
import cs132.minijava.visitor.GJDepthFirst;

import java.util.*;

public class MyVisitor extends GJDepthFirst {
    HashMap<String, String> table;

    public MyVisitor() {
        table = new HashMap<>();
    }

    private String classname(ClassDeclaration cd) {
        return cd.f1.f0.tokenImage;
    }

    private Pair<String, String> linkset(ClassDeclaration cd) {
        return null;
    }

    private Pair<String, String> linkset(ClassExtendsDeclaration ced) {
        String id = ced.f1.f0.tokenImage;
        String idP = ced.f3.f0.tokenImage;

        Pair<String, String> lset = new Pair<>(id, idP);
        return lset;
    }

    private String methodname(MethodDeclaration md) {
        return md.f2.f0.tokenImage;
    }

    private boolean distinct(List<Identifier> ids) {
        int n = ids.size();

        Set<Identifier> s = new HashSet<>();
        for (Identifier id : ids) {
            s.add(id);
        }

        return (s.size() == ids.size());
    }

    private boolean noIncoming(String node, Set<Pair<String, String>> relationships) {
        for (Pair<String, String> rel : relationships) {
            if (rel.snd == node) {
                return true;
            }
        }
        return false;
    }

    private boolean acyclic(Set<Pair<String, String>> relationships) {
        // no incoming edges vertex list setup
        List<String> verts = new ArrayList<>();
        Set<String> allVerts = new HashSet<>();
        for (Pair<String, String> rel : relationships) {
            allVerts.add(rel.fst);
            allVerts.add(rel.snd);
        }
        for (String vert : allVerts) {
            if (noIncoming(vert, relationships)) {
                verts.add(vert);
            }
        }

        while (!verts.isEmpty()) {
            String n = verts.remove(0);
            for (String m : allVerts) {
                Pair<String, String> target = new Pair(n, m);
                if (relationships.contains(target)) {
                    relationships.remove(target);
                    if (noIncoming(m, relationships)) {
                        verts.add(m);
                    }
                }
            }
        }

        if (relationships.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

//    public List<Pair<String, String>> fields (ClassDeclaration cd) {
//
//    }
//
//    public List<Pair<String, String>> fields (ClassExtendsDeclaration ced) {
//
//    }

    @Override
    public Object visit(ClassDeclaration n, Object argu) {
        System.out.println("Visited a class declaration");
        return super.visit(n, argu);
    }

    @Override
    public Object visit(VarDeclaration n, Object argu) {
        String type = n.f0.f0.toString();
        String var = n.f1.f0.tokenImage;
        return super.visit(n, argu);
    }

    @Override
    public Object visit(AssignmentStatement n, Object argu) {
        String var = n.f0.f0.tokenImage;

        if (table.get(var) == null){
            System.out.println("Type error");
            System.exit(1);
        }
        System.out.println("Visited an assignment Statement");
        return super.visit(n, argu);
    }
}
