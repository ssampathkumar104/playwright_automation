
/**
 * Highlights the visible, clickable client rects of an element
 * and returns detailed debug information about visibility checks.
 *
 * @param {Element} el    - Target DOM element to inspect
 * @param {string} color - Border color for highlight overlay
 * @returns {Array}      - Debug info for each client rect
 */
(el, color) => {

	// Get all layout rectangles for the element
	// (important for inline elements, wrapped text, etc.)
	const rects = el.getClientRects();
	const effectiveRects = rects.length
		? [...rects]
		: [el.getBoundingClientRect()];

	function getStackingContextParent(el) {
		let parent = el.parentElement;

		while (parent) {
			const style = getComputedStyle(parent);

			const createsStackingContext =
				style.transform !== 'none' ||
				style.perspective !== 'none' ||
				style.filter !== 'none' ||
				style.willChange.includes('transform') ||
				style.position === 'fixed' ||
				style.position === 'sticky';

			if (createsStackingContext) {
				return parent;
			}

			parent = parent.parentElement;
		}

		return el.ownerDocument.body;
	}

	/**
		 * Determines whether an element is a *real* scroll container.
		 * - overflow must allow scrolling
		 * - content must actually overflow
		 */
	function isRealScrollContainer(el) {
		const style = getComputedStyle(el);

		const canScrollY =
			(style.overflowY === 'auto' || style.overflowY === 'scroll') &&
			el.scrollHeight > el.clientHeight;

		const canScrollX =
			(style.overflowX === 'auto' || style.overflowX === 'scroll') &&
			el.scrollWidth > el.clientWidth;

		return canScrollY || canScrollX;
	}

	/**
		 * Collects all ancestor elements that can scroll.
		 * Used to ensure the element is visible inside *every*
		 * relevant scroll container.
		 */
	function getScrollableAncestors(el) {
		const ancestors = [];
		let parent = el.parentElement;

		while (parent) {
			if (isRealScrollContainer(parent)) {
				ancestors.push(parent);
			}
			parent = parent.parentElement;
		}

		return ancestors;
	}

	/**
		 * Resolves the deepest element at a screen coordinate.
		 * Traverses into Shadow DOMs to find the real hit target.
		 */
	function resolveDeepestElement(x, y) {
		let el = document.elementFromPoint(x, y);

		while (el && el.shadowRoot) {
			const inner = el.shadowRoot.elementFromPoint(x, y);
			if (!inner || inner === el) break;
			el = inner;
		}

		return el;
	}

	/**
		 * Checks if a rectangle intersects the browser viewport.
		 */
	function isWithinViewport(rect) {
		return (
			rect.bottom > 0 &&
			rect.right > 0 &&
			rect.top < window.innerHeight &&
			rect.left < window.innerWidth
		);
	}

	/**
		 * Verifies that a rectangle is visible inside
		 * ALL scrollable ancestor containers.
		 */
	function isVisibleInAnyScrollContainers(rect, containers) {
		if (!containers || containers.length === 0) return true;

		return containers.every(c => {
			const r = c.getBoundingClientRect();
			return (
				rect.bottom > r.top &&
				rect.top < r.bottom &&
				rect.right > r.left &&
				rect.left < r.right
			);
		});
	}

	/**
		 * Determines whether `node` is inside `host`,
		 * including traversal across Shadow DOM boundaries.
		 */
	function isShadowDescendant(host, node) {
		let current = node;

		while (current) {
			if (current === host) return true;

			if (current.assignedSlot) {
				// Slotted node → jump to slot
				current = current.assignedSlot;
			} else if (current.parentNode instanceof ShadowRoot) {
				// Shadow DOM → jump to host
				current = current.parentNode.host;
			} else {
				// Normal DOM traversal
				current = current.parentNode;
			}
		}

		return false;
	}

	/**
		 * Determines whether the hit-tested element can be considered
		 * "effectively clickable" for the target element.
		 *
		 * Covers:
		 * - direct equality
		 * - containment relationships
		 * - Shadow DOM ancestry
		 * - ARIA role equivalence
		 */
	function isEffectivelyClickable(target, hit) {
		if (!hit || !target) return false;

		// Same element or simple containment
		if (target === hit || target.contains(hit) || hit.contains(target)) {
			return true;
		}

		// Shadow DOM containment in either direction
		if (
			isShadowDescendant(target, hit) ||
			isShadowDescendant(hit, target)
		) {
			return true;
		}

		// ARIA role matching (e.g. role="button")
		const targetRole = target.getAttribute?.('role');
		if (targetRole) {
			let node = hit;
			while (node && node !== document.body) {
				if (
					node.getAttribute?.('role') === targetRole &&
					node.contains(target)
				) {
					return true;
				}
				node = node.parentElement;
			}
		}

		return false;
	}

	/**
		 * Checks if the element is visually on top at key points
		 * within the rectangle.
		 */
	function isElementOnTop(el, rect) {
		const points = [
			[rect.left + 2, rect.top + 2],
			[rect.right - 2, rect.top + 2],
			[rect.left + 2, rect.bottom - 2],
			[rect.right - 2, rect.bottom - 2],
			[rect.left + rect.width / 2, rect.top + rect.height / 2]
		];

		return points.some(([x, y]) => {
			const topEl = resolveDeepestElement(x, y);
			return topEl && isEffectivelyClickable(el, topEl);
		});
	}

	// Gather all scrollable ancestors for visibility checks
	const scrollContainers = getScrollableAncestors(el);

	// Accumulates debug info for each rect
	const debugInfo = [];

	// Evaluate each client rectangle independently
	effectiveRects.forEach(rect => {
		const inViewport = isWithinViewport(rect);
		const inScrollView = isVisibleInAnyScrollContainers(rect, scrollContainers);
		const isTopElement = isElementOnTop(el, rect);

		/*
				// Collect detailed diagnostics
				debugInfo.push({
					rect: rect.toJSON(),
					isInViewport: inViewport,
					isInScrollView: inScrollView,
					isTopElement: isTopElement,
					scrollContainers: scrollContainers.map(c => c.tagName)
				});
		*/
		// Skip highlighting if any visibility rule fails
		if (!inViewport || !inScrollView || !isTopElement) return;

		// Create visual highlight overlay
		const overlay = document.createElement('div');
		overlay.dataset.pwHighlight = 'true';

		Object.assign(overlay.style, {
			position: 'fixed',
			pointerEvents: 'none',
			zIndex: '2147483647',

			// border: `4px solid ${color}`,
			border: 'none',
			outline: `4px solid ${color}`,
			outlineOffset: '2px',
			left: `${rect.left - 1}px`,
			top: `${rect.top - 1}px`,
			width: `${rect.width + 2}px`,
			height: `${rect.height + 2}px`,
			backgroundColor: 'transparent',

			/* critical for screenshot stability */
			contain: 'layout style paint',
			willChange: 'transform',
			transform: 'translateZ(0)',
			boxSizing: 'border-box'
		});
		/*
				const container = getStackingContextParent(el);
				container.appendChild(overlay);
		*/

		document.body.appendChild(overlay);
	});

	// Return all collected debug information
	return debugInfo;
};
