package panetina.elarion.core.metric;

/** Deterministic order-statistic treap for logarithmic competition-rank queries. */
final class MetricValueCountIndex {
    private Node root;

    void add(long value, int delta) {
        root = add(root, value, delta);
    }

    long countLess(long value) {
        return countLess(root, value);
    }

    long countGreater(long value) {
        return size(root) - countLess(root, value) - count(value);
    }

    private static Node add(Node node, long key, int delta) {
        if (node == null) {
            if (delta <= 0) throw new IllegalStateException("cannot remove missing metric value");
            return new Node(key, delta);
        }
        if (key == node.key) {
            node.count += delta;
            if (node.count < 0) throw new IllegalStateException("metric value count underflow");
            if (node.count == 0) return merge(node.left, node.right);
        } else if (key < node.key) {
            node.left = add(node.left, key, delta);
            if (node.left != null && higher(node.left, node)) node = rotateRight(node);
        } else {
            node.right = add(node.right, key, delta);
            if (node.right != null && higher(node.right, node)) node = rotateLeft(node);
        }
        node.recalculate();
        return node;
    }

    private static long countLess(Node node, long value) {
        if (node == null) return 0;
        if (value <= node.key) return countLess(node.left, value);
        return size(node.left) + node.count + countLess(node.right, value);
    }

    private static long count(long value, Node node) {
        Node current = node;
        while (current != null) {
            if (value == current.key) return current.count;
            current = value < current.key ? current.left : current.right;
        }
        return 0;
    }

    private long count(long value) {
        return count(value, root);
    }

    private static Node merge(Node left, Node right) {
        if (left == null) return right;
        if (right == null) return left;
        if (higher(left, right)) {
            left.right = merge(left.right, right);
            left.recalculate();
            return left;
        }
        right.left = merge(left, right.left);
        right.recalculate();
        return right;
    }

    private static Node rotateRight(Node root) {
        Node next = root.left;
        root.left = next.right;
        next.right = root;
        root.recalculate();
        next.recalculate();
        return next;
    }

    private static Node rotateLeft(Node root) {
        Node next = root.right;
        root.right = next.left;
        next.left = root;
        root.recalculate();
        next.recalculate();
        return next;
    }

    private static boolean higher(Node left, Node right) {
        int priority = Long.compareUnsigned(left.priority, right.priority);
        return priority > 0 || priority == 0 && left.key < right.key;
    }

    private static long size(Node node) {
        return node == null ? 0 : node.subtreeCount;
    }

    private static long mix(long value) {
        value ^= value >>> 30;
        value *= 0xbf58476d1ce4e5b9L;
        value ^= value >>> 27;
        value *= 0x94d049bb133111ebL;
        return value ^ value >>> 31;
    }

    private static final class Node {
        private final long key;
        private final long priority;
        private int count;
        private long subtreeCount;
        private Node left;
        private Node right;

        private Node(long key, int count) {
            this.key = key;
            this.priority = mix(key);
            this.count = count;
            recalculate();
        }

        private void recalculate() {
            subtreeCount = Math.addExact(count, Math.addExact(size(left), size(right)));
        }
    }
}
