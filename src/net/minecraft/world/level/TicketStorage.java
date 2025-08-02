package net.minecraft.world.level;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class TicketStorage extends SavedData {
	private static final int INITIAL_TICKET_LIST_CAPACITY = 4;
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Codec<Pair<ChunkPos, Ticket>> TICKET_ENTRY = Codec.mapPair(ChunkPos.CODEC.fieldOf("chunk_pos"), Ticket.CODEC).codec();
	public static final Codec<TicketStorage> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(TICKET_ENTRY.listOf().optionalFieldOf("tickets", List.of()).forGetter(TicketStorage::packTickets))
			.apply(instance, TicketStorage::fromPacked)
	);
	public static final SavedDataType<TicketStorage> TYPE = new SavedDataType<>("chunks", TicketStorage::new, CODEC, DataFixTypes.SAVED_DATA_FORCED_CHUNKS);
	private final Long2ObjectOpenHashMap<List<Ticket>> tickets;
	private final Long2ObjectOpenHashMap<List<Ticket>> deactivatedTickets;
	private LongSet chunksWithForcedTickets = new LongOpenHashSet();
	@Nullable
	private TicketStorage.ChunkUpdated loadingChunkUpdatedListener;
	@Nullable
	private TicketStorage.ChunkUpdated simulationChunkUpdatedListener;

	private TicketStorage(Long2ObjectOpenHashMap<List<Ticket>> long2ObjectOpenHashMap, Long2ObjectOpenHashMap<List<Ticket>> long2ObjectOpenHashMap2) {
		this.tickets = long2ObjectOpenHashMap;
		this.deactivatedTickets = long2ObjectOpenHashMap2;
		this.updateForcedChunks();
	}

	public TicketStorage() {
		this(new Long2ObjectOpenHashMap<>(4), new Long2ObjectOpenHashMap<>());
	}

	private static TicketStorage fromPacked(List<Pair<ChunkPos, Ticket>> list) {
		Long2ObjectOpenHashMap<List<Ticket>> long2ObjectOpenHashMap = new Long2ObjectOpenHashMap<>();

		for (Pair<ChunkPos, Ticket> pair : list) {
			ChunkPos chunkPos = pair.getFirst();
			List<Ticket> list2 = long2ObjectOpenHashMap.computeIfAbsent(chunkPos.toLong(), (Long2ObjectFunction<? extends List<Ticket>>)(l -> new ObjectArrayList<>(4)));
			list2.add(pair.getSecond());
		}

		return new TicketStorage(new Long2ObjectOpenHashMap<>(4), long2ObjectOpenHashMap);
	}

	private List<Pair<ChunkPos, Ticket>> packTickets() {
		List<Pair<ChunkPos, Ticket>> list = new ArrayList();
		this.forEachTicket((chunkPos, ticket) -> {
			if (ticket.getType().persist()) {
				list.add(new Pair<>(chunkPos, ticket));
			}
		});
		return list;
	}

	private void forEachTicket(BiConsumer<ChunkPos, Ticket> biConsumer) {
		forEachTicket(biConsumer, this.tickets);
		forEachTicket(biConsumer, this.deactivatedTickets);
	}

	private static void forEachTicket(BiConsumer<ChunkPos, Ticket> biConsumer, Long2ObjectOpenHashMap<List<Ticket>> long2ObjectOpenHashMap) {
		for (Entry<List<Ticket>> entry : Long2ObjectMaps.fastIterable(long2ObjectOpenHashMap)) {
			ChunkPos chunkPos = new ChunkPos(entry.getLongKey());

			for (Ticket ticket : entry.getValue()) {
				biConsumer.accept(chunkPos, ticket);
			}
		}
	}

	public void activateAllDeactivatedTickets() {
		for (Entry<List<Ticket>> entry : Long2ObjectMaps.fastIterable(this.deactivatedTickets)) {
			for (Ticket ticket : entry.getValue()) {
				this.addTicket(entry.getLongKey(), ticket);
			}
		}

		this.deactivatedTickets.clear();
	}

	public void setLoadingChunkUpdatedListener(@Nullable TicketStorage.ChunkUpdated chunkUpdated) {
		this.loadingChunkUpdatedListener = chunkUpdated;
	}

	public void setSimulationChunkUpdatedListener(@Nullable TicketStorage.ChunkUpdated chunkUpdated) {
		this.simulationChunkUpdatedListener = chunkUpdated;
	}

	public boolean hasTickets() {
		return !this.tickets.isEmpty();
	}

	public List<Ticket> getTickets(long l) {
		return this.tickets.getOrDefault(l, List.of());
	}

	private List<Ticket> getOrCreateTickets(long l) {
		return this.tickets.computeIfAbsent(l, (Long2ObjectFunction<? extends List<Ticket>>)(lx -> new ObjectArrayList<>(4)));
	}

	public void addTicketWithRadius(TicketType ticketType, ChunkPos chunkPos, int i) {
		Ticket ticket = new Ticket(ticketType, ChunkLevel.byStatus(FullChunkStatus.FULL) - i);
		this.addTicket(chunkPos.toLong(), ticket);
	}

	public void addTicket(Ticket ticket, ChunkPos chunkPos) {
		this.addTicket(chunkPos.toLong(), ticket);
	}

	public boolean addTicket(long l, Ticket ticket) {
		List<Ticket> list = this.getOrCreateTickets(l);

		for (Ticket ticket2 : list) {
			if (isTicketSameTypeAndLevel(ticket, ticket2)) {
				ticket2.resetTicksLeft();
				this.setDirty();
				return false;
			}
		}

		int i = getTicketLevelAt(list, true);
		int j = getTicketLevelAt(list, false);
		list.add(ticket);
		if (ticket.getType().doesSimulate() && ticket.getTicketLevel() < i && this.simulationChunkUpdatedListener != null) {
			this.simulationChunkUpdatedListener.update(l, ticket.getTicketLevel(), true);
		}

		if (ticket.getType().doesLoad() && ticket.getTicketLevel() < j && this.loadingChunkUpdatedListener != null) {
			this.loadingChunkUpdatedListener.update(l, ticket.getTicketLevel(), true);
		}

		if (ticket.getType().equals(TicketType.FORCED)) {
			this.chunksWithForcedTickets.add(l);
		}

		this.setDirty();
		return true;
	}

	private static boolean isTicketSameTypeAndLevel(Ticket ticket, Ticket ticket2) {
		return ticket2.getType() == ticket.getType() && ticket2.getTicketLevel() == ticket.getTicketLevel();
	}

	public int getTicketLevelAt(long l, boolean bl) {
		return getTicketLevelAt(this.getTickets(l), bl);
	}

	private static int getTicketLevelAt(List<Ticket> list, boolean bl) {
		Ticket ticket = getLowestTicket(list, bl);
		return ticket == null ? ChunkLevel.MAX_LEVEL + 1 : ticket.getTicketLevel();
	}

	@Nullable
	private static Ticket getLowestTicket(@Nullable List<Ticket> list, boolean bl) {
		if (list == null) {
			return null;
		} else {
			Ticket ticket = null;

			for (Ticket ticket2 : list) {
				if (ticket == null || ticket2.getTicketLevel() < ticket.getTicketLevel()) {
					if (bl && ticket2.getType().doesSimulate()) {
						ticket = ticket2;
					} else if (!bl && ticket2.getType().doesLoad()) {
						ticket = ticket2;
					}
				}
			}

			return ticket;
		}
	}

	public void removeTicketWithRadius(TicketType ticketType, ChunkPos chunkPos, int i) {
		Ticket ticket = new Ticket(ticketType, ChunkLevel.byStatus(FullChunkStatus.FULL) - i);
		this.removeTicket(chunkPos.toLong(), ticket);
	}

	public void removeTicket(Ticket ticket, ChunkPos chunkPos) {
		this.removeTicket(chunkPos.toLong(), ticket);
	}

	public boolean removeTicket(long l, Ticket ticket) {
		List<Ticket> list = this.tickets.get(l);
		if (list == null) {
			return false;
		} else {
			boolean bl = false;
			Iterator<Ticket> iterator = list.iterator();

			while (iterator.hasNext()) {
				Ticket ticket2 = (Ticket)iterator.next();
				if (isTicketSameTypeAndLevel(ticket, ticket2)) {
					iterator.remove();
					bl = true;
					break;
				}
			}

			if (!bl) {
				return false;
			} else {
				if (list.isEmpty()) {
					this.tickets.remove(l);
				}

				if (ticket.getType().doesSimulate() && this.simulationChunkUpdatedListener != null) {
					this.simulationChunkUpdatedListener.update(l, getTicketLevelAt(list, true), false);
				}

				if (ticket.getType().doesLoad() && this.loadingChunkUpdatedListener != null) {
					this.loadingChunkUpdatedListener.update(l, getTicketLevelAt(list, false), false);
				}

				if (ticket.getType().equals(TicketType.FORCED)) {
					this.updateForcedChunks();
				}

				this.setDirty();
				return true;
			}
		}
	}

	private void updateForcedChunks() {
		this.chunksWithForcedTickets = this.getAllChunksWithTicketThat(ticket -> ticket.getType().equals(TicketType.FORCED));
	}

	public String getTicketDebugString(long l, boolean bl) {
		List<Ticket> list = this.getTickets(l);
		Ticket ticket = getLowestTicket(list, bl);
		return ticket == null ? "no_ticket" : ticket.toString();
	}

	public void purgeStaleTickets(ChunkMap chunkMap) {
		this.removeTicketIf((long_, ticket) -> {
			ChunkHolder chunkHolder = chunkMap.getUpdatingChunkIfPresent(long_);
			boolean bl = chunkHolder != null && !chunkHolder.isReadyForSaving() && ticket.getType().doesSimulate();
			if (bl) {
				return false;
			} else {
				ticket.decreaseTicksLeft();
				return ticket.isTimedOut();
			}
		}, null);
		this.setDirty();
	}

	public void deactivateTicketsOnClosing() {
		this.removeTicketIf((long_, ticket) -> ticket.getType() != TicketType.UNKNOWN, this.deactivatedTickets);
	}

	public void removeTicketIf(BiPredicate<Long, Ticket> biPredicate, @Nullable Long2ObjectOpenHashMap<List<Ticket>> long2ObjectOpenHashMap) {
		ObjectIterator<Entry<List<Ticket>>> objectIterator = this.tickets.long2ObjectEntrySet().fastIterator();
		boolean bl = false;

		while (objectIterator.hasNext()) {
			Entry<List<Ticket>> entry = (Entry<List<Ticket>>)objectIterator.next();
			Iterator<Ticket> iterator = ((List)entry.getValue()).iterator();
			long l = entry.getLongKey();
			boolean bl2 = false;
			boolean bl3 = false;

			while (iterator.hasNext()) {
				Ticket ticket = (Ticket)iterator.next();
				if (biPredicate.test(l, ticket)) {
					if (long2ObjectOpenHashMap != null) {
						List<Ticket> list = long2ObjectOpenHashMap.computeIfAbsent(
							l, (Long2ObjectFunction<? extends List<Ticket>>)(lx -> new ObjectArrayList<>(((List)entry.getValue()).size()))
						);
						list.add(ticket);
					}

					iterator.remove();
					if (ticket.getType().doesLoad()) {
						bl3 = true;
					}

					if (ticket.getType().doesSimulate()) {
						bl2 = true;
					}

					if (ticket.getType().equals(TicketType.FORCED)) {
						bl = true;
					}
				}
			}

			if (bl3 || bl2) {
				if (bl3 && this.loadingChunkUpdatedListener != null) {
					this.loadingChunkUpdatedListener.update(l, getTicketLevelAt((List<Ticket>)entry.getValue(), false), false);
				}

				if (bl2 && this.simulationChunkUpdatedListener != null) {
					this.simulationChunkUpdatedListener.update(l, getTicketLevelAt((List<Ticket>)entry.getValue(), true), false);
				}

				this.setDirty();
				if (((List)entry.getValue()).isEmpty()) {
					objectIterator.remove();
				}
			}
		}

		if (bl) {
			this.updateForcedChunks();
		}
	}

	public void replaceTicketLevelOfType(int i, TicketType ticketType) {
		List<Pair<Ticket, Long>> list = new ArrayList();

		for (Entry<List<Ticket>> entry : this.tickets.long2ObjectEntrySet()) {
			for (Ticket ticket : entry.getValue()) {
				if (ticket.getType() == ticketType) {
					list.add(Pair.of(ticket, entry.getLongKey()));
				}
			}
		}

		for (Pair<Ticket, Long> pair : list) {
			Long long_ = pair.getSecond();
			Ticket ticketx = pair.getFirst();
			this.removeTicket(long_, ticketx);
			TicketType ticketType2 = ticketx.getType();
			this.addTicket(long_, new Ticket(ticketType2, i));
		}
	}

	public boolean updateChunkForced(ChunkPos chunkPos, boolean bl) {
		Ticket ticket = new Ticket(TicketType.FORCED, ChunkMap.FORCED_TICKET_LEVEL);
		return bl ? this.addTicket(chunkPos.toLong(), ticket) : this.removeTicket(chunkPos.toLong(), ticket);
	}

	public LongSet getForceLoadedChunks() {
		return this.chunksWithForcedTickets;
	}

	private LongSet getAllChunksWithTicketThat(Predicate<Ticket> predicate) {
		LongOpenHashSet longOpenHashSet = new LongOpenHashSet();

		for (Entry<List<Ticket>> entry : Long2ObjectMaps.fastIterable(this.tickets)) {
			for (Ticket ticket : entry.getValue()) {
				if (predicate.test(ticket)) {
					longOpenHashSet.add(entry.getLongKey());
					break;
				}
			}
		}

		return longOpenHashSet;
	}

	@FunctionalInterface
	public interface ChunkUpdated {
		void update(long l, int i, boolean bl);
	}
}
