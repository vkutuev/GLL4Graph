package org.iguana.parsetree;

import iguana.utils.collections.CollectionsUtil;
import org.iguana.grammar.symbol.Rule;
import org.iguana.grammar.symbol.Symbol;
import org.iguana.sppf.PackedNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class VisitResult {

    public abstract MergeResultVisitor<VisitResult> visitor();

    public abstract VisitResult merge(VisitResult other);

    public abstract <T> T accept(CreateNodeVisitor<T> visitor, PackedNode packedNode);

    public abstract java.util.List<Object> getValues();

    public static Empty empty() {
        return new VisitResult.Empty();
    }

    public static Single single(Object value) {
        return new VisitResult.Single(value);
    }

    public static List list(java.util.List<Object> values) {
        return new VisitResult.List(values);
    }

    public static ListOfResult listOfResult(java.util.List<VisitResult> values) {
        return new ListOfResult(values);
    }

    public static EBNF ebnf(java.util.List<Object> values, Symbol symbol) {
        return new EBNF(values, symbol);
    }

    public static class Empty extends VisitResult {

        @Override
        public MergeResultVisitor visitor() {
            return new EmptyVisitor();
        }

        @Override
        public VisitResult merge(VisitResult other) {
            return other;
        }

        @Override
        public <T> T accept(CreateNodeVisitor<T> visitor, PackedNode packedNode) {
            return visitor.visit(this, packedNode);
        }

        @Override
        public java.util.List<Object> getValues() {
            return Collections.emptyList();
        }

        @Override
        public String toString() {
            return "[]";
        }
    }

    public static class Single extends VisitResult {

        private Object value;

        public Single(Object value) {
            this.value = new Wrapper(value);
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            ((Wrapper) this.value).setValue(value);
        }

        public VisitResult merge(VisitResult other) {
            return other.visitor().visit(this);
        }

        @Override
        public <T> T accept(CreateNodeVisitor<T> visitor, PackedNode packedNode) {
            return visitor.visit(this, packedNode);
        }

        @Override
        public java.util.List<Object> getValues() {
            java.util.List<Object> list = new ArrayList<>(1);
//            if (value instanceof Wrapper) {
//                list.add(((Wrapper) value).getValue());
//            } else {
            list.add(value);
//            }

            return list;
        }

        public VisitResult unpack() {
            if (value instanceof Wrapper) {
                this.value = ((Wrapper) value).value;
            }
            return this;
        }

        public MergeResultVisitor visitor() {
            return new SingleVisitor(this);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Single)) return false;
            Single other = (Single) obj;
            return ((Wrapper) value).getValue().equals(((Wrapper) other.value).getValue());
        }

        @Override
        public String toString() {
            return "[" + ((Wrapper) value).getValue().toString() + "]";
        }

    }

    public static class Wrapper {
        Object value;

        public Wrapper(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }

    public static class List extends VisitResult {

        private final java.util.List<Object> values;

        public List(java.util.List<Object> values) {
            this.values = values;
        }

        public java.util.List<Object> getValues() {
            return values;
        }

        public VisitResult merge(VisitResult other) {
            return other.visitor().visit(this);
        }

        @Override
        public <T> T accept(CreateNodeVisitor<T> visitor, PackedNode packedNode) {
            return visitor.visit(this, packedNode);
        }

        @Override
        public MergeResultVisitor visitor() {
            return new ListVisitor(this);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof List)) return false;
            List other = (List) obj;
            return values.equals(other.values);
        }

        @Override
        public String toString() {
            return values.toString();
        }
    }

    public static class EBNF extends VisitResult {

        private final java.util.List<Object> values;
        private final Symbol symbol;

        EBNF(java.util.List<Object> values, Symbol symbol) {
            this.values = values;
            this.symbol = symbol;
        }

        public VisitResult merge(VisitResult other) {
            return other.visitor().visit(this);
        }

        @Override
        public <T> T accept(CreateNodeVisitor<T> visitor, PackedNode packedNode) {
            return visitor.visit(this, packedNode);
        }

        @Override
        public java.util.List<Object> getValues() {
            return values;
        }

        @Override
        public MergeResultVisitor visitor() {
            return new EBNFResultVisitor(this);
        }

        public Symbol getSymbol() {
            return symbol;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof EBNF)) return false;
            EBNF other = (EBNF) obj;
            return values.equals(other.values);
        }

        @Override
        public String toString() {
            return values.toString();
        }
    }

    public static class ListOfResult extends VisitResult {

        private final java.util.List<VisitResult> values;

        public ListOfResult(java.util.List<VisitResult> values) {
            this.values = values;
        }

        @Override
        public MergeResultVisitor visitor() {
            return new ListOfResultVisitor(this);
        }

        @Override
        public VisitResult merge(VisitResult other) {
            return other.visitor().visit(this);
        }

        @Override
        public <T> T accept(CreateNodeVisitor<T> visitor, PackedNode packedNode) {
            return visitor.visit(this, packedNode);
        }

        @Override
        public java.util.List<Object> getValues() {
            throw new UnsupportedOperationException();
        }

        public java.util.List<VisitResult> getVisitResults() {
            return values;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ListOfResult)) return false;
            ListOfResult other = (ListOfResult) obj;
            return values.equals(other.values);
        }

        @Override
        public String toString() {
            return values.toString();
        }
    }

    interface MergeResultVisitor<T> {
        T visit(Empty other);

        T visit(Single other);

        T visit(List other);

        T visit(EBNF other);

        T visit(ListOfResult other);
    }

    class SingleVisitor implements MergeResultVisitor<VisitResult> {

        private final Single result;

        public SingleVisitor(Single result) {
            this.result = result;
        }

        @Override
        public VisitResult visit(Empty empty) {
            return result;
        }

        @Override
        public VisitResult visit(Single other) {
            java.util.List<Object> values = new ArrayList<>();
            values.add(other.value);
            values.add(result.value);
            return new List(values);
        }

        @Override
        public VisitResult visit(List other) {
            java.util.List<Object> values = new ArrayList<>();
            values.addAll(other.values);
            values.add(result.value);
            return new List(values);
        }

        @Override
        public EBNF visit(EBNF other) {
            java.util.List<Object> values = new ArrayList<>(other.values);
            values.add(result.value);
            return new EBNF(values, other.symbol);
        }

        @Override
        public VisitResult visit(ListOfResult other) {
            java.util.List<VisitResult> visitResults = new ArrayList<>();
            for (VisitResult otherValue : other.values) {
                java.util.List<Object> values = new ArrayList<>();
                values.addAll(otherValue.getValues());
                values.add(result.value);
                visitResults.add(new List(values));
            }
            return new ListOfResult(visitResults);
        }
    }

    class ListVisitor implements MergeResultVisitor<VisitResult> {

        private final List result;

        ListVisitor(List result) {
            this.result = result;
        }

        @Override
        public List visit(Empty empty) {
            return result;
        }

        @Override
        public List visit(Single other) {
            java.util.List<Object> values = new ArrayList<>();
            values.add(other.value);
            values.addAll(result.values);
            return new List(values);
        }

        @Override
        public ListOfResult visit(List other) {
            return new ListOfResult(iguana.utils.collections.CollectionsUtil.list(other, result));
        }

        @Override
        public VisitResult visit(EBNF result) {
            throw new RuntimeException("Combination is not possible");
        }

        @Override
        public ListOfResult visit(ListOfResult other) {
            java.util.List<VisitResult> visitResults = new ArrayList<>();
            visitResults.addAll(other.values);
            visitResults.add(result);
            return new ListOfResult(visitResults);
        }
    }

    class EBNFResultVisitor implements MergeResultVisitor<VisitResult> {

        private final EBNF result;

        EBNFResultVisitor(EBNF result) {
            this.result = result;
        }

        @Override
        public EBNF visit(Empty empty) {
            return result;
        }

        @Override
        public List visit(Single other) {
            java.util.List<Object> values = new ArrayList<>(2);
            values.add(other.value);
            values.add(result);
            return new List(values);
        }

        @Override
        public VisitResult visit(List other) {
            java.util.List<Object> values = new ArrayList<>(other.values.size() + 1);
            values.addAll(other.values);
            values.add(result);
            return new List(values);
        }

        @Override
        public VisitResult visit(EBNF other) {
            throw new RuntimeException("Combination is not possible");
        }

        @Override
        public VisitResult visit(ListOfResult other) {
            throw new RuntimeException("Combination is not possible");
        }
    }

    class ListOfResultVisitor implements MergeResultVisitor<VisitResult> {

        private final ListOfResult result;

        ListOfResultVisitor(ListOfResult result) {
            this.result = result;
        }

        @Override
        public ListOfResult visit(Empty empty) {
            return result;
        }

        @Override
        public VisitResult visit(Single other) {
            java.util.List<VisitResult> visitResults = new ArrayList<>();
            for (VisitResult otherValue : result.values) {
                java.util.List<Object> values = new ArrayList<>();
                values.add(other.value);
                values.addAll(otherValue.getValues());
                visitResults.add(new List(values));
            }
            return new ListOfResult(visitResults);
        }

        @Override
        public VisitResult visit(List other) {
            java.util.List<VisitResult> visitResults = new ArrayList<>();
            visitResults.add(other);
            visitResults.addAll(result.values);
            return new ListOfResult(visitResults);
        }

        @Override
        public VisitResult visit(EBNF other) {
            throw new RuntimeException("Combination is not possible");
        }

        @Override
        public VisitResult visit(ListOfResult other) {
            java.util.List<VisitResult> visitResults = new ArrayList<>();
            for (VisitResult otherValue : other.values) {
                for (VisitResult thisValue : result.values) {
                    java.util.List<Object> values = new ArrayList<>();
                    values.addAll(otherValue.getValues());
                    values.addAll(thisValue.getValues());
                    if (values.size() > 1)
                        visitResults.add(new List(values));
                    else
                        visitResults.add(new Single(values.get(0)));
                }
            }
            return new ListOfResult(visitResults);
        }
    }

    class EmptyVisitor implements MergeResultVisitor<VisitResult> {

        @Override
        public VisitResult visit(Empty other) {
            return other;
        }

        @Override
        public VisitResult visit(Single other) {
            return other;
        }

        @Override
        public VisitResult visit(List other) {
            return other;
        }

        @Override
        public VisitResult visit(EBNF other) {
            return other;
        }

        @Override
        public VisitResult visit(ListOfResult other) {
            return other;
        }
    }

    public interface CreateNodeVisitor<T> {
        T visit(Empty result, PackedNode packedNode);

        T visit(Single result, PackedNode packedNode);

        T visit(List result, PackedNode packedNode);

        T visit(EBNF result, PackedNode packedNode);

        T visit(ListOfResult result, PackedNode packedNode);
    }


    public static class CreateParseTreeVisitor<T> implements CreateNodeVisitor<java.util.List<T>> {

        private final ParseTreeBuilder<T> parseTreeBuilder;

        public CreateParseTreeVisitor(ParseTreeBuilder<T> parseTreeBuilder) {
            this.parseTreeBuilder = parseTreeBuilder;
        }

        @Override
        public java.util.List<T> visit(Empty result, PackedNode packedNode) {
            Rule rule = packedNode.getGrammarSlot().getRule();
            return CollectionsUtil.list(parseTreeBuilder.nonterminalNode(rule, (java.util.List<T>) result.getValues(), packedNode.getLeftExtent(), packedNode.getIndex()));
        }

        @Override
        public java.util.List<T> visit(Single result, PackedNode packedNode) {
            Rule rule = packedNode.getGrammarSlot().getRule();
            return CollectionsUtil.list((parseTreeBuilder.nonterminalNode(rule, (java.util.List<T>) result.getValues(), packedNode.getLeftExtent(), packedNode.getIndex())));
        }

        @Override
        public java.util.List<T> visit(List result, PackedNode packedNode) {
            java.util.List<T> values = new ArrayList<>();
            for (Object o : result.getValues()) {
                if (o instanceof VisitResult) {
                    values.addAll(((VisitResult) o).accept(this, packedNode));
                } else {
                    values.add((T) o);
                }
            }
            Rule rule = packedNode.getGrammarSlot().getRule();
            return CollectionsUtil.list(parseTreeBuilder.nonterminalNode(rule, values, packedNode.getLeftExtent(), packedNode.getIndex()));
        }

        @Override
        public java.util.List<T> visit(EBNF result, PackedNode packedNode) {
            T ebnfNode = parseTreeBuilder.metaSymbolNode(result.getSymbol(), (java.util.List<T>) result.getValues(), packedNode.getLeftExtent(), packedNode.getIndex());
            return CollectionsUtil.list(ebnfNode);

        }

        @Override
        public java.util.List<T> visit(ListOfResult result, PackedNode packedNode) {
            Set<T> set = new HashSet<>();
            for (VisitResult vResult : result.getVisitResults()) {
                set.add(parseTreeBuilder.nonterminalNode(packedNode.getGrammarSlot().getRule(), (java.util.List<T>) vResult.getValues(), packedNode.getLeftExtent(), packedNode.getIndex()));
            }
            return CollectionsUtil.list(parseTreeBuilder.ambiguityNode(set));
        }
    }

}
