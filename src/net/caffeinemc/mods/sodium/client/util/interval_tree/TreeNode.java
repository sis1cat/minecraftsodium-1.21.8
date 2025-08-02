package net.caffeinemc.mods.sodium.client.util.interval_tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

public class TreeNode<T extends Comparable<? super T>> implements Iterable<Interval<T>> {
   protected final NavigableSet<Interval<T>> increasing;
   protected final NavigableSet<Interval<T>> decreasing = new TreeSet<>(Interval.sweepRightToLeft);
   protected TreeNode<T> left;
   protected TreeNode<T> right;
   protected final T midpoint;
   protected int height;

   public TreeNode(Interval<T> interval) {
      this.increasing = new TreeSet<>(Interval.sweepLeftToRight);
      this.decreasing.add(interval);
      this.increasing.add(interval);
      this.midpoint = interval.getMidpoint();
      this.height = 1;
   }

   public static <T extends Comparable<? super T>> TreeNode<T> addInterval(IntervalTree<T> tree, TreeNode<T> root, Interval<T> interval) {
      if (root == null) {
         tree.size++;
         return new TreeNode<>(interval);
      } else if (interval.contains(root.midpoint)) {
         if (root.decreasing.add(interval)) {
            tree.size++;
         }

         root.increasing.add(interval);
         return root;
      } else {
         if (interval.isLeftOf(root.midpoint)) {
            root.left = addInterval(tree, root.left, interval);
            root.height = Math.max(height(root.left), height(root.right)) + 1;
         } else {
            root.right = addInterval(tree, root.right, interval);
            root.height = Math.max(height(root.left), height(root.right)) + 1;
         }

         return root.balanceOut();
      }
   }

   public int height() {
      return this.height;
   }

   private static int height(TreeNode node) {
      return node == null ? 0 : node.height();
   }

   private TreeNode<T> balanceOut() {
      int balance = height(this.left) - height(this.right);
      if (balance < -1) {
         if (height(this.right.left) > height(this.right.right)) {
            this.right = this.right.rightRotate();
            return this.leftRotate();
         } else {
            return this.leftRotate();
         }
      } else if (balance > 1) {
         if (height(this.left.right) > height(this.left.left)) {
            this.left = this.left.leftRotate();
            return this.rightRotate();
         } else {
            return this.rightRotate();
         }
      } else {
         return this;
      }
   }

   private TreeNode<T> leftRotate() {
      TreeNode<T> head = this.right;
      this.right = head.left;
      head.left = this;
      this.height = Math.max(height(this.right), height(this.left)) + 1;
      head.left = head.assimilateOverlappingIntervals(this);
      return head;
   }

   private TreeNode<T> rightRotate() {
      TreeNode<T> head = this.left;
      this.left = head.right;
      head.right = this;
      this.height = Math.max(height(this.right), height(this.left)) + 1;
      head.right = head.assimilateOverlappingIntervals(this);
      return head;
   }

   private TreeNode<T> assimilateOverlappingIntervals(TreeNode<T> from) {
      ArrayList<Interval<T>> tmp = new ArrayList<>();
      if (this.midpoint.compareTo(from.midpoint) < 0) {
         for (Interval<T> next : from.increasing) {
            if (next.isRightOf(this.midpoint)) {
               break;
            }

            tmp.add(next);
         }
      } else {
         for (Interval<T> next : from.decreasing) {
            if (next.isLeftOf(this.midpoint)) {
               break;
            }

            tmp.add(next);
         }
      }

      tmp.forEach(from.increasing::remove);
      tmp.forEach(from.decreasing::remove);
      this.increasing.addAll(tmp);
      this.decreasing.addAll(tmp);
      return from.increasing.isEmpty() ? deleteNode(from) : from;
   }

