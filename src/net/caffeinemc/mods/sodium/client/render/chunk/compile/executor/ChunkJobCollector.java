package net.caffeinemc.mods.sodium.client.render.chunk.compile.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.BuilderTaskOutput;

public class ChunkJobCollector {
   private final Semaphore semaphore = new Semaphore(0);
   private final Consumer<ChunkJobResult<? extends BuilderTaskOutput>> collector;
   private final List<ChunkJob> submitted = new ArrayList<>();
   private int submittedHighEffort = 0;
   private int submittedLowEffort = 0;
   private final int highEffortBudget;
   private final int lowEffortBudget;
   private final boolean unlimitedBudget;

   public ChunkJobCollector(Consumer<ChunkJobResult<? extends BuilderTaskOutput>> collector) {
      this.unlimitedBudget = true;
      this.highEffortBudget = 0;
      this.lowEffortBudget = 0;
      this.collector = collector;
   }

   public ChunkJobCollector(int highEffortBudget, int lowEffortBudget, Consumer<ChunkJobResult<? extends BuilderTaskOutput>> collector) {
      this.unlimitedBudget = false;
      this.highEffortBudget = highEffortBudget;
      this.lowEffortBudget = lowEffortBudget;
      this.collector = collector;
   }

   public void onJobFinished(ChunkJobResult<? extends BuilderTaskOutput> result) {
      this.semaphore.release(1);
      this.collector.accept(result);
   }

   public void awaitCompletion(ChunkBuilder builder) {
      if (!this.submitted.isEmpty()) {
         for (ChunkJob job : this.submitted) {
            if (!job.isStarted() && !job.isCancelled()) {
               builder.tryStealTask(job);
            }
         }

         this.semaphore.acquireUninterruptibly(this.submitted.size());
      }
   }

   public void addSubmittedJob(ChunkJob job) {
      this.submitted.add(job);
      if (!this.unlimitedBudget) {
         int effort = job.getEffort();
         if (effort <= 1) {
            this.submittedLowEffort += effort;
         } else {
            this.submittedHighEffort += effort;
         }
      }
   }

   public boolean hasBudgetFor(int effort, boolean ignoreEffortCategory) {
      if (this.unlimitedBudget) {
         return true;
      } else if (ignoreEffortCategory) {
         return this.submittedLowEffort + this.submittedHighEffort + effort <= this.highEffortBudget + this.lowEffortBudget;
      } else {
         return effort <= 1 ? this.submittedLowEffort + effort <= this.lowEffortBudget : this.submittedHighEffort + effort <= this.highEffortBudget;
      }
   }
}
