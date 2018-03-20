package org.iguana.sppf;

import org.iguana.grammar.symbol.Nonterminal;
import org.iguana.grammar.symbol.Symbol;
import org.iguana.parsetree.ParseTreeBuilder;
import org.iguana.parsetree.VisitResult;
import org.iguana.traversal.SPPFVisitor;

import java.util.*;

import static org.iguana.parsetree.VisitResult.ebnf;
import static org.iguana.parsetree.VisitResult.empty;
import static org.iguana.parsetree.VisitResult.single;

public class SPPFParseTreeVisitor<T> implements SPPFVisitor<VisitResult> {

    private final ParseTreeBuilder<T> parseTreeBuilder;
    private final Set<NonterminalNode> visitedNodes;
    private final Map<NonPackedNode, VisitResult> convertedNodes;

    private final VisitResult.CreateParseTreeVisitor<T> createNodeVisitor;

    public SPPFParseTreeVisitor(ParseTreeBuilder<T> parseTreeBuilder) {
        this.parseTreeBuilder = parseTreeBuilder;
        this.convertedNodes = new HashMap<>();
        this.visitedNodes = new LinkedHashSet<>();
        this.createNodeVisitor = new VisitResult.CreateParseTreeVisitor(parseTreeBuilder);
    }

    @Override
    public VisitResult visit(TerminalNode node) {
        return convertedNodes.computeIfAbsent(node, key -> {
                    if (node.getLeftExtent() == node.getRightExtent()) return empty();
                    Object terminalNode = parseTreeBuilder.terminalNode(node.getGrammarSlot().getTerminal(), node.getLeftExtent(), node.getRightExtent());
                    return single(terminalNode);
                }
        );
    }

    @Override
    public VisitResult visit(org.iguana.sppf.NonterminalNode node) {
        VisitResult result = convertedNodes.get(node);
        if (result != null) return result;

        // To guard for cyclic SPPFs
        if (visitedNodes.contains(node)) {
            List<Nonterminal> cycle = new ArrayList<>();
            boolean seen = false;
            for (NonterminalNode n : visitedNodes) {
                if (seen) {
                    cycle.add(n.getGrammarSlot().getNonterminal());
                } else {
                    if (n == node) {
                        cycle.add(n.getGrammarSlot().getNonterminal());
                        seen = true;
                    }
                }
            }
            cycle.add(node.getGrammarSlot().getNonterminal());
            throw new CyclicGrammarException(cycle);
        } else {
            visitedNodes.add(node);
        }

        if (node.isAmbiguous()) {
            Set<T> children = new HashSet<>();
            for (PackedNode packedNode : node.getChildren()) {
                VisitResult visitResult = packedNode.accept(this);
                children.addAll(visitResult.accept(createNodeVisitor, packedNode));
            }
            result = single(parseTreeBuilder.ambiguityNode(children));
        } else {
            PackedNode packedNode = node.getChildAt(0);
            switch (node.getGrammarSlot().getNodeType()) {
                case Basic:
                case Layout: {
                    List<T> children = packedNode.accept(this).accept(createNodeVisitor, packedNode);
                    if (children.size() > 1) {
                        result = single(parseTreeBuilder.ambiguityNode(new HashSet<>(children)));
                    } else {
                        result = single(children.get(0));
                    }
                    break;
                }

                case Plus: {
                    Symbol symbol = packedNode.getGrammarSlot().getPosition().getRule().getDefinition();
                    VisitResult visitResult = packedNode.accept(this);
                    result = ebnf(visitResult.getValues(), symbol);
                    break;
                }

                case Star:
                case Seq:
                case Alt:
                case Opt:
                case Start: {
                    Symbol symbol = packedNode.getGrammarSlot().getPosition().getRule().getDefinition();
                    VisitResult visitResult = packedNode.accept(this);
                    result = single(parseTreeBuilder.metaSymbolNode(symbol, (List<T>) visitResult.getValues(), node.getLeftExtent(), node.getRightExtent()));
                    break;
                }
            }
        }
        visitedNodes.remove(node);
        convertedNodes.put(node, result);
        return result;
    }

    @Override
    public VisitResult visit(IntermediateNode node) {
        VisitResult result = convertedNodes.get(node);
        if (result != null) return result;

        if (node.isAmbiguous()) {
            result = empty();
            for (PackedNode packedNode : node.getChildren()) {
                result = result.merge(packedNode.accept(this));
            }
        } else {
            PackedNode packedNode = node.getChildAt(0);
            result = packedNode.accept(this);
        }
        convertedNodes.put(node, result);
        return result;
    }

    @Override
    public VisitResult visit(PackedNode node) {
        VisitResult left = node.getLeftChild().accept(this);
        VisitResult right;
        if (node.getRightChild() != null)
            right = node.getRightChild().accept(this);
        else
            right = empty();

        return left.merge(right);
    }

}
