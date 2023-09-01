package com.db.praktikum.utils;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.hibernate.Session;
import com.db.praktikum.entity.Category;

public class CategoriesWindow extends JFrame implements TreeSelectionListener {
    JTree tree;

    public CategoriesWindow(Session session) {
        setTitle("Categories");
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");

        JPanel panel = new JPanel();
        List<DefaultMutableTreeNode> rooTreeNodes = getChildCategories(session, null);
        for (DefaultMutableTreeNode node : rooTreeNodes) {
            root.add(node);
        }
        tree = new JTree(root);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
        panel.add(tree);

        JScrollPane scrollPane = new JScrollPane(panel);
        add(scrollPane);
    }

    private <T> List<DefaultMutableTreeNode> getChildCategories(Session session, String parentId) {
        String hql = parentId == null ? "FROM Category WHERE parentCategory is null"
                : ("FROM Category WHERE parentCategory = " + parentId);

        // Retrieve all categories from the database
        List<Category> categories = session.createQuery(hql, Category.class).getResultList();

        List<DefaultMutableTreeNode> list = (List<DefaultMutableTreeNode>) categories.stream()
                .map(new Function<Category, DefaultMutableTreeNode>() {
                    public DefaultMutableTreeNode apply(Category category) {
                        return new DefaultMutableTreeNode(category.getCategoryName());
                    }
                }).toList();

        for (int i = 0; i < list.size(); i++) {
            Category category = categories.get(i);
            for (DefaultMutableTreeNode node : getChildCategories(session, category.getId().toString())) {
                list.get(i).add(node);
            }
        }
        return list;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }

        String path = Arrays.asList(node.getPath()).stream().skip(1).map(new Function<TreeNode, String>() {
            public String apply(TreeNode n) {
                return n.toString();
            }
        }).reduce(null, new BinaryOperator<String>() {
            public String apply(String a, String b) {
                return a == null ? b : a.concat("/").concat(b);
            }
        });

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(path), null);

        Toast toast = new Toast("Copied " + path + " to clipboard", 3000);
        toast.setVisible(true);
    }
}
