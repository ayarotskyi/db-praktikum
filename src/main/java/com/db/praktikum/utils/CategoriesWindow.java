package com.db.praktikum.utils;

import java.util.List;
import java.util.function.Function;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.hibernate.Session;
import com.db.praktikum.entity.Category;

public class CategoriesWindow extends JFrame {

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
        JTree tree = new JTree(root);
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
}