   public static <T extends Comparable<? super T>> TreeNode<T> removeInterval(IntervalTree<T> tree, TreeNode<T> root, Interval<T> interval) {
      if (root == null) {
         return null;
      } else {
         if (interval.contains(root.midpoint)) {
            if (root.decreasing.remove(interval)) {
               tree.size--;
            }

            root.increasing.remove(interval);
            if (root.increasing.isEmpty()) {
               return deleteNode(root);
            }
         } else if (interval.isLeftOf(root.midpoint)) {
            root.left = removeInterval(tree, root.left, interval);
         } else {
            root.right = removeInterval(tree, root.right, interval);
         }

         return root.balanceOut();
      }
   }

   private static <T extends Comparable<? super T>> TreeNode<T> deleteNode(TreeNode<T> root) {
      if (root.left == null && root.right == null) {
         return null;
      } else if (root.left == null) {
         return root.right;
      } else {
         TreeNode<T> node = root.left;

         Stack<TreeNode<T>> stack;
         for (stack = new Stack<>(); node.right != null; node = node.right) {
            stack.push(node);
         }

         if (!stack.isEmpty()) {
            stack.peek().right = node.left;
            node.left = root.left;
         }

         node.right = root.right;
         TreeNode<T> newRoot = node;

         while (!stack.isEmpty()) {
            node = stack.pop();
            if (!stack.isEmpty()) {
               stack.peek().right = newRoot.assimilateOverlappingIntervals(node);
            } else {
               newRoot.left = newRoot.assimilateOverlappingIntervals(node);
            }
         }

         return newRoot.balanceOut();
      }
   }

   static <T extends Comparable<? super T>> void rangeQueryLeft(TreeNode<T> node, Interval<T> query, Set<Interval<T>> result) {
      while (node != null) {
         if (query.contains(node.midpoint)) {
            result.addAll(node.increasing);
            if (node.right != null) {
               for (Interval<T> next : node.right) {
                  result.add(next);
               }
            }

            node = node.left;
         } else {
            Iterator var3 = node.decreasing.iterator();

            while (true) {
               if (var3.hasNext()) {
                  Interval<T> next = (Interval<T>)var3.next();
                  if (!next.isLeftOf(query)) {
                     result.add(next);
                     continue;
                  }
               }

               node = node.right;
               break;
            }
         }
      }
   }

   static <T extends Comparable<? super T>> void rangeQueryRight(TreeNode<T> node, Interval<T> query, Set<Interval<T>> result) {
      while (node != null) {
         if (query.contains(node.midpoint)) {
            result.addAll(node.increasing);
            if (node.left != null) {
               for (Interval<T> next : node.left) {
                  result.add(next);
               }
            }

            node = node.right;
         } else {
            Iterator var3 = node.increasing.iterator();

            while (true) {
               if (var3.hasNext()) {
                  Interval<T> next = (Interval<T>)var3.next();
                  if (!next.isRightOf(query)) {
                     result.add(next);
                     continue;
                  }
               }

               node = node.left;
               break;
            }
         }
      }
   }

   public TreeNode<T>.TreeNodeIterator iterator() {
      return new TreeNode.TreeNodeIterator();
   }

   class TreeNodeIterator implements Iterator<Interval<T>> {
      Stack<TreeNode<T>> stack = new Stack<>();
      TreeNode<T> subtreeRoot = TreeNode.this;
      TreeNode<T> currentNode;
      Interval<T> currentInterval;
      Iterator<Interval<T>> iterator = Collections.emptyIterator();

      @Override
      public boolean hasNext() {
         return this.subtreeRoot != null || !this.stack.isEmpty() || this.iterator.hasNext();
      }

      public Interval<T> next() {
         if (!this.iterator.hasNext()) {
            while (this.subtreeRoot != null) {
               this.stack.push(this.subtreeRoot);
               this.subtreeRoot = this.subtreeRoot.left;
            }

            if (this.stack.isEmpty()) {
               throw new NoSuchElementException();
            }

            this.currentNode = this.stack.pop();
            this.iterator = this.currentNode.increasing.iterator();
            this.subtreeRoot = this.currentNode.right;
         }

         this.currentInterval = this.iterator.next();
         return this.currentInterval;
      }

      @Override
      public void remove() {
         this.iterator.remove();
      }
   }
}
