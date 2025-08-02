package net.minecraft.commands.execution.tasks;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.context.ContextChain.Stage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.CustomModifierExecutor;
import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.network.chat.Component;

public class BuildContexts<T extends ExecutionCommandSource<T>> {
	@VisibleForTesting
	public static final DynamicCommandExceptionType ERROR_FORK_LIMIT_REACHED = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("command.forkLimit", object)
	);
	private final String commandInput;
	private final ContextChain<T> command;

	public BuildContexts(String string, ContextChain<T> contextChain) {
		this.commandInput = string;
		this.command = contextChain;
	}

	protected void execute(T pOriginalSource, List<T> pSources, ExecutionContext<T> pContext, Frame pFrame, ChainModifiers pChainModifiers) {
		ContextChain<T> contextchain = this.command;
		ChainModifiers chainmodifiers = pChainModifiers;
		List<T> list = pSources;
		if (contextchain.getStage() != Stage.EXECUTE) {
			pContext.profiler().push(() -> "prepare " + this.commandInput);

			try {
				for (int i = pContext.forkLimit(); contextchain.getStage() != Stage.EXECUTE; contextchain = contextchain.nextStage()) {
					CommandContext<T> commandcontext = contextchain.getTopContext();
					if (commandcontext.isForked()) {
						chainmodifiers = chainmodifiers.setForked();
					}

					RedirectModifier<T> redirectmodifier = commandcontext.getRedirectModifier();
					if (redirectmodifier instanceof CustomModifierExecutor custommodifierexecutor) {
						custommodifierexecutor.apply(pOriginalSource, list, contextchain, chainmodifiers, ExecutionControl.create(pContext, pFrame));
						return;
					}

					if (redirectmodifier != null) {
						pContext.incrementCost();
						boolean flag = chainmodifiers.isForked();
						List<T> list1 = new ObjectArrayList<>();

						for (T t : list) {
							try {
								Collection<T> collection = ContextChain.runModifier(commandcontext, t, (p_311026_, p_312291_, p_310245_) -> {
								}, flag);
								if (list1.size() + collection.size() >= i) {
									pOriginalSource.handleError(ERROR_FORK_LIMIT_REACHED.create(i), flag, pContext.tracer());
									return;
								}

								list1.addAll(collection);
							} catch (CommandSyntaxException commandsyntaxexception) {
								t.handleError(commandsyntaxexception, flag, pContext.tracer());
								if (!flag) {
									return;
								}
							}
						}

						list = list1;
					}
				}
			} finally {
				pContext.profiler().pop();
			}
		}

		if (list.isEmpty()) {
			if (chainmodifiers.isReturn()) {
				pContext.queueNext(new CommandQueueEntry<T>(pFrame, FallthroughTask.instance()));
			}
		} else {
			CommandContext<T> commandcontext1 = contextchain.getTopContext();
			if (commandcontext1.getCommand() instanceof CustomCommandExecutor customcommandexecutor) {
				ExecutionControl<T> executioncontrol = ExecutionControl.create(pContext, pFrame);

				for (T t2 : list) {
					customcommandexecutor.run(t2, contextchain, chainmodifiers, executioncontrol);
				}
			} else {
				if (chainmodifiers.isReturn()) {
					T t1 = list.get(0);
					t1 = t1.withCallback(CommandResultCallback.chain(t1.callback(), pFrame.returnValueConsumer()));
					list = List.of(t1);
				}

				ExecuteCommand<T> executecommand = new ExecuteCommand<>(this.commandInput, chainmodifiers, commandcontext1);
				ContinuationTask.schedule(
						pContext, pFrame, list, (p_311832_, p_309437_) -> new CommandQueueEntry<>(p_311832_, executecommand.bind(p_309437_))
				);
			}
		}
	}

	protected void traceCommandStart(ExecutionContext<T> executionContext, Frame frame) {
		TraceCallbacks traceCallbacks = executionContext.tracer();
		if (traceCallbacks != null) {
			traceCallbacks.onCommand(frame.depth(), this.commandInput);
		}
	}

	public String toString() {
		return this.commandInput;
	}

	public static class Continuation<T extends ExecutionCommandSource<T>> extends BuildContexts<T> implements EntryAction<T> {
		private final ChainModifiers modifiers;
		private final T originalSource;
		private final List<T> sources;

		public Continuation(String string, ContextChain<T> contextChain, ChainModifiers chainModifiers, T executionCommandSource, List<T> list) {
			super(string, contextChain);
			this.originalSource = executionCommandSource;
			this.sources = list;
			this.modifiers = chainModifiers;
		}

		@Override
		public void execute(ExecutionContext<T> executionContext, Frame frame) {
			this.execute(this.originalSource, this.sources, executionContext, frame, this.modifiers);
		}
	}

	public static class TopLevel<T extends ExecutionCommandSource<T>> extends BuildContexts<T> implements EntryAction<T> {
		private final T source;

		public TopLevel(String string, ContextChain<T> contextChain, T executionCommandSource) {
			super(string, contextChain);
			this.source = executionCommandSource;
		}

		@Override
		public void execute(ExecutionContext<T> executionContext, Frame frame) {
			this.traceCommandStart(executionContext, frame);
			this.execute(this.source, List.of(this.source), executionContext, frame, ChainModifiers.DEFAULT);
		}
	}

	public static class Unbound<T extends ExecutionCommandSource<T>> extends BuildContexts<T> implements UnboundEntryAction<T> {
		public Unbound(String string, ContextChain<T> contextChain) {
			super(string, contextChain);
		}

		public void execute(T executionCommandSource, ExecutionContext<T> executionContext, Frame frame) {
			this.traceCommandStart(executionContext, frame);
			this.execute(executionCommandSource, List.of(executionCommandSource), executionContext, frame, ChainModifiers.DEFAULT);
		}
	}
}
