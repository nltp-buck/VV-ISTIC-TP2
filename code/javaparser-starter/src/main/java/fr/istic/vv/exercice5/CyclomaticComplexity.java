package fr.istic.vv.exercice5;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

// Cette classe visite une unité de compilation et imprime tous les noeuds publics,
// les classes ou les interfaces, ainsi que leurs méthodes publiques
public class CyclomaticComplexity extends VoidVisitorWithDefaults<Void> {

    private int ifCount = 0;
    private int forCount = 0;
    private int whileCount = 0;

    private BufferedWriter writer;

    public CyclomaticComplexity(String outputFilePath) {
        try {
            this.writer = new BufferedWriter(new FileWriter(outputFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void visit(CompilationUnit unit, Void arg) {
        for (TypeDeclaration<?> type : unit.getTypes()) {
            type.accept(this, null);
        }
    }

    public void visitTypeDeclaration(TypeDeclaration<?> declaration, Void arg) {
        if (!declaration.isPublic()) return;
        System.out.println(declaration.getFullyQualifiedName().orElse("[Anonymous]"));
        for (MethodDeclaration method : declaration.getMethods()) {
            method.accept(this, arg);
        }
        // Printing nested types in the top level
        for (BodyDeclaration<?> member : declaration.getMembers()) {
            if (member instanceof TypeDeclaration)
                member.accept(this, arg);
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
    public void visit(MethodDeclaration declaration, Void arg) {
        System.out.println("  " + declaration.getDeclarationAsString(true, true));

        for (Statement stmt : declaration.getBody().get().getStatements()) {
            countControlFlowNodes(stmt);
        }

        writeToFile(declaration);

        ifCount = 0;
        forCount = 0;
        whileCount = 0;
    }

    private void countControlFlowNodes(Statement stmt) {

        if (stmt.isBlockStmt()) {
            System.out.println("block");
            for (Statement innerStmt : stmt.asBlockStmt().getStatements()) {
                countControlFlowNodes(innerStmt);
            }
        } else if (stmt instanceof ForStmt) {
            System.out.println("for");
            forCount++;
            countControlFlowNodes(((ForStmt) stmt).getBody());
        } else if (stmt instanceof WhileStmt) {
            System.out.println("while");
            whileCount++;
            countControlFlowNodes(((WhileStmt) stmt).getBody());
        } else if (stmt instanceof IfStmt) {
            System.out.println("if");
            ifCount++;
            countControlFlowNodes(((IfStmt) stmt).getThenStmt());
            countControlFlowNodes(((IfStmt) stmt).getElseStmt().get());
        }
    }

    private void writeToFile(MethodDeclaration declaration) {
        try {
            writer.write("Package: " + declaration.findCompilationUnit().get().getPackageDeclaration().get().getName() + "  ");
            writer.write("Class: " + declaration.findCompilationUnit().get().getPrimaryTypeName().toString() + "  ");
            writer.write("Method: " + declaration.getNameAsString() + "  ");
            writer.write("Parameters: ");
            for (Parameter parameter : declaration.getParameters()) {
                writer.write(parameter.getType().asString() + " " + parameter.getNameAsString() + ", ");
            }
            writer.write("  ");
            int totalNodes = ifCount + forCount + whileCount + 1;
            writer.write("Total Control Flow Nodes: " + totalNodes + "\n\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeWriter() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
