package net.minecraft.client.multiplayer;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.searchtree.FullTextSearchTree;
import net.minecraft.client.searchtree.IdSearchTree;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class SessionSearchTrees {
	private static final SessionSearchTrees.Key RECIPE_COLLECTIONS = new SessionSearchTrees.Key();
	private static final SessionSearchTrees.Key CREATIVE_NAMES = new SessionSearchTrees.Key();
	private static final SessionSearchTrees.Key CREATIVE_TAGS = new SessionSearchTrees.Key();
	private CompletableFuture<SearchTree<ItemStack>> creativeByNameSearch = CompletableFuture.completedFuture(SearchTree.empty());
	private CompletableFuture<SearchTree<ItemStack>> creativeByTagSearch = CompletableFuture.completedFuture(SearchTree.empty());
	private CompletableFuture<SearchTree<RecipeCollection>> recipeSearch = CompletableFuture.completedFuture(SearchTree.empty());
	private final Map<SessionSearchTrees.Key, Runnable> reloaders = new IdentityHashMap();

	private void register(SessionSearchTrees.Key key, Runnable runnable) {
		runnable.run();
		this.reloaders.put(key, runnable);
	}

	public void rebuildAfterLanguageChange() {
		for (Runnable runnable : this.reloaders.values()) {
			runnable.run();
		}
	}

	private static Stream<String> getTooltipLines(Stream<ItemStack> stream, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag) {
		return stream.flatMap(itemStack -> itemStack.getTooltipLines(tooltipContext, null, tooltipFlag).stream())
			.map(component -> ChatFormatting.stripFormatting(component.getString()).trim())
			.filter(string -> !string.isEmpty());
	}

	public void updateRecipes(ClientRecipeBook pRecipeBook, Level pLevel) {
		this.register(
				RECIPE_COLLECTIONS,
				() -> {
					List<RecipeCollection> list = pRecipeBook.getCollections();
					RegistryAccess registryaccess = pLevel.registryAccess();
					Registry<Item> registry = registryaccess.lookupOrThrow(Registries.ITEM);
					Item.TooltipContext item$tooltipcontext = Item.TooltipContext.of(registryaccess);
					ContextMap contextmap = SlotDisplayContext.fromLevel(pLevel);
					TooltipFlag tooltipflag = TooltipFlag.Default.NORMAL;
					CompletableFuture<?> completablefuture = this.recipeSearch;
					this.recipeSearch = CompletableFuture.supplyAsync(
							() -> new FullTextSearchTree<>(
									p_357799_ -> getTooltipLines(
											p_357799_.getRecipes().stream().flatMap(p_357810_ -> p_357810_.resultItems(contextmap).stream()),
											item$tooltipcontext,
											tooltipflag
									),
									p_357813_ -> p_357813_.getRecipes()
											.stream()
											.flatMap(p_357803_ -> p_357803_.resultItems(contextmap).stream())
											.map(p_357808_ -> registry.getKey(p_357808_.getItem())),
									list
							),
							Util.backgroundExecutor()
					);
					completablefuture.cancel(true);
				}
		);
	}

	public SearchTree<RecipeCollection> recipes() {
		return (SearchTree<RecipeCollection>)this.recipeSearch.join();
	}

	public void updateCreativeTags(List<ItemStack> pItems) {
		this.register(
				CREATIVE_TAGS,
				() -> {
					CompletableFuture<?> completablefuture = this.creativeByTagSearch;
					this.creativeByTagSearch = CompletableFuture.supplyAsync(
							() -> new IdSearchTree<>(p_342206_ -> p_342206_.getTags().map(TagKey::location), pItems), Util.backgroundExecutor()
					);
					completablefuture.cancel(true);
				}
		);
	}

	public SearchTree<ItemStack> creativeTagSearch() {
		return (SearchTree<ItemStack>)this.creativeByTagSearch.join();
	}

	public void updateCreativeTooltips(HolderLookup.Provider p_343364_, List<ItemStack> p_342500_) {
		this.register(
				CREATIVE_NAMES,
				() -> {
					Item.TooltipContext item$tooltipcontext = Item.TooltipContext.of(p_343364_);
					TooltipFlag tooltipflag = TooltipFlag.Default.NORMAL.asCreative();
					CompletableFuture<?> completablefuture = this.creativeByNameSearch;
					this.creativeByNameSearch = CompletableFuture.supplyAsync(
							() -> new FullTextSearchTree<>(
									p_345254_ -> getTooltipLines(Stream.of(p_345254_), item$tooltipcontext, tooltipflag),
									p_344415_ -> p_344415_.getItemHolder().unwrapKey().map(ResourceKey::location).stream(),
									p_342500_
							),
							Util.backgroundExecutor()
					);
					completablefuture.cancel(true);
				}
		);
	}


	public SearchTree<ItemStack> creativeNameSearch() {
		return (SearchTree<ItemStack>)this.creativeByNameSearch.join();
	}

	@Environment(EnvType.CLIENT)
	static class Key {
	}
}
