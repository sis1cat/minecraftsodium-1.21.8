package net.caffeinemc.mods.sodium.client.util.interval_tree;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

public class IntervalTree<T extends Comparable<? super T>> extends AbstractSet<Interval<T>> {
   TreeNode<T> root;
   int size;

   public boolean add(Interval<T> interval) {
      if (interval.isEmpty()) {
         return false;
      } else {
         int sizeBeforeOperation = this.size;
         this.root = TreeNode.addInterval(this, this.root, interval);
         return this.size == sizeBeforeOperation;
      }
   }

   public Set<Interval<T>> query(Interval<T> interval) {
      Set<Interval<T>> result = new HashSet<>();
      if (this.root != null && !interval.isEmpty()) {
         TreeNode<T> node = this.root;

         while (node != null) {
            if (interval.contains(node.midpoint)) {
               result.addAll(node.increasing);
               TreeNode.rangeQueryLeft(node.left, interval, result);
               TreeNode.rangeQueryRight(node.right, interval, result);
               break;
            }

            if (interval.isLeftOf(node.midpoint)) {
               Iterator var6 = node.increasing.iterator();

               while (true) {
                  if (var6.hasNext()) {
                     Interval<T> next = (Interval<T>)var6.next();
                     if (interval.intersects(next)) {
                        result.add(next);
                        continue;
                     }
                  }

                  node = node.left;
                  break;
               }
            } else {
               Iterator var4 = node.decreasing.iterator();

               while (true) {
                  if (var4.hasNext()) {
                     Interval<T> next = (Interval<T>)var4.next();
                     if (interval.intersects(next)) {
                        result.add(next);
                        continue;
                     }
                  }

                  node = node.right;
                  break;
               }
            }
         }

         return result;
      } else {
         return result;
      }
   }

   public boolean remove(Interval<T> interval) {
      if (!interval.isEmpty() && this.root != null) {
         int sizeBeforeOperation = this.size;
         this.root = TreeNode.removeInterval(this, this.root, interval);
         return this.size == sizeBeforeOperation;
      } else {
         return false;
      }
   }

   @Override
   public Iterator<Interval<T>> iterator() {
      if (this.root == null) {
         return Collections.emptyIterator();
      } else {
         final TreeNode.TreeNodeIterator it = this.root.iterator();
         return new Iterator<Interval<T>>() {
            @Override
            public void remove() {
               if (it.currentNode.increasing.size() == 1) {
                  IntervalTree.this.root = TreeNode.removeInterval(IntervalTree.this, IntervalTree.this.root, it.currentInterval);
                  TreeNode<T> node = IntervalTree.this.root;
                  it.stack = new Stack<>();

                  while (node != it.subtreeRoot) {
                     if (it.currentNode.midpoint.compareTo(node.midpoint) < 0) {
                        it.stack.push(node);
                        node = node.left;
                     } else {
                        node = node.right;
                     }
                  }
               } else {
                  it.remove();
               }
            }

            @Override
            public boolean hasNext() {
               return it.hasNext();
            }

            public Interval<T> next() {
               return it.next();
            }
         };
      }
   }

   @Override
   public int size() {
      return this.size;
   }

   @Override
   public void clear() {
      this.size = 0;
      this.root = null;
   }

   @Override
   public boolean contains(Object o) {
      if (this.root == null || o == null)
         return false;
      if (!(o instanceof Interval))
         return false;
      Interval<T> query;
      query = (Interval<T>) o;
      TreeNode<T> node = this.root;
      while (node != null) {
         if (query.contains(node.midpoint)) {
            return node.increasing.contains(query);
         }
         if (query.isLeftOf(node.midpoint)) {
            node = node.left;
         } else {
            node = node.right;
         }
      }

      return false;
   }
}
