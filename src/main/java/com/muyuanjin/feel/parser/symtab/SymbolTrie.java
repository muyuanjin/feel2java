package com.muyuanjin.feel.parser.symtab;

import com.muyuanjin.common.entity.Pair;
import com.muyuanjin.common.util.MapUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import jakarta.annotation.Nullable;

import java.io.Serializable;
import java.util.*;

/**
 * <a href="https://www.geeksforgeeks.org/longest-prefix-matching-a-trie-based-solution-in-java/?ref=ml_lbp">最长前缀匹配</a>
 *
 * @author muyuanjin
 */
@SuppressWarnings("DuplicatedCode")
final class SymbolTrie implements Serializable {
    private final Node root;
    private Deque<List<Pair<Node, Node>>> markNodes;

    public SymbolTrie() {
        root = new Node();
    }

    private SymbolTrie(Node source) {
        root = Node.copy(this, source);
    }

    /**
     * 标记当前状态
     */
    public void mark() {
        if (markNodes == null) {
            markNodes = new ArrayDeque<>();
        }
        markNodes.push(new ArrayList<>());
    }

    /**
     * 复原到上一次标记的状态
     */
    public void release() {
        if (markNodes == null || markNodes.isEmpty()) {
            return;
        }
        for (var entry : markNodes.pop()) {
            Node now = entry.getKey();
            now.reset(entry.getValue());
        }
    }


    public void releaseAll() {
        if (markNodes == null || markNodes.isEmpty()) {
            return;
        }
        while (!markNodes.isEmpty()) {
            for (var entry : markNodes.pop()) {
                Node now = entry.getKey();
                now.reset(entry.getValue());
            }
        }
    }

    public void insert(@Nullable Symbol.Node node) {
        if (node == null) {
            return;
        }
        String name = node.getName();
        List<String> tokens = node.getTokens();
        if (tokens == null || name == null || tokens.isEmpty()) {
            return;
        }
        Node current = root;
        for (String token : tokens) {
            current = current.findChildOrNew(token);
        }
        current.endOfKey = true;
        if (node.getTypeSymbol() != null) {
            current.typeSymbol = node.getTypeSymbol();
        }
        if (node.getVarSymbol() != null) {
            current.varSymbol = node.getVarSymbol();
        }
        if (node.getFunSymbol() != null) {
            current.funSymbol = node.getFunSymbol();
        }
    }

    public void insert(@Nullable Symbol symbol) {
        if (symbol == null) {
            return;
        }
        String name = symbol.getName();
        List<String> tokens = symbol.getTokens();
        if (tokens == null || name == null || tokens.isEmpty()) {
            return;
        }
        Node current = root;
        for (String token : tokens) {
            current = current.findChildOrNew(token);
        }
        current.endOfKey = true;
        if (symbol instanceof TypeSymbol type) {
            current.typeSymbol = type;
        }
        if (symbol instanceof VariableSymbol var) {
            current.varSymbol = var;
        }
        if (symbol instanceof FunctionSymbol fun) {
            current.funSymbol = fun;
        }
    }

    @Nullable
    public Symbol.Node get(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        Node node = root.findChild(token);
        if (node == null || !node.alive || !node.endOfKey) {
            return null;
        }
        return node;
    }

    /**
     * 查找以给定token组合（token不可拆分）最长匹配的符号节点{@link Symbol.Node}
     *
     * @return 匹配的符号组和 对应的token index (相对值)
     */
    @Nullable
    public Pair<Symbol.Node, Integer> prefix(Iterator<String> tokens) {
        if (tokens == null) {
            return null;
        }
        Node current = root;
        Node lastEndNode = null;
        int i = 0;
        int numberOfMatched = 0;
        while (tokens.hasNext()) {
            String token = tokens.next();
            Node node = current.findChild(token);
            if (node == null || !node.alive) {
                return lastEndNode == null ? null : new Pair<>(lastEndNode, numberOfMatched);
            }
            if (node.endOfKey) {
                numberOfMatched = i;
                lastEndNode = node;
            }
            current = node;
            i++;
        }
        return lastEndNode == null ? null : new Pair<>(lastEndNode, numberOfMatched);
    }

    public SymbolTrie copy() {
        return new SymbolTrie(root);
    }

    @EqualsAndHashCode
    @ToString(onlyExplicitlyIncluded = true)
    private final class Node implements Symbol.Node, Serializable {
        @Getter
        @ToString.Include
        private TypeSymbol typeSymbol;
        @Getter
        @ToString.Include
        private VariableSymbol varSymbol;
        @Getter
        @ToString.Include
        private FunctionSymbol funSymbol;

        private Map<String, Node> children;
        private boolean endOfKey = false;
        private boolean alive = false;

        public Node() {
        }

        public Node(String token) {
        }

        public Node findChildOrNew(String token) {
            if (children == null) {
                children = new HashMap<>();
            }
            return children.computeIfAbsent(token, Node::new).enable();
        }

        public Node findChild(String token) {
            if (children == null || children.isEmpty()) {
                return null;
            }
            return children.get(token);
        }

        private Node enable() {
            if (markNodes != null && !markNodes.isEmpty()) {
                markNodes.peek().add(new Pair<>(this, alive ? snapshot() : null));
            }
            alive = true;
            return this;
        }

        private void reset(Node node) {
            if (node == null) {
                alive = false;
                endOfKey = false;
                typeSymbol = null;
                varSymbol = null;
                funSymbol = null;
                return;
            }
            alive = node.alive;
            endOfKey = node.endOfKey;
            typeSymbol = node.typeSymbol;
            varSymbol = node.varSymbol;
            funSymbol = node.funSymbol;
        }

        private Node snapshot() {
            Node node = new Node();
            node.typeSymbol = typeSymbol;
            node.varSymbol = varSymbol;
            node.funSymbol = funSymbol;
            node.endOfKey = endOfKey;
            node.alive = alive;
            return node;
        }

        private static Node copy(SymbolTrie newParent, Node source) {
            Node node = newParent.new Node();
            node.typeSymbol = source.typeSymbol;
            node.varSymbol = source.varSymbol;
            node.funSymbol = source.funSymbol;
            node.endOfKey = source.endOfKey;
            node.alive = source.alive;
            if (source.children != null) {
                node.children = MapUtil.newHashMap(source.children.size());
                for (var entry : source.children.entrySet()) {
                    node.children.put(entry.getKey(), copy(newParent, entry.getValue()));
                }
            }
            return node;
        }
    }
}