package net.caffeinemc.mods.sodium.client.util.interval_tree;

import java.util.Comparator;

public abstract class Interval<T extends Comparable<? super T>> {
   private T start;
   private T end;
   private boolean isStartInclusive;
   private boolean isEndInclusive;
   public static final Comparator<Interval> sweepLeftToRight = (a, b) -> {
      int compare = a.compareStarts(b);
      if (compare != 0) {
         return compare;
      } else {
         compare = a.compareEnds(b);
         return compare != 0 ? compare : a.compareSpecialization(b);
      }
   };
   public static final Comparator<Interval> sweepRightToLeft = (a, b) -> {
      int compare = b.compareEnds(a);
      if (compare != 0) {
         return compare;
      } else {
         compare = b.compareStarts(a);
         return compare != 0 ? compare : a.compareSpecialization(b);
      }
   };

   public Interval() {
      this.isStartInclusive = true;
      this.isEndInclusive = true;
   }

   public Interval(T start, T end, Interval.Bounded type) {
      this.start = start;
      this.end = end;
      if (type == null) {
         type = Interval.Bounded.CLOSED;
      }

      switch (type) {
         case OPEN:
            break;
         case CLOSED:
            this.isStartInclusive = true;
            this.isEndInclusive = true;
            break;
         case CLOSED_RIGHT:
            this.isEndInclusive = true;
            break;
         default:
            this.isStartInclusive = true;
      }
   }

   public Interval(T value, Interval.Unbounded type) {
      if (type == null) {
         type = Interval.Unbounded.CLOSED_RIGHT;
      }

      switch (type) {
         case OPEN_LEFT:
            this.start = value;
            this.isStartInclusive = false;
            this.isEndInclusive = true;
            break;
         case CLOSED_LEFT:
            this.start = value;
            this.isStartInclusive = true;
            this.isEndInclusive = true;
            break;
         case OPEN_RIGHT:
            this.end = value;
            this.isStartInclusive = true;
            this.isEndInclusive = false;
            break;
         default:
            this.end = value;
            this.isStartInclusive = true;
            this.isEndInclusive = true;
      }
   }

   public boolean isEmpty() {
      if (this.start != null && this.end != null) {
         int compare = this.start.compareTo(this.end);
         return compare > 0 ? true : compare == 0 && (!this.isEndInclusive || !this.isStartInclusive);
      } else {
         return false;
      }
   }

   protected abstract Interval<T> create();

   public abstract T getMidpoint();

   protected Interval<T> create(T start, boolean isStartInclusive, T end, boolean isEndInclusive) {
      Interval<T> interval = this.create();
      interval.start = start;
      interval.isStartInclusive = isStartInclusive;
      interval.end = end;
      interval.isEndInclusive = isEndInclusive;
      return interval;
   }

   public T getStart() {
      return this.start;
   }

   public T getEnd() {
      return this.end;
   }

   public boolean isStartInclusive() {
      return this.isStartInclusive;
   }

   public boolean isEndInclusive() {
      return this.isEndInclusive;
   }

   public boolean contains(T query) {
      if (!this.isEmpty() && query != null) {
         int startCompare = this.start == null ? 1 : query.compareTo(this.start);
         int endCompare = this.end == null ? -1 : query.compareTo(this.end);
         return startCompare > 0 && endCompare < 0 ? true : startCompare == 0 && this.isStartInclusive || endCompare == 0 && this.isEndInclusive;
      } else {
         return false;
      }
   }

