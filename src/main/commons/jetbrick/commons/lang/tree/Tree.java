package jetbrick.commons.lang.tree;

import java.util.*;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tree<T> {
    protected static final Logger log = LoggerFactory.getLogger(Tree.class);

    @SuppressWarnings("unchecked")
    private Map<String, TreeNode<T>> nodesMap = new ListOrderedMap();
    private List<TreeNode<T>> roots = new ArrayList<TreeNode<T>>();
    private long loadedTimestamp = 0;

    public Tree(List<T> nodes, TreeNodeTransformer<T> transformer) {
        log.debug("Tree is reloading all data ...");

        synchronized (this) {
            // initialize
            nodesMap.clear();
            roots.clear();

            for (int i = 0; i < nodes.size(); i++) {
                TreeNode<T> node = transformer.transform(nodes.get(i)); // transform
                node.tree = this;
                node.data = nodes.get(i);
                nodesMap.put(node.getId(), node);
            }

            Iterator<TreeNode<T>> it = nodesMap.values().iterator();
            while (it.hasNext()) {
                TreeNode<T> node = it.next();
                String parentId = node.getParentId();
                if (this.isRoot(node)) {
                    roots.add(node);
                } else {
                    TreeNode<T> parent = nodesMap.get(parentId);
                    if (parent != null) {
                        parent.children.add(node);
                        node.parent = parent;
                    } else {
                        log.warn("node [id=" + node.getId() + "]: missing parent node.");
                    }
                }
            }
            loadedTimestamp = System.currentTimeMillis();
        }
        if (roots.size() == 0) {
            log.error("Root node is not be defined");
        }

        log.debug("Tree reloaded all data ok.");
    }

    public long getLoadedTimestamp() {
        return loadedTimestamp;
    }

    /**
     * can be override
     */
    protected boolean isRoot(TreeNode<T> node) {
        return StringUtils.isBlank(node.getParentId()) || "0".equals(node.getParentId());
    }

    public TreeNode<T> getRoot() {
        return roots.size() > 0 ? roots.get(0) : null;
    }

    public List<TreeNode<T>> getRoots() {
        return roots;
    }

    public List<T> getDataRoots() {
        return TreeNode.transformedList(roots);
    }

    public List<TreeNode<T>> getAllNode() {
        return (List<TreeNode<T>>) nodesMap.values();
    }

    public List<T> getAllData() {
        return TreeNode.transformedList(getAllNode());
    }

    public TreeNode<T> getNode(String id) {
        return nodesMap.get(id);
    }

    public T getData(String id) {
        TreeNode<T> node = getNode(id);
        return node == null ? null : node.getData();
    }
}
