package jetbrick.commons.lang.tree;

import java.util.*;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.ObjectUtils;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;

public class TreeNode<T> implements JSONAware {
    protected static final Transformer dataTransformer = new Transformer() {
        public Object transform(Object treeNode) {
            return ((TreeNode<?>) treeNode).getData();
        }
    };

    protected Tree<T> tree;
    protected TreeNode<T> parent;
    protected List<TreeNode<T>> children = new Vector<TreeNode<T>>();
    protected List<TreeNode<T>> childrenGroup = null;
    protected String id;
    protected String parentId;
    protected String name;
    protected T data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public T getData() {
        return data;
    }

    public Tree<T> getTree() {
        return tree;
    }

    public TreeNode<T> getParent() {
        return this.parent;
    }

    public List<TreeNode<T>> getChildren() {
        return this.children;
    }

    /**
     * get all children, and children's children
     */
    public List<TreeNode<T>> getAllChildren() {
        if (childrenGroup == null) {
            synchronized (tree) {
                childrenGroup = new ArrayList<TreeNode<T>>();
                for (int i = 0; i < children.size(); i++) {
                    TreeNode<T> node = children.get(i);
                    childrenGroup.add(node);
                    childrenGroup.addAll(node.getAllChildren());
                }
            }
        }
        return childrenGroup;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected static <T> List<T> transformedList(List<TreeNode<T>> nodes) {
        List results = ListUtils.transformedList(new ArrayList(), dataTransformer);
        results.addAll(nodes);
        return results;
    }

    /**
     * get the children (bind data list)
     */
    public List<T> getBindChildren() {
        return transformedList(getChildren());
    }

    /**
     * get all children, and children's children (bind data list)
     */
    public List<T> getAllBindChildren() {
        return transformedList(getAllChildren());
    }

    /**
     * get all parents, and parent's parent
     */
    public List<TreeNode<T>> getParents() {
        List<TreeNode<T>> results = new Vector<TreeNode<T>>();
        TreeNode<T> parent = getParent();
        while (parent != null) {
            results.add(parent);
            parent = parent.getParent();
        }
        return results;
    }

    /**
     * A.isMyParent(B) == B is A' parent ? <br>
     * root.isMyParent(null) == true; <br>
     * root.isMyParent(*) == false <br>
     * *.isMyParent(null) == false
     */
    public boolean isMyParent(String id) {
        TreeNode<T> target = tree.getNode(id);
        TreeNode<T> parent = getParent();
        if (parent == null) {
            return target == null;
        } else {
            return parent.equals(target);
        }
    }

    /**
     * A.isMyAncestor(B) == B is A' ancestor ? <br>
     * *.isMyAncestor(null) == true;
     */
    public boolean isMyAncestor(String id) {
        TreeNode<T> target = tree.getNode(id);
        if (target == null) return true;

        return target.getAllChildren().contains(this);
    }

    /**
     * A.isMyBrother(B) == B is A' brother ? <br>
     * *.isMyBrother(null) == false
     */
    public boolean isMyBrother(String id) {
        TreeNode<T> target = tree.getNode(id);
        if (target == null) return false;

        TreeNode<T> p1 = getParent();
        TreeNode<T> p2 = target.getParent();
        return ObjectUtils.equals(p1, p2);
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        json.put("data", data);
        json.put("children", children);
        return json;
    }

    @Override
    public String toJSONString() {
        return toJSONObject().toString();
    }
}
