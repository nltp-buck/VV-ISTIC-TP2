package fr.istic.vv;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.nodeTypes.modifiers.NodeWithAccessModifiers;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


// This class visits a compilation unit and
// prints all public enum, classes or interfaces along with their public methods
public class NoGetterPrinter extends VoidVisitorWithDefaults<Void> {

    private List<String> privateAttributes = new ArrayList<>();
    private List<String> publicMethods = new ArrayList<>();
    private List<String> faultyArttributes = new ArrayList<>();

    @Override
    public void visit(CompilationUnit unit, Void arg) {
        for (TypeDeclaration<?> type : unit.getTypes()) {
            type.accept(this, null);
        }
    }

    public void visitTypeDeclaration(TypeDeclaration<?> declaration, Void arg) {

        System.out.println("[i] TypeDecl : " + declaration.getName());

        for (FieldDeclaration field : declaration.getFields()) {
            field.accept(this, arg);
        }

        for (MethodDeclaration method : declaration.getMethods()) {
            method.accept(this, arg);
        }

        for (String attribute : privateAttributes) {
            List<String> publicGetters = publicMethods.stream().filter((String method) -> method.toLowerCase().matches("get" + attribute.toLowerCase())).toList();
            if (publicGetters.isEmpty()) {
                faultyArttributes.add(attribute);
            }
        }

        System.out.println("[i] REPORT :");
        for (String attribute : faultyArttributes) {
            System.out.println("[i] No Public Getter : " + attribute);
        }
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, Void arg) {
        visitTypeDeclaration(declaration, arg);
    }

    @Override
    public void visit(EnumDeclaration declaration, Void arg) {
        visitTypeDeclaration(declaration, arg);
    }

    @Override
    public void visit(FieldDeclaration declaration, Void arg) {
        for (VariableDeclarator var : declaration.getVariables()) {
            System.out.println("[i] FieldDecl : (" + declaration.isPublic() + ") " + var.getNameAsString());
            if (declaration.isPrivate()) {
                privateAttributes.add(var.getNameAsString());
            }
        }
    }

    @Override
    public void visit(MethodDeclaration declaration, Void arg) {
        System.out.println("[i] MethodDecl : " + declaration.getName());
        if (declaration.isPublic()) {
            publicMethods.add(declaration.getNameAsString());
        }
    }

    public void GenerateFile(List<String> lines) {
            String cheminFichier = "/home/knicolle/IdeaProjects/VV-ISTIC-TP2/code/javaparser-starter/src/main/java/fr/istic/vv";

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(cheminFichier));
                for(String line : lines){
                    writer.write(line+"\n");
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
    }

    private String GetVisibilityAsString(NodeWithAccessModifiers<?> declaration) {
        if (declaration.isPublic()) {
            return "Public";
        }
        if (declaration.isPrivate()) {
            return "Private";
        }
        return "Unknown";
    }

    static class FaultyAttribute {
        String attribute;
        String clazz;

        FaultyAttribute(String attribute, String clazz) {
            this.attribute = attribute;
            this.clazz = clazz;
        }
    }
}
