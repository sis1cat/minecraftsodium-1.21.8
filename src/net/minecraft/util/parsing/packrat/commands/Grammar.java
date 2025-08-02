package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.util.parsing.packrat.DelayedException;
import net.minecraft.util.parsing.packrat.Dictionary;
import net.minecraft.util.parsing.packrat.ErrorCollector;
import net.minecraft.util.parsing.packrat.ErrorEntry;
import net.minecraft.util.parsing.packrat.NamedRule;
import net.minecraft.util.parsing.packrat.ParseState;

public record Grammar<T>(Dictionary<StringReader> rules, NamedRule<StringReader, T> top) implements CommandArgumentParser<T> {
	public Grammar(Dictionary<StringReader> rules, NamedRule<StringReader, T> top) {
		rules.checkAllBound();
		this.rules = rules;
		this.top = top;
	}

	public Optional<T> parse(ParseState<StringReader> parseState) {
		return parseState.parseTopRule(this.top);
	}

	@Override
	public T parseForCommands(StringReader p_333110_) throws CommandSyntaxException {
		ErrorCollector.LongestOnly<StringReader> longestonly = new ErrorCollector.LongestOnly<>();
		StringReaderParserState stringreaderparserstate = new StringReaderParserState(longestonly, p_333110_);
		Optional<T> optional = this.parse(stringreaderparserstate);
		if (optional.isPresent()) {
			return optional.get();
		} else {
			List<ErrorEntry<StringReader>> list = longestonly.entries();
			List<Exception> list1 = list.stream().<Exception>mapMulti((p_390457_, p_390458_) -> {
				if (p_390457_.reason() instanceof DelayedException<?> delayedexception) {
					p_390458_.accept(delayedexception.create(p_333110_.getString(), p_390457_.cursor()));
				} else if (p_390457_.reason() instanceof Exception exception1) {
					p_390458_.accept(exception1);
				}
			}).toList();

			for (Exception exception : list1) {
				if (exception instanceof CommandSyntaxException commandsyntaxexception) {
					throw commandsyntaxexception;
				}
			}

			if (list1.size() == 1 && list1.get(0) instanceof RuntimeException runtimeexception) {
				throw runtimeexception;
			} else {
				throw new IllegalStateException("Failed to parse: " + list.stream().map(ErrorEntry::toString).collect(Collectors.joining(", ")));
			}
		}
	}


	@Override
	public CompletableFuture<Suggestions> parseForSuggestions(SuggestionsBuilder suggestionsBuilder) {
		StringReader stringReader = new StringReader(suggestionsBuilder.getInput());
		stringReader.setCursor(suggestionsBuilder.getStart());
		ErrorCollector.LongestOnly<StringReader> longestOnly = new ErrorCollector.LongestOnly<>();
		StringReaderParserState stringReaderParserState = new StringReaderParserState(longestOnly, stringReader);
		this.parse(stringReaderParserState);
		List<ErrorEntry<StringReader>> list = longestOnly.entries();
		if (list.isEmpty()) {
			return suggestionsBuilder.buildFuture();
		} else {
			SuggestionsBuilder suggestionsBuilder2 = suggestionsBuilder.createOffset(longestOnly.cursor());

			for (ErrorEntry<StringReader> errorEntry : list) {
				if (errorEntry.suggestions() instanceof ResourceSuggestion resourceSuggestion) {
					SharedSuggestionProvider.suggestResource(resourceSuggestion.possibleResources(), suggestionsBuilder2);
				} else {
					SharedSuggestionProvider.suggest(errorEntry.suggestions().possibleValues(stringReaderParserState), suggestionsBuilder2);
				}
			}

			return suggestionsBuilder2.buildFuture();
		}
	}
}