   public Interval<T> getIntersection(Interval<T> other) {
      if (other != null && !this.isEmpty() && !other.isEmpty()) {
         if ((other.start != null || this.start == null) && (this.start == null || this.start.compareTo(other.start) <= 0)) {
            if (this.end == null
               || other.start == null
               || this.end.compareTo(other.start) >= 0 && (this.end.compareTo(other.start) != 0 || this.isEndInclusive && other.isStartInclusive)) {
               T newStart;
               boolean isNewStartInclusive;
               if (other.start == null) {
                  newStart = null;
                  isNewStartInclusive = true;
               } else {
                  newStart = other.start;
                  if (this.start != null && other.start.compareTo(this.start) == 0) {
                     isNewStartInclusive = other.isStartInclusive && this.isStartInclusive;
                  } else {
                     isNewStartInclusive = other.isStartInclusive;
                  }
               }

               T newEnd;
               boolean isNewEndInclusive;
               if (this.end == null) {
                  newEnd = other.end;
                  isNewEndInclusive = other.isEndInclusive;
               } else if (other.end == null) {
                  newEnd = this.end;
                  isNewEndInclusive = this.isEndInclusive;
               } else {
                  int compare = this.end.compareTo(other.end);
                  if (compare == 0) {
                     newEnd = this.end;
                     isNewEndInclusive = this.isEndInclusive && other.isEndInclusive;
                  } else if (compare < 0) {
                     newEnd = this.end;
                     isNewEndInclusive = this.isEndInclusive;
                  } else {
                     newEnd = other.end;
                     isNewEndInclusive = other.isEndInclusive;
                  }
               }

               Interval<T> intersection = this.create(newStart, isNewStartInclusive, newEnd, isNewEndInclusive);
               return intersection.isEmpty() ? null : intersection;
            } else {
               return null;
            }
         } else {
            return other.getIntersection(this);
         }
      } else {
         return null;
      }
   }

   public boolean intersects(Interval<T> query) {
      if (query == null) {
         return false;
      } else {
         Interval<T> intersection = this.getIntersection(query);
         return intersection != null;
      }
   }

   public boolean isRightOf(T point, boolean inclusive) {
      if (point != null && this.start != null) {
         int compare = point.compareTo(this.start);
         return compare != 0 ? compare < 0 : !this.isStartInclusive() || !inclusive;
      } else {
         return false;
      }
   }

   public boolean isRightOf(T point) {
      return this.isRightOf(point, true);
   }

   public boolean isRightOf(Interval<T> other) {
      return other != null && !other.isEmpty() ? this.isRightOf(other.end, other.isEndInclusive()) : false;
   }

   public boolean isLeftOf(T point, boolean inclusive) {
      if (point != null && this.end != null) {
         int compare = point.compareTo(this.end);
         return compare != 0 ? compare > 0 : !this.isEndInclusive() || !inclusive;
      } else {
         return false;
      }
   }

   public boolean isLeftOf(T point) {
      return this.isLeftOf(point, true);
   }

   public boolean isLeftOf(Interval<T> other) {
      return other != null && !other.isEmpty() ? this.isLeftOf(other.start, other.isStartInclusive()) : false;
   }

   private int compareStarts(Interval<T> other) {
      if (this.start == null && other.start == null) {
         return 0;
      } else if (this.start == null) {
         return -1;
      } else if (other.start == null) {
         return 1;
      } else {
         int compare = this.start.compareTo(other.start);
         if (compare != 0) {
            return compare;
         } else if (this.isStartInclusive ^ other.isStartInclusive) {
            return this.isStartInclusive ? -1 : 1;
         } else {
            return 0;
         }
      }
   }

   private int compareEnds(Interval<T> other) {
      if (this.end == null && other.end == null) {
         return 0;
      } else if (this.end == null) {
         return 1;
      } else if (other.end == null) {
         return -1;
      } else {
         int compare = this.end.compareTo(other.end);
         if (compare != 0) {
            return compare;
         } else if (this.isEndInclusive ^ other.isEndInclusive) {
            return this.isEndInclusive ? 1 : -1;
         } else {
            return 0;
         }
      }
   }

   protected int compareSpecialization(Interval<T> other) {
      return 0;
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = this.start == null ? 0 : this.start.hashCode();
      result = prime * result + (this.end == null ? 0 : this.end.hashCode());
      result = prime * result + (this.isStartInclusive ? 1 : 0);
      return prime * result + (this.isEndInclusive ? 1 : 0);
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof Interval))
         return false;
      Interval<T> other = (Interval<T>) obj;
      if (this.start == null ^ other.start == null)
         return false;
      if (this.end == null ^ other.end == null)
         return false;
      if (this.isEndInclusive ^ other.isEndInclusive)
         return false;
      if (this.isStartInclusive ^ other.isStartInclusive)
         return false;
      if (this.start != null && !this.start.equals(other.start))
         return false;
      return this.end == null || this.end.equals(other.end);
   }

   public static enum Bounded {
      OPEN,
      CLOSED,
      CLOSED_RIGHT,
      CLOSED_LEFT;
   }

   public static enum Unbounded {
      OPEN_LEFT,
      CLOSED_LEFT,
      OPEN_RIGHT,
      CLOSED_RIGHT;
   }
}
