package fr.istic.vv;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.nodeTypes.modifiers.NodeWithAccessModifiers;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// This class visits a compilation unit and
// prints all public enum, classes or interfaces along with their public methods
public class NoGetterPrinter extends VoidVisitorWithDefaults<Void> {

    private final List<String> privateAttributes = new ArrayList<>();
    private final List<String> publicMethods = new ArrayList<>();
    private final List<FaultyAttribute> faultyArttributes = new ArrayList<>();

    @Override
    public void visit(CompilationUnit unit, Void arg) {
        for (TypeDeclaration<?> type : unit.getTypes()) {
            type.accept(this, null);
        }
    }

    public void visitTypeDeclaration(TypeDeclaration<?> declaration, Void arg) {

        String clazz = declaration.getNameAsString();
        System.out.println("[i] Class : " + clazz);

        for (FieldDeclaration field : declaration.getFields()) {
            field.accept(this, arg);
        }

        for (MethodDeclaration method : declaration.getMethods()) {
            method.accept(this, arg);
        }

        for (String attribute : privateAttributes) {
            List<String> publicGetters = publicMethods.stream().filter((String method) -> method.toLowerCase().matches("get" + attribute.toLowerCase())).toList();
            if (publicGetters.isEmpty()) {
                faultyArttributes.add(new FaultyAttribute(attribute, clazz));
            }
        }

        List<String> export = new ArrayList<>();

        String exportHeader = "[i] REPORT :";
        export.add(exportHeader);
        System.out.println(exportHeader);

        for (FaultyAttribute attribute : faultyArttributes) {
            String exportLine = "[i] '" + attribute.attribute + "' attribute from class '" + attribute.clazz + "' is private and has no public getter.";
            System.out.println(exportLine);
            export.add(exportLine);
        }

        exportAsFile(export);
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
            System.out.println("[i]     Attribute : (" + getVisibilityAsString(declaration) + ") " + var.getNameAsString());
            if (declaration.isPrivate()) {
                privateAttributes.add(var.getNameAsString());
            }
        }
    }

    @Override
    public void visit(MethodDeclaration declaration, Void arg) {
        System.out.println("[i]     Method : (" + getVisibilityAsString(declaration) + ") " + declaration.getName());
        if (declaration.isPublic()) {
            publicMethods.add(declaration.getNameAsString());
        }
    }

    private String getVisibilityAsString(NodeWithAccessModifiers<?> declaration) {
        if (declaration.isPublic()) {
            return "Public";
        }
        if (declaration.isPrivate()) {
            return "Private";
        }
        return "Unknown";
    }

    private void exportAsFile(List<String> lines) {
        try {
            FileWriter myWriter = new FileWriter("res/no-public-getter-report.txt");
            for (String line : lines) {
                myWriter.write(line + "\n");
            }
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
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
