package net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.trigger;

import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import java.util.Collection;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.AlignableNormal;
import net.caffeinemc.mods.sodium.client.util.interval_tree.DoubleInterval;
import net.caffeinemc.mods.sodium.client.util.interval_tree.Interval;
import net.caffeinemc.mods.sodium.client.util.interval_tree.IntervalTree;
import org.joml.Math;
import org.joml.Vector3dc;

public class NormalList {
   private static final int HASH_SET_THRESHOLD = 20;
   private static final int ARRAY_SET_THRESHOLD = 10;
   private final AlignableNormal normal;
   private final IntervalTree<Double> intervalTree = new IntervalTree<>();
   private final Object2ReferenceOpenHashMap<DoubleInterval, Collection<Group>> groupsByInterval = new Object2ReferenceOpenHashMap();
   private final Long2ReferenceOpenHashMap<Group> groupsBySection = new Long2ReferenceOpenHashMap();

   NormalList(AlignableNormal normal) {
      this.normal = normal;
   }

   public AlignableNormal getNormal() {
      return this.normal;
   }

   private double normalDotDouble(Vector3dc v) {
      return Math.fma(this.normal.x, v.x(), Math.fma(this.normal.y, v.y(), this.normal.z * v.z()));
   }

   void processMovement(SortTriggering ts, CameraMovement movement) {
      double start = this.normalDotDouble(movement.start());
      double end = this.normalDotDouble(movement.end());
      if (!(start >= end)) {
         DoubleInterval interval = new DoubleInterval(start, end, Interval.Bounded.CLOSED);

         for (Interval<Double> groupInterval : this.intervalTree.query(interval)) {
            for (Group group : this.groupsByInterval.get(groupInterval)) {
               group.triggerRange(ts, start, end);
            }
         }
      }
   }

   void processCatchup(SortTriggering ts, CameraMovement movement, long sectionPos) {
      double start = this.normalDotDouble(movement.start());
      double end = this.normalDotDouble(movement.end());
      if (!(start >= end)) {
         Group group = (Group)this.groupsBySection.get(sectionPos);
         if (group != null) {
            group.triggerRange(ts, start, end);
         }
      }
   }

   private void removeGroupInterval(Group group) {
      Collection<Group> groups = (Collection<Group>)this.groupsByInterval.get(group.distances);
      if (groups != null) {
         groups.remove(group);
         if (groups.isEmpty()) {
            this.groupsByInterval.remove(group.distances);
            this.intervalTree.remove(group.distances);
         } else if (groups.size() <= 10) {
            Collection<Group> var3 = new ReferenceArraySet(groups);
            this.groupsByInterval.put(group.distances, var3);
         }
      }
   }

   private void addGroupInterval(Group group) {
      Collection<Group> groups = (Collection<Group>)this.groupsByInterval.get(group.distances);
      if (groups == null) {
         groups = new ReferenceArraySet();
         this.groupsByInterval.put(group.distances, groups);
         this.intervalTree.add(group.distances);
      } else if (groups.size() >= 20) {
         groups = new ReferenceLinkedOpenHashSet(groups);
         this.groupsByInterval.put(group.distances, groups);
      }

      groups.add(group);
   }

   boolean hasSection(long sectionPos) {
      return this.groupsBySection.containsKey(sectionPos);
   }

   boolean isEmpty() {
      return this.groupsBySection.isEmpty();
   }

   void addSection(NormalPlanes normalPlanes, long sectionPos) {
      Group group = new Group(normalPlanes);
      this.groupsBySection.put(sectionPos, group);
      this.addGroupInterval(group);
   }

   void removeSection(long sectionPos) {
      Group group = (Group)this.groupsBySection.remove(sectionPos);
      if (group != null) {
         this.removeGroupInterval(group);
      }
   }

   void updateSection(NormalPlanes normalPlanes, long sectionPos) {
      Group group = (Group)this.groupsBySection.get(sectionPos);
      if (!group.normalPlanesEquals(normalPlanes)) {
         this.removeGroupInterval(group);
         group.replaceWith(normalPlanes);
         this.addGroupInterval(group);
      }
   }
}
