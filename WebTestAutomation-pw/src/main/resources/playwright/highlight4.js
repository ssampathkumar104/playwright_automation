(el, color) => {

	const rects = el.getClientRects();
	const effectiveRects = rects.length
		? [...rects]
		: [el.getBoundingClientRect()];

	function isWithinViewport(rect) {
		return (
			rect.bottom > 0 &&
			rect.right > 0 &&
			rect.top < window.innerHeight &&
			rect.left < window.innerWidth
		);
	}

	function resolveDeepestElement(x, y) {
		let el = document.elementFromPoint(x, y);
		while (el && el.shadowRoot) {
			const inner = el.shadowRoot.elementFromPoint(x, y);
			if (!inner || inner === el) break;
			el = inner;
		}
		return el;
	}

	function isShadowDescendant(host, node) {
		let current = node;
		while (current) {
			if (current === host) return true;
			if (current.assignedSlot) {
				current = current.assignedSlot;
			} else if (current.parentNode instanceof ShadowRoot) {
				current = current.parentNode.host;
			} else {
				current = current.parentNode;
			}
		}
		return false;
	}

	function isTransparentOverlay(hit) {
		if (!hit) return false;
		const style = getComputedStyle(hit);
		const hasNoBackground = (style.backgroundColor === 'transparent' || style.backgroundColor === 'rgba(0, 0, 0, 0)' || style.backgroundColor === '') &&
			(style.backgroundImage === 'none' || style.backgroundImage === '');
		const hasNoBorder = (style.borderWidth === '0px' || style.borderStyle === 'none');
		const hasNoContent = hit.textContent.trim() === '' && hit.children.length === 0;
		const isFullCover = hit.offsetWidth >= hit.parentElement?.offsetWidth * 0.9 && hit.offsetHeight >= hit.parentElement?.offsetHeight * 0.9;
		return hasNoBackground && hasNoBorder && (hasNoContent || isFullCover);
	}

	function isRelatedElement(el, hit) {
		if (!hit) return false;
		// Direct match or containment
		if (hit === el || el.contains(hit) || hit.contains(el)) return true;
		// Shadow DOM containment
		if (isShadowDescendant(el, hit)) return true;
		// Walk up from hit to find el as ancestor (handles deep nesting)
		let current = hit;
		while (current) {
			if (current === el) return true;
			current = current.parentNode instanceof ShadowRoot ? current.parentNode.host : current.parentElement;
		}
		// Check if hit is a sibling/cousin within the same parent tree (up to 3 levels)
		let ancestor = el.parentElement;
		for (let i = 0; i < 3 && ancestor; i++) {
			if (ancestor.contains(hit)) return true;
			ancestor = ancestor.parentElement;
		}
		// Treat transparent overlay elements as non-blocking
		if (isTransparentOverlay(hit)) return true;
		return false;
	}

	function visiblePixelCount(el, rect) {
		const points = [];
		const xs = [0.05, 0.1, 0.25, 0.5, 0.75, 0.9, 0.95];
		const ys = [0.05, 0.1, 0.25, 0.5, 0.75, 0.9, 0.95];
		xs.forEach(x => {
			ys.forEach(y => {
				points.push([
					rect.left + rect.width * x,
					rect.top + rect.height * y
				]);
			});
		});
		let visible = 0;
		let centerHitTag = null;
		let centerHitClass = null;
		let centerHitId = null;
		points.forEach(([x, y]) => {
			const hit = resolveDeepestElement(x, y);
			if (hit && isRelatedElement(el, hit)) {
				visible++;
			}
			// Capture center point hit info for debugging
			if (centerHitTag === null && hit) {
				centerHitTag = hit.tagName;
				centerHitClass = hit.className;
				centerHitId = hit.id;
			}
		});
		return { visible, total: points.length, centerHitTag, centerHitClass, centerHitId };
	}

	function isCSSVisible(el) {
		const style = getComputedStyle(el);
		return (
			style.display !== 'none' &&
			style.visibility !== 'hidden' &&
			parseFloat(style.opacity) > 0
		);
	}

	const debugInfo = [];

	effectiveRects.forEach(rect => {
		const inViewport = isWithinViewport(rect);
		const isCssVisible = isCSSVisible(el);
		const { visible, total, centerHitTag, centerHitClass, centerHitId } = visiblePixelCount(el, rect);
		const notCovered = visible / total > 0.25;

		debugInfo.push({
			rect: rect.toJSON(),
			isInViewport: inViewport,
			isTopElement: notCovered,
			noOfVisiblePoints: visible,
			isCssVisible: isCssVisible,
			hitElementTag: centerHitTag,
			hitElementClass: centerHitClass,
			hitElementId: centerHitId
		});
	});

	return debugInfo;
};
