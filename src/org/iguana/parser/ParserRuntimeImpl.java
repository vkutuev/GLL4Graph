package org.iguana.parser;

import iguana.utils.benchmark.Timer;
import iguana.utils.input.Input;
import org.iguana.datadependent.ast.Expression;
import org.iguana.datadependent.ast.Statement;
import org.iguana.datadependent.env.Environment;
import org.iguana.datadependent.env.GLLEvaluator;
import org.iguana.datadependent.env.IEvaluatorContext;
import org.iguana.grammar.condition.DataDependentCondition;
import org.iguana.grammar.slot.BodyGrammarSlot;
import org.iguana.grammar.slot.GrammarSlot;
import org.iguana.gss.GSSNode;
import org.iguana.parser.descriptor.Descriptor;
import org.iguana.result.ResultOps;
import org.iguana.util.Configuration;
import org.iguana.util.ParseStatistics;
import org.iguana.util.ParserLogger;

import java.util.ArrayDeque;
import java.util.Deque;

public class ParserRuntimeImpl<T> implements ParserRuntime<T> {

    /**
     * The grammar slot at which a parse error is occurred.
     */
    private GrammarSlot<T> errorSlot;

    /**
     * The last input index at which an error is occurred.
     */
    private int errorIndex;

    private Input errorInput;

    /**
     * The current GSS node at which an error is occurred.
     */
    private GSSNode<T> errorGSSNode;

    private final Deque<Descriptor<T>> descriptorPool;

    private final Deque<Descriptor<T>> descriptorsStack;

    private final IEvaluatorContext ctx;

    private final Configuration config;

    private final ParserLogger logger = ParserLogger.getInstance();

    private ResultOps<T> resultOps;

    public ParserRuntimeImpl(Configuration config, ResultOps<T> resultOps) {
        this.resultOps = resultOps;
        this.descriptorsStack = new ArrayDeque<>(512);
        this.descriptorPool = new ArrayDeque<>(512);
        this.ctx = GLLEvaluator.getEvaluatorContext(config);
        this.config = config;
    }

    /**
     * Replaces the previously reported parse error with the new one if the
     * inputIndex of the new parse error is greater than the previous one. In
     * other words, we throw away an error if we find an error which happens at
     * the next position of input.
     *
     */
    @Override
    public void recordParseError(Input input, int i, GrammarSlot<T> slot, GSSNode<T> u) {
        if (i >= this.errorIndex) {
            logger.log("Error recorded at %s %d", slot, i);
            this.errorInput = input;
            this.errorIndex = i;
            this.errorSlot = slot;
            this.errorGSSNode = u;
        }
    }

    @Override
    public boolean hasDescriptor() {
        return !descriptorsStack.isEmpty();
    }

    @Override
    public Descriptor<T> nextDescriptor() {
        Descriptor<T> descriptor = descriptorsStack.pop();
        descriptorPool.push(descriptor);
        return descriptor;
    }

    @Override
    public void scheduleDescriptor(BodyGrammarSlot<T> grammarSlot, GSSNode<T> gssNode, T t, Environment env) {
        Descriptor<T> descriptor;
        if (!descriptorPool.isEmpty()) {
            descriptor = descriptorPool.pop();
            descriptor.init(grammarSlot, gssNode, t, env);
        } else {
            descriptor = new Descriptor<>(grammarSlot, gssNode, t, env);
        }
        descriptorsStack.push(descriptor);
        logger.descriptorAdded(descriptor);
    }

    @Override
    public IEvaluatorContext getEvaluatorContext() {
        return ctx;
    }

    @Override
    public Environment getEnvironment() {
        return ctx.getEnvironment();
    }

    @Override
    public void setEnvironment(Environment env) {
        ctx.setEnvironment(env);
    }

    @Override
    public Environment getEmptyEnvironment() {
        return ctx.getEmptyEnvironment();
    }

    @Override
    public Object evaluate(Statement[] statements, Environment env, Input input) {
        assert statements.length > 1;

        ctx.setEnvironment(env);

        int i = 0;
        while (i < statements.length) {
            statements[i].interpret(ctx, input);
            i++;
        }

        return null;
    }

    @Override
    public Object evaluate(DataDependentCondition condition, Environment env, Input input) {
        ctx.setEnvironment(env);
        return condition.getExpression().interpret(ctx, input);
    }

    @Override
    public Object evaluate(Expression expression, Environment env, Input input) {
        ctx.setEnvironment(env);
        return expression.interpret(ctx, input);
    }

    @Override
    public Object[] evaluate(Expression[] arguments, Environment env, Input input) {
        if (arguments == null) return null;

        ctx.setEnvironment(env);

        Object[] values = new Object[arguments.length];

        int i = 0;
        while (i < arguments.length) {
            values[i] = arguments[i].interpret(ctx, input);
            i++;
        }

        return values;
    }

    @Override
    public ParseError getParseError() {
        return new ParseError(errorSlot, errorInput == null ? Input.empty() : errorInput, errorIndex, errorGSSNode);
    }

    @Override
    public ParseStatistics getParseStatistics(Timer timer) {
        return ParseStatistics.builder()
                              .setNanoTime(timer.getNanoTime())
                              .setUserTime(timer.getUserTime())
                              .setSystemTime(timer.getSystemTime())
                              .setMemoryUsed(getMemoryUsed())
                              .setDescriptorsCount(logger.getDescriptorsCount())
                              .setGSSNodesCount(logger.getCountGSSNodes() + 1) // + start gss node
                              .setGSSEdgesCount(logger.getCountGSSEdges())
                              .setNonterminalNodesCount(logger.getCountNonterminalNodes())
                              .setTerminalNodesCount(logger.getCountTerminalNodes())
                              .setIntermediateNodesCount(logger.getCountIntermediateNodes())
                              .setPackedNodesCount(logger.getCountPackedNodes())
                              .setAmbiguousNodesCount(logger.getCountAmbiguousNodes())
                              .build();
    }

    private static int getMemoryUsed() {
        int mb = 1024 * 1024;
        Runtime runtime = Runtime.getRuntime();
        return (int) ((runtime.totalMemory() - runtime.freeMemory()) / mb);
    }

    @Override
    public Configuration getConfiguration() {
        return config;
    }

    @Override
    public int getDescriptorPoolSize() {
        return descriptorPool.size();
    }

    @Override
    public ResultOps<T> getResultOps() {
        return resultOps;
    }

}
