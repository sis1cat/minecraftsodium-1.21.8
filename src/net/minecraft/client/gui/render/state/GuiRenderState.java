package net.minecraft.client.gui.render.state;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;

import org.apache.commons.lang3.mutable.MutableInt;


public class GuiRenderState {
	private static final int DEBUG_RECTANGLE_COLOR = 2000962815;
	private final List<GuiRenderState.Node> strata = new ArrayList<>();
	private int firstStratumAfterBlur = Integer.MAX_VALUE;
	private GuiRenderState.Node current;
	private final Set<Object> itemModelIdentities = new HashSet<>();
	@Nullable
	private ScreenRectangle lastElementBounds;

	public GuiRenderState() {
		this.nextStratum();
	}

	public void nextStratum() {
		this.current = new GuiRenderState.Node(null);
		this.strata.add(this.current);
	}

	public void blurBeforeThisStratum() {
		if (this.firstStratumAfterBlur != Integer.MAX_VALUE) {
			throw new IllegalStateException("Can only blur once per frame");
		} else {
			this.firstStratumAfterBlur = this.strata.size() - 1;
		}
	}

	public void up() {
		if (this.current.up == null) {
			this.current.up = new GuiRenderState.Node(this.current);
		}

		this.current = this.current.up;
	}

	public void down() {
		if (this.current.down == null) {
			this.current.down = new GuiRenderState.Node(this.current);
		}

		this.current = this.current.down;
	}

	public void submitItem(GuiItemRenderState p_409706_) {
		if (this.findAppropriateNode(p_409706_)) {
			this.itemModelIdentities.add(p_409706_.itemStackRenderState().getModelIdentity());
			this.current.submitItem(p_409706_);
			this.sumbitDebugRectangleIfEnabled(p_409706_.bounds());
		}
	}

	public void submitText(GuiTextRenderState p_408805_) {
		if (this.findAppropriateNode(p_408805_)) {
			this.current.submitText(p_408805_);
			this.sumbitDebugRectangleIfEnabled(p_408805_.bounds());
		}
	}

	public void submitPicturesInPictureState(PictureInPictureRenderState p_406536_) {
		if (this.findAppropriateNode(p_406536_)) {
			this.current.submitPicturesInPictureState(p_406536_);
			this.sumbitDebugRectangleIfEnabled(p_406536_.bounds());
		}
	}

	public void submitGuiElement(GuiElementRenderState p_407568_) {
		if (this.findAppropriateNode(p_407568_)) {
			this.current.submitGuiElement(p_407568_);
			this.sumbitDebugRectangleIfEnabled(p_407568_.bounds());
		}
	}

	private void sumbitDebugRectangleIfEnabled(@Nullable ScreenRectangle p_407508_) {
	}

	private boolean findAppropriateNode(ScreenArea p_409677_) {
		ScreenRectangle screenrectangle = p_409677_.bounds();
		if (screenrectangle == null) {
			return false;
		} else {
			if (this.lastElementBounds != null && this.lastElementBounds.encompasses(screenrectangle)) {
				this.up();
			} else {
				this.navigateToAboveHighestElementWithIntersectingBounds(screenrectangle);
			}

			this.lastElementBounds = screenrectangle;
			return true;
		}
	}

	private void navigateToAboveHighestElementWithIntersectingBounds(ScreenRectangle p_408085_) {
		GuiRenderState.Node guirenderstate$node = this.strata.getLast();

		while (guirenderstate$node.up != null) {
			guirenderstate$node = guirenderstate$node.up;
		}

		boolean flag = false;

		while (!flag) {
			flag = this.hasIntersection(p_408085_, guirenderstate$node.elementStates)
					|| this.hasIntersection(p_408085_, guirenderstate$node.itemStates)
					|| this.hasIntersection(p_408085_, guirenderstate$node.textStates)
					|| this.hasIntersection(p_408085_, guirenderstate$node.picturesInPictureStates);
			if (guirenderstate$node.parent == null) {
				break;
			}

			if (!flag) {
				guirenderstate$node = guirenderstate$node.parent;
			}
		}

		this.current = guirenderstate$node;
		if (flag) {
			this.up();
		}
	}

	private boolean hasIntersection(ScreenRectangle p_408222_, @Nullable List<? extends ScreenArea> p_409418_) {
		if (p_409418_ != null) {
			for (ScreenArea screenarea : p_409418_) {
				ScreenRectangle screenrectangle = screenarea.bounds();
				if (screenrectangle != null && screenrectangle.intersects(p_408222_)) {
					return true;
				}
			}
		}

		return false;
	}

	public void submitBlitToCurrentLayer(BlitRenderState p_409139_) {
		this.current.submitGuiElement(p_409139_);
	}

	public void submitGlyphToCurrentLayer(GuiElementRenderState p_405836_) {
		this.current.submitGlyph(p_405836_);
	}

	public Set<Object> getItemModelIdentities() {
		return this.itemModelIdentities;
	}

	public void forEachElement(GuiRenderState.LayeredElementConsumer p_406526_, GuiRenderState.TraverseRange p_406592_) {
		MutableInt mutableint = new MutableInt(0);
		this.traverse(p_405927_ -> {
			if (p_405927_.elementStates != null || p_405927_.glyphStates != null) {
				int i = mutableint.incrementAndGet();
				if (p_405927_.elementStates != null) {
					for (GuiElementRenderState guielementrenderstate : p_405927_.elementStates) {
						p_406526_.accept(guielementrenderstate, i);
					}
				}

				if (p_405927_.glyphStates != null) {
					for (GuiElementRenderState guielementrenderstate1 : p_405927_.glyphStates) {
						p_406526_.accept(guielementrenderstate1, i);
					}
				}
			}
		}, p_406592_);
	}

