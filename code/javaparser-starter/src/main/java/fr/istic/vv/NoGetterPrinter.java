package fr.istic.vv;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;

import java.util.ArrayList;
import java.util.List;


// This class visits a compilation unit and
// prints all public enum, classes or interfaces along with their public methods
public class NoGetterPrinter extends VoidVisitorWithDefaults<Void> {

    private List<TypeDeclaration<?>> privateAttributes = new ArrayList<>();

    @Override
    public void visit(CompilationUnit unit, Void arg) {
        for(TypeDeclaration<?> type : unit.getTypes()) {
            type.accept(this, null);
        }
    }

    public void visitTypeDeclaration(TypeDeclaration<?> declaration, Void arg) {

        System.out.println("[i] TypeDecl : " + declaration.getName());

        for(FieldDeclaration field : declaration.getFields()) {
            field.accept(this, arg);
        }

        for(MethodDeclaration method : declaration.getMethods()) {
            method.accept(this, arg);
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
            System.out.println("[i] FieldDecl : " + var.getNameAsString());
        }
    }

    @Override
    public void visit(MethodDeclaration declaration, Void arg) {
        System.out.println("[i] MethodDecl : " + declaration.getName());
    }
}
