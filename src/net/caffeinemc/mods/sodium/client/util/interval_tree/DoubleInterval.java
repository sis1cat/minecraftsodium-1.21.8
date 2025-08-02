package net.caffeinemc.mods.sodium.client.util.interval_tree;

public class DoubleInterval extends Interval<Double> {
   private static final int OFFSET = 1000;

   public DoubleInterval() {
   }

   public DoubleInterval(Double start, Double end, Interval.Bounded type) {
      super(0.0 + start, 0.0 + end, type);
   }

   public DoubleInterval(Double value, Interval.Unbounded type) {
      super(0.0 + value, type);
   }

   @Override
   protected Interval<Double> create() {
      return new DoubleInterval();
   }

   @Override
   public boolean isEmpty() {
      if (this.getStart() != null && this.getStart().isNaN()) {
         return true;
      } else if (this.getEnd() != null && this.getEnd().isNaN()) {
         return true;
      } else {
         if (this.getStart() != null && this.getEnd() != null) {
            if (this.getStart() == Double.POSITIVE_INFINITY && this.getEnd() == Double.POSITIVE_INFINITY) {
               return true;
            }

            if (this.getStart() == Double.NEGATIVE_INFINITY && this.getEnd() == Double.NEGATIVE_INFINITY) {
               return true;
            }
         }

         return super.isEmpty();
      }
   }

   public Double getMidpoint() {
      if (this.isEmpty()) {
         return null;
      } else if (this.getStart() == null && this.getEnd() == null) {
         return 0.0;
      } else if (this.getStart() == null) {
         return this.getEnd() - 1000.0;
      } else if (this.getEnd() == null) {
         return this.getStart() + 1000.0;
      } else if (this.getStart() == Double.NEGATIVE_INFINITY && this.getEnd() == Double.POSITIVE_INFINITY) {
         return 0.0;
      } else if (this.getStart() == Double.NEGATIVE_INFINITY) {
         return this.getEnd() - 1000.0;
      } else {
         return this.getEnd() == Double.POSITIVE_INFINITY ? this.getStart() + 1000.0 : this.getStart() + (this.getEnd() - this.getStart()) / 2.0;
      }
   }
}
