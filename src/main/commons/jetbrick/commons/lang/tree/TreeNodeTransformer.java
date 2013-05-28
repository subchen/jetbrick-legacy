package jetbrick.commons.lang.tree;

public interface TreeNodeTransformer<T> {

    /**
     * <pre>
     * Usage:
     *     TreeNode node = new TreeNode();
     *     node.setId(info.getId());
     *     node.setParentId(info.getParentId());
     *     node.setName(info.getName());
     *     node.setData(info);
     *     return node;
     * </pre>
     */
    public TreeNode<T> transform(T info);

}