	public void forEachItem(Consumer<GuiItemRenderState> p_407719_) {
		GuiRenderState.Node guirenderstate$node = this.current;
		this.traverse(p_409949_ -> {
			if (p_409949_.itemStates != null) {
				this.current = p_409949_;

				for (GuiItemRenderState guiitemrenderstate : p_409949_.itemStates) {
					p_407719_.accept(guiitemrenderstate);
				}
			}
		}, GuiRenderState.TraverseRange.ALL);
		this.current = guirenderstate$node;
	}

	public void forEachText(Consumer<GuiTextRenderState> p_410457_) {
		GuiRenderState.Node guirenderstate$node = this.current;
		this.traverse(p_409407_ -> {
			if (p_409407_.textStates != null) {
				for (GuiTextRenderState guitextrenderstate : p_409407_.textStates) {
					this.current = p_409407_;

					p_410457_.accept(guitextrenderstate);
				}
			}
		}, GuiRenderState.TraverseRange.ALL);
		this.current = guirenderstate$node;
	}

	public void forEachPictureInPicture(Consumer<PictureInPictureRenderState> p_407751_) {
		GuiRenderState.Node guirenderstate$node = this.current;
		this.traverse(p_407281_ -> {
			if (p_407281_.picturesInPictureStates != null) {
				this.current = p_407281_;

				for (PictureInPictureRenderState pictureinpicturerenderstate : p_407281_.picturesInPictureStates) {
					p_407751_.accept(pictureinpicturerenderstate);
				}
			}
		}, GuiRenderState.TraverseRange.ALL);
		this.current = guirenderstate$node;
	}

	public void sortElements(Comparator<GuiElementRenderState> p_408774_) {
		this.traverse(p_406585_ -> {
			if (p_406585_.elementStates != null) {
				p_406585_.elementStates.sort(p_408774_);
			}
		}, GuiRenderState.TraverseRange.ALL);
	}

	private void traverse(Consumer<GuiRenderState.Node> p_406857_, GuiRenderState.TraverseRange p_405917_) {
		int i = 0;
		int j = this.strata.size();
		if (p_405917_ == GuiRenderState.TraverseRange.BEFORE_BLUR) {
			j = Math.min(this.firstStratumAfterBlur, this.strata.size());
		} else if (p_405917_ == GuiRenderState.TraverseRange.AFTER_BLUR) {
			i = this.firstStratumAfterBlur;
		}

		for (int k = i; k < j; k++) {
			GuiRenderState.Node guirenderstate$node = this.strata.get(k);
			this.traverse(guirenderstate$node, p_406857_);
		}
	}

	private void traverse(GuiRenderState.Node p_407791_, Consumer<GuiRenderState.Node> p_406697_) {
		if (p_407791_.down != null) {
			this.traverse(p_407791_.down, p_406697_);
		}

		p_406697_.accept(p_407791_);
		if (p_407791_.up != null) {
			this.traverse(p_407791_.up, p_406697_);
		}
	}

	public void reset() {
		this.itemModelIdentities.clear();
		this.strata.clear();
		this.firstStratumAfterBlur = Integer.MAX_VALUE;
		this.nextStratum();
	}


	public interface LayeredElementConsumer {
		void accept(GuiElementRenderState p_407900_, int p_406637_);
	}


	static class Node {
		@Nullable
		public final GuiRenderState.Node parent;
		@Nullable
		public GuiRenderState.Node up;
		@Nullable
		public GuiRenderState.Node down;
		@Nullable
		public List<GuiElementRenderState> elementStates;
		@Nullable
		public List<GuiElementRenderState> glyphStates;
		@Nullable
		public List<GuiItemRenderState> itemStates;
		@Nullable
		public List<GuiTextRenderState> textStates;
		@Nullable
		public List<PictureInPictureRenderState> picturesInPictureStates;

		Node(@Nullable GuiRenderState.Node p_407057_) {
			this.parent = p_407057_;
		}

		public void submitItem(GuiItemRenderState p_407245_) {
			if (this.itemStates == null) {
				this.itemStates = new ArrayList<>();
			}

			this.itemStates.add(p_407245_);
		}

		public void submitText(GuiTextRenderState p_408973_) {
			if (this.textStates == null) {
				this.textStates = new ArrayList<>();
			}

			this.textStates.add(p_408973_);
		}

		public void submitPicturesInPictureState(PictureInPictureRenderState p_410293_) {
			if (this.picturesInPictureStates == null) {
				this.picturesInPictureStates = new ArrayList<>();
			}

			this.picturesInPictureStates.add(p_410293_);
		}

		public void submitGuiElement(GuiElementRenderState p_408558_) {
			if (this.elementStates == null) {
				this.elementStates = new ArrayList<>();
			}

			this.elementStates.add(p_408558_);
		}

		public void submitGlyph(GuiElementRenderState p_410595_) {
			if (this.glyphStates == null) {
				this.glyphStates = new ArrayList<>();
			}

			this.glyphStates.add(p_410595_);
		}
	}


	public static enum TraverseRange {
		ALL,
		BEFORE_BLUR,
		AFTER_BLUR;
	}
}